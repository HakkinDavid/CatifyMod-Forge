package org.daylight.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.daylight.CatSize;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record CatVariantSyncPayload(UUID playerUuid, String variant, boolean isVanilla, CatSize size) implements CustomPacketPayload {
    public static final Type<CatVariantSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("catify", "sync_variant"));

    public static final Map<UUID, CatVariantSyncPayload> playerVariants = new HashMap<>();

    public static final StreamCodec<FriendlyByteBuf, CatVariantSyncPayload> CODEC = StreamCodec.ofMember(
        CatVariantSyncPayload::write,
        CatVariantSyncPayload::read
    );

    public static CatVariantSyncPayload read(FriendlyByteBuf buf) {
        return new CatVariantSyncPayload(buf.readUUID(), buf.readUtf(), buf.readBoolean(), CatSize.fromName(buf.readUtf()));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeUtf(variant);
        buf.writeBoolean(isVanilla);
        buf.writeUtf(size.serializedName());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
