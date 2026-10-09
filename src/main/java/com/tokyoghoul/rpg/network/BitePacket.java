package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.entity.CCGNPC;
import com.tokyoghoul.rpg.entity.GhoulBoss;
import com.tokyoghoul.rpg.entity.GhoulNPC;
import com.tokyoghoul.rpg.entity.SpecialNPC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Ghoul bite: short lock, small damage, mod-hunger restoration and regeneration. */
public class BitePacket {
    private static final double BITE_RANGE = 3.0D;
    public static void encode(BitePacket p, FriendlyByteBuf b) {}
    public static BitePacket decode(FriendlyByteBuf b) { return new BitePacket(); }
    public static void handle(BitePacket p, Supplier<NetworkEvent.Context> c) {
        c.get().enqueueWork(() -> {
            Player user = c.get().getSender();
            if (user == null) return;
            GhoulData.get(user).ifPresent(d -> {
                if (d.getRace() != GhoulData.Race.GHOUL && d.getRace() != GhoulData.Race.HALF_GHOUL) return;
                long now = user.level().getGameTime();
                if (user.getPersistentData().getLong("TG_BiteCooldown") > now) return;
                LivingEntity target = findTarget(user);
                if (target == null) return;

                target.getPersistentData().putLong("TG_BiteLock", now + 25L);
                target.getPersistentData().putFloat("TG_BiteLockYRot", target.getYRot());
                target.getPersistentData().putFloat("TG_BiteLockXRot", target.getXRot());
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 6, false, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.JUMP, 28, 250, false, false, false));

                // Restore the mod's hunger meter instead of filling vanilla food.
                d.setHunger(100);
                user.heal(Math.max(2.5F, user.getMaxHealth() * 0.12F));
                user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 12, 3, false, false, false));
                d.addRage(5);
                user.getPersistentData().putLong("TG_BiteCooldown", now + 600L); // 30 seconds
                target.hurt(user.damageSources().playerAttack(user), 1.0F);
                user.level().broadcastEntityEvent(user, (byte) 60);
                d.sync(user);
            });
        });
        c.get().setPacketHandled(true);
    }

    private static LivingEntity findTarget(Player user) {
        // The bite is proximity-based: when a humanoid target is within 3 blocks,
        // V can bite it without requiring the player to aim a crosshair at its head.
        var box = user.getBoundingBox().inflate(BITE_RANGE);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : user.level().getEntitiesOfClass(LivingEntity.class, box, x -> x != user && x.isAlive())) {
            double dist = user.distanceToSqr(e);
            if (dist <= BITE_RANGE * BITE_RANGE && dist < bestDist && isBiteTarget(e)
                    && user.hasLineOfSight(e)) {
                best = e;
                bestDist = dist;
            }
        }
        return best;
    }

    private static boolean isBiteTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        return target instanceof Player
            || target instanceof AbstractVillager
            || target instanceof GhoulNPC
            || target instanceof CCGNPC
            || target instanceof SpecialNPC
            || target instanceof GhoulBoss;
    }
}
