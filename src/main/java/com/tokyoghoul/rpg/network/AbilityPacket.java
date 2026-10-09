package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.entity.GhoulNPC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AbilityPacket(int ability, float forward, float strafe){
    public AbilityPacket(int ability){ this(ability, 0.0f, 0.0f); }
    public static void encode(AbilityPacket p,FriendlyByteBuf b){b.writeInt(p.ability);b.writeFloat(p.forward);b.writeFloat(p.strafe);}
    public static AbilityPacket decode(FriendlyByteBuf b){return new AbilityPacket(b.readInt(),b.readFloat(),b.readFloat());}

    public static void handle(AbilityPacket p,Supplier<NetworkEvent.Context> c){
        c.get().enqueueWork(() -> {
            var s=c.get().getSender();
            if(s==null || p.ability<0 || p.ability>2 || KaguneGrabPacket.isHolding(s)
                    || KaguneGrabPacket.isCurrentlyGrabbed(s)) return;
            GhoulData.get(s).ifPresent(d -> {
                // Ability 1 is the universal dash and is available to every origin.
                if(p.ability != 1 && d.getRace()==GhoulData.Race.HUMAN){
                    s.displayClientMessage(Component.literal("§7У человека пока нет боевых способностей."), true);
                    return;
                }
                if(p.ability != 1 && (d.getRace()==GhoulData.Race.GHOUL || d.getRace()==GhoulData.Race.HALF_GHOUL) && !d.isKaguneActive()){
                    s.displayClientMessage(Component.literal("§8Сначала выпусти кагуне — §fG"), true);
                    return;
                }
                if(p.ability==0){
                    boolean allowed = d.getRace()==GhoulData.Race.CCG ? d.quinqueTechnique1() : d.kaguneTechnique1();
                    if(!allowed){ s.displayClientMessage(Component.literal("§7Эта техника ещё не изучена в древе."), true); return; }
                }
                if(!d.useAbility()){
                    s.displayClientMessage(Component.literal("§7Способность перезаряжается."), true);
                    return;
                }

                if(p.ability==0){
                    float damage = d.getRace()==GhoulData.Race.CCG ? 6.0f + d.getQuinque()*1.65f : 6.0f + d.getKagune()*1.45f;
                    if(d.getRace()==GhoulData.Race.CCG){
                        // Quinque technique: a focused forward strike with a short defensive recovery.
                        for(LivingEntity target : s.level().getEntitiesOfClass(LivingEntity.class, s.getBoundingBox().inflate(3.2), x -> x != s && x.isAlive())){
                            target.hurt(s.damageSources().playerAttack(s), damage);
                        }
                        s.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 18, 0, false, false, true));
                        s.displayClientMessage(Component.literal("§bТехника Квинке: усиленный удар"), true);
                    } else {
                        var type=d.getKaguneType();
                        double radius = type.isKoukaku() ? 3.0 : 3.8;
                        for(LivingEntity target : s.level().getEntitiesOfClass(LivingEntity.class, s.getBoundingBox().inflate(radius), x -> x != s && x.isAlive())){
                            target.hurt(s.damageSources().playerAttack(s), damage);
                            if(type.isUkaku()) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, false, true));
                            if(type.isBikaku()) target.knockback(0.55, s.getX()-target.getX(), s.getZ()-target.getZ());
                        }
                        s.displayClientMessage(Component.literal("§cТехника кагуне: удар"), true);
                    }
                    s.level().broadcastEntityEvent(s,(byte)60);
                    d.setAbilityCooldown(32);
                } else if(p.ability==1){
                    // Directional dodge: use the client's movement intent, falling back to facing direction.
                    float forwardInput = Math.max(-1f, Math.min(1f, p.forward()));
                    float strafeInput = Math.max(-1f, Math.min(1f, p.strafe()));
                    float yaw = s.getYRot() * ((float)Math.PI / 180f);
                    Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
                    Vec3 right = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));
                    Vec3 direction = forward.scale(forwardInput).subtract(right.scale(strafeInput));
                    if (direction.lengthSqr() < 0.01D) direction = new Vec3(s.getLookAngle().x, 0.0D, s.getLookAngle().z);
                    if (direction.lengthSqr() < 0.01D) direction = forward;
                    direction = direction.normalize();
                    s.setDeltaMovement(direction.scale(1.05D).add(0.0D, 0.16D, 0.0D));
                    s.getPersistentData().putLong("TG_DodgeUntil", s.level().getGameTime() + 6L);
                    s.hurtMarked=true;
                    s.displayClientMessage(Component.literal("§eУклонение!"), true);
                    d.setAbilityCooldown(35);
                } else {
                    boolean advanced = d.getRace()==GhoulData.Race.CCG ? d.quinqueTechnique2() : d.kaguneTechnique2();
                    if(advanced){
                        if(d.getRace()==GhoulData.Race.CCG){
                            for(LivingEntity target : s.level().getEntitiesOfClass(LivingEntity.class, s.getBoundingBox().inflate(4.2), x -> x != s && x.isAlive())){
                                target.hurt(s.damageSources().playerAttack(s), 8.0f + d.getQuinque()*1.9f);
                                target.knockback(0.85, s.getX()-target.getX(), s.getZ()-target.getZ());
                            }
                            s.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 30, 1, false, false, true));
                            s.displayClientMessage(Component.literal("§bМастер-техника Квинке"), true);
                        } else {
                            var type=d.getKaguneType();
                            for(LivingEntity target : s.level().getEntitiesOfClass(LivingEntity.class, s.getBoundingBox().inflate(4.5), x -> x != s && x.isAlive())){
                                target.hurt(s.damageSources().playerAttack(s), 8.0f + d.getKagune()*1.7f);
                                if(type.isKoukaku()) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,35,1,false,false,true));
                                else if(type.isUkaku() || false) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,20,0,false,false,true));
                                else target.knockback(0.95, s.getX()-target.getX(), s.getZ()-target.getZ());
                            }
                            s.heal(2.0f + d.getRegen()*0.25f);
                            s.displayClientMessage(Component.literal("§cПродвинутая техника кагуне"), true);
                        }
                        s.level().broadcastEntityEvent(s,(byte)61);
                        d.setAbilityCooldown(70);
                    } else {
                        float heal = d.getRace()==GhoulData.Race.CCG ? 2.0f + d.getTactics()*0.35f : 3.0f + d.getRegen()*0.5f;
                        s.heal(heal);
                        s.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 60, d.getRace()==GhoulData.Race.CCG ? 0 : 1, false, false, true));
                        s.displayClientMessage(Component.literal("§aВосстановление: +"+String.format(java.util.Locale.ROOT,"%.1f",heal)+" HP"), true);
                        d.setAbilityCooldown(100);
                    }
                }
                d.sync(s);
            });
        });
        c.get().setPacketHandled(true);
    }
}
