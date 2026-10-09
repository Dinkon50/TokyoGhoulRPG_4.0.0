package com.tokyoghoul.rpg.item;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.TokyoGhoulRPG;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class QuinqueItem extends SwordItem {
    public enum Effect { NONE, BLEED, KNOCK, CRIT }
    private final int bonusDamage;
    private final Effect effect;
    private final float humanMultiplier;

    public QuinqueItem(int bonusDamage, Effect effect, float humanMultiplier, float speed) {
        super(Tiers.IRON, bonusDamage, speed, new Properties().stacksTo(1));
        this.bonusDamage = bonusDamage;
        this.effect = effect;
        this.humanMultiplier = humanMultiplier;
    }

    public Effect getEffect(){ return effect; }
    public float getHumanMultiplier(){ return humanMultiplier; }

    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof net.minecraft.world.entity.player.Player player)) return super.hurtEnemy(stack,target,attacker);
        GhoulData.Race race = GhoulData.get(player).map(GhoulData::getRace).orElse(GhoulData.Race.HUMAN);
        if (race == GhoulData.Race.GHOUL || race == GhoulData.Race.HALF_GHOUL) return false;
        if (attacker.level() instanceof ServerLevel sl) {
            switch(effect) {
                case BLEED -> target.getPersistentData().putInt("TG_QuinqueBleed", 60 + GhoulData.get(player).map(d -> d.getQuinqueMastery()*10).orElse(0));
                case KNOCK -> target.knockback(0.8D, attacker.getX()-target.getX(), attacker.getZ()-target.getZ());
                case CRIT -> { if (player.getRandom().nextFloat() < 0.25f) target.hurt(attacker.damageSources().playerAttack(player), 3.0f); }
                default -> {}
            }
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY()+target.getBbHeight()*0.5, target.getZ(), 5, .15,.15,.15,.02);
            attacker.playSound(TokyoGhoulRPG.QUINQUE_SWING.get(), .68f, .78f + player.getRandom().nextFloat()*.22f);
        }
        return super.hurtEnemy(stack,target,attacker);
    }
}
