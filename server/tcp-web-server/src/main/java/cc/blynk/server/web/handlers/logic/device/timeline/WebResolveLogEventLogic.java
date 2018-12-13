package cc.blynk.server.web.handlers.logic.device.timeline;

import cc.blynk.server.Holder;
import cc.blynk.server.core.PermissionBasedLogic;
import cc.blynk.server.core.model.permissions.Role;
import cc.blynk.server.core.protocol.model.messages.StringMessage;
import cc.blynk.server.core.session.web.WebAppStateHolder;
import io.netty.channel.ChannelHandlerContext;

import static cc.blynk.server.core.model.permissions.PermissionsTable.ORG_DEVICES_EDIT;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 13.04.18.
 */
public final class WebResolveLogEventLogic implements PermissionBasedLogic<WebAppStateHolder> {

    private final WebResolveOwnLogEventLogic webResolveOwnLogEventLogic;

    public WebResolveLogEventLogic(Holder holder) {
        this.webResolveOwnLogEventLogic = new WebResolveOwnLogEventLogic(holder);
    }

    @Override
    public boolean hasPermission(Role role) {
        return role.canEditOrgDevice();
    }

    @Override
    public int getPermission() {
        return ORG_DEVICES_EDIT;
    }

    @Override
    public void noPermissionAction(ChannelHandlerContext ctx, WebAppStateHolder state, StringMessage msg) {
        webResolveOwnLogEventLogic.messageReceived(ctx, state, msg);
    }

    @Override
    public void messageReceived0(ChannelHandlerContext ctx, WebAppStateHolder state, StringMessage msg) {
        webResolveOwnLogEventLogic.messageReceived0(ctx, state, msg);
    }
}
