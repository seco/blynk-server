package cc.blynk.server.http.web;

import cc.blynk.core.http.BaseHttpHandler;
import cc.blynk.core.http.Response;
import cc.blynk.core.http.annotation.Context;
import cc.blynk.core.http.annotation.ContextUser;
import cc.blynk.core.http.annotation.GET;
import cc.blynk.core.http.annotation.Path;
import cc.blynk.core.http.annotation.PathParam;
import cc.blynk.core.http.annotation.QueryParam;
import cc.blynk.server.Holder;
import cc.blynk.server.core.BlockingIOProcessor;
import cc.blynk.server.core.dao.DeviceDao;
import cc.blynk.server.core.dao.OrganizationDao;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.model.device.Device;
import cc.blynk.server.core.model.enums.PinType;
import cc.blynk.server.core.model.widgets.web.SourceType;
import cc.blynk.server.core.reporting.average.AggregationKey;
import cc.blynk.server.core.reporting.raw.BaseReportingKey;
import cc.blynk.server.db.DBManager;
import cc.blynk.server.internal.ParseUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cc.blynk.core.http.Response.badRequest;
import static cc.blynk.core.http.Response.ok;
import static cc.blynk.core.http.Response.serverError;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 04.04.17.
 */
@Path("/data")
@ChannelHandler.Sharable
public class DataHandler extends BaseHttpHandler {

    private final DeviceDao deviceDao;
    private final OrganizationDao organizationDao;
    private final BlockingIOProcessor blockingIOProcessor;
    private final DBManager dbManager;

    DataHandler(Holder holder, String rootPath) {
        super(holder, rootPath);
        this.deviceDao = holder.deviceDao;
        this.organizationDao = holder.organizationDao;
        this.blockingIOProcessor = holder.blockingIOProcessor;
        this.dbManager = holder.dbManager;
    }

    @GET
    @Path("/{deviceId}/insert")
    public Response insertSinglePoint(@Context ChannelHandlerContext ctx,
                                      @ContextUser User user,
                                      @PathParam("deviceId") int deviceId,
                                      @QueryParam("dataStream") String dataStream,
                                      @QueryParam("value") Double value,
                                      @QueryParam("ts") Long inTs) {

        Device device = deviceDao.getById(deviceId);
        organizationDao.verifyUserAccessToDevice(user, device);

        PinType pinType = PinType.getPinType(dataStream.charAt(0));
        byte pin = ParseUtil.parseByte(dataStream.substring(1));

        long ts = (inTs == null ? System.currentTimeMillis() : inTs);

        AggregationKey key = new AggregationKey(new BaseReportingKey(user.email,
                user.appName, 0, deviceId, pinType, pin), ts);

        blockingIOProcessor.executeDB(() -> {
            dbManager.insertSingleEntryRaw(key, value);
            ctx.writeAndFlush(ok(), ctx.voidPromise());
        });

        return null;
    }

    @GET
    @Path("/{deviceId}/history")
    public Response getAll(@Context ChannelHandlerContext ctx,
                           @ContextUser User user,
                           @PathParam("deviceId") int deviceId,
                           @QueryParam("dataStream") String[] dataStreams,
                           @QueryParam("from") long from,
                           @QueryParam("to") long to,
                           @QueryParam("sourceType") SourceType sourceType,
                           @QueryParam("offset") int offset,
                           @QueryParam("limit") int limit) {

        if (dataStreams == null || dataStreams.length == 0) {
            return badRequest("No data stream provided for request.");
        }

        Device device = deviceDao.getById(deviceId);
        organizationDao.verifyUserAccessToDevice(user, device);

        blockingIOProcessor.executeDB(() -> {
            try {
                Map<String, Data> finalModel = new HashMap<>();
                for (String dataStream : dataStreams) {
                    PinType pinType = PinType.getPinType(dataStream.charAt(0));
                    byte pin = ParseUtil.parseByte(dataStream.substring(1));
                    List<AbstractMap.SimpleEntry<Long, Double>> data =
                            dbManager.getRawData(deviceId, pinType, pin, from, to, sourceType, offset, limit);
                    finalModel.put(dataStream, new Data(data));
                }
                ctx.writeAndFlush(ok(finalModel), ctx.voidPromise());
            } catch (Exception e) {
                log.error("Error fetching history data.", e);
                ctx.writeAndFlush(serverError("Error fetching history data."), ctx.voidPromise());
            }
        });

        return null;
    }

    private class Data {

        private final List<AbstractMap.SimpleEntry<Long, Double>> data;

        Data(List<AbstractMap.SimpleEntry<Long, Double>> data) {
            this.data = data;
        }
    }

}
