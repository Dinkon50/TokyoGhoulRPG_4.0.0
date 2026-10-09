package com.tokyoghoul.rpg.item;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.init.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class HumanizingInjectionItem extends Item {
    public HumanizingInjectionItem(Properties p) { super(p); }

    @Override public int getUseDuration(ItemStack stack) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.consume(stack);
        GhoulData.Race race = GhoulData.get(player).map(GhoulData::getRace).orElse(GhoulData.Race.HUMAN);
        if (race == GhoulData.Race.HUMAN) {
            player.displayClientMessage(Component.literal("§7Ты уже человек."), true);
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, player.getSoundSource(), 0.7F, 1.15F);
        return InteractionResultHolder.consume(stack);
    }

    @Override public void onUseTick(Level level, net.minecraft.world.entity.LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && entity.tickCount % 4 == 0) {
            level.addParticle(ParticleTypes.END_ROD, entity.getX(), entity.getY() + 1.0D, entity.getZ(), 0, 0.02D, 0);
        }
        super.onUseTick(level, entity, stack, remainingUseDuration);
    }

    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide) {
            GhoulData.get(player).ifPresent(d -> {
                GhoulData.Race oldRace = d.getRace();
                if (oldRace == GhoulData.Race.HUMAN) return;

                // Full reset: no old skill-tree progress, rage unlocks, stats or kagune survive.
                d.resetProgress();
                d.choose(GhoulData.Race.HUMAN, com.tokyoghoul.rpg.v2.KaguneType.NONE);
                d.setKaguneActive(false);
                // Human is the current final state; choosing Ghoul/Half-Ghoul/CCG again starts from zero.
                player.getPersistentData().remove("TG_QuinqueStrikeCooldown");
                d.sync(player);
                player.displayClientMessage(Component.literal("§fИнъекция завершена. Ты снова человек. Весь прошлый прогресс сброшен."), false);
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
