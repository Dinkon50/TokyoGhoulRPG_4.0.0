package com.tokyoghoul.rpg.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class SimpleHumanoidRenderer<T extends Mob> extends MobRenderer<T, HumanoidModel<T>> {
    private final ResourceLocation texture;

    public SimpleHumanoidRenderer(EntityRendererProvider.Context context, ResourceLocation texture){
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.texture = texture;
    }

    @Override public ResourceLocation getTextureLocation(T entity){return texture;}
}
