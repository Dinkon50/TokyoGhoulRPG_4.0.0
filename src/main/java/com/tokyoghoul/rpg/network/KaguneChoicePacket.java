package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record KaguneChoicePacket(int kaguneType) {
    public static void encode(KaguneChoicePacket p, FriendlyByteBuf b) { b.writeInt(p.kaguneType); }
    public static KaguneChoicePacket decode(FriendlyByteBuf b) { return new KaguneChoicePacket(b.readInt()); }
    public static void handle(KaguneChoicePacket p, Supplier<NetworkEvent.Context> c) {
        c.get().enqueueWork(() -> {
            var player = c.get().getSender();
            if (player == null) return;
            GhoulData.get(player).ifPresent(d -> {
                if (d.getRace() != GhoulData.Race.HALF_GHOUL || d.getKaguneType() != KaguneType.NONE) return;
                KaguneType[] allowed = {KaguneType.KANEKI_RINKAKU, KaguneType.TAKIZAWA_UKAKU};
                if (p.kaguneType < 0 || p.kaguneType >= allowed.length) return;
                d.choose(GhoulData.Race.HALF_GHOUL, allowed[p.kaguneType]);
                d.sync(player);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§dВыбран кагуне: §f" + allowed[p.kaguneType].displayName() + " §7(навсегда)"), false);
            });
        });
        c.get().setPacketHandled(true);
    }
}
