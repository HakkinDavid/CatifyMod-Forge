package org.daylight.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;

public class GraphicsUtils {
    public static boolean doesTextureExist(Identifier id) {
//        System.out.println(id);
        if(id == null) return false;
        TextureManager resourceManager = Minecraft.getInstance().getTextureManager();
        try {
//            System.out.println(resourceManager.getTexture(id));
            return resourceManager.getTexture(id) != null;
        } catch (Exception e) {
            return false;
        }
    }
}