package com.tokyoghoul.rpg.item;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.network.OpenKaguneChoicePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HalfGhoulOrganItem extends Item {
    public HalfGhoulOrganItem(Properties p) { super(p); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            GhoulData.get(player).ifPresent(d -> {
                if (d.getRace() != GhoulData.Race.HUMAN) {
                    player.displayClientMessage(Component.literal("§cОрган можно использовать только человеку."), true);
                    return;
                }
                d.resetProgress();
                d.choose(GhoulData.Race.HALF_GHOUL, com.tokyoghoul.rpg.v2.KaguneType.NONE);
                d.setKaguneActive(false);
                d.sync(player);
                stack.shrink(1);
                player.displayClientMessage(Component.literal("§dОрган принят. Выбери свой кагуне."), false);
                NetworkHandler.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player),
                    new OpenKaguneChoicePacket());
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
