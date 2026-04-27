package org.daylight.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.daylight.CatifyModClient;
import org.daylight.config.ConfigHandler;
import org.daylight.config.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerToCatReplacer {
    public static final Logger LOGGER = LoggerFactory.getLogger(CatifyModClient.MOD_ID);
    private static final Minecraft client = Minecraft.getInstance();
    private static final Map<UUID, LivingEntity> dummyModelMap = new HashMap<>();
    private static final Map<UUID, Identifier> customCatTextureByPlayer = new HashMap<>();
    private static Level targetWorld;

    public static void initWorld() {
        if (client == null || client.level == null) return;
        targetWorld = client.level;
    }

    public static void replaceWithCat(AbstractClientPlayer player) {
        if (dummyModelMap.containsKey(player.getUUID())) return;

        Cat cat = new Cat(EntityType.CAT, targetWorld);
        // Variant handling is applied later (needs Mojang registry holder wiring).
        cat.setTame(false, false);

        cat.setNoGravity(true);
        cat.setPos(player.getX(), player.getY(), player.getZ());

        dummyModelMap.put(player.getUUID(), cat);

        CatSkinManager.setupCustomSkin();
    }

    private static float lerpAngle(float current, float target, float factor) {
        float delta = Mth.wrapDegrees(target - current);
        return current + delta * factor;
    }

    public static void cleanup() {
        dummyModelMap.values().forEach(Entity::discard);
        dummyModelMap.clear();
    }

    public static boolean shouldReplace(Player player) {
        return dummyModelMap.containsKey(player.getUUID()) &&
                player == Minecraft.getInstance().player;
    }

    public static Player getPlayerById(UUID uuid) {
        Level world = Minecraft.getInstance().level;

        if (world != null) {
            Player player = world.getPlayerByUUID(uuid);
            if (player != null) {
                return player;
            }
        }
        return null;
    }

    public static LivingEntity getCatForPlayer(Player player) {
        return dummyModelMap.get(player.getUUID());
    }

    public static boolean isDummyCat(Cat catEntity) {
        return dummyModelMap.containsValue(catEntity); // && player == MinecraftClient.getInstance().player;
    }

    public static void syncEntity2(Player player, Cat existingCat) {
        existingCat.xOld = player.xOld;
        existingCat.yOld = player.yOld;
        existingCat.zOld = player.zOld;
        existingCat.setPos(player.getX(), player.getY(), player.getZ());

        existingCat.yRotO = player.yRotO;
        existingCat.setYRot(player.getYRot());

        existingCat.xRotO = player.xRotO;
        existingCat.setXRot(player.getXRot());

        syncSittingAndLimbs(player, existingCat);
    }

    public static void syncSittingAndLimbs(Player player, Cat existingCat) {
        // Sitting
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;
        double horizontalSpeed = Math.sqrt(dx * dx + dz * dz);

        boolean sneaking = player.isCrouching(); // or isInSneakingPose()
        boolean slowEnough = horizontalSpeed < 0.01;

        boolean sitting = sneaking && slowEnough;
        existingCat.setInSittingPose(sitting);

        // Limb sync will be restored once the render pipeline is ported.
    }

    private static float getPlayerMovementSpeed(Player player) {
        // Вычисляем скорость движения игрока
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;
        double horizontalSpeed = Math.sqrt(dx * dx + dz * dz);

        // Нормализуем скорость для анимаций
        float speed = (float) horizontalSpeed * 4f; // 20.0f;

//        System.out.println(horizontalSpeed);

        // Ограничиваем максимальную скорость
        return Math.min(speed, 1.0f);
    }

    public static void setLocalCatVariant(String variant) {
        LivingEntity catLivingEntity = client.player == null ? null : getCatForPlayer(client.player);
        if(catLivingEntity instanceof Cat catEntity) {
            // Variant wiring will be restored once registry holders are hooked up.
        }
    }

    public static Identifier getCustomCatTexture(Player player) {
        return customCatTextureByPlayer.get(player.getUUID());
    }

    public static boolean setCustomCatEntityTexture(Player player, String skinName) {
        LivingEntity cat = getCatForPlayer(player);
        if(cat instanceof Cat catEntity) {
            Identifier identifier = loadCustomTexture("cat_enitity_skins", skinName);
            if(!GraphicsUtils.doesTextureExist(identifier)) return false;
            customCatTextureByPlayer.put(player.getUUID(), identifier);
            return true;
        }
        return false;
    }

    public static boolean setCustomCatHandTexture(String skinName) {
        Identifier identifier = loadCustomTexture("cat_hand_skins", skinName);
        if(!GraphicsUtils.doesTextureExist(identifier)) {
            Data.catHandTexture = null;
            return false;
        }
        Data.catHandTexture = identifier;
        return true;
    }

    public static Identifier loadCustomTexture(String subfolder, String skinName) {
        try {
            String basePath = Minecraft.getInstance().gameDirectory.getAbsolutePath() + "/data/catify/" + subfolder;
            File folder = new File(basePath);
            folder.mkdirs();
//            System.out.println(Arrays.toString(folder.listFiles()));
            File file = new File(folder, skinName + ".png");
            if (!file.exists()) {
                return null;
            }

            NativeImage image = NativeImage.read(new FileInputStream(file));
            DynamicTexture texture = new DynamicTexture(() -> subfolder + "/" + skinName, image);

            Identifier id = Identifier.fromNamespaceAndPath("catify", subfolder + "/" + skinName);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Entity findAsCat(UUID entityId) {
        return dummyModelMap.values().stream()
                .filter(entity -> entityId.equals(entity.getUUID()))
                .findFirst()
                .orElse(null);
    }
}
