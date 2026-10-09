package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Procedural organic kagune model. The four standard branches use many short
 * articulated segments so movement reads as a flexible biological appendage.
 * Legacy non-canon slots are retained only for save compatibility; canonical selection is handled separately.
 */
public class KaguneModel extends EntityModel<Player> {
    // The model itself starts behind the torso; PlayerKaguneRenderer places its root at the back.
    // Root branches spread after emerging, but their attachment point remains on the back.

    private static final int ROOTS = 4, SEGS = 36;
    private final ModelPart[][] seg = new ModelPart[ROOTS][SEGS];
    private final ModelPart[][] ridge = new ModelPart[ROOTS][SEGS];
    private final ModelPart[][] spine = new ModelPart[ROOTS][SEGS];
    private final ModelPart[][] wingMembrane = new ModelPart[ROOTS][SEGS];
    private final ModelPart[][] armorPlate = new ModelPart[ROOTS][SEGS];
    private final ModelPart[] roots = new ModelPart[ROOTS];
    private final ModelPart[] accents = new ModelPart[ROOTS];
    private int activeSegments = SEGS;
    private boolean activeMicroDetail = true;

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            PartDefinition branch = root.addOrReplaceChild("r" + r,
                CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-3.0f, -2.7f, 0, 6.0f, 5.4f, 6.0f),
                PartPose.offset(side * 1.55f, -0.55f + row * 0.42f, 1.85f + row * 0.58f));

            for (int s = 0; s < SEGS; s++) {
                float t = s / (float)(SEGS - 1);
                float w = 4.8f * (1f - 0.70f * t) + 0.70f;
                float h = 4.2f * (1f - 0.72f * t) + 0.60f;
                float len = 5.4f - s * 0.13f;
                branch = branch.addOrReplaceChild("s" + s,
                    CubeListBuilder.create()
                        .texOffs((s % 16) * 64, (r * 3 + s / 16) * 64)
                        .addBox(-w / 2f, -h / 2f, 0, w, h, len),
                    PartPose.offset(0, 0, s == 0 ? 5.55f : len - 0.28f));
                // Extra 3D organic ridges and small dorsal spines give every segment
                // a real volumetric silhouette instead of looking like flat cubes.
                branch.addOrReplaceChild("ridge",
                    CubeListBuilder.create().texOffs((s % 16) * 64, 768 + r * 64)
                        .addBox(-w * 0.24f, -h * 0.62f, 0.45f, w * 0.48f, h * 0.20f, len * 0.82f),
                    PartPose.ZERO);
                branch.addOrReplaceChild("spine",
                    CubeListBuilder.create().texOffs((s % 16) * 64, 780 + r * 64)
                        .addBox(-0.34f, -h * 0.95f, len * 0.24f, 0.68f, h * 0.55f, Math.max(0.9f, len * 0.18f)),
                    PartPose.ZERO);
                // Dedicated volumetric surfaces: Ukaku gets layered wing membranes,
                // Koukaku gets dense armored plates. They are real 3D boxes, not sprites.
                branch.addOrReplaceChild("membrane",
                    CubeListBuilder.create().texOffs((s % 16) * 64, 792 + r * 64)
                        .addBox(-5.2f, -0.22f, 0.55f, 10.4f, 0.44f, Math.max(1.0f, len * 0.88f)),
                    PartPose.ZERO);
                branch.addOrReplaceChild("plate",
                    CubeListBuilder.create().texOffs((s % 16) * 64, 804 + r * 64)
                        .addBox(-3.8f, -2.6f, 0.35f, 7.6f, 5.2f, Math.max(1.0f, len * 0.78f)),
                    PartPose.ZERO);
            }

            branch.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs((r % 16) * 64, 816 + r * 64)
                    .addBox(-0.48f, -0.48f, 0, 0.96f, 0.96f, 5.4f),
                PartPose.offset(0, 0, 4.35f));

            PartDefinition original = root.getChild("r" + r);
            original.addOrReplaceChild("accent",
                CubeListBuilder.create().texOffs((r % 16) * 64, 816 + r * 64)
                    .addBox(-1.05f, -0.42f, 0.5f, 2.1f, 0.84f, 5.0f),
                PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 1024, 1024);
    }

    public KaguneModel(ModelPart root) {
        for (int r = 0; r < ROOTS; r++) {
            ModelPart p = root.getChild("r" + r);
            roots[r] = p;
            accents[r] = p.getChild("accent");
            for (int s = 0; s < SEGS; s++) {
                seg[r][s] = p.getChild("s" + s);
                ridge[r][s] = seg[r][s].getChild("ridge");
                spine[r][s] = seg[r][s].getChild("spine");
                wingMembrane[r][s] = seg[r][s].getChild("membrane");
                armorPlate[r][s] = seg[r][s].getChild("plate");
                p = seg[r][s];
            }
        }
    }

    public void animate(KaguneType type, float age, float move, float attack, float hurt,
                        boolean onGround, boolean crouching, float emergence, float grapple, float dash, float forward, float strafe, float vertical,
                        float attackPulse, float abilityPulse, float ragePulse,
                        float grappleYaw, float grapplePitch, float grappleScale) {
        animate(type, age, move, attack, hurt, onGround, crouching, emergence, grapple, dash, forward, strafe, vertical,
                attackPulse, abilityPulse, ragePulse, grappleYaw, grapplePitch, grappleScale, SEGS, true);
    }

    /**
     * Performance-aware animation path. All 36 nested segments remain visible so
     * the appendage never appears chopped at distance; distant entities skip only
     * secondary ridges, spines, wing membranes and armor plates.
     */
    public void animate(KaguneType type, float age, float move, float attack, float hurt,
                        boolean onGround, boolean crouching, float emergence, float grapple, float dash, float forward, float strafe, float vertical,
                        float attackPulse, float abilityPulse, float ragePulse,
                        float grappleYaw, float grapplePitch, float grappleScale, int detailSegments, boolean microDetail) {
        float locomotion = Mth.clamp(move, 0f, 1.8f);
        float emergenceEase = emergence * emergence * (3f - 2f * emergence);
        float attackEase = attack * attack * (3f - 2f * attack);
        // First the root snaps out, then the length catches up, reproducing a
        // muscle-like launch rather than a rigid telescoping rod.
        float grappleLaunch = Mth.clamp(grapple / 0.28f, 0f, 1f);
        float grappleEase = grapple * grapple * (3f - 2f * grapple);
        float grappleReach = Mth.clamp((grapple - 0.10f) / 0.90f, 0f, 1f);
        grappleReach = grappleReach * grappleReach * (3f - 2f * grappleReach);
        float dashEase = dash * dash * (3f - 2f * dash);
        // Keep the entire nested chain visible at every distance. Hiding a parent
        // ModelPart also hides all of its descendant segments and visibly chops the
        // kagune short, so distance LOD only disables secondary micro-geometry.
        activeSegments = SEGS;
        activeMicroDetail = microDetail;

        for (int r = 0; r < ROOTS; r++) {
            roots[r].xScale = roots[r].yScale = roots[r].zScale = 1f;
            accents[r].xScale = accents[r].yScale = accents[r].zScale = 1f;
            roots[r].visible = true;
            accents[r].visible = true;
            for (int s = 0; s < SEGS; s++) {
                boolean visible = s < activeSegments;
                seg[r][s].visible = visible;
                ridge[r][s].visible = visible && activeMicroDetail;
                spine[r][s].visible = visible && activeMicroDetail;
                wingMembrane[r][s].visible = false;
                armorPlate[r][s].visible = false;
                seg[r][s].xScale = seg[r][s].yScale = seg[r][s].zScale = 1f;
                ridge[r][s].xScale = ridge[r][s].yScale = ridge[r][s].zScale = 1f;
                spine[r][s].xScale = spine[r][s].yScale = spine[r][s].zScale = 1f;
                seg[r][s].xRot = seg[r][s].yRot = seg[r][s].zRot = 0f;
                ridge[r][s].xRot = ridge[r][s].yRot = ridge[r][s].zRot = 0f;
                spine[r][s].xRot = spine[r][s].yRot = spine[r][s].zRot = 0f;
            }
        }

        resetShape();

        // During a grapple all appendages converge toward the actual block hit
        // point. This is a local model transform, so the anchor remains attached
        // to the player's lower back while the player rotates.
        if (grappleEase > 0.001f) {
            for (int r = 0; r < ROOTS; r++) {
                float side = r % 2 == 0 ? -1f : 1f;
                float row = r < 2 ? -1f : 1f;
                roots[r].yRot += grappleYaw * (0.82f + 0.10f * Math.abs(side));
                roots[r].xRot += grapplePitch * (0.72f + 0.08f * row);
                roots[r].xScale *= 0.90f + 0.10f * grappleLaunch;
                roots[r].yScale *= 0.90f + 0.10f * grappleLaunch;
                roots[r].zScale = 0.35f + 0.65f * grappleReach * grappleScale;
            }
        }
        for (int r = 0; r < ROOTS; r++) {
            for (int s = 0; s < activeSegments; s++) {
                boolean micro = activeMicroDetail;
                wingMembrane[r][s].visible = micro && type.isUkaku() && r < 2;
                armorPlate[r][s].visible = micro && type.isKoukaku() && r < 2 && s % 2 == 0;
                wingMembrane[r][s].xScale = 0.78f;
                wingMembrane[r][s].yScale = 0.72f;
                armorPlate[r][s].xScale = 0.84f;
                armorPlate[r][s].yScale = 0.82f;
            }
        }

        switch (type.baseType()) {
            case RINKAKU -> animateRinkaku(age, locomotion, attackEase, grappleEase, dashEase);
            case UKAKU -> animateUkaku(age, locomotion, attackEase, grappleEase, dashEase);
            case KOUKAKU -> animateKoukaku(age, locomotion, attackEase, grappleEase, dashEase);
            case BIKAKU -> animateBikaku(age, locomotion, attackEase, grappleEase, dashEase);
            default -> animateRinkaku(age, locomotion, attackEase, grappleEase, dashEase);
        }

        // High-quality universal locomotion layer.  This is deliberately applied AFTER
        // each kagune's own style animation, so every type gets the same physical
        // principles: inertia, delayed tips, counter-sway, air lift/drop and directional
        // banking.  Grapple/dash impulses are additional accents, not the base animation.
        float horizontalSpeed = (float)Math.sqrt(forward * forward + strafe * strafe);
        float totalSpeed = (float)Math.sqrt(forward * forward + strafe * strafe + vertical * vertical);
        float motion = Mth.clamp(totalSpeed * 1.65f, 0f, 1f);
        float horizontal = Mth.clamp(horizontalSpeed * 1.75f, 0f, 1f);
        float air = onGround ? 0f : 1f;
        float rise = Mth.clamp(vertical * 3.4f, -1f, 1f);
        float grappleImpulse = grappleEase * 0.9f + dashEase * 0.28f;

        // The root reacts first; the tip follows later, producing real-looking lag.
        float pitch = Mth.clamp(forward * 2.4f, -1f, 1f);
        float bank = Mth.clamp(strafe * 2.4f, -1f, 1f);
        float lift = Mth.clamp(rise, -1f, 1f);
        // Directional locomotion is calculated in the player's local frame:
        // forward/back, left/right and vertical motion each affect the appendage
        // differently. This prevents the kagune from looking like a generic
        // idle animation while the player is actually moving.
        float forwardDrive = Mth.clamp(forward * 4.0f, -1f, 1f);
        float strafeDrive = Mth.clamp(strafe * 4.0f, -1f, 1f);
        float backwardDrive = Math.max(0f, -forwardDrive);
        float forwardDrivePositive = Math.max(0f, forwardDrive);
        float idleBreath = Mth.sin(age * 0.105f) * 0.035f;
        float slowPulse = Mth.sin(age * 0.047f + 1.2f) * 0.022f;
        float gaitPhase = age * (0.48f + horizontal * 1.55f);
        float directionWave = age * (0.13f + horizontal * 0.10f) + pitch * 1.4f - bank * 0.9f;

        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            float phase = directionWave + r * 0.82f;

            roots[r].xRot += -pitch * (0.16f + 0.20f * motion) * row;
            roots[r].xRot += lift * (0.12f + 0.16f * air) * row;
            roots[r].yRot += bank * (0.20f + 0.25f * motion);
            roots[r].zRot += bank * 0.10f * side;
            roots[r].zRot += idleBreath * side + slowPulse * row;

            for (int s = 0; s < activeSegments; s++) {
                float t = (s + 1f) / SEGS;
                // Quadratic delay: base follows the body, tip follows last.
                float lag = t * t;
                float tipLag = t * t * t;
                float wave = Mth.sin(phase - s * 0.52f) * (0.018f + 0.070f * t);
                float secondary = Mth.cos(phase * 0.83f - s * 0.31f + r) * (0.012f + 0.038f * t);
                float breathing = Mth.sin(age * 0.095f + s * 0.30f + r) * 0.018f * (0.35f + t);

                seg[r][s].xRot += -pitch * (0.10f + 0.52f * lag) * row;
                seg[r][s].xRot += lift * (0.07f + 0.27f * tipLag) * row;
                seg[r][s].yRot += bank * (0.10f + 0.54f * lag);
                seg[r][s].zRot += bank * 0.07f * side * lag;
                seg[r][s].zRot += wave * side + secondary * side + breathing * side;

                // Eight-direction walk/run language: the roots stay comparatively
                // stable while the outer segments swing wider and later. Forward
                // movement folds the appendage back; backward movement lets it
                // trail forward; strafing produces a counter-swing.
                float gait = Mth.sin(gaitPhase - s * 0.24f + r * 0.65f);
                float gaitDelayed = Mth.sin(gaitPhase - s * 0.39f + r * 0.65f);
                seg[r][s].xRot += -forwardDrivePositive * (0.035f + 0.16f * lag) * row;
                seg[r][s].xRot += backwardDrive * (0.025f + 0.12f * lag) * row;
                seg[r][s].yRot += strafeDrive * (0.035f + 0.18f * lag);
                seg[r][s].zRot += gait * (0.018f + 0.070f * tipLag) * side;
                seg[r][s].yRot += gaitDelayed * (0.012f + 0.045f * tipLag) * side;

                // Vertical movement: rising makes the tips float upward, falling
                // makes them lag behind, which is especially visible while flying.
                seg[r][s].xRot += -lift * (0.035f + 0.12f * tipLag) * row;

                // Volumetric secondary motion: ridges and spines lag behind the main segment.
                // This makes the appendage read as flexible tissue in 3D, not a chain of blocks.
                ridge[r][s].xRot = seg[r][s].xRot * 1.12f + wave * 0.45f;
                ridge[r][s].yRot = seg[r][s].yRot * 0.92f - secondary * 0.35f;
                ridge[r][s].zRot = seg[r][s].zRot * 1.20f;
                ridge[r][s].yScale = 0.82f + 0.22f * (float)Math.sin(Math.PI * t);
                spine[r][s].xRot = seg[r][s].xRot * 0.78f - wave * 0.28f;
                spine[r][s].yRot = seg[r][s].yRot * 0.72f;
                spine[r][s].zRot = seg[r][s].zRot * 1.55f + side * 0.025f;
                spine[r][s].zScale = 0.72f + 0.42f * (float)Math.sin(Math.PI * t);

                // Airborne movement makes the appendage float before falling back,
                // instead of snapping between ground and air poses.
                if (air > 0f) {
                    float airWave = Mth.sin(age * 0.16f - s * 0.40f + r) * 0.035f * (0.3f + t);
                    seg[r][s].xRot += airWave + lift * 0.08f * t * row;
                }

                // Grapple/dash creates a travelling recoil wave.  Only grapple-capable
                // kagune receive grappleEase; every kagune can still receive dashEase.
                if (grappleImpulse > 0.025f) {
                    float snap = Mth.sin(t * Mth.PI) * grappleImpulse;
                    float travel = Mth.sin((1f - t) * Mth.PI + age * 0.12f) * grappleImpulse;
                    seg[r][s].xRot += -snap * 0.11f * row;
                    seg[r][s].yRot += snap * 0.16f * side;
                    seg[r][s].zRot += travel * 0.08f * side;
                }
            }
        }

        // Advanced animation pass: cinematic attack, special-ability, Rage and hit reactions.
        // This is visual-only. It never changes movement, damage, cooldowns or targeting.
        float attackWave = Mth.sin((1f - attackPulse) * Mth.PI) * attackPulse;
        float abilityWave = Mth.sin((1f - abilityPulse) * Mth.PI) * abilityPulse;
        float rageWave = Mth.sin((1f - ragePulse) * Mth.PI) * ragePulse;
        // Strong anticipation + follow-through makes an attack read as a body
        // motion rather than a simple rotation of the finished pose.
        float attackWindup = Mth.sin(attackPulse * Mth.PI * 0.82f) * attackPulse;
        float abilityBloom = Mth.sin(abilityPulse * Mth.PI * 0.72f) * abilityPulse;
        float transitionWave = Mth.sin(emergenceEase * Mth.PI);
        float hitWave = hurt * hurt * (3f - 2f * hurt);

        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            // Each root unfolds a fraction later than the previous one; the kagune
            // opens from the back instead of popping into a fully formed fan.
            float rootProgress = Mth.clamp((emergence - r * 0.075f) / (1f - r * 0.075f), 0f, 1f);
            float rootEase = rootProgress * rootProgress * (3f - 2f * rootProgress);
            float scale = 0.06f + 0.94f * rootEase;
            float emergenceOvershoot = 1f + transitionWave * 0.10f;
            roots[r].xScale = roots[r].yScale = roots[r].zScale = scale * emergenceOvershoot;
            if (crouching) roots[r].xRot += 0.05f;
            roots[r].xRot += hurt * 0.08f * side;
            roots[r].xRot += -attackWindup * 0.18f * row;
            roots[r].xRot += attackWave * (0.22f + 0.08f * row);
            roots[r].yRot += abilityBloom * 0.20f * side;
            roots[r].yRot += abilityWave * 0.16f * side;
            roots[r].zRot += rageWave * 0.10f * side;
            roots[r].zRot += hitWave * 0.16f * side;

            for (int s = 0; s < activeSegments; s++) {
                float t = (s + 1f) / SEGS;
                // Segment growth travels from the root to the tip during emergence,
                // producing a 3D unfolding motion rather than a whole-model scale pop.
                float growth = Mth.clamp((emergence - t * 0.62f) / (1f - t * 0.62f), 0f, 1f);
                float growthEase = growth * growth * (3f - 2f * growth);
                seg[r][s].xScale *= 0.08f + 0.92f * growthEase;
                seg[r][s].yScale *= 0.08f + 0.92f * growthEase;
                seg[r][s].zScale *= 0.12f + 0.88f * growthEase;
                // The impulse travels down the appendage: root first, tip last.
                float travel = Mth.clamp((attackWave - t * 0.22f) / 0.78f, 0f, 1f);
                float specialTravel = Mth.clamp((abilityWave - t * 0.16f) / 0.84f, 0f, 1f);
                float rageTravel = Mth.clamp((rageWave - t * 0.12f) / 0.88f, 0f, 1f);
                float pulse = Mth.sin(t * Mth.PI);

                seg[r][s].xRot += -attackWindup * (0.08f + 0.12f * t) * row;
                seg[r][s].xRot += -travel * (0.34f + 0.42f * t) * row;
                seg[r][s].yRot += travel * (0.24f + 0.34f * t) * side;
                seg[r][s].zRot += abilityBloom * (0.06f + 0.10f * t) * side;
                seg[r][s].zRot += specialTravel * 0.20f * side;
                seg[r][s].xRot += specialTravel * 0.22f * pulse * row;
                seg[r][s].zRot += rageTravel * 0.13f * side;
                seg[r][s].yRot += hitWave * pulse * 0.11f * side;

                // Secondary geometry intentionally lags the main segment.
                ridge[r][s].xRot += travel * 0.16f * row;
                ridge[r][s].yRot += specialTravel * 0.12f * side;
                spine[r][s].xRot -= travel * 0.11f * row;
                spine[r][s].zRot += rageTravel * 0.08f * side;

                // Rage makes the silhouette breathe/pulse without recoloring the texture.
                float rageScale = 1f + rageTravel * (0.07f + 0.06f * pulse);
                ridge[r][s].xScale *= rageScale;
                ridge[r][s].yScale *= rageScale;
            }
        }

        // A visible but smooth emergence/retraction remains attached to the back.
        // The easing is intentionally slow enough to read as tissue unfolding.
        // Character-specific finishing pass. These are animation accents only: they
        // preserve the canonical RC type silhouette while making each profile move
        // with a distinct anime-like rhythm instead of sharing one generic loop.
        applyProfileMotion(type, age, locomotion, attackEase, grappleEase, dashEase,
                horizontal, air, pitch, bank);
        // Shape pass runs after motion so the eight character profiles do not share
        // one generic silhouette. All parts remain volumetric ModelParts (3D).
        applyProfileSilhouette(type, age, emergenceEase, attackEase);
    }

    /**
     * Profile-specific volumetric shaping. The source anime defines four RC kagune
     * families; these eight playable profiles are presentation variants within those
     * families, not new canonical RC types. Dimensions are deliberately restrained
     * to keep the model attached to the back and avoid clipping through the torso.
     */
    private void applyProfileSilhouette(KaguneType type, float age, float emergence, float attack) {
        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            for (int s = 0; s < activeSegments; s++) {
                float t = (s + 1f) / SEGS;
                switch (type) {
                    case KANEKI_RINKAKU -> {
                        // Four long, supple tendrils with a clean taper and strong tip lag.
                        seg[r][s].xScale *= 0.92f - 0.18f * t;
                        seg[r][s].yScale *= 0.94f - 0.20f * t;
                        seg[r][s].zScale *= 1.025f;
                    }
                    case RIZE_RINKAKU -> {
                        // Rize's silhouette is fuller and more predatory than Kaneki's.
                        seg[r][s].xScale *= 1.16f - 0.22f * t;
                        seg[r][s].yScale *= 1.12f - 0.18f * t;
                        ridge[r][s].xScale *= 1.12f;
                        spine[r][s].zScale *= 1.14f;
                    }
                    case TAKIZAWA_UKAKU -> {
                        // Broad, angular wing-fan with a pointed outer edge.
                        seg[r][s].xScale *= 1.42f - 0.66f * t;
                        seg[r][s].yScale *= 0.70f - 0.24f * t;
                        seg[r][s].zScale *= 0.96f;
                        ridge[r][s].xScale *= 1.28f;
                    }
                    case TOUKA_UKAKU -> {
                        // Wider, softer wing spread; tips remain narrow and quick.
                        seg[r][s].xScale *= 1.52f - 0.86f * t;
                        seg[r][s].yScale *= 0.62f - 0.18f * t;
                        seg[r][s].zScale *= 0.94f;
                        ridge[r][s].xScale *= 1.18f;
                    }
                    case AYATO_UKAKU -> {
                        // Sharper, more compact wing blades with less vertical bulk.
                        seg[r][s].xScale *= 1.18f - 0.54f * t;
                        seg[r][s].yScale *= 0.84f - 0.34f * t;
                        seg[r][s].zScale *= 1.04f;
                        spine[r][s].zScale *= 1.22f;
                    }
                    case TSUKIYAMA_KOUKAKU -> {
                        // Heavy armored blade silhouette: thick root, broad middle, tapered end.
                        float blade = 0.90f + 0.40f * Mth.sin(t * Mth.PI);
                        seg[r][s].xScale *= (1.36f - 0.52f * t) * blade;
                        seg[r][s].yScale *= 1.28f - 0.20f * t;
                        seg[r][s].zScale *= 0.92f;
                        ridge[r][s].xScale *= 1.36f;
                        spine[r][s].yScale *= 1.28f;
                    }
                    case NISHIKI_BIKAKU -> {
                        // Single balanced tail, narrow enough to read as one appendage.
                        if (r == 0) {
                            seg[r][s].xScale *= 0.82f - 0.22f * t;
                            seg[r][s].yScale *= 0.88f - 0.26f * t;
                            seg[r][s].zScale *= 1.025f;
                        }
                    }
                    case NORO_BIKAKU -> {
                        // Noro's current game profile is deliberately heavier and more organic.
                        // This is a visual profile, not a claim that the anime confirms its RC class.
                        if (r == 0) {
                            float mass = 1.0f + 0.34f * (float)Math.sin(Math.PI * Math.min(1f, t * 1.25f));
                            seg[r][s].xScale *= (1.10f - 0.32f * t) * mass;
                            seg[r][s].yScale *= (1.16f - 0.34f * t) * mass;
                            ridge[r][s].xScale *= 1.22f;
                            spine[r][s].zScale *= 1.18f;
                        }
                    }
                    default -> { }
                }
                // Give the outer tips a small, continuous organic counter-sway;
                // it is deliberately geometry animation, not a 2D sprite effect.
                float tipLag = t * t;
                seg[r][s].zRot += Mth.sin(age * 0.075f - s * 0.24f + r * 0.7f)
                        * (0.004f + 0.012f * tipLag) * side * emergence;
                if (attack > 0.01f) {
                    seg[r][s].xRot -= attack * tipLag * 0.025f * row;
                }
            }
        }
    }

    /** Distinct follow-through poses for the hit-confirmed three-step combo. */
    public void applyComboPose(int comboStage, float pulse, float age) {
        float strength = Mth.clamp(pulse, 0f, 1f);
        if (strength <= 0.01f) return;
        for (int r = 0; r < ROOTS; r++) {
            float side = (r % 2 == 0) ? -1f : 1f;
            if (comboStage == 1) {
                roots[r].yRot += side * 0.12f * strength;
                roots[r].zRot += side * 0.07f * strength;
                seg[r][Math.min(8, SEGS - 1)].xRot += 0.05f * strength;
            } else if (comboStage >= 2) {
                roots[r].xRot += 0.20f * strength;
                roots[r].yRot += side * 0.16f * strength;
                roots[r].zRot += side * 0.10f * strength;
                seg[r][Math.min(16, SEGS - 1)].xRot += 0.09f * strength;
                seg[r][Math.min(24, SEGS - 1)].yRot += side * 0.06f * strength;
            } else {
                roots[r].yRot += side * 0.045f * strength;
                roots[r].xRot -= 0.035f * strength;
            }
        }
    }

    /** Overrides the normal pose with one extended, back-attached 3D tendril for the grab. */
    public void applyGrabPose(float yaw, float pitch, float reachScale, float progress, float age) {
        float p = Mth.clamp(progress, 0f, 1f);
        for (int r = 0; r < ROOTS; r++) {
            boolean active = r == 0;
            roots[r].visible = active && p > 0.001f;
            accents[r].visible = active && p > 0.12f;
            if (!active) continue;
            roots[r].x = 0f;
            roots[r].y = 0.18f;
            roots[r].z = 2.05f;
            roots[r].xRot = pitch;
            roots[r].yRot = yaw;
            roots[r].zRot = Mth.sin(age * 0.13f) * 0.025f;
            roots[r].xScale = roots[r].yScale = 0.72f * p;
            roots[r].zScale = Mth.clamp(reachScale, 0.45f, 1.75f) * p;
            for (int s = 0; s < SEGS; s++) {
                float t = (s + 1f) / SEGS;
                float growth = Mth.clamp((p - t * 0.62f) / (1f - t * 0.62f), 0f, 1f);
                float growthEase = growth * growth * (3f - 2f * growth);
                seg[r][s].visible = true;
                ridge[r][s].visible = activeMicroDetail && s % 2 == 0;
                spine[r][s].visible = activeMicroDetail && s % 3 == 0;
                wingMembrane[r][s].visible = false;
                armorPlate[r][s].visible = false;
                seg[r][s].xScale *= 0.08f + 0.92f * growthEase;
                seg[r][s].yScale *= 0.08f + 0.92f * growthEase;
                seg[r][s].zScale *= 0.12f + 0.88f * growthEase;
                seg[r][s].xRot += Mth.sin(age * 0.12f - s * 0.26f) * (0.008f + 0.025f * t) * p;
                seg[r][s].yRot += Mth.sin(age * 0.09f - s * 0.22f) * 0.018f * t * p;
                seg[r][s].xScale *= 0.88f - 0.12f * t;
                seg[r][s].yScale *= 0.90f - 0.14f * t;
            }
        }
    }

    private void applyProfileMotion(KaguneType type, float age, float move, float attack,
                                    float grapple, float dash, float horizontal, float air,
                                    float pitch, float bank) {
        float pulse = Mth.sin(age * 0.14f);
        float fastPulse = Mth.sin(age * 0.22f);
        switch (type) {
            case KANEKI_RINKAKU -> {
                // Kaneki: delayed whip-like follow-through and a stronger launch at the root.
                for (int r = 0; r < ROOTS; r++) {
                    float side = r % 2 == 0 ? -1f : 1f;
                    for (int s = 0; s < activeSegments; s++) {
                        float t = (s + 1f) / SEGS;
                        seg[r][s].yRot += side * Mth.sin(age * 0.16f - t * 3.4f) * 0.045f * t;
                        seg[r][s].xRot += -pitch * 0.025f * t + attack * 0.06f * t;
                    }
                }
            }
            case RIZE_RINKAKU -> {
                // Rize: predatory circular sweep, keeping the four roots compact near the back.
                for (int r = 0; r < ROOTS; r++) {
                    float side = r % 2 == 0 ? -1f : 1f;
                    roots[r].yRot += side * pulse * 0.055f;
                    roots[r].zRot += side * fastPulse * 0.028f;
                }
            }
            case TAKIZAWA_UKAKU -> {
                // Takizawa: aggressive wing snap and short recoil.
                for (int r = 0; r < 2; r++) {
                    float side = r == 0 ? -1f : 1f;
                    roots[r].zRot += side * fastPulse * 0.075f;
                    roots[r].xRot -= attack * 0.08f;
                }
            }
            case TOUKA_UKAKU -> {
                // Touka: broad wing beat with a softer return.
                for (int r = 0; r < 2; r++) {
                    float side = r == 0 ? -1f : 1f;
                    roots[r].zRot += side * Mth.sin(age * 0.085f) * 0.055f;
                    roots[r].yRot += side * bank * 0.045f;
                }
            }
            case AYATO_UKAKU -> {
                // Ayato: tighter, sharper oscillation and quicker attack recoil.
                for (int r = 0; r < 2; r++) {
                    float side = r == 0 ? -1f : 1f;
                    roots[r].zRot += side * fastPulse * 0.095f;
                    for (int s = 0; s < activeSegments; s++) {
                        float t = (s + 1f) / SEGS;
                        seg[r][s].xRot -= attack * t * 0.08f;
                    }
                }
            }
            case TSUKIYAMA_KOUKAKU -> {
                // Tsukiyama: dense, controlled motion with a heavier settling lag.
                for (int r = 0; r < 2; r++) {
                    float side = r == 0 ? -1f : 1f;
                    roots[r].zRot += side * pulse * 0.025f;
                    roots[r].xRot += -air * 0.025f;
                }
            }
            case NISHIKI_BIKAKU -> {
                // Nishiki: one tail with a pronounced side-to-side counter-swing.
                roots[0].yRot += Mth.sin(age * 0.11f) * 0.07f + bank * 0.05f;
                for (int s = 0; s < activeSegments; s++) {
                    float t = (s + 1f) / SEGS;
                    seg[0][s].yRot += Mth.sin(age * 0.13f - t * 2.8f) * 0.06f * t;
                }
            }
            case NORO_BIKAKU -> {
                // Noro: heavier mass, slower oscillation and stronger inertial lag.
                roots[0].xScale *= 1.10f;
                roots[0].yScale *= 1.12f;
                roots[0].zScale *= 1.08f;
                roots[0].xRot += -attack * 0.05f;
                for (int s = 0; s < activeSegments; s++) {
                    float t = (s + 1f) / SEGS;
                    seg[0][s].yRot += Mth.sin(age * 0.06f - t * 1.7f) * 0.035f * t;
                }
            }
            default -> { }
        }
    }

    private void resetShape() {
        for (int r = 0; r < ROOTS; r++) {
            roots[r].visible = true;
            roots[r].visible = true;
            roots[r].x = (r % 2 == 0 ? -3.6f : 3.6f);
            roots[r].y = -0.55f + (r < 2 ? -0.4f : 0.4f);
            roots[r].z = 1.9f + (r < 2 ? -0.65f : 0.65f);
            roots[r].xRot = roots[r].yRot = roots[r].zRot = 0f;
            roots[r].xScale = roots[r].yScale = roots[r].zScale = 1f;
            accents[r].visible = true;
            accents[r].xRot = accents[r].yRot = accents[r].zRot = 0f;
            accents[r].xScale = accents[r].yScale = accents[r].zScale = 1f;
            for (int s = 0; s < activeSegments; s++) {
                boolean visible = s < activeSegments;
                seg[r][s].visible = visible;
                ridge[r][s].visible = visible && activeMicroDetail;
                spine[r][s].visible = visible && activeMicroDetail;
                wingMembrane[r][s].visible = false;
                armorPlate[r][s].visible = false;
                seg[r][s].xScale = seg[r][s].yScale = seg[r][s].zScale = 1f;
                ridge[r][s].xScale = ridge[r][s].yScale = ridge[r][s].zScale = 1f;
                spine[r][s].xScale = spine[r][s].yScale = spine[r][s].zScale = 1f;
            }
        }
    }

    private void hideAll() { for (ModelPart r : roots) r.visible = false; }

    private void animateRinkaku(float age, float move, float attack, float grapple, float dash) {
        // Rinkaku: released from the lower back, soft/whip-like and highly flexible.
        // The anime repeatedly shows tentacles behaving like living muscles: the root
        // commits first, then the tip follows with a delayed wave.
        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            float phase = r * 1.35f;
            roots[r].yRot = side * (0.12f + 0.06f * Mth.sin(age * 0.075f + phase));
            roots[r].xRot = row * 0.08f - attack * 0.18f * row - grapple * 0.62f * row;
            roots[r].zRot = side * Mth.sin(age * 0.06f + phase) * 0.035f;
            accents[r].xRot = -0.12f + attack * 0.28f;
            for (int s = 0; s < activeSegments; s++) {
                float t = (s + 1f) / SEGS;
                float delayed = age * (0.075f + 0.045f * t);
                float wave = Mth.sin(delayed + s * 0.52f + phase) * (0.035f + 0.085f * t);
                float cross = Mth.cos(delayed * 0.77f + s * 0.31f + phase) * (0.018f + 0.055f * t);
                seg[r][s].xRot = row * (0.018f + 0.045f * t)
                        + wave * row - move * row * t * 0.055f
                        - grappleReach * row * t * 0.52f;
                seg[r][s].yRot = side * (0.055f + 0.16f * t) + cross
                        + attack * side * t * 0.34f;
                seg[r][s].zRot = side * wave * 0.45f;
            }
        }
    }

    private void animateUkaku(float age, float move, float attack, float grapple, float dash) {
        // Ukaku: upper-back/shoulder release point, wing-like silhouette and quick recoil.
        roots[2].visible = roots[3].visible = false;
        accents[2].visible = accents[3].visible = false;
        float recoil = Mth.sin(dash * Mth.PI) * dash;
        float wingBeat = Mth.sin(age * 0.10f) * 0.035f;
        for (int r = 0; r < 2; r++) {
            float side = r == 0 ? -1f : 1f;
            roots[r].xScale = 1.62f;
            roots[r].yScale = 0.58f;
            roots[r].zScale = 1.12f;
            roots[r].zRot = side * (0.28f + wingBeat + attack * 0.18f) + side * recoil * 0.24f;
            roots[r].xRot = -0.20f - attack * 0.24f;
            accents[r].zRot = side * (0.34f + attack * 0.45f);
            for (int s = 0; s < activeSegments; s++) {
                float t = (s + 1f) / SEGS;
                float featherWave = Mth.sin(age * 0.095f + s * 0.43f + r) * (0.018f + t * 0.045f);
                seg[r][s].xRot = -0.08f - t * 0.19f - attack * t * 0.42f - recoil * t * 0.32f + featherWave;
                seg[r][s].yRot = side * (0.03f + t * 0.065f) + move * side * t * 0.045f;
                seg[r][s].zRot = side * (0.035f + t * 0.075f) + featherWave * side;
                // Wing-like broadening near the shoulder and taper toward the tips.
                seg[r][s].xScale = 1.22f - 0.34f * t;
                seg[r][s].yScale = 0.72f - 0.25f * t;
            }
        }
    }

    private void animateKoukaku(float age, float move, float attack, float grapple, float dash) {
        // Koukaku: dense and heavy. It should read as a solid shield/blade rather than a soft tentacle.
        roots[2].visible = roots[3].visible = false;
        accents[2].visible = accents[3].visible = false;
        float recoil = Mth.sin(dash * Mth.PI) * dash;
        for (int r = 0; r < 2; r++) {
            float side = r == 0 ? -1f : 1f;
            roots[r].xScale = 1.34f;
            roots[r].yScale = 1.48f;
            roots[r].zScale = 1.16f;
            roots[r].xRot = -0.02f - attack * 0.12f - recoil * 0.16f;
            roots[r].yRot = side * (0.10f + 0.03f * Mth.sin(age * 0.055f + r));
            roots[r].zRot = side * 0.16f;
            accents[r].yRot = side * (0.38f + attack * 0.42f);
            for (int s = 0; s < activeSegments; s++) {
                float t = (s + 1f) / SEGS;
                float settle = Mth.sin(age * 0.055f + s * 0.22f) * 0.012f;
                seg[r][s].xScale = 1.26f - 0.30f * t;
                seg[r][s].yScale = 1.32f - 0.26f * t;
                seg[r][s].xRot = -0.02f - attack * t * 0.24f - recoil * t * 0.34f + settle;
                seg[r][s].yRot = side * (0.035f + t * 0.07f);
                seg[r][s].zRot = side * settle;
            }
        }
    }

    private void animateBikaku(float age, float move, float attack, float grapple, float dash) {
        // Bikaku: a balanced tail released around the coccyx. One strong central line,
        // with a controlled side-to-side counter-swing rather than four independent tentacles.
        roots[1].visible = roots[2].visible = roots[3].visible = false;
        accents[1].visible = accents[2].visible = accents[3].visible = false;
        roots[0].x = 0f;
        roots[0].y = 0.45f;
        roots[0].z = 2.58f;
        roots[0].xScale = 0.86f;
        roots[0].yScale = 0.90f;
        roots[0].zScale = 1.20f;
        float recoil = Mth.sin(dash * Mth.PI) * dash;
        roots[0].xRot = -0.04f - attack * 0.12f - recoil * 0.24f;
        roots[0].yRot = Mth.sin(age * 0.075f) * 0.035f;
        for (int s = 0; s < activeSegments; s++) {
            float t = (s + 1f) / SEGS;
            float sway = Mth.sin(age * 0.072f + s * 0.40f) * (0.02f + 0.075f * t);
            float counter = Mth.cos(age * 0.055f + s * 0.26f) * (0.012f + 0.035f * t);
            seg[0][s].xRot = 0.035f + t * 0.10f - attack * t * 0.30f - recoil * t * 0.52f;
            seg[0][s].yRot = sway + counter + move * t * 0.035f;
            seg[0][s].zRot = counter * 0.65f;
            seg[0][s].xScale = 0.96f - 0.28f * t;
            seg[0][s].yScale = 0.98f - 0.30f * t;
        }
    }

    private void animateKagero(float age, float move, float attack, float grapple, float dash) {
        // Hybrid three-branch silhouette with a dominant central tendril.
        roots[3].visible = accents[3].visible = false;
        roots[0].x = -2.8f; roots[1].x = 2.8f; roots[2].x = 0f; roots[2].y = 0.1f; roots[2].z = 2.25f;
        roots[2].xScale = 1.12f; roots[2].yScale = 1.12f;
        float recoil = Mth.sin(dash * (float)Math.PI) * dash;
        float surge = Mth.sin((1f - dash) * (float)Math.PI) * dash; 
        for (int r=0;r<ROOTS;r++){
            float side=r%2==0?-1f:1f; float row=r<2?-1f:1f;
            roots[r].zRot=side*(0.22f+0.05f*Mth.sin(age*0.13f+r));
            roots[r].xRot=row*0.10f-attack*0.35f*row-recoil*0.28f*row;
            accents[r].zRot=side*(0.45f+attack*0.7f);
            for(int s=0;s<SEGS;s++){
                float t=(s+1f)/SEGS;
                seg[r][s].yRot=side*(0.08f+0.10f*t)+Mth.sin(age*0.15f+s*0.7f+r)*0.05f+attack*side*t*0.32f;
                seg[r][s].zRot=side*(0.04f+0.06f*t);
                seg[r][s].xRot=row*(0.03f+0.06f*t)-move*row*t*0.06f-recoil*row*t*0.70f;
            }
        }
    }

    private void animateShooting(float age, float attack, float dash) {
        roots[0].x = 0f; roots[0].y = -0.15f; roots[0].z = 2.35f; roots[0].xScale = 0.72f; roots[0].yScale = 0.72f; roots[0].zScale = 1.25f;
        float recoil = Mth.sin(dash * (float)Math.PI) * dash;
        float surge = Mth.sin((1f - dash) * (float)Math.PI) * dash; 
        for (int r=0;r<ROOTS;r++) roots[r].visible=accents[r].visible=r==0;
        roots[0].xRot=-0.26f+0.06f*Mth.sin(age*0.12f)-attack*0.18f-recoil*0.55f;
        roots[0].yRot=0f; roots[0].zRot=0f;
        accents[0].xRot=-0.25f-attack*0.5f;
        for(int s=0;s<SEGS;s++){
            float t=(s+1f)/SEGS;
            seg[0][s].xRot=-0.03f-attack*0.55f*t-recoil*0.70f*t;
            seg[0][s].yRot=Mth.sin(age*0.10f+s*0.5f)*0.035f*t;
            seg[0][s].zRot=Mth.cos(age*0.09f+s*0.4f)*0.018f*t;
        }
    }

    private void animateLong(float age, float move, float attack, float grapple, float dash) {
        roots[0].x = 0f; roots[0].y = -0.25f; roots[0].z = 2.35f; roots[0].xScale = 0.82f; roots[0].yScale = 0.82f; roots[0].zScale = 1.35f;
        float recoil = Mth.sin(dash * (float)Math.PI) * dash;
        float surge = Mth.sin((1f - dash) * (float)Math.PI) * dash; 
        for (int r=0;r<ROOTS;r++) roots[r].visible=accents[r].visible=r==0;
        roots[0].xRot=-0.06f+0.04f*Mth.sin(age*0.08f)-attack*0.25f-grapple*0.95f-recoil*0.65f;
        roots[0].yRot=0.02f*Mth.sin(age*0.06f);
        accents[0].xRot=-0.1f-attack*0.6f-grapple*0.5f;
        for(int s=0;s<SEGS;s++){
            float t=(s+1f)/SEGS;
            seg[0][s].xRot=0.02f+attack*0.42f*t-grapple*0.80f*t-recoil*0.85f*t;
            seg[0][s].yRot=Mth.sin(age*0.075f+s*0.36f)*0.055f*t;
            seg[0][s].zRot=Mth.cos(age*0.06f+s*0.29f)*0.035f*t;
            seg[0][s].zScale=1.12f+0.35f*t;
        }
    }

    @Override public void setupAnim(Player e,float a,float b,float c,float d,float f) {}

    @Override public void renderToBuffer(PoseStack pose, VertexConsumer vc,int light,int overlay,float r,float g,float b,float a){
        for(ModelPart p:roots) if(p.visible) p.render(pose,vc,light,overlay,r,g,b,a);
    }
}
