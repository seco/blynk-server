package cc.blynk.server.application.handlers.main.logic.sharing;

import cc.blynk.server.Holder;
import cc.blynk.server.core.model.DashBoard;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.protocol.exceptions.JsonException;
import cc.blynk.server.core.protocol.model.messages.StringMessage;
import io.netty.channel.ChannelHandlerContext;

import static cc.blynk.server.core.protocol.enums.Command.GET_SHARE_TOKEN;
import static cc.blynk.server.internal.CommonByteBufUtil.makeUTF8StringMessage;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 *
 */
public final class MobileGetShareTokenLogic {

    private MobileGetShareTokenLogic() {
    }

    public static void messageReceived(Holder holder, ChannelHandlerContext ctx,
                                       User user, StringMessage message) {
        String dashBoardIdString = message.body;

        int dashId;
        try {
            dashId = Integer.parseInt(dashBoardIdString);
        } catch (NumberFormatException ex) {
            throw new JsonException("Project id is not valid.");
        }

        DashBoard dash = user.profile.getDashByIdOrThrow(dashId);
        String token = dash.sharedToken;

        //if token not exists. generate new one
        if (token == null) {
            token = holder.sharedTokenManager.refreshSharedToken(user, dash);
            user.lastModifiedTs = System.currentTimeMillis();
        }

        if (ctx.channel().isWritable()) {
            ctx.writeAndFlush(makeUTF8StringMessage(GET_SHARE_TOKEN, message.id, token), ctx.voidPromise());
        }
    }
}
