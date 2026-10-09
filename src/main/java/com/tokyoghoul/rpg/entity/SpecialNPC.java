package com.tokyoghoul.rpg.entity;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.init.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class SpecialNPC extends PathfinderMob {

    public SpecialNPC(EntityType<? extends SpecialNPC> type, Level level) {
        super(type, level);
        setCustomNameVisible(true);
    }

    public abstract String role();

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28)
                .add(Attributes.MOVEMENT_SPEED, .28)
                .add(Attributes.ATTACK_DAMAGE, 7)
                .add(Attributes.KNOCKBACK_RESISTANCE, .15)
                .add(Attributes.FOLLOW_RANGE, 18);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, true));
        goalSelector.addGoal(7, new RandomStrollGoal(this, .8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        targetSelector.addGoal(
                1,
                new NearestAttackableTargetGoal<Player>(
                        this,
                        Player.class,
                        16,
                        true,
                        false,
                        entity -> entity.isAlive()
                )
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        switch (role()) {
            case "ccg_quartermaster" -> {
                if (tickCount % 20 == 0) {
                    for (Player p : level().getEntitiesOfClass(
                            Player.class,
                            getBoundingBox().inflate(4),
                            x -> x.isAlive()
                    )) {
                        GhoulData.get(p).ifPresent(d -> {
                            if (d.getRace() == GhoulData.Race.CCG) {
                                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                        net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                                        30,
                                        0,
                                        true,
                                        false,
                                        false
                                ));
                            }
                        });
                    }
                }
            }

            case "ccg_heavy" -> {
                setItemSlot(
                        EquipmentSlot.MAINHAND,
                        new ItemStack(ModItems.QUINQUE_SCORPION.get())
                );

                addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                        25,
                        0,
                        true,
                        false,
                        false
                ));
            }

            case "ccg_sniper" -> {
                setItemSlot(
                        EquipmentSlot.MAINHAND,
                        new ItemStack(ModItems.QUINQUE_SCORPION.get())
                );

                if (tickCount % 35 == 0) {
                    Player p = level().getNearestPlayer(this, 20);

                    if (p != null && p.isAlive()) {
                        p.hurt(
level().damageSources().mobAttack(this),
                                10f
                        );

                        if (level() instanceof net.minecraft.server.level.ServerLevel sl) {
                            sl.sendParticles(
                                    net.minecraft.core.particles.ParticleTypes.CRIT,
                                    p.getX(),
                                    p.getY() + 1,
                                    p.getZ(),
                                    8,
                                    .1,
                                    .1,
                                    .1,
                                    .04
                            );
                        }
                    }
                }
            }

            case "ghoul_scavenger" -> addEffect(
                    new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                            25,
                            1,
                            true,
                            false,
                            false
                    )
            );

            case "ghoul_medic" -> {
                if (tickCount % 30 == 0) {
                    heal(1.5f);

                    for (LivingEntity ally : level().getEntitiesOfClass(
                            LivingEntity.class,
                            getBoundingBox().inflate(5),
                            x -> x != this
                                    && x.isAlive()
                                    && !(x instanceof Player)
                    )) {
                        ally.addEffect(
                                new net.minecraft.world.effect.MobEffectInstance(
                                        net.minecraft.world.effect.MobEffects.REGENERATION,
                                        35,
                                        0,
                                        true,
                                        false,
                                        false
                                )
                        );
                    }
                }
            }

            default -> {}
        }
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(
            net.minecraft.world.damagesource.DamageSource s
    ) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }
}
