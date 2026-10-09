package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.client.ClientGhoulData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sends the server-confirmed combo stage back to the attacking client for animation. */
public record KaguneComboSyncPacket(int stage) {
    public static void encode(KaguneComboSyncPacket packet, FriendlyByteBuf buffer) { buffer.writeInt(packet.stage()); }
    public static KaguneComboSyncPacket decode(FriendlyByteBuf buffer) { return new KaguneComboSyncPacket(buffer.readInt()); }
    public static void handle(KaguneComboSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientGhoulData.setComboStage(packet.stage())));
        context.get().setPacketHandled(true);
    }
}
