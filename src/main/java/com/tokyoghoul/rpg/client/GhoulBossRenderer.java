package com.tokyoghoul.rpg.client;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.entity.GhoulBoss;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class GhoulBossRenderer extends MobRenderer<GhoulBoss, GhoulBossModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TokyoGhoulRPG.MODID, "textures/entity/ghoul_boss.png");
    public GhoulBossRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer) {
        super(ctx, new GhoulBossModel(ctx.bakeLayer(layer)), 1.05F);
    }
    @Override public ResourceLocation getTextureLocation(GhoulBoss entity) { return TEXTURE; }
}
