package com.tokyoghoul.rpg.entity;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.init.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class CCGNPC extends PathfinderMob {
    public CCGNPC(EntityType<? extends CCGNPC> type, Level level){
        super(type, level);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.QUINQUE_SCORPION.get()));
    }

    public static AttributeSupplier.Builder createAttributes(){
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 32.0)
            .add(Attributes.MOVEMENT_SPEED, 0.30)
            .add(Attributes.ATTACK_DAMAGE, 6.0)
            .add(Attributes.ATTACK_KNOCKBACK, 0.25)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.10);
    }

    private static boolean isGhoul(LivingEntity entity){
        if(!(entity instanceof Player player)) return false;
        return GhoulData.get(player).map(d -> d.getRace()==GhoulData.Race.GHOUL).orElse(false);
    }

    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new MeleeAttackGoal(this,1.15,true));
        goalSelector.addGoal(7,new RandomStrollGoal(this,0.9));
        goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,12.0F));
        goalSelector.addGoal(9,new RandomLookAroundGoal(this));

        // CCG автоматически преследует только чистых гулей.
        // Людей и полугулей не трогает, пока они сами не нападут.
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(
            this, Player.class, 12, true, false, CCGNPC::isGhoul
        ));
    }

    @Override protected SoundEvent getAmbientSound(){ return SoundEvents.SPIDER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source){ return SoundEvents.PLAYER_HURT; }
    @Override protected SoundEvent getDeathSound(){ return SoundEvents.ZOMBIE_DEATH; }

    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity target){
        boolean hit = super.doHurtTarget(target);
        if(hit) playSound(SoundEvents.PLAYER_ATTACK_STRONG, 0.65F, 0.82F + random.nextFloat() * 0.18F);
        return hit;
    }

    @Override public boolean isPersistenceRequired(){return true;}
    @Override public boolean removeWhenFarAway(double distanceToClosestPlayer){return false;}
}
