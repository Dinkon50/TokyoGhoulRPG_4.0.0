package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import com.tokyoghoul.rpg.TokyoGhoulRPG;
import java.util.function.Supplier;

public record KagunePacket(){
 public static void encode(KagunePacket p,FriendlyByteBuf b){}
 public static KagunePacket decode(FriendlyByteBuf b){return new KagunePacket();}
 public static void handle(KagunePacket p,Supplier<NetworkEvent.Context> c){
  c.get().enqueueWork(()->{var s=c.get().getSender();if(s==null)return;GhoulData.get(s).ifPresent(d->{
   if(d.getRace()!=GhoulData.Race.GHOUL&&d.getRace()!=GhoulData.Race.HALF_GHOUL)return;
   d.setKaguneActive(!d.isKaguneActive());
   s.playSound(d.isKaguneActive()?TokyoGhoulRPG.KAGUNE_APPEAR.get():TokyoGhoulRPG.KAGUNE_RETRACT.get(), d.isKaguneActive()?0.82f:0.62f, d.isKaguneActive()?0.72f:0.82f);
   d.sync(s);s.level().broadcastEntityEvent(s,(byte)60);
  });});c.get().setPacketHandled(true);
 }
}
