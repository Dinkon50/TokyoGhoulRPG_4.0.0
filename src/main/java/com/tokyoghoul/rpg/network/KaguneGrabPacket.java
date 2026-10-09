package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.entity.GhoulBoss;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Shift + left-click grab. This is a gameplay-only mod mechanic; the server owns
 * target selection, the 10-block limit, movement, the 10-second timer and release.
 */
public class KaguneGrabPacket {
    private static final double MAX_RANGE = 10.0D;
    private static final long MAX_HOLD_TICKS = 200L;
    private static final String OWNER_TARGET = "TG_KaguneGrabTarget";
    private static final String OWNER_END = "TG_KaguneGrabEnd";
    private static final String TARGET_OWNER = "TG_KaguneGrabbedBy";

    public static void encode(KaguneGrabPacket packet, FriendlyByteBuf buffer) {}
    public static KaguneGrabPacket decode(FriendlyByteBuf buffer) { return new KaguneGrabPacket(); }

    public static void handle(KaguneGrabPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer owner = context.get().getSender();
            if (owner == null) return;
            if (owner.getPersistentData().hasUUID(OWNER_TARGET)) {
                release(owner);
                return;
            }
            GhoulData.get(owner).ifPresent(data -> {
                if ((data.getRace() != GhoulData.Race.GHOUL && data.getRace() != GhoulData.Race.HALF_GHOUL)
                        || !data.isKaguneActive()) return;
                // Only long, flexible Rinkaku profiles can perform a restraint grab.
                // Short wing/armour/tail profiles must not gain an invented long-range tentacle.
                if (!supportsGrab(data.getKaguneType())) {
                    owner.displayClientMessage(net.minecraft.network.chat.Component.literal("§7Эта форма кагуне слишком короткая для захвата."), true);
                    return;
                }
                LivingEntity target = findTarget(owner);
                if (target == null) return; // includes targets beyond the strict 10-block reach
                long now = owner.level().getGameTime();
                owner.getPersistentData().putUUID(OWNER_TARGET, target.getUUID());
                owner.getPersistentData().putLong(OWNER_END, now + MAX_HOLD_TICKS);
                target.getPersistentData().putUUID(TARGET_OWNER, owner.getUUID());
                target.fallDistance = 0f;
                if (target instanceof Mob mob) mob.getNavigation().stop();
                syncState(owner, target, true);
                owner.playSound(TokyoGhoulRPG.KAGUNE_APPEAR.get(), 0.8f, 0.82f + owner.getRandom().nextFloat() * 0.12f);
            });
        });
        context.get().setPacketHandled(true);
    }

    public static boolean supportsGrab(com.tokyoghoul.rpg.v2.KaguneType type) {
        return type == com.tokyoghoul.rpg.v2.KaguneType.KANEKI_RINKAKU
                || type == com.tokyoghoul.rpg.v2.KaguneType.RIZE_RINKAKU;
    }

    private static LivingEntity findTarget(ServerPlayer owner) {
        Vec3 from = owner.getEyePosition();
        Vec3 look = owner.getLookAngle().normalize();
        Vec3 to = from.add(look.scale(MAX_RANGE));
        var block = owner.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        double blockDistance = block.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                ? from.distanceTo(block.getLocation()) : MAX_RANGE;
        AABB search = owner.getBoundingBox().expandTowards(look.scale(MAX_RANGE)).inflate(1.0D);
        LivingEntity best = null;
        double bestAlong = Double.MAX_VALUE;
        for (LivingEntity candidate : owner.level().getEntitiesOfClass(LivingEntity.class, search,
                entity -> entity != owner && entity.isAlive() && validTarget(entity))) {
            Vec3 delta = candidate.getBoundingBox().getCenter().subtract(from);
            double along = delta.dot(look);
            if (along < 0.0D || along > MAX_RANGE || along > blockDistance) continue;
            double side = delta.subtract(look.scale(along)).length();
            if (side > Math.max(0.70D, candidate.getBbWidth() * 0.72D)) continue;
            if (owner.distanceTo(candidate) > MAX_RANGE) continue;
            if (along < bestAlong) { best = candidate; bestAlong = along; }
        }
        return best;
    }

    private static boolean validTarget(LivingEntity target) {
        if (target instanceof GhoulBoss || target.isSpectator()) return false;
        String className = target.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT);
        if (className.contains("boss") || target.getTags().stream().anyMatch(tag -> tag.toLowerCase(java.util.Locale.ROOT).contains("boss"))) return false;
        if (!(target instanceof Player) && (!(target instanceof Mob) || target instanceof Animal)) return false;
        // Prevent grabbing large/tall entities as well as the explicit boss class.
        return target.getBbWidth() <= 1.0f && target.getBbHeight() <= 2.6f
                && !target.getPersistentData().hasUUID(TARGET_OWNER);
    }

    public static boolean isHolding(Player player) {
        return player.getPersistentData().hasUUID(OWNER_TARGET);
    }

    /** Returns true only while the recorded owner still actively holds this exact entity. */
    public static boolean isCurrentlyGrabbed(Entity target) {
        if (!target.getPersistentData().hasUUID(TARGET_OWNER) || !(target.level() instanceof ServerLevel level)) return false;
        UUID ownerId = target.getPersistentData().getUUID(TARGET_OWNER);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        boolean active = owner != null && owner.serverLevel() == level
                && owner.getPersistentData().hasUUID(OWNER_TARGET)
                && owner.getPersistentData().getUUID(OWNER_TARGET).equals(target.getUUID())
                && owner.getPersistentData().getLong(OWNER_END) > level.getGameTime();
        if (!active) target.getPersistentData().remove(TARGET_OWNER);
        return active;
    }

    /** Called once per server player tick. */
    public static void tick(Player player) {
        if (!(player instanceof ServerPlayer owner) || !owner.getPersistentData().hasUUID(OWNER_TARGET)) return;
        long now = owner.level().getGameTime();
        UUID targetId = owner.getPersistentData().getUUID(OWNER_TARGET);
        Entity entity = owner.serverLevel().getEntity(targetId);
        if (!(entity instanceof LivingEntity target) || !target.isAlive()
                || now >= owner.getPersistentData().getLong(OWNER_END)
                || owner.distanceTo(target) > MAX_RANGE
                || !GhoulData.get(owner).map(d -> d.isKaguneActive()
                    && (d.getRace() == GhoulData.Race.GHOUL || d.getRace() == GhoulData.Race.HALF_GHOUL)
                    && supportsGrab(d.getKaguneType())).orElse(false)) {
            release(owner);
            return;
        }

        Vec3 from = owner.getEyePosition();
        Vec3 look = owner.getLookAngle().normalize();
        Vec3 to = from.add(look.scale(2.45D));
        var wall = owner.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        double holdDistance = wall.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                ? Math.max(0.9D, Math.min(2.45D, from.distanceTo(wall.getLocation()) - 0.55D)) : 2.45D;
        Vec3 desiredCenter = from.add(look.scale(holdDistance)).add(0.0D, -0.35D, 0.0D);
        Vec3 delta = desiredCenter.subtract(target.getBoundingBox().getCenter());
        Vec3 velocity = delta.scale(0.28D);
        if (velocity.length() > 0.85D) velocity = velocity.normalize().scale(0.85D);
        target.setDeltaMovement(velocity);
        target.hurtMarked = true;
        target.fallDistance = 0f;
        target.getPersistentData().putUUID(TARGET_OWNER, owner.getUUID());
        if (target instanceof Mob mob) mob.getNavigation().stop();
        // Suppress AI and actions for the entire hold; look/head rotation remains client-controlled.
        target.getPersistentData().putLong("TG_GrabLock", now + 2L);
        target.getPersistentData().putFloat("TG_GrabLockBodyYaw", target.getYRot());
    }

    public static void release(ServerPlayer owner) {
        if (!owner.getPersistentData().hasUUID(OWNER_TARGET)) return;
        UUID targetId = owner.getPersistentData().getUUID(OWNER_TARGET);
        Entity entity = owner.level() instanceof ServerLevel level ? level.getEntity(targetId) : null;
        if (entity instanceof LivingEntity target && target.getPersistentData().hasUUID(TARGET_OWNER)
                && target.getPersistentData().getUUID(TARGET_OWNER).equals(owner.getUUID())) {
            target.getPersistentData().remove(TARGET_OWNER);
            target.fallDistance = 0f;
        }
        owner.getPersistentData().remove(OWNER_TARGET);
        owner.getPersistentData().remove(OWNER_END);
        syncState(owner, entity instanceof LivingEntity living ? living : null, false, targetId);
        owner.playSound(TokyoGhoulRPG.KAGUNE_RETRACT.get(), 0.72f, 0.92f + owner.getRandom().nextFloat() * 0.10f);
    }

    public static void onLogout(Player player) {
        if (player instanceof ServerPlayer serverPlayer && player.getPersistentData().hasUUID(OWNER_TARGET)) release(serverPlayer);
        if (player.getPersistentData().hasUUID(TARGET_OWNER) && player.getServer() != null) {
            UUID ownerId = player.getPersistentData().getUUID(TARGET_OWNER);
            ServerPlayer owner = player.getServer().getPlayerList().getPlayer(ownerId);
            if (owner != null) release(owner);
            player.getPersistentData().remove(TARGET_OWNER);
        }
    }

    private static void syncState(ServerPlayer owner, LivingEntity target, boolean active) {
        syncState(owner, target, active, target == null ? new UUID(0L, 0L) : target.getUUID());
    }

    private static void syncState(ServerPlayer owner, LivingEntity target, boolean active, UUID targetId) {
        KaguneGrabStatePacket packet = new KaguneGrabStatePacket(owner.getUUID(), targetId, active);
        NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> owner), packet);
        if (target instanceof ServerPlayer targetPlayer && targetPlayer != owner) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> targetPlayer), packet);
        }
    }

    public KaguneGrabPacket() {}
}
