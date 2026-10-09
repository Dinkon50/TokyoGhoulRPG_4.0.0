package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.client.ClientGhoulData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Client render-state sync for the temporary single-tendril grab. */
public record KaguneGrabStatePacket(UUID ownerId, UUID targetId, boolean active) {
    public static void encode(KaguneGrabStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.ownerId());
        buffer.writeUUID(packet.targetId());
        buffer.writeBoolean(packet.active());
    }
    public static KaguneGrabStatePacket decode(FriendlyByteBuf buffer) {
        return new KaguneGrabStatePacket(buffer.readUUID(), buffer.readUUID(), buffer.readBoolean());
    }
    public static void handle(KaguneGrabStatePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientGhoulData.setGrabState(packet.ownerId(), packet.targetId(), packet.active())));
        context.get().setPacketHandled(true);
    }
}
