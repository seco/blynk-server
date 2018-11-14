package cc.blynk.server.application.handlers.main.logic;

import cc.blynk.server.Holder;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.model.device.Device;
import cc.blynk.server.core.protocol.model.messages.StringMessage;
import cc.blynk.server.db.DBManager;
import cc.blynk.server.db.model.FlashedToken;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cc.blynk.server.internal.CommonByteBufUtil.notAllowed;
import static cc.blynk.server.internal.CommonByteBufUtil.ok;

/**
 * Assigns static generated token to assigned device.
 *
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 *
 */
public final class MobileAssignTokenLogic {

    private static final Logger log = LogManager.getLogger(MobileAssignTokenLogic.class);

    private MobileAssignTokenLogic() {
    }

    public static void messageReceived(Holder holder, ChannelHandlerContext ctx,
                                       User user, StringMessage message) {
        String token = message.body;

        DBManager dbManager = holder.dbManager;
        holder.blockingIOProcessor.executeDB(() -> {
            FlashedToken dbFlashedToken = dbManager.selectFlashedToken(token);

            if (dbFlashedToken == null) {
                log.error("{} token not exists for orgId {}.", token, user.orgId);
                ctx.writeAndFlush(notAllowed(message.id), ctx.voidPromise());
                return;
            }

            if (dbFlashedToken.isActivated) {
                log.error("{} token is already activated for orgId {}.", token, user.orgId);
                ctx.writeAndFlush(notAllowed(message.id), ctx.voidPromise());
                return;
            }

            Device device = holder.deviceDao.getById(dbFlashedToken.deviceId);

            if (device == null) {
                log.error("Device with {} id not exists in dashboards.", dbFlashedToken.deviceId);
                ctx.writeAndFlush(notAllowed(message.id), ctx.voidPromise());
                return;
            }

            if (!dbManager.activateFlashedToken(token)) {
                log.error("Error activated flashed token {}", token);
                ctx.writeAndFlush(notAllowed(message.id), ctx.voidPromise());
                return;
            }

            holder.deviceDao.assignNewToken(user.orgId, user.email, device, token);

            ctx.writeAndFlush(ok(message.id), ctx.voidPromise());
        });
    }

}
