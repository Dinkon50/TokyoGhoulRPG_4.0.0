package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PlayerKaguneRenderer {
    // Kagune is a body-attached organic organ/weapon, never an inventory item.
    // Minecraft player model local +Z is the back; the anchor is deliberately behind the torso.
    private static final float KAGUNE_BACK_ANCHOR_Y = 1.34f;
    private static final float KAGUNE_BACK_ANCHOR_Z = 0.34f;
    private static KaguneModel MODEL;
    private static final Map<UUID, Float> EMERGENCE = new HashMap<>();
    private static final Map<UUID, Boolean> PREVIOUS_ACTIVE = new HashMap<>();
    private static final Map<UUID, Float> GRAB_PROGRESS = new HashMap<>();
    private static int grappleTicks = 0;
    private static int dashTicks = 0;
    private static Vec3 grappleTarget = null;
    private static float grappleVisualTicks = 0f;
    private static final Map<UUID, float[]> SMOOTH_VELOCITY = new HashMap<>();
    private static final Map<UUID, float[]> ANIMATION_STATE = new HashMap<>();
    private static final ResourceLocation[] KAGUNE_TEXTURES = new ResourceLocation[] {
        new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_kaneki_rinkaku.png"),
        new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_takizawa_ukaku.png"),
        new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_rize_rinkaku.png"),
        new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_touka_ukaku.png"),
        new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_ayato_ukaku.png"),
        new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_tsukiyama_koukaku.png"),
        new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_nishiki_bikaku.png"),
        new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_noro_special.png")
    };

    // Animation-only impulses. They never affect damage, cooldowns or movement.
    // [0] attack, [1] ability, [2] rage, [3] hit reaction, [4] transition,
    // [5] previous attack, [6] last decay tick, [7] last server-confirmed combo stage.
    private static float[] animationState(UUID id) {
        return ANIMATION_STATE.computeIfAbsent(id, k -> new float[8]);
    }
    public static void triggerAttack() { triggerAttack(ClientGhoulData.comboStage()); }
    public static void triggerAttack(int comboStage) {
        if (Minecraft.getInstance().player == null) return;
        float[] state = animationState(Minecraft.getInstance().player.getUUID());
        state[0] = 1f;
        state[7] = Mth.clamp(comboStage, 0, 2);
        spawnAttackBurst(Minecraft.getInstance().player, currentKaguneType(), false);
    }
    public static void triggerAbility() {
        if (Minecraft.getInstance().player == null) return;
        animationState(Minecraft.getInstance().player.getUUID())[1] = 1f;
        spawnAttackBurst(Minecraft.getInstance().player, currentKaguneType(), true);
    }
    public static void triggerRage() {
        if (Minecraft.getInstance().player == null) return;
        animationState(Minecraft.getInstance().player.getUUID())[2] = 1f;
        spawnRageBurst(Minecraft.getInstance().player);
    }
    public static void triggerHit() { if (Minecraft.getInstance().player != null) animationState(Minecraft.getInstance().player.getUUID())[3] = 1f; }
    public static void triggerTransition() { if (Minecraft.getInstance().player != null) animationState(Minecraft.getInstance().player.getUUID())[4] = 1f; }

    public static void triggerGrapple(Vec3 target){
        grappleTicks = 28;
        grappleVisualTicks = 0f;
        grappleTarget = target;
    }
    public static void triggerDash(){ dashTicks = 24; }
    public static boolean isDashActive(){ return dashTicks > 0; }

    private static float decay(float value, float amount) {
        return value <= 0.001f ? 0f : Math.max(0f, value - amount);
    }

    private static final DustParticleOptions RC_DUST = new DustParticleOptions(new Vector3f(0.78f, 0.035f, 0.085f), 0.72f);
    private static final DustParticleOptions RC_HIGHLIGHT = new DustParticleOptions(new Vector3f(1.0f, 0.16f, 0.20f), 0.42f);
    private static final DustParticleOptions RC_DARK = new DustParticleOptions(new Vector3f(0.16f, 0.012f, 0.025f), 0.88f);

    private static void spawnAttackBurst(net.minecraft.client.player.LocalPlayer player, KaguneType type, boolean special) {
        // Compact 3D RC trail along the attack direction; particle counts stay bounded.
        int count = special ? 14 : 9;
        double reach = type.isUkaku() ? 1.65 : type.isKoukaku() ? 1.10 : type.isBikaku() ? 1.45 : 1.30;
        Vec3 look = player.getLookAngle().normalize();
        Vec3 right = new Vec3(-look.z, 0.0D, look.x);
        for (int i = 0; i < count; i++) {
            double along = 0.35D + reach * (i / (double)Math.max(1, count - 1));
            double spread = (player.getRandom().nextDouble() - 0.5D) * (special ? 0.52D : 0.34D);
            double height = (player.getRandom().nextDouble() - 0.5D) * 0.62D;
            double px = player.getX() + look.x * along + right.x * spread;
            double py = player.getY() + 1.0D + look.y * along + height;
            double pz = player.getZ() + look.z * along + right.z * spread;
            player.level().addParticle(i % 4 == 0 ? RC_HIGHLIGHT : RC_DUST,
                    px, py, pz, look.x * 0.035D, 0.015D + look.y * 0.025D, look.z * 0.035D);
        }
        player.level().addParticle(ParticleTypes.CRIT,
                player.getX() + look.x * reach, player.getY() + 1.0D + look.y * reach,
                player.getZ() + look.z * reach, look.x * 0.10D, 0.025D, look.z * 0.10D);
        if (special) {
            for (int i = 0; i < 4; i++) {
                double side = (player.getRandom().nextDouble() - 0.5D) * 0.7D;
                player.level().addParticle(RC_DARK, player.getX() + look.x * 0.6D + right.x * side,
                        player.getY() + 1.0D + player.getRandom().nextDouble() * 0.9D,
                        player.getZ() + look.z * 0.6D + right.z * side,
                        -look.x * 0.015D, 0.008D, -look.z * 0.015D);
            }
        }
    }

    private static void spawnRageBurst(net.minecraft.client.player.LocalPlayer player) {
        // A restrained, dark crimson RC aura rather than a large cloud of generic soul particles.
        for (int i = 0; i < 16; i++) {
            double a = player.getRandom().nextDouble() * Math.PI * 2.0D;
            double radius = 0.32D + player.getRandom().nextDouble() * 0.72D;
            double y = player.getY() + 0.25D + player.getRandom().nextDouble() * 1.65D;
            player.level().addParticle(i % 3 == 0 ? RC_DARK : RC_DUST,
                    player.getX() + Math.cos(a) * radius, y, player.getZ() + Math.sin(a) * radius,
                    Math.cos(a) * 0.018D, 0.025D + player.getRandom().nextDouble() * 0.025D,
                    Math.sin(a) * 0.018D);
        }
    }

    @SubscribeEvent
    public static void render(RenderPlayerEvent.Post e) {
        var player = e.getEntity();
        UUID playerId = player.getUUID();
        boolean localPlayer = player == Minecraft.getInstance().player;
        ClientGhoulData.EntityKaguneState entityState = ClientGhoulData.entityKagune(playerId);
        if (entityState == null && localPlayer) {
            entityState = new ClientGhoulData.EntityKaguneState(
                    ClientGhoulData.race(), ClientGhoulData.kaguneActive(), ClientGhoulData.kaguneType(), ClientGhoulData.rageActive());
        }
        if (entityState == null || (entityState.race() != GhoulData.Race.GHOUL
                && entityState.race() != GhoulData.Race.HALF_GHOUL)) return;

        boolean active = entityState.active();
        float emergence = EMERGENCE.getOrDefault(playerId, 0f);
        if (active) emergence = Mth.clamp(emergence + 0.050f, 0f, 1f);
        else emergence = Mth.clamp(emergence - 0.0625f, 0f, 1f);
        EMERGENCE.put(playerId, emergence);
        boolean wasActive = PREVIOUS_ACTIVE.getOrDefault(playerId, false);
        boolean grabVisualPending = ClientGhoulData.grabState(playerId) != null;
        if (!active && emergence <= 0.01f && !grabVisualPending) {
            PREVIOUS_ACTIVE.put(playerId, false);
            return;
        }

        if (MODEL == null) MODEL = new KaguneModel(Minecraft.getInstance().getEntityModels().bakeLayer(ClientSetup.KAGUNE_LAYER));
        if (player.tickCount % 200 == 0) cleanupStateMaps();
        KaguneType renderType = kaguneType(entityState.kaguneType());
        PoseStack pose = e.getPoseStack();
        pose.pushPose();
        pose.translate(0.0D, KAGUNE_BACK_ANCHOR_Y, KAGUNE_BACK_ANCHOR_Z);

        float t = player.tickCount + e.getPartialTick();
        Vec3 velocity = player.getDeltaMovement();
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        float rawForward = (float)(velocity.x * -Mth.sin(yaw) + velocity.z * Mth.cos(yaw));
        float rawStrafe = (float)(velocity.x * Mth.cos(yaw) + velocity.z * Mth.sin(yaw));
        float rawVertical = (float)velocity.y;
        float[] sv = SMOOTH_VELOCITY.computeIfAbsent(player.getUUID(), k -> new float[3]);
        float smoothing = player.isFallFlying() || player.getAbilities().flying ? 0.18f : 0.24f;
        sv[0] = Mth.lerp(smoothing, sv[0], rawForward);
        sv[1] = Mth.lerp(smoothing, sv[1], rawStrafe);
        sv[2] = Mth.lerp(smoothing, sv[2], rawVertical);
        float forward = sv[0];
        float strafe = sv[1];
        float vertical = sv[2];
        float move = Mth.clamp((float)Math.sqrt(forward * forward + strafe * strafe) * 6.0f, 0f, 1.8f);
        if (player.isFallFlying() || player.getAbilities().flying) move = Math.max(move, 0.18f);
        float attack = player.getAttackAnim(e.getPartialTick());
        float hurt = player.hurtTime > 0 ? player.hurtTime / 10f : 0f;
        float[] state = animationState(player.getUUID());
        if (attack > 0.10f && state[5] < 0.10f) state[0] = 1f;
        state[5] = attack;
        // Decay impulses once per game tick rather than once per render frame.
        // This keeps the animation duration stable at 30/60/120/144 FPS.
        if (state[6] != player.tickCount) {
            state[0] = decay(state[0], 1f / 12f);
            state[1] = decay(state[1], 1f / 16f);
            state[2] = decay(state[2], 1f / 25f);
            state[3] = decay(state[3], 1f / 11f);
            state[6] = player.tickCount;
        }
        state[4] = Mth.clamp(emergence, 0f, 1f);
        if (hurt > 0.05f) state[3] = Math.max(state[3], hurt);
        if (localPlayer && grappleTicks > 0) grappleTicks--;
        if (localPlayer && dashTicks > 0) dashTicks--;
        // Grapple/dash are local action impulses; remote players still get their
        // synchronized kagune silhouette and normal locomotion animation.
        boolean grappleCapable = renderType == KaguneType.KANEKI_RINKAKU;
        float grapple = localPlayer && grappleCapable && grappleTicks > 0 ? 1f - (grappleTicks / 28f) : 0f;
        float dash = localPlayer && dashTicks > 0 ? 1f - (dashTicks / 24f) : 0f;
        float attackPulse = state[0] * (0.82f + state[7] * 0.09f);
        float abilityPulse = state[1];
        float ragePulse = entityState.rageActive() ? 1f : state[2];
        float hitPulse = Math.max(state[3], hurt);
        float grappleYaw = 0f;
        float grapplePitch = 0f;
        float grappleScale = 1f;
        if (localPlayer && grapple > 0f && grappleTarget != null) {
            Vec3 localTarget = grappleTarget.subtract(player.position()).yRot(-yaw);
            double horizontal = Math.sqrt(localTarget.x * localTarget.x + localTarget.z * localTarget.z);
            grappleYaw = (float)Math.atan2(localTarget.x, localTarget.z);
            grapplePitch = (float)-Math.atan2(localTarget.y + 0.55D, Math.max(0.001D, horizontal));
            double distance = grappleTarget.distanceTo(player.position());
            double maxDistance = 15.0D;
            grappleScale = (float)Mth.clamp(distance / 5.8D, 0.65D, maxDistance / 5.8D);
        }
        if (localPlayer && grappleTicks > 0) grappleVisualTicks += 1f;
        else if (localPlayer && grappleTarget != null) {
            grappleVisualTicks = Math.max(0f, grappleVisualTicks - 0.8f);
            if (grappleVisualTicks <= 0f) grappleTarget = null;
        }
        double distSq = player.distanceToSqr(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        // Keep all 36 longitudinal segments so the kagune never collapses into a short low-LOD stump.
        // Only secondary ridges, plates and membranes are distance-gated.
        int detail = 36;
        boolean microDetail = distSq <= 48.0 * 48.0;
        MODEL.animate(renderType, t, move, attack, hitPulse, player.onGround(), player.isCrouching(), emergence, grapple, dash, forward, strafe, vertical, attackPulse, abilityPulse, ragePulse, grappleYaw, grapplePitch, grappleScale, detail, microDetail);
        if (attackPulse > 0.04f) MODEL.applyComboPose((int)state[7], attackPulse, t);

        // A separate one-tendril pose is used for the 10-second entity grab.
        ClientGhoulData.GrabState grabState = ClientGhoulData.grabState(playerId);
        float grabProgress = GRAB_PROGRESS.getOrDefault(playerId, 0f);
        if (grabState != null && grabState.active()) grabProgress = Mth.clamp(grabProgress + 0.10f, 0f, 1f);
        else grabProgress = Mth.clamp(grabProgress - 0.075f, 0f, 1f);
        if (grabProgress > 0.001f && grabState != null && grabState.targetId() != null && Minecraft.getInstance().level != null) {
            var targetEntity = findClientEntity(grabState.targetId());
            if (targetEntity != null && targetEntity.isAlive()) {
                Vec3 localTarget = targetEntity.getBoundingBox().getCenter().subtract(player.position()).yRot(-yaw);
                double dx = localTarget.x;
                double dy = localTarget.y - KAGUNE_BACK_ANCHOR_Y;
                double dz = localTarget.z - KAGUNE_BACK_ANCHOR_Z;
                double horizontal = Math.sqrt(dx * dx + dz * dz);
                float grabYaw = (float)Math.atan2(dx, dz);
                float grabPitch = (float)-Math.atan2(dy, Math.max(0.001D, horizontal));
                float reach = (float)Mth.clamp(Math.sqrt(dx * dx + dy * dy + dz * dz) / 5.8D, 0.45D, 1.75D);
                MODEL.applyGrabPose(grabYaw, grabPitch, reach, grabProgress, t);
            }
        }
        GRAB_PROGRESS.put(playerId, grabProgress);
        if (grabProgress <= 0.001f && grabState != null && !grabState.active()) {
            GRAB_PROGRESS.remove(playerId);
            ClientGhoulData.removeGrabState(playerId);
        }

        MultiBufferSource buf = e.getMultiBufferSource();
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(texture(renderType)));
        MODEL.renderToBuffer(pose, vc, e.getPackedLight(), OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 0.98f);
        pose.popPose();

        // A restrained burst of particles makes release feel physical rather than like an item swap.
        if (localPlayer && active && !wasActive && player.level().isClientSide) {
            for (int i = 0; i < 18; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 0.9;
                double oy = 0.7 + player.getRandom().nextDouble() * 1.1;
                double oz = (player.getRandom().nextDouble() - 0.5) * 0.6;
                player.level().addParticle(i % 3 == 0 ? RC_HIGHLIGHT : RC_DUST,
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    (player.getRandom().nextDouble() - 0.5) * 0.025, 0.018, (player.getRandom().nextDouble() - 0.5) * 0.025);
            }
        }
        if (player == Minecraft.getInstance().player && active && dashTicks > 0 && player.level().isClientSide && player.tickCount % 2 == 0) {
            Vec3 look = player.getLookAngle();
            for (int i = 0; i < 2; i++) {
                double side = (player.getRandom().nextDouble() - 0.5) * 0.55;
                player.level().addParticle(ParticleTypes.CLOUD,
                    player.getX() - look.x * 0.35 + side, player.getY() + 0.15 + player.getRandom().nextDouble() * 0.8,
                    player.getZ() - look.z * 0.35 + side, -look.x * 0.03, 0.005, -look.z * 0.03);
            }
        }
        if (player == Minecraft.getInstance().player && active && player.tickCount % 10 == 0 && player.level().isClientSide) {
            player.level().addParticle(RC_DARK, player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.45,
                    player.getY() + 0.65 + player.getRandom().nextDouble() * 0.85,
                    player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.45, 0, 0.012, 0);
        }
        PREVIOUS_ACTIVE.put(playerId, active);
    }

    private static KaguneType currentKaguneType() {
        int i = ClientGhoulData.kaguneType();
        KaguneType[] v = KaguneType.values();
        return i >= 0 && i < v.length ? v[i] : KaguneType.KANEKI_RINKAKU;
    }

    private static net.minecraft.world.entity.Entity findClientEntity(UUID id) {
        var level = Minecraft.getInstance().level;
        if (level == null || id == null) return null;
        var targetPlayer = level.getPlayerByUUID(id);
        if (targetPlayer != null) return targetPlayer;
        for (var entity : level.entitiesForRendering()) if (entity.getUUID().equals(id)) return entity;
        return null;
    }

    private static ResourceLocation texture(KaguneType type) {
        int i = type.ordinal() - 1; // enum ordinal 0 is NONE; texture array contains the 8 playable profiles.
        return i >= 0 && i < KAGUNE_TEXTURES.length ? KAGUNE_TEXTURES[i] : KAGUNE_TEXTURES[0];
    }

    private static KaguneType kaguneType(int ordinal) {
        KaguneType[] values = KaguneType.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : KaguneType.KANEKI_RINKAKU;
    }

    private static void cleanupStateMaps() {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var online = new java.util.HashSet<UUID>();
        for (var p : mc.level.players()) online.add(p.getUUID());
        SMOOTH_VELOCITY.keySet().removeIf(id -> !online.contains(id));
        ANIMATION_STATE.keySet().removeIf(id -> !online.contains(id));
        EMERGENCE.keySet().removeIf(id -> !online.contains(id));
        PREVIOUS_ACTIVE.keySet().removeIf(id -> !online.contains(id));
        GRAB_PROGRESS.keySet().removeIf(id -> !online.contains(id));
        for (UUID id : ClientGhoulData.entityKaguneIds()) if (!online.contains(id)) ClientGhoulData.removeEntityKagune(id);
        for (UUID id : ClientGhoulData.grabStateOwners()) if (!online.contains(id)) ClientGhoulData.removeGrabState(id);
    }

    private PlayerKaguneRenderer() {}
}
