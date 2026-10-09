package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** Server-authoritative kagune strike with a short hit-confirmed three-step combo. */
public record KaguneAttackPacket(int action) {
    private static final int COMBO_WINDOW_TICKS = 14;
    private static final long ATTACK_COOLDOWN_TICKS = 8L;

    public static void encode(KaguneAttackPacket packet, FriendlyByteBuf buffer) { buffer.writeInt(packet.action()); }
    public static KaguneAttackPacket decode(FriendlyByteBuf buffer) { return new KaguneAttackPacket(buffer.readInt()); }

    public static void handle(KaguneAttackPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            var player = context.get().getSender();
            if (player == null || packet.action() < 0 || packet.action() > 1
                    || KaguneGrabPacket.isHolding(player)) return;
            GhoulData.get(player).ifPresent(data -> {
                if ((data.getRace() != GhoulData.Race.GHOUL && data.getRace() != GhoulData.Race.HALF_GHOUL)
                        || !data.isKaguneActive()) return;

                long now = player.level().getGameTime();
                if (now < player.getPersistentData().getLong("TG_KaguneAttackCooldown")) return;
                player.getPersistentData().putLong("TG_KaguneAttackCooldown", now + ATTACK_COOLDOWN_TICKS);

                KaguneType type = data.getKaguneType();
                double range = type.isUkaku() ? 20.0D : type.isKoukaku() ? 3.8D : type.isBikaku() ? 6.0D : 4.8D;
                Vec3 from = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                Vec3 to = from.add(look.scale(range));
                var clip = player.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                double max = clip.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                        ? from.distanceTo(clip.getLocation()) : range;
                AABB search = player.getBoundingBox().expandTowards(look.scale(max)).inflate(type.isKoukaku() ? 0.9D : 0.65D);
                LivingEntity best = null;
                double bestAlong = Double.MAX_VALUE;
                for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, search,
                        entity -> entity != player && entity.isAlive() && !entity.isSpectator())) {
                    Vec3 delta = candidate.getBoundingBox().getCenter().subtract(from);
                    double along = delta.dot(look);
                    if (along < 0.0D || along > max) continue;
                    double side = delta.subtract(look.scale(along)).length();
                    if (side > candidate.getBbWidth() + 0.75D) continue;
                    if (along < bestAlong) { best = candidate; bestAlong = along; }
                }

                if (best == null) {
                    // Combo advances only on confirmed hits; a miss breaks the chain.
                    player.getPersistentData().putInt("TG_KaguneComboStage", 0);
                    player.getPersistentData().putLong("TG_KaguneComboExpire", 0L);
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new KaguneComboSyncPacket(0));
                    return;
                }

                int comboStage = 0;
                float comboMultiplier = 1.0f;
                if (packet.action() == 0) {
                    long comboExpire = player.getPersistentData().getLong("TG_KaguneComboExpire");
                    int previous = player.getPersistentData().getInt("TG_KaguneComboStage");
                    comboStage = now <= comboExpire ? (previous + 1) % 3 : 0;
                    comboMultiplier = comboStage == 0 ? 1.0f : comboStage == 1 ? 1.12f : 1.28f;
                    player.getPersistentData().putInt("TG_KaguneComboStage", comboStage);
                    player.getPersistentData().putLong("TG_KaguneComboExpire", now + COMBO_WINDOW_TICKS);
                } else {
                    comboStage = 2;
                    comboMultiplier = 1.35f;
                    player.getPersistentData().putInt("TG_KaguneComboStage", 0);
                    player.getPersistentData().putLong("TG_KaguneComboExpire", 0L);
                }

                float baseDamage = type.isKoukaku() ? 5.2f + data.getKagune() * 1.15f
                        : type.isUkaku() ? 4.2f + data.getKagune() * 0.95f
                        : type.isBikaku() ? 4.8f + data.getKagune()
                        : 4.5f + data.getKagune();
                best.hurt(player.damageSources().playerAttack(player), baseDamage * comboMultiplier);
                if (type.isUkaku()) best.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 20, 0));
                if (type.isKoukaku()) best.knockback(packet.action() > 0 ? 0.85D : 0.65D,
                        player.getX() - best.getX(), player.getZ() - best.getZ());
                if (packet.action() == 0 && comboStage == 2) {
                    best.knockback(0.35D, player.getX() - best.getX(), player.getZ() - best.getZ());
                }

                if (player.level() instanceof ServerLevel server) {
                    int count = packet.action() > 0 ? 18 : comboStage == 2 ? 15 : comboStage == 1 ? 12 : 9;
                    server.sendParticles(ParticleTypes.CRIT, best.getX(), best.getY() + best.getBbHeight() * 0.55D,
                            best.getZ(), count, 0.2D, 0.2D, 0.2D, 0.08D);
                    server.playSound(null, player.blockPosition(), TokyoGhoulRPG.KAGUNE_HIT.get(),
                            player.getSoundSource(), 0.82f, packet.action() > 0 ? 0.55f : comboStage == 2 ? 0.62f : 0.80f + comboStage * 0.08f);
                }
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new KaguneComboSyncPacket(packet.action() == 0 ? comboStage : 0));
            });
        });
        context.get().setPacketHandled(true);
    }
}
