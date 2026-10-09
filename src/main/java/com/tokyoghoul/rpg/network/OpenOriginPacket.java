package com.tokyoghoul.rpg.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenOriginPacket(){
    public static void encode(OpenOriginPacket p, FriendlyByteBuf b){}
    public static OpenOriginPacket decode(FriendlyByteBuf b){return new OpenOriginPacket();}
    public static void handle(OpenOriginPacket p, Supplier<NetworkEvent.Context> c){
        c.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                net.minecraft.client.Minecraft.getInstance().setScreen(new com.tokyoghoul.rpg.screen.OriginScreen())
            )
        );
        c.get().setPacketHandled(true);
    }
}
