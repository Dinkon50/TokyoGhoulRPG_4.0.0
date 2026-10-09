package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.v2.KaguneType;
import com.tokyoghoul.rpg.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record OriginPacket(int race, int kaguneType){
    public OriginPacket(int race){ this(race, 0); }
    public static void encode(OriginPacket p,FriendlyByteBuf b){b.writeInt(p.race);b.writeInt(p.kaguneType);}
    public static OriginPacket decode(FriendlyByteBuf b){return new OriginPacket(b.readInt(),b.readInt());}
    public static void handle(OriginPacket p,Supplier<NetworkEvent.Context> c){
        c.get().enqueueWork(() -> {
            var s=c.get().getSender();
            if(s==null) return;
            GhoulData.get(s).ifPresent(d -> {
                GhoulData.Race r = switch(p.race){
                    case 1 -> GhoulData.Race.GHOUL;
                    case 2 -> GhoulData.Race.HALF_GHOUL;
                    case 3 -> GhoulData.Race.CCG;
                    default -> GhoulData.Race.HUMAN;
                };
                KaguneType[] types=KaguneType.values();
                KaguneType type=p.kaguneType>=0&&p.kaguneType<types.length?types[p.kaguneType]:KaguneType.NONE;
                if (r == GhoulData.Race.GHOUL) {
                    KaguneType[] allowed = {KaguneType.RIZE_RINKAKU, KaguneType.TOUKA_UKAKU, KaguneType.AYATO_UKAKU, KaguneType.TSUKIYAMA_KOUKAKU, KaguneType.NISHIKI_BIKAKU, KaguneType.NORO_BIKAKU};
                    if (!java.util.Arrays.asList(allowed).contains(type)) type = KaguneType.RIZE_RINKAKU;
                }
                if (r == GhoulData.Race.HALF_GHOUL) {
                    KaguneType[] allowed = {KaguneType.KANEKI_RINKAKU, KaguneType.TAKIZAWA_UKAKU};
                    if (!java.util.Arrays.asList(allowed).contains(type)) type = KaguneType.KANEKI_RINKAKU;
                }
                d.choose(r,type);
                if (r == GhoulData.Race.CCG && !s.getInventory().contains(new ItemStack(ModItems.QUINQUE_SCORPION.get()))) {
                    s.getInventory().placeItemBackInInventory(new ItemStack(ModItems.QUINQUE_SCORPION.get()));
                }
                d.sync(s);
                s.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    switch(r){
                        case GHOUL -> "§cПуть гуля выбран: "+type.displayName();
                        case HALF_GHOUL -> "§dПуть полугулю выбран: "+type.displayName();
                        case CCG -> "§bТы вступил в CCG. Получено +5 стартовых очков.";
                        default -> "§fТы остался человеком.";
                    }), false);
            });
        });
        c.get().setPacketHandled(true);
    }
}
