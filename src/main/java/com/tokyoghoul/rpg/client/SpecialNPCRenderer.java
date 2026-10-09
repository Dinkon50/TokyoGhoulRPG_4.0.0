package com.tokyoghoul.rpg.client;
import com.tokyoghoul.rpg.entity.SpecialNPC; import net.minecraft.client.renderer.entity.*; import net.minecraft.resources.ResourceLocation;
public class SpecialNPCRenderer extends MobRenderer<SpecialNPC,SpecialNPCModel>{
 public SpecialNPCRenderer(EntityRendererProvider.Context c){super(c,new SpecialNPCModel(c.bakeLayer(ClientSetup.SPECIAL_NPC_LAYER)),.5f);}
 @Override public ResourceLocation getTextureLocation(SpecialNPC e){return new ResourceLocation("tokyoghoulrpg","textures/entity/"+e.role()+".png");}
}
