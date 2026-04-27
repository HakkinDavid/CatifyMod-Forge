package org.daylight;

import net.minecraft.resources.Identifier;

import java.util.Map;

public class ModResources {
    public static final Identifier ALL_BLACK_HAND_TEXTURE   = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/all_black_hand.png");
    public static final Identifier BLACK_HAND_TEXTURE       = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/black_hand.png");
    public static final Identifier BRITISH_SHORTHAIR_HAND_TEXTURE = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/british_shorthair_hand.png");
    public static final Identifier CALICO_HAND_TEXTURE      = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/calico_hand.png");
    public static final Identifier JELLIE_HAND_TEXTURE      = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/jellie_hand.png");
    public static final Identifier PERSIAN_HAND_TEXTURE     = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/persian_hand.png");
    public static final Identifier RAGDOLL_HAND_TEXTURE     = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/ragdoll_hand.png");
    public static final Identifier RED_HAND_TEXTURE         = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/red_hand.png");
    public static final Identifier SIAMESE_HAND_TEXTURE     = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/siamese_hand.png");
    public static final Identifier TABBY_HAND_TEXTURE       = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/tabby_hand.png");
    public static final Identifier WHITE_HAND_TEXTURE       = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/white_hand.png");

    public static final Map<String, Identifier> CAT_HAND_BY_VARIANT = Map.ofEntries(
            Map.entry("all_black", ALL_BLACK_HAND_TEXTURE),
            Map.entry("black", BLACK_HAND_TEXTURE),
            Map.entry("british_shorthair", BRITISH_SHORTHAIR_HAND_TEXTURE),
            Map.entry("calico", CALICO_HAND_TEXTURE),
            Map.entry("jellie", JELLIE_HAND_TEXTURE),
            Map.entry("persian", PERSIAN_HAND_TEXTURE),
            Map.entry("ragdoll", RAGDOLL_HAND_TEXTURE),
            Map.entry("red", RED_HAND_TEXTURE),
            Map.entry("siamese", SIAMESE_HAND_TEXTURE),
            Map.entry("tabby", TABBY_HAND_TEXTURE),
            Map.entry("white", WHITE_HAND_TEXTURE)
    );
    public static final Identifier GHOST_TEXTURE = Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "textures/entity/cat_charge.png");;

    public static void init() {
    }

    public static void postInit() {
//        MinecraftClient.getInstance().getTextureManager().registerTexture(ModResources.CAT_HAND_TEXTURE, new ResourceTexture(ModResources.CAT_HAND_TEXTURE));
    }
}
