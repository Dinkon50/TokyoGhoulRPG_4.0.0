package com.tokyoghoul.rpg.item;

import com.tokyoghoul.rpg.capability.GhoulData;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.tokyoghoul.rpg.init.ModItems;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import com.tokyoghoul.rpg.v2.KaguneType;

public class RCInjectorItem extends Item {
    public RCInjectorItem(Properties p) { super(p); }
    @Override public int getUseDuration(ItemStack stack) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.consume(stack);
        GhoulData.Race race = GhoulData.get(player).map(GhoulData::getRace).orElse(GhoulData.Race.HUMAN);
        if (race != GhoulData.Race.HUMAN) {
            player.displayClientMessage(Component.literal("§bЭта CCG-инъекция доступна только человеку."), true);
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, player.getSoundSource(), 0.7F, 1.0F);
        return InteractionResultHolder.consume(stack);
    }

    @Override public void onUseTick(Level level, net.minecraft.world.entity.LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && entity.tickCount % 4 == 0)
            level.addParticle(ParticleTypes.END_ROD, entity.getX(), entity.getY() + 1.0D, entity.getZ(), 0, 0.02D, 0);
        super.onUseTick(level, entity, stack, remainingUseDuration);
    }

    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide) {
            GhoulData.get(player).ifPresent(d -> {
                if (d.getRace() != GhoulData.Race.HUMAN) return;
                d.resetProgress();
                d.choose(GhoulData.Race.CCG, KaguneType.NONE);
                if (!player.getInventory().contains(new ItemStack(ModItems.QUINQUE_SCORPION.get()))) {
                    player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.QUINQUE_SCORPION.get()));
                }
                d.sync(player);
                player.displayClientMessage(Component.literal("§bИнъекция CCG завершена. Ты вступил в CCG. Получено +5 стартовых очков."), false);
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 18, .45, .8, .45, .04);
                    sl.playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP, player.getSoundSource(), .55F, 1.35F);
                }
            });
            stack.shrink(1);
        }
        return stack;
    }
}
