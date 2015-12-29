package cc.blynk.server.utils;

import cc.blynk.server.handlers.app.main.AppHandler;
import cc.blynk.server.handlers.app.main.auth.AppStateHolder;
import cc.blynk.server.handlers.app.sharing.AppShareHandler;
import cc.blynk.server.handlers.app.sharing.auth.AppShareStateHolder;
import cc.blynk.server.handlers.hardware.HardwareHandler;
import cc.blynk.server.handlers.hardware.auth.HardwareStateHolder;
import cc.blynk.server.model.auth.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 13.09.15.
 */
public class StateHolderUtil {

    private static final Logger log = LogManager.getLogger(StateHolderUtil.class);

    public static HardwareStateHolder getHardState(Channel channel) {
        return getHardState(channel.pipeline());
    }

    public static HardwareStateHolder getHardState(ChannelPipeline pipeline) {
        HardwareHandler handler = pipeline.get(HardwareHandler.class);
        return handler == null ? null : handler.state;
    }

    public static AppStateHolder getAppState(Channel channel) {
        return getAppState(channel.pipeline());
    }

    public static AppStateHolder getAppState(ChannelPipeline pipeline) {
        AppHandler handler = pipeline.get(AppHandler.class);
        if (handler == null) {
            return getShareState(pipeline);
        }
        return handler.state;
    }

    public static AppShareStateHolder getShareState(Channel channel) {
        return getShareState(channel.pipeline());
    }

    private static AppShareStateHolder getShareState(ChannelPipeline pipeline) {
        AppShareHandler handler = pipeline.get(AppShareHandler.class);
        return handler == null ? null : handler.state;
    }

    public static boolean needSync(Channel channel, String sharedToken) {
        ChannelPipeline pipeline = channel.pipeline();
        AppHandler appHandler = pipeline.get(AppHandler.class);
        //means admin channel. shared check is done before.
        if (appHandler != null) {
            return true;
        }

        AppShareHandler appShareHandler = pipeline.get(AppShareHandler.class);
        if (appShareHandler == null) {
            log.error("Channel has no state. Should never happen.");
            return false;
        }
        return appShareHandler.state.contains(sharedToken);
    }

    //use only for rare cases
    public static User getStateUser(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        HardwareHandler hardwareHandler = pipeline.get(HardwareHandler.class);
        if (hardwareHandler != null) {
            return hardwareHandler.state.user;
        }
        AppHandler appHandler = pipeline.get(AppHandler.class);
        if (appHandler != null) {
            return appHandler.state.user;
        }

        return pipeline.get(AppShareHandler.class).state.user;
    }

}
