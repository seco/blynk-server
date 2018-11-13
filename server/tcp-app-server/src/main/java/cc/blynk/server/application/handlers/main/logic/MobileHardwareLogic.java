package cc.blynk.server.application.handlers.main.logic;

import cc.blynk.server.Holder;
import cc.blynk.server.core.dao.DeviceDao;
import cc.blynk.server.core.dao.SessionDao;
import cc.blynk.server.core.model.DashBoard;
import cc.blynk.server.core.model.auth.Session;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.model.device.Device;
import cc.blynk.server.core.model.device.Tag;
import cc.blynk.server.core.model.enums.PinType;
import cc.blynk.server.core.model.widgets.Target;
import cc.blynk.server.core.model.widgets.Widget;
import cc.blynk.server.core.model.widgets.ui.DeviceSelector;
import cc.blynk.server.core.processors.BaseProcessorHandler;
import cc.blynk.server.core.processors.WebhookProcessor;
import cc.blynk.server.core.protocol.model.messages.StringMessage;
import cc.blynk.server.core.session.mobile.MobileStateHolder;
import cc.blynk.utils.NumberUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cc.blynk.server.core.protocol.enums.Command.DEVICE_SYNC;
import static cc.blynk.server.core.protocol.enums.Command.HARDWARE;
import static cc.blynk.server.internal.CommonByteBufUtil.deviceNotInNetwork;
import static cc.blynk.server.internal.CommonByteBufUtil.illegalCommandBody;
import static cc.blynk.server.internal.CommonByteBufUtil.ok;
import static cc.blynk.utils.MobileStateHolderUtil.getAppState;
import static cc.blynk.utils.StringUtils.split2;
import static cc.blynk.utils.StringUtils.split3;

/**
 * Responsible for handling incoming hardware commands from applications and forwarding it to
 * appropriate hardware.
 *
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 *
 */
public class MobileHardwareLogic extends BaseProcessorHandler {

    private static final Logger log = LogManager.getLogger(MobileHardwareLogic.class);

    private final SessionDao sessionDao;
    private final DeviceDao deviceDao;

    public MobileHardwareLogic(Holder holder) {
        super(holder.eventorProcessor, new WebhookProcessor(holder.asyncHttpClient,
                holder.limits.webhookPeriodLimitation,
                holder.limits.webhookResponseSizeLimitBytes,
                holder.limits.webhookFailureLimit,
                holder.stats),
                holder.deviceDao);
        this.sessionDao = holder.sessionDao;
        this.deviceDao = holder.deviceDao;
    }

    public static void processDeviceSelectorCommand(ChannelHandlerContext ctx,
                                             DeviceDao deviceDao,
                                             Session session, DashBoard dash,
                                             StringMessage message, String[] splitBody) {
        //in format "vu 200000 1"
        long widgetId = Long.parseLong(splitBody[1]);
        Widget deviceSelector = dash.getWidgetByIdOrThrow(widgetId);
        if (deviceSelector instanceof DeviceSelector) {
            int selectedDeviceId = Integer.parseInt(splitBody[2]);
            ((DeviceSelector) deviceSelector).value = selectedDeviceId;
            ctx.write(ok(message.id), ctx.voidPromise());

            //sending to shared dashes and master-master apps
            session.sendToSharedApps(ctx.channel(), dash.sharedToken, DEVICE_SYNC, message.id, message.body);

            //we need to send syncs not only to main app, but all to all shared apps
            Device device = deviceDao.getByIdOrThrow(selectedDeviceId);
            for (Channel channel : session.appChannels) {
                MobileStateHolder mobileStateHolder = getAppState(channel);
                if (mobileStateHolder != null && mobileStateHolder.contains(dash.sharedToken)) {
                    device.sendPinStorageSyncs(channel);
                }
                channel.flush();
            }
        }
    }

    public void messageReceived(ChannelHandlerContext ctx, MobileStateHolder state, StringMessage message) {
        Session session = sessionDao.getOrgSession(state.orgId);

        //here expecting command in format "200000 vw 88 1"
        String[] split = split2(message.body);

        //deviceId or tagId or device selector widget id
        int targetId = Integer.parseInt(split[0]);

        User user = state.user;

        //sending message only if widget assigned to device or tag has assigned devices
        Target target = null;
        if (targetId < Tag.START_TAG_ID) {
            target = deviceDao.getById(targetId);
        } else if (targetId < DeviceSelector.DEVICE_SELECTOR_STARTING_ID) {
            target = user.profile.getTagById(targetId);
        } else {
            //means widget assigned to device selector widget.
            //target = dash.getDeviceSelector(targetId);
        }

        if (target == null) {
            log.debug("No assigned target id for received command.");
            return;
        }

        int[] deviceIds = target.getDeviceIds();

        if (deviceIds.length == 0) {
            log.debug("No devices assigned to target.");
            return;
        }

        char operation = split[1].charAt(1);
        String[] splitBody;
        switch (operation) {
            //case 'u' :
                //splitting "vu 200000 1"
            //    String[] splitBody = split3(split[1]);
            //    processDeviceSelectorCommand(ctx, deviceDao, session, dash, message, splitBody);
            //    break;
            case 'w' :
                splitBody = split3(split[1]);

                if (splitBody.length < 3) {
                    log.debug("Not valid write command.");
                    ctx.writeAndFlush(illegalCommandBody(message.id), ctx.voidPromise());
                    return;
                }

                PinType pinType = PinType.getPinType(splitBody[0].charAt(0));
                short pin = NumberUtil.parsePin(splitBody[1]);
                String value = splitBody[2];
                long now = System.currentTimeMillis();

                for (int deviceId : deviceIds) {
                    Device device = deviceDao.getById(deviceId);
                    if (device != null) {
                        device.updateValue(pin, pinType, value, now);
                        device.webDashboard.update(device.id, pin, pinType, value);
                    }
                }

                //sending to shared dashes and master-master apps
                //session.sendToSharedApps(ctx.channel(), dash.sharedToken, DEVICE_SYNC, message.id, message.body);
                session.sendToSelectedDeviceOnWeb(DEVICE_SYNC, message.id, split[1], deviceIds);

                if (session.sendMessageToHardware(HARDWARE, message.id, split[1], deviceIds)) {
                    log.debug("No device in session.");
                    ctx.writeAndFlush(deviceNotInNetwork(message.id), ctx.voidPromise());
                }

                //processEventorAndWebhook(state.user, dash, targetId, session, pin, pinType, value, now);
                break;
        }
    }

}
