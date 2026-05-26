package org.daylight.util;

import net.minecraft.world.entity.player.Player;
import org.daylight.InvisibilityBehaviour;
import org.daylight.config.ConfigHandler;

public class ModStateUtils {
    public static boolean shouldRenderCat(Player player) {
        boolean visible = !player.isInvisible();
        InvisibilityBehaviour behaviour = (InvisibilityBehaviour) ConfigHandler.invisibilityBehaviour.getCached();
        return visible || behaviour == InvisibilityBehaviour.NEVER;
    }

    public static boolean shouldRenderShadow(Player player) {
        boolean visible = !player.isInvisible();
        InvisibilityBehaviour behaviour = (InvisibilityBehaviour) ConfigHandler.invisibilityBehaviour.getCached();
        return visible || behaviour == InvisibilityBehaviour.NEVER || behaviour == InvisibilityBehaviour.CHARGED;
    }

    public static boolean shouldRenderCharge(Player player) {
        boolean visible = !player.isInvisible();
        InvisibilityBehaviour behaviour = (InvisibilityBehaviour) ConfigHandler.invisibilityBehaviour.getCached();
        return !visible && behaviour == InvisibilityBehaviour.CHARGED;
    }
}
