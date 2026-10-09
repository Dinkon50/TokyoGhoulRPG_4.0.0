package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import com.tokyoghoul.rpg.TokyoGhoulRPG;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RagePacket(){
    public static void encode(RagePacket p,FriendlyByteBuf b){}
    public static RagePacket decode(FriendlyByteBuf b){return new RagePacket();}
    public static void handle(RagePacket p,Supplier<NetworkEvent.Context> c){
        c.get().enqueueWork(() -> {
            var s=c.get().getSender();
            if(s==null) return;
            GhoulData.get(s).ifPresent(d -> {
                if((d.getRace()==GhoulData.Race.GHOUL || d.getRace()==GhoulData.Race.HALF_GHOUL) && !d.isKaguneActive()){ s.displayClientMessage(Component.literal("§8Сначала выпусти кагуне — §fG"), true); return; }
                if(d.activateRage()){
                    d.sync(s);
                    String name=d.getRace()==GhoulData.Race.CCG ? "БОЕВОЙ ДУХ" : "ЯРОСТЬ";
                    int seconds=d.getRace()==GhoulData.Race.CCG ? 30 : 60;
                    s.displayClientMessage(Component.literal(
                        "§4"+name+" АКТИВИРОВАН! §cУсиление на "+seconds+" сек."
                    ),true);
                    s.level().broadcastEntityEvent(s,(byte)61);
                    s.playSound(TokyoGhoulRPG.RAGE_ACTIVATE.get(), 0.9f, 0.82f + s.getRandom().nextFloat()*0.08f);
                } else if(!d.isRageUnlocked()){
                    int cost=d.getRace()==GhoulData.Race.CCG ? 4 : 5;
                    s.displayClientMessage(Component.literal(
                        "§7Сначала открой способность за §c"+cost+" очка."
                    ),true);
                } else if(d.getRage()<100){
                    s.displayClientMessage(Component.literal(
                        "§7Шкала заполнена только на §c"+d.getRage()+"%§7."
                    ),true);
                } else if(d.isRageActive()){
                    s.displayClientMessage(Component.literal("§7Способность уже активна."),true);
                }
            });
        });
        c.get().setPacketHandled(true);
    }
}
