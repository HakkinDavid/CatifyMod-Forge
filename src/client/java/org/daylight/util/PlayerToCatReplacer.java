package org.daylight.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.daylight.CatifyModClient;
import org.daylight.config.ConfigHandler;
import org.daylight.config.Data;
import org.daylight.network.CatVariantSyncPayload;
import org.daylight.CatifyMod;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    public static Map<UUID, LivingEntity> getDummyModelMap() {
        return dummyModelMap;
    }

    public static void replaceWithCat(AbstractClientPlayer player) {
        if (dummyModelMap.containsKey(player.getUUID())) return;

        Cat cat = new Cat(EntityType.CAT, targetWorld);
        cat.setTame(false, false);

        cat.setNoGravity(true);
        cat.setPos(player.getX(), player.getY(), player.getZ());

        dummyModelMap.put(player.getUUID(), cat);

        // Apply variant/skin
        if (player == Minecraft.getInstance().player) {
            if (ConfigHandler.catVariantVanilla.get()) {
                changeCatVariant(cat, CatVariantUtils.deserializeVariant(ConfigHandler.catVariant.get()));
            } else {
                CatSkinManager.setupCustomSkin();
            }
        } else {
            // Other players
            CatVariantSyncPayload payload = CatVariantSyncPayload.playerVariants.get(player.getUUID());
            if (payload != null) {
                setupSkinForPlayer(player, payload.variant(), payload.isVanilla());
            } else {
                // Fallback: pick a unique cat variant deterministically
                List<String> variants = CatSkinManager.STATIC_VARIANTS;
                int index = Math.abs(player.getUUID().hashCode()) % variants.size();
                String fallbackVariant = variants.get(index);
                changeCatVariant(cat, fallbackVariant);
            }
        }
    }

    public static void setupSkinForPlayer(Player player, String variantName, boolean isVanilla) {
        Cat cat = (Cat) getCatForPlayer(player);
        if (cat == null) return;

        if (isVanilla) {
            changeCatVariant(cat, variantName);
            customCatTextureByPlayer.remove(player.getUUID());
        } else {
            setCustomCatEntityTexture(player, variantName);
        }
    }

    public static void changeCatVariant(Cat cat, String variantName) {
        Identifier variantKey = Identifier.fromNamespaceAndPath("minecraft", variantName.toLowerCase(Locale.ROOT));
        cat.level().registryAccess().lookupOrThrow(Registries.CAT_VARIANT)
            .get(ResourceKey.create(Registries.CAT_VARIANT, variantKey))
            .ifPresent(variant -> ((org.daylight.mixin.client.CatEntityAccessor) cat).invokeSetVariant(variant));
    }

    public static void cleanup() {
        dummyModelMap.values().forEach(Entity::discard);
        dummyModelMap.clear();
        customCatTextureByPlayer.clear();
    }

    public static boolean shouldReplace(Player player) {
        return dummyModelMap.containsKey(player.getUUID());
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

        syncSittingAndLimbs(player, existingCat);
    }

    public static void syncSittingAndLimbs(Player player, Cat existingCat) {
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;
        double horizontalSpeed = Math.sqrt(dx * dx + dz * dz);

        boolean sneaking = player.isCrouching();
        boolean slowEnough = horizontalSpeed < 0.01;

        boolean sitting = sneaking && slowEnough;
        existingCat.setInSittingPose(sitting);
    }

    public static void setLocalCatVariant(String variant) {
        LivingEntity catLivingEntity = client.player == null ? null : getCatForPlayer(client.player);
        if(catLivingEntity instanceof Cat catEntity) {
            changeCatVariant(catEntity, variant);
            sendVariantSyncPacket(variant, true);
        }
    }

    public static Identifier getCustomCatTexture(Player player) {
        return customCatTextureByPlayer.get(player.getUUID());
    }

    public static boolean setCustomCatEntityTexture(Player player, String skinName) {
        LivingEntity cat = getCatForPlayer(player);
        if(cat instanceof Cat catEntity) {
            Identifier identifier = loadCustomTexture("cat_enitity_skins", skinName);
            if(identifier == null || !GraphicsUtils.doesTextureExist(identifier)) return false;
            customCatTextureByPlayer.put(player.getUUID(), identifier);
            if (player == client.player) {
                sendVariantSyncPacket(skinName, false);
            }
            return true;
        }
        return false;
    }

    public static void sendVariantSyncPacket(String variant, boolean isVanilla) {
        if (client.getConnection() != null && client.player != null) {
            CatifyMod.INSTANCE.send(new CatVariantSyncPayload(client.player.getUUID(), variant, isVanilla), PacketDistributor.SERVER.noArg());
        }
    }

    public static boolean setCustomCatHandTexture(String skinName) {
        Identifier identifier = loadCustomTexture("cat_hand_skins", skinName);
        if(identifier == null || !GraphicsUtils.doesTextureExist(identifier)) {
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
