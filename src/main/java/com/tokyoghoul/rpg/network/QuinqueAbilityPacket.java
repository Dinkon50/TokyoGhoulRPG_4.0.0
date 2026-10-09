package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.item.QuinqueItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class QuinqueAbilityPacket {
    public static void encode(QuinqueAbilityPacket p, FriendlyByteBuf b) {}
    public static QuinqueAbilityPacket decode(FriendlyByteBuf b) { return new QuinqueAbilityPacket(); }
    public static void handle(QuinqueAbilityPacket p, Supplier<NetworkEvent.Context> c) {
        c.get().enqueueWork(() -> {
            var player = c.get().getSender();
            if (player == null) return;
            GhoulData.get(player).ifPresent(d -> {
                if (d.getRace() != GhoulData.Race.CCG) return;
                ItemStack held = player.getMainHandItem();
                if (!(held.getItem() instanceof QuinqueItem)) return;
                long now = player.level().getGameTime();
                long cooldown = player.getPersistentData().getLong("TG_QuinqueStrikeCooldown");
                if (now < cooldown || held.getOrCreateTag().getBoolean("TG_StrikeReady")) return;
                held.getOrCreateTag().putBoolean("TG_StrikeReady", true);
                player.displayClientMessage(Component.literal("§bКвинке заряжена: следующий удар будет усилен"), true);
            });
        });
        c.get().setPacketHandled(true);
    }
}
