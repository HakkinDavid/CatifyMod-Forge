package org.daylight.util;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateStorage {
    public static Map<EntityRenderState, UUID> currentStates = new HashMap<>(); // state to player uuid
    public static Float inventoryRelativeHeadYaw = null;
    public static Float inventoryBodyYaw = null;
    public static Float inventoryPitch = null;
    public static boolean currentlyRenderingUi = false;
}
