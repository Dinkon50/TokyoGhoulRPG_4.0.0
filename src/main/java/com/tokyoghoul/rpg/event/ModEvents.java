package com.tokyoghoul.rpg.event;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.network.KaguneGrabPacket;
import com.tokyoghoul.rpg.network.OpenOriginPacket;
import com.tokyoghoul.rpg.entity.GhoulBoss;
import com.tokyoghoul.rpg.init.ModItems;
import com.tokyoghoul.rpg.world.GhoulBossArena;
import com.tokyoghoul.rpg.world.RareLocations;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void biteLock(LivingEvent.LivingTickEvent e){
        LivingEntity entity = e.getEntity();
        if (KaguneGrabPacket.isCurrentlyGrabbed(entity)) {
            entity.setDeltaMovement(Vec3.ZERO);
            entity.fallDistance = 0f;
            if (entity instanceof net.minecraft.world.entity.Mob mob) {
                mob.getNavigation().stop();
                mob.setTarget(null);
                mob.setAggressive(false);
            }
            // Keep body fixed, but allow the head to rotate and look around.
            float bodyYaw = entity.getPersistentData().getFloat("TG_GrabLockBodyYaw");
            entity.setYRot(bodyYaw);
            entity.setYBodyRot(bodyYaw);
            entity.setDeltaMovement(Vec3.ZERO);
            return;
        }
        long until = entity.getPersistentData().getLong("TG_BiteLock");
        if(until > entity.level().getGameTime()){
            entity.setDeltaMovement(0, 0, 0);
            entity.setJumping(false);
            if(entity instanceof net.minecraft.world.entity.Mob mob) mob.getNavigation().stop();
            float lockY = entity.getPersistentData().getFloat("TG_BiteLockYRot");
            float lockX = entity.getPersistentData().getFloat("TG_BiteLockXRot");
            entity.setYRot(lockY);
            entity.setXRot(lockX);
            entity.setYHeadRot(lockY);
            entity.setYBodyRot(lockY);
        }
    }


    @SubscribeEvent
    public static void biteLockInteract(PlayerInteractEvent e){
        Player player = e.getEntity();
        long now = player.level().getGameTime();
        if (player.getPersistentData().getLong("TG_BiteLock") > now
                || KaguneGrabPacket.isCurrentlyGrabbed(player)) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void grabPlace(BlockEvent.EntityPlaceEvent e){
        if (e.getEntity() instanceof Player player && KaguneGrabPacket.isCurrentlyGrabbed(player)) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void grabUseItem(LivingEntityUseItemEvent.Start e){
        if (KaguneGrabPacket.isCurrentlyGrabbed(e.getEntity())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void biteLockAttack(AttackEntityEvent e){
        if(e.getEntity().getPersistentData().getLong("TG_BiteLock") > e.getEntity().level().getGameTime()) { e.setCanceled(true); return; }
        // A holder cannot attack while moving a target; a grabbed target cannot attack either.
        if (KaguneGrabPacket.isHolding(e.getEntity()) || KaguneGrabPacket.isCurrentlyGrabbed(e.getEntity())
                || KaguneGrabPacket.isCurrentlyGrabbed(e.getTarget())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void biteLockBreak(BlockEvent.BreakEvent e){
        if(e.getPlayer().getPersistentData().getLong("TG_BiteLock") > e.getPlayer().level().getGameTime()
                || KaguneGrabPacket.isCurrentlyGrabbed(e.getPlayer())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent e){
        if(e.phase != TickEvent.Phase.END) return;
        ServerLevel overworld = e.getServer().overworld();
        GhoulBossArena.tick(overworld);
        RareLocations.tick(overworld);
    }
    @SubscribeEvent
    public static void join(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)) return;
        GhoulData.get(sp).ifPresent(d -> {
            if(!d.isOriginChosen())
                NetworkHandler.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                    new OpenOriginPacket()
                );
            d.sync(sp);
        });
        // A joining client needs the current kagune state of already-online players,
        // not only its own local HUD state.
        if (sp.getServer() != null) {
            for (net.minecraft.server.level.ServerPlayer other : sp.getServer().getPlayerList().getPlayers()) {
                GhoulData.get(other).ifPresent(data -> NetworkHandler.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                    new com.tokyoghoul.rpg.network.SyncEntityKagunePacket(
                        other.getUUID(), data.getRace().ordinal(), data.isKaguneActive(), data.getKaguneType().ordinal(), data.isRageActive())
                ));
            }
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        KaguneGrabPacket.onLogout(e.getEntity());
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e){
        if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)
            GhoulData.get(sp).ifPresent(d -> d.sync(sp));
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone e){
        e.getOriginal().getPersistentData().getAllKeys().forEach(k ->
            e.getEntity().getPersistentData().put(k,e.getOriginal().getPersistentData().get(k))
        );
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent e){
        if(e.getEntity() instanceof GhoulBoss boss && e.getEntity().level() instanceof ServerLevel sl){
            if(e.getSource().getEntity() instanceof Player p){
                p.getInventory().placeItemBackInInventory(new ItemStack(ModItems.QUINQUE_OWL.get()));
                p.displayClientMessage(Component.literal("§4§lКвинке «Сова» получена"), false);
            } else {
                boss.spawnAtLocation(new ItemStack(ModItems.QUINQUE_OWL.get()));
            }
            GhoulBossArena.onBossDeath(sl);
            return;
        }
        if(!(e.getSource().getEntity() instanceof Player p) || e.getEntity()==p) return;

        GhoulData.get(p).ifPresent(d -> {
            d.addPoints(1);
            d.addRC(1);

            if(d.getRace()==GhoulData.Race.GHOUL || d.getRace()==GhoulData.Race.HALF_GHOUL)
                d.addRage(3);
            else if(d.getRace()==GhoulData.Race.CCG)
                d.addRage(3);

            int q=p.getPersistentData().getInt("GhoulQuestKills")+1;
            p.getPersistentData().putInt("GhoulQuestKills",q);
            if(q>=10){
                d.addPoints(5);
                d.addRC(5);
                if(d.getRace()!=GhoulData.Race.CCG) d.addRage(3);
                else d.addRage(3);
                p.getPersistentData().putInt("GhoulQuestKills",0);
                p.displayClientMessage(Component.literal(
                    "§6Квест выполнен: Охота ×10 — §e+5 очков, +5 RC"
                ),false);
            }

            d.sync(p);
            p.displayClientMessage(Component.literal(
                d.getRace()==GhoulData.Race.CCG ? "§b+3 боевого духа" : "§c+5 ярости"
            ),true);
        });
    }

    @SubscribeEvent
    public static void eatMeat(LivingEntityUseItemEvent.Finish e){
        if(!(e.getEntity() instanceof Player p)) return;
        ItemStack stack=e.getItem();
        if(stack.isEmpty()) return;

        GhoulData.get(p).ifPresent(d -> {
            if(d.getRace()!=GhoulData.Race.GHOUL && d.getRace()!=GhoulData.Race.HALF_GHOUL) return;
            boolean flesh=stack.is(Items.ROTTEN_FLESH);
            var food=stack.getFoodProperties(p);
            boolean meat=food!=null && food.isMeat();
            if(flesh || meat){
                int gain=flesh?6:3;
                d.addRage(gain);
                d.addRC(2);
                d.setHunger(Math.min(100, d.getHunger() + (flesh ? 35 : 20)));
                d.sync(p);
                p.displayClientMessage(Component.literal("§4Плоть поглощена: +"+gain+" ярости"),true);
            }
        });
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent e){
        // A grabbed target cannot be attacked or damaged until release.
        if (KaguneGrabPacket.isCurrentlyGrabbed(e.getEntity())) { e.setCanceled(true); return; }
        // The dodge window is server-authoritative and intentionally very short.
        if (e.getEntity() instanceof Player dodging
                && dodging.getPersistentData().getLong("TG_DodgeUntil") > dodging.level().getGameTime()) {
            e.setCanceled(true);
            return;
        }
        if(!(e.getSource().getEntity() instanceof Player p)) return;

        GhoulData.get(p).ifPresent(d -> {
            boolean quinque = p.getMainHandItem().getItem() instanceof com.tokyoghoul.rpg.item.QuinqueItem;
            if(d.getRace() == GhoulData.Race.CCG && quinque) {
                var stack = p.getMainHandItem();
                if(stack.getOrCreateTag().getBoolean("TG_StrikeReady")) {
                    stack.getOrCreateTag().putBoolean("TG_StrikeReady", false);
                    p.getPersistentData().putLong("TG_QuinqueStrikeCooldown", p.level().getGameTime() + 20L * 30L);
                    e.setAmount(e.getAmount() * 3.0F);
                    d.addRage(5);
                    d.sync(p);
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, false, false));
                    if(p.level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY()+1.0, p.getZ(), 18, .5,.6,.5,.08);
                    p.displayClientMessage(Component.literal("§b§lКВИНКЕ: СИЛЬНЫЙ УДАР"), true);
                }
            }
            if(!d.isRageActive()) return;

            if(d.getRace()==GhoulData.Race.CCG)
                e.setAmount(e.getAmount()*(1.12f + d.getTactics()*0.015f));
            else
                e.setAmount(e.getAmount()*(1.5f + d.getStrength()*0.03f));
        });
    }

    @SubscribeEvent
    public static void quinqueBleed(LivingEvent.LivingTickEvent e){
        LivingEntity entity=e.getEntity();
        int ticks=entity.getPersistentData().getInt("TG_QuinqueBleed");
        if(ticks<=0)return;
        ticks--; entity.getPersistentData().putInt("TG_QuinqueBleed",ticks);
        if(ticks%20==0) entity.hurt(entity.damageSources().generic(),1.0f);
        if(entity.level() instanceof ServerLevel sl && ticks%6==0) sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR,entity.getX(),entity.getY()+entity.getBbHeight()*.6,entity.getZ(),2,.12,.15,.12,.02);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END || e.player.level().isClientSide) return;

        KaguneGrabPacket.tick(e.player);

        // Server-authoritative kagune grapple pull. The client only supplies
        // the activation input; the server validates the block and owns movement.
        long grappleEnd = e.player.getPersistentData().getLong("TG_GrappleEnd");
        if (grappleEnd > e.player.level().getGameTime()) {
            double tx = e.player.getPersistentData().getDouble("TG_GrappleTargetX");
            double ty = e.player.getPersistentData().getDouble("TG_GrappleTargetY");
            double tz = e.player.getPersistentData().getDouble("TG_GrappleTargetZ");
            Vec3 target = new Vec3(tx, ty, tz);
            Vec3 delta = target.subtract(e.player.position());
            double dist = delta.length();

            if (dist <= 0.78D) {
                e.player.setDeltaMovement(Vec3.ZERO);
                e.player.getPersistentData().putLong("TG_GrappleEnd", 0L);
            } else {
                double speed = dist > 5.0D ? 1.85D : Math.max(0.55D, dist * 0.42D);
                Vec3 velocity = delta.normalize().scale(Math.min(2.0D, speed));
                e.player.setDeltaMovement(velocity);
                e.player.hurtMarked = true;
            }
        } else if (grappleEnd != 0L) {
            e.player.getPersistentData().putLong("TG_GrappleEnd", 0L);
        }

        GhoulData.get(e.player).ifPresent(d -> {
            d.tick();

            if(d.isRageActive()){
                if(d.getRace()==GhoulData.Race.CCG){
                    e.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,25,0,false,false,false));
                    e.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,25,0,false,false,false));
                } else {
                    e.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,25,1,false,false,false));
                    e.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,25,1,false,false,false));
                    e.player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,25,Math.min(2,d.getRegen()/5),false,false,false
                    ));
                }

                if(e.player.level() instanceof ServerLevel sl && e.player.tickCount%2==0){
                    double x=e.player.getX(), y=e.player.getY()+1.0, z=e.player.getZ();
                    if(d.getRace()==GhoulData.Race.CCG){
                        sl.sendParticles(new DustParticleOptions(new Vector3f(0.48f,0.50f,0.55f),0.62f),
                            x,y,z,4,0.55,0.8,0.55,0.008);
                        sl.sendParticles(ParticleTypes.CRIT,x,y,z,1,0.25,0.4,0.25,0.01);
                    } else {
                        sl.sendParticles(new DustParticleOptions(new Vector3f(0.62f,0.015f,0.045f),1.05f),
                            x,y,z,7,0.62,0.9,0.62,0.014);
                        sl.sendParticles(ParticleTypes.SMOKE,x,y,z,1,0.28,0.45,0.28,0.006);
                    }
                }
            }

            if(d.getRace()==GhoulData.Race.GHOUL || d.getRace()==GhoulData.Race.HALF_GHOUL){
                // Slower anime-style hunger drain: 1 point every 15 seconds.
                if(e.player.tickCount%300==0) d.setHunger(d.getHunger()-1);
                if(d.isKaguneActive() && e.player.tickCount%10==0)
                    e.player.heal(0.15f+d.getRegen()*0.03f);
                boolean critical=e.player.getHealth()<=e.player.getMaxHealth()*0.20f && d.getHunger()<=20;
                if(critical && d.getBleedingTicks()<=0) d.setBleedingTicks(20*30);
            } else if(d.getRace()==GhoulData.Race.CCG){
                // CCG uses a very slow custom hunger drain: 1 point every 30 seconds.
                if(e.player.tickCount%600==0) d.setHunger(d.getHunger()-1);
                if(e.player.getHealth()<=e.player.getMaxHealth()*0.15f && d.getBleedingTicks()<=0)
                    d.setBleedingTicks(20*30);
            } else if(d.getRace()==GhoulData.Race.HUMAN){
                // Humans also have a slow custom hunger meter: 1 point every 30 seconds.
                if(e.player.tickCount%600==0) d.setHunger(d.getHunger()-1);
            }

            if(d.getBleedingTicks()>0){
                if(e.player.tickCount%20==0){
                    e.player.hurt(e.player.damageSources().generic(), 0.35f);
                }
                if(d.getBleedingTicks()==1){
                    e.player.hurt(e.player.damageSources().fellOutOfWorld(), 1000f);
                }
            }

            if(e.player.tickCount%10==0)
                d.sync(e.player);
        });
    }
}
