package org.daylight.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.daylight.CatifyModClient;
import org.daylight.CustomCatTextureHolder;
import org.daylight.config.ConfigHandler;
import org.daylight.config.Data;
import org.daylight.mixin.client.CatEntityAccessor;
import org.daylight.mixin.client.LimbAnimatorAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class PlayerToCatReplacer {
    public static final Logger LOGGER = LoggerFactory.getLogger(CatifyModClient.MOD_ID);
    private static final Minecraft client = Minecraft.getInstance();
    private static final Map<UUID, LivingEntity> dummyModelMap = new HashMap<>();
    private static Level targetWorld;

    public static void initWorld() {
        if (client == null || client.level == null) return;
        targetWorld = client.level;
    }

    public static void replaceWithCat(AbstractClientPlayer player) {
        if (dummyModelMap.containsKey(player.getUUID())) return;

        Cat cat = new Cat(EntityType.CAT, targetWorld);
        if(ConfigHandler.catVariantVanilla.get()) changeCatVariant(cat, CatVariantUtils.deserializeVariant(ConfigHandler.catVariant.get()));
        cat.setTame(false, false);

        cat.setNoGravity(true);
        cat.noPhysics = true;
        cat.setNoAi(true);
        cat.setPos(player.getX(), player.getY(), player.getZ());

        dummyModelMap.put(player.getUUID(), cat);

        CatSkinManager.setupCustomSkin();
    }

    private static Holder<CatVariant> getCatVariant(ResourceKey<CatVariant> variantKey) {
        if(client.level == null) {
            LOGGER.warn("Client world is null");
            return null;
        }

        return client.level.registryAccess()
                .lookupOrThrow(Registries.CAT_VARIANT)
                .getOrThrow(variantKey);
    }

    public static void changeCatVariant(Cat cat, ResourceKey<CatVariant> variantKey) {
        Holder<CatVariant> variant = getCatVariant(variantKey);
        ((CatEntityAccessor) cat).invokeSetVariant(variant);
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
        ClientLevel world = Minecraft.getInstance().level;

        if (world != null) {
            for (Player player : world.players()) {
                if (player.getUUID().equals(uuid)) {
                    return player;
                }
            }
        }
        return null;
    }

    public static LivingEntity getCatForPlayer(Player player) {
        return dummyModelMap.get(player.getUUID());
    }

    public static boolean isDummyCat(Cat catEntity) {
        return dummyModelMap.containsValue(catEntity);
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

        existingCat.yBodyRotO = player.yBodyRotO;
        existingCat.yBodyRot = player.yBodyRot;

        existingCat.yHeadRotO = player.yHeadRotO;
        existingCat.yHeadRot = player.yHeadRot;

        syncSittingAndLimbs(player, existingCat);
    }

    public static void syncSittingAndLimbs(Player player, Cat existingCat) {
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;
        double horizontalSpeed = Math.sqrt(dx * dx + dz * dz);

        boolean sneaking = player.isShiftKeyDown();
        boolean slowEnough = horizontalSpeed < 0.01;

        boolean sitting = sneaking && slowEnough;
        existingCat.setInSittingPose(sitting);

        existingCat.walkAnimation.update(
                getPlayerMovementSpeed(player),
                0.9f,
                1.0f
        );

        if (existingCat.walkAnimation instanceof LimbAnimatorAccessor acc) {
            if (player.walkAnimation instanceof LimbAnimatorAccessor playerAcc) {
                acc.setAnimationProgress(playerAcc.getAnimationProgress());
            }
        }
    }

    private static float getPlayerMovementSpeed(Player player) {
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;
        double horizontalSpeed = Math.sqrt(dx * dx + dz * dz);

        float speed = (float) horizontalSpeed * 4f;

        return Math.min(speed, 1.0f);
    }

    public static void setLocalCatVariant(ResourceKey<CatVariant> variant) {
        LivingEntity catLivingEntity = getCatForPlayer(client.player);
        if(catLivingEntity instanceof Cat catEntity) {
            changeCatVariant(catEntity, variant);
            if(catEntity instanceof CustomCatTextureHolder customCatTextureHolder) {
                customCatTextureHolder.catModel$setCustomTexture(null);
                customCatTextureHolder.catModel$requestCustomTextureUpdate();
            }
        }
    }

    public static Identifier getCustomCatTexture(Player player) {
        LivingEntity cat = getCatForPlayer(player);
        if(cat instanceof CustomCatTextureHolder customCatTextureHolder) {
            return customCatTextureHolder.catModel$getCustomTexture();
        }
        return null;
    }

    public static boolean setCustomCatEntityTexture(Player player, String skinName) {
        LivingEntity cat = getCatForPlayer(player);
        if(cat instanceof Cat catEntity) {
            Identifier identifier = loadCustomTexture("cat_enitity_skins", skinName);
            if(!GraphicsUtils.doesTextureExist(identifier)) return false;
            if(catEntity instanceof CustomCatTextureHolder customCatTextureHolder) {
                customCatTextureHolder.catModel$setCustomTexture(identifier);
                customCatTextureHolder.catModel$requestCustomTextureUpdate();
            }
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
            File file = new File(folder, skinName + ".png");
            if (!file.exists()) {
                return null;
            }

            NativeImage image = NativeImage.read(new FileInputStream(file));
            Supplier<String> nameSupplier = () -> subfolder + "/" + skinName;
            DynamicTexture texture = new DynamicTexture(nameSupplier, image);

            Identifier id = Identifier.fromNamespaceAndPath("catmodel", nameSupplier.get());
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