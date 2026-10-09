package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.entity.GhoulBoss;
import com.tokyoghoul.rpg.entity.GhoulNPC;
import com.tokyoghoul.rpg.entity.SpecialNPC;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Gives ghoul NPCs the same procedural KaguneModel used by the player renderer.
 * This intentionally reuses the player-quality geometry/animation instead of
 * maintaining a second, lower-quality NPC kagune implementation.
 */
public final class NPCKaguneRenderer {
    // Same lower-back attachment as the player renderer: never an item/armor slot.
    private static final double KAGUNE_BACK_ANCHOR_Y = 1.34D;
    private static final double KAGUNE_BACK_ANCHOR_Z = 0.34D;
    private static KaguneModel MODEL;
    private static final ResourceLocation GENERIC_RINKAKU = new ResourceLocation(TokyoGhoulRPG.MODID, "textures/entity/kagune_generic_rinkaku.png");
    private static final java.util.EnumMap<KaguneType, ResourceLocation> TEXTURES = new java.util.EnumMap<>(KaguneType.class);
    static {
        TEXTURES.put(KaguneType.KANEKI_RINKAKU, new ResourceLocation(TokyoGhoulRPG.MODID, "textures/entity/kagune_kaneki_rinkaku.png"));
        TEXTURES.put(KaguneType.RIZE_RINKAKU, new ResourceLocation(TokyoGhoulRPG.MODID, "textures/entity/kagune_rize_rinkaku.png"));
        TEXTURES.put(KaguneType.TAKIZAWA_UKAKU, new ResourceLocation(TokyoGhoulRPG.MODID, "textures/entity/kagune_takizawa_ukaku.png"));
        TEXTURES.put(KaguneType.TOUKA_UKAKU, new ResourceLocation(TokyoGhoulRPG.MODID, "textures/entity/kagune_touka_ukaku.png"));
        TEXTURES.put(KaguneType.AYATO_UKAKU, new ResourceLocation(TokyoGhoulRPG.MODID, "textures/entity/kagune_ayato_ukaku.png"));
        TEXTURES.put(KaguneType.TSUKIYAMA_KOUKAKU, new ResourceLocation(TokyoGhoulRPG.MODID, "textures/entity/kagune_tsukiyama_koukaku.png"));
        TEXTURES.put(KaguneType.NISHIKI_BIKAKU, new ResourceLocation(TokyoGhoulRPG.MODID, "textures/entity/kagune_nishiki_bikaku.png"));
        TEXTURES.put(KaguneType.NORO_BIKAKU, new ResourceLocation(TokyoGhoulRPG.MODID, "textures/entity/kagune_noro_special.png"));
    }

    @SubscribeEvent
    public static void render(RenderLivingEvent.Post<?, ?> event) {
        Object raw = event.getEntity();
        if (!(raw instanceof GhoulNPC) && !(raw instanceof GhoulBoss) && !(raw instanceof SpecialNPC special && isGhoulRole(special.role()))) return;

        if (MODEL == null) {
            MODEL = new KaguneModel(Minecraft.getInstance().getEntityModels().bakeLayer(ClientSetup.KAGUNE_LAYER));
        }

        var entity = event.getEntity();
        PoseStack pose = event.getPoseStack();
        float partial = event.getPartialTick();
        float age = entity.tickCount + partial;
        float limb = event.getEntity().walkAnimation.position(partial);
        float amount = event.getEntity().walkAnimation.speed(partial);
        float attack = event.getEntity().getAttackAnim(partial);
        float hurt = entity.hurtTime > 0 ? entity.hurtTime / 10f : 0f;
        float move = Mth.clamp(amount * 4.5f, 0f, 1.8f);
        float forward = 0f;
        float strafe = 0f;
        float vertical = (float)entity.getDeltaMovement().y;
        KaguneType type = typeFor(entity);

        pose.pushPose();
        // Same lower-back anchor used by the player Kagune renderer.
        pose.translate(0.0D, KAGUNE_BACK_ANCHOR_Y, KAGUNE_BACK_ANCHOR_Z);

        double distSq = entity.distanceToSqr(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        int detail = distSq <= 20.0 * 20.0 ? 30 : distSq <= 40.0 * 40.0 ? 20 : 12;
        boolean microDetail = distSq <= 40.0 * 40.0;
        MODEL.animate(
                type, age, move, attack, hurt,
                entity.onGround(), entity.isCrouching(), 1.0f,
                0f, entity.getDeltaMovement().horizontalDistanceSqr() > 0.35 ? 1f : 0f,
                forward, strafe, vertical,
                attack, 0f, 0f,
                0f, 0f, 1f, detail, microDetail
        );

        MultiBufferSource buffers = event.getMultiBufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture(type, entity)));
        MODEL.renderToBuffer(pose, consumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 0.98f);
        pose.popPose();
    }

    private static boolean isGhoulRole(String role) {
        return role != null && role.startsWith("ghoul_");
    }

    private static KaguneType typeFor(net.minecraft.world.entity.LivingEntity entity) {
        StringBuilder identity = new StringBuilder();
        for (String tag : entity.getTags()) identity.append(tag.toLowerCase(java.util.Locale.ROOT)).append(' ');
        identity.append(entity.getName().getString().toLowerCase(java.util.Locale.ROOT)).append(' ');
        if (entity instanceof SpecialNPC special) identity.append(special.role().toLowerCase(java.util.Locale.ROOT));
        String key = identity.toString();
        if (key.contains("kaneki")) return KaguneType.KANEKI_RINKAKU;
        if (key.contains("rize")) return KaguneType.RIZE_RINKAKU;
        if (key.contains("takizawa")) return KaguneType.TAKIZAWA_UKAKU;
        if (key.contains("touka")) return KaguneType.TOUKA_UKAKU;
        if (key.contains("ayato")) return KaguneType.AYATO_UKAKU;
        if (key.contains("tsukiyama")) return KaguneType.TSUKIYAMA_KOUKAKU;
        if (key.contains("nishiki")) return KaguneType.NISHIKI_BIKAKU;
        if (key.contains("noro")) return KaguneType.NORO_BIKAKU;
        // Generic NPCs can declare a confirmed family without pretending to be a named character.
        if (key.contains("ukaku")) return KaguneType.TOUKA_UKAKU;
        if (key.contains("koukaku")) return KaguneType.TSUKIYAMA_KOUKAKU;
        if (key.contains("bikaku")) return KaguneType.NISHIKI_BIKAKU;
        if (key.contains("rinkaku")) return KaguneType.RIZE_RINKAKU;
        if (entity instanceof GhoulBoss) return KaguneType.KANEKI_RINKAKU; // unclassified boss fallback; do not assign a character's canon type.
        // Unnamed standard ghoul NPCs use one of the four confirmed RC families for variety.
        return switch (Math.floorMod(entity.getUUID().hashCode(), 4)) {
            case 1 -> KaguneType.TOUKA_UKAKU;
            case 2 -> KaguneType.TSUKIYAMA_KOUKAKU;
            case 3 -> KaguneType.NISHIKI_BIKAKU;
            default -> KaguneType.RIZE_RINKAKU;
        };
    }

    private static boolean hasExplicitProfile(net.minecraft.world.entity.LivingEntity entity) {
        StringBuilder identity = new StringBuilder();
        for (String tag : entity.getTags()) identity.append(tag.toLowerCase(java.util.Locale.ROOT)).append(' ');
        identity.append(entity.getName().getString().toLowerCase(java.util.Locale.ROOT)).append(' ');
        if (entity instanceof SpecialNPC special) identity.append(special.role().toLowerCase(java.util.Locale.ROOT));
        String key = identity.toString();
        return key.contains("kaneki") || key.contains("rize") || key.contains("takizawa") || key.contains("touka")
                || key.contains("ayato") || key.contains("tsukiyama") || key.contains("nishiki") || key.contains("noro");
    }

    private static ResourceLocation texture(KaguneType type, net.minecraft.world.entity.LivingEntity entity) {
        return hasExplicitProfile(entity) ? TEXTURES.getOrDefault(type, GENERIC_RINKAKU) : GENERIC_RINKAKU;
    }

    private NPCKaguneRenderer() {}
}
