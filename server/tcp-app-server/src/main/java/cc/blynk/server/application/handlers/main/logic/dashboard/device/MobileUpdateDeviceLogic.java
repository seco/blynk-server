package cc.blynk.server.application.handlers.main.logic.dashboard.device;

import cc.blynk.server.Holder;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.model.device.Device;
import cc.blynk.server.core.model.serialization.JsonParser;
import cc.blynk.server.core.protocol.exceptions.IllegalCommandException;
import cc.blynk.server.core.protocol.model.messages.StringMessage;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cc.blynk.server.internal.CommonByteBufUtil.illegalCommandBody;
import static cc.blynk.server.internal.CommonByteBufUtil.ok;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 01.02.16.
 */
public final class MobileUpdateDeviceLogic {

    private static final Logger log = LogManager.getLogger(MobileUpdateDeviceLogic.class);

    private MobileUpdateDeviceLogic() {
    }

    public static void messageReceived(Holder holder, ChannelHandlerContext ctx, User user, StringMessage message) {
        String deviceString = message.body;

        if (deviceString == null || deviceString.isEmpty()) {
            throw new IllegalCommandException("Income device message is empty.");
        }

        Device newDevice = JsonParser.parseDevice(deviceString, message.id);

        log.debug("Updating new device {}.", newDevice.id);
        log.trace(deviceString);

        if (newDevice.isNotValid()) {
            throw new IllegalCommandException("Income device message is not valid.");
        }

        Device existingDevice = holder.deviceDao.getById(newDevice.id);

        if (existingDevice == null) {
            log.debug("Attempt to update device with non existing id.");
            ctx.writeAndFlush(illegalCommandBody(message.id), ctx.voidPromise());
            return;
        }

        existingDevice.updateFromMobile(newDevice);

        ctx.writeAndFlush(ok(message.id), ctx.voidPromise());
    }

}
