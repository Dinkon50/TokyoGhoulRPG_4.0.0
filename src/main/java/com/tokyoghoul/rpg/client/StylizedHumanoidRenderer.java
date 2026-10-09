package com.tokyoghoul.rpg.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class StylizedHumanoidRenderer<T extends Mob> extends MobRenderer<T, StylizedHumanoidModel<T>> {
    private final ResourceLocation texture;
    public StylizedHumanoidRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer, ResourceLocation texture, boolean ccg) {
        super(ctx, new StylizedHumanoidModel<>(ctx.bakeLayer(layer), ccg), 0.48f);
        this.texture=texture;
    }
    @Override public ResourceLocation getTextureLocation(T entity){
        String role = entity.getTags().stream().findFirst().orElse("");
        String file = texture.getPath();
        if (file.contains("ghoul_npc")) {
            file = switch (role) {
                case "refuge" -> "textures/entity/ghoul_refuge.png";
                case "maskmaker" -> "textures/entity/ghoul_maskmaker.png";
                case "elite" -> "textures/entity/ghoul_elite.png";
                default -> "textures/entity/ghoul_npc.png";
            };
        } else {
            file = switch (role) {
                case "medic" -> "textures/entity/ccg_medic.png";
                case "hunter" -> "textures/entity/ccg_hunter.png";
                case "elite" -> "textures/entity/ccg_elite.png";
                default -> "textures/entity/ccg_npc.png";
            };
        }
        return new ResourceLocation(texture.getNamespace(), file);
    }
}
