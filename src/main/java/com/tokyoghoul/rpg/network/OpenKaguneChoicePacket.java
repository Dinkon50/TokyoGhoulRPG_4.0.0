package com.tokyoghoul.rpg.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record OpenKaguneChoicePacket() {
    public static void encode(OpenKaguneChoicePacket p, FriendlyByteBuf b) {}
    public static OpenKaguneChoicePacket decode(FriendlyByteBuf b) { return new OpenKaguneChoicePacket(); }
    public static void handle(OpenKaguneChoicePacket p, Supplier<NetworkEvent.Context> c) {
        c.get().enqueueWork(() -> net.minecraft.client.Minecraft.getInstance().setScreen(
            new com.tokyoghoul.rpg.screen.KaguneChoiceScreen()));
        c.get().setPacketHandled(true);
    }
}
