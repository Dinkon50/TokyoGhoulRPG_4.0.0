package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpgradePacket(String stat){
    public static void encode(UpgradePacket p,FriendlyByteBuf b){b.writeUtf(p.stat);}
    public static UpgradePacket decode(FriendlyByteBuf b){return new UpgradePacket(b.readUtf(32));}
    public static void handle(UpgradePacket p,Supplier<NetworkEvent.Context> c){
        c.get().enqueueWork(() -> {
            var s=c.get().getSender();
            if(s!=null) GhoulData.get(s).ifPresent(d -> {
                if(d.upgrade(p.stat)) {
                    if(d.getRace() == GhoulData.Race.CCG && d.getInvestigation() >= 10 && d.getQuinque() >= 10 && d.getTactics() >= 10 && d.getArmor() >= 10 && d.getDiscipline() >= 10) {
                        if(!s.getInventory().contains(new ItemStack(ModItems.QUINQUE_YUKIMURA_1_3.get()))) {
                            s.getInventory().placeItemBackInInventory(new ItemStack(ModItems.QUINQUE_YUKIMURA_1_3.get()));
                            s.displayClientMessage(Component.literal("§b§lПОЛНАЯ ПРОКАЧКА CCG: §fполучена квинке «Юкимура 1/3»"), false);
                        }
                    }
                    d.sync(s);
                    if(p.stat().contains("technique")) s.displayClientMessage(Component.literal("§6Новая боевая техника разблокирована"), true);
                }
            });
        });
        c.get().setPacketHandled(true);
    }
}
