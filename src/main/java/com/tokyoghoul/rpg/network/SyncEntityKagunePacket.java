package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.client.ClientGhoulData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Syncs only the render state needed to show each player's own kagune to nearby clients. */
public record SyncEntityKagunePacket(UUID playerId, int race, boolean active, int kaguneType, boolean rageActive) {
    public static void encode(SyncEntityKagunePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerId());
        buffer.writeInt(packet.race());
        buffer.writeBoolean(packet.active());
        buffer.writeInt(packet.kaguneType());
        buffer.writeBoolean(packet.rageActive());
    }

    public static SyncEntityKagunePacket decode(FriendlyByteBuf buffer) {
        return new SyncEntityKagunePacket(buffer.readUUID(), buffer.readInt(), buffer.readBoolean(), buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(SyncEntityKagunePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientGhoulData.setEntityKagune(packet.playerId(), packet.race(), packet.active(), packet.kaguneType(), packet.rageActive())));
        context.get().setPacketHandled(true);
    }
}
