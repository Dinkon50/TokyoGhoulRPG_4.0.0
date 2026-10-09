package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server-authoritative Kagune Leap.  The visual language follows the official
 * Dead by Daylight Tokyo Ghoul reference: the kagune attaches first, then the
 * user is propelled toward the anchor. DBD is only an implementation reference;
 * the mod's kagune forms remain based on Tokyo Ghoul material.
 */
public class GrapplePacket {
    private static final double KANEKI_RANGE = 15.0D; // DBD-inspired Kaneki profile
    private static final double MIN_RANGE = 1.75D;
    private static final int COOLDOWN_TICKS = 60; // 3 seconds

    public static void encode(GrapplePacket p, net.minecraft.network.FriendlyByteBuf b) {}
    public static GrapplePacket decode(net.minecraft.network.FriendlyByteBuf b) { return new GrapplePacket(); }

    public static void handle(GrapplePacket p, Supplier<NetworkEvent.Context> c) {
        c.get().enqueueWork(() -> {
            Player s = c.get().getSender();
            if (s == null || KaguneGrabPacket.isHolding(s) || KaguneGrabPacket.isCurrentlyGrabbed(s)) return;
            GhoulData.get(s).ifPresent(d -> {
                if ((d.getRace() != GhoulData.Race.GHOUL && d.getRace() != GhoulData.Race.HALF_GHOUL) || !d.isKaguneActive()) return;
                KaguneType type = d.getKaguneType();
                if (type != KaguneType.KANEKI_RINKAKU) return;

                long now = s.level().getGameTime();
                if (now < s.getPersistentData().getLong("TG_KaguneGrappleCooldown")) return;
                double maxRange = KANEKI_RANGE;

                Vec3 from = s.getEyePosition();
                Vec3 look = s.getLookAngle();
                Vec3 to = from.add(look.scale(maxRange));
                BlockHitResult block = s.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, s));
                EntityHitResult entity = findEntityHit(s, from, to);

                double blockDist = block.getType() == HitResult.Type.BLOCK ? from.distanceTo(block.getLocation()) : Double.MAX_VALUE;
                double entityDist = entity != null ? from.distanceTo(entity.getLocation()) : Double.MAX_VALUE;
                if (blockDist == Double.MAX_VALUE && entityDist == Double.MAX_VALUE) return;

                Vec3 destination;
                Entity targetEntity = null;
                if (entityDist < blockDist) {
                    targetEntity = entity.getEntity();
                    Vec3 targetCenter = targetEntity.getBoundingBox().getCenter();
                    Vec3 away = targetCenter.subtract(s.position());
                    if (away.lengthSqr() < 0.001D) return;
                    destination = targetCenter.subtract(away.normalize().scale(Math.max(1.05D, s.getBbWidth() * 0.9D)));
                } else {
                    // The DBD reference targets vertical environmental surfaces.
                    // This also makes tree trunks/walls valid while preventing a
                    // grapple onto the ground from behaving like a teleport.
                    if (block.getDirection().getAxis().isVertical()) return;
                    Vec3 normal = Vec3.atLowerCornerOf(block.getDirection().getNormal());
                    destination = block.getLocation().add(normal.scale(0.78D));
                }

                double distance = destination.distanceTo(s.position());
                if (distance < MIN_RANGE || distance > maxRange + 0.75D) return;

                s.getPersistentData().putDouble("TG_GrappleTargetX", destination.x);
                s.getPersistentData().putDouble("TG_GrappleTargetY", destination.y);
                s.getPersistentData().putDouble("TG_GrappleTargetZ", destination.z);
                s.getPersistentData().putLong("TG_GrappleEnd", now + 24L);
                s.getPersistentData().putLong("TG_KaguneGrappleCooldown", now + COOLDOWN_TICKS);
                if (targetEntity != null) s.getPersistentData().putString("TG_GrappleTargetEntity", targetEntity.getUUID().toString());
                else s.getPersistentData().remove("TG_GrappleTargetEntity");

                Vec3 delta = destination.subtract(s.position());
                Vec3 velocity = delta.normalize().scale(Math.min(2.55D, 0.82D + distance * 0.15D));
                s.setDeltaMovement(velocity);
                s.hurtMarked = true;
                d.sync(s);
            });
        });
        c.get().setPacketHandled(true);
    }

    private static EntityHitResult findEntityHit(Player player, Vec3 from, Vec3 to) {
        AABB search = player.getBoundingBox().expandTowards(to.subtract(from)).inflate(0.55D);
        return net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
            player, from, to, search,
            e -> e != player && e.isAlive() && !e.isSpectator(),
            0.0F
        );
    }
}
