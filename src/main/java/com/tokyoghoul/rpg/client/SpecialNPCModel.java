package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import com.tokyoghoul.rpg.entity.SpecialNPC;

/** More detailed role-based NPC silhouettes. */
public class SpecialNPCModel extends EntityModel<SpecialNPC> {
    private final ModelPart head, body, armL, armR, legL, legR, back, weapon, hood, pauldron;
    private final ModelPart belt, satchel, chestRig, bootL, bootR, facePlate, antenna, weaponGuard;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition m = new MeshDefinition();
        PartDefinition r = m.getRoot();
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4,8,8,8), PartPose.ZERO);
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(128,128).addBox(-4,0,-2,8,12,4), PartPose.ZERO);
        r.addOrReplaceChild("armL", CubeListBuilder.create().texOffs(320,128).addBox(-2,-1,-2,4,12,4), PartPose.offset(-5,2,0));
        r.addOrReplaceChild("armR", CubeListBuilder.create().texOffs(320,128).mirror().addBox(-2,-1,-2,4,12,4), PartPose.offset(5,2,0));
        r.addOrReplaceChild("legL", CubeListBuilder.create().texOffs(0,256).addBox(-2,0,-2,4,12,4), PartPose.offset(-2,12,0));
        r.addOrReplaceChild("legR", CubeListBuilder.create().texOffs(0,256).mirror().addBox(-2,0,-2,4,12,4), PartPose.offset(2,12,0));
        r.addOrReplaceChild("back", CubeListBuilder.create().texOffs(384,0).addBox(-5,-2,1.5f,10,14,2), PartPose.ZERO);
        r.addOrReplaceChild("weapon", CubeListBuilder.create().texOffs(256,384).addBox(-.7f,-1,-1,1.4f,2,12), PartPose.offset(5,12,0));
        r.addOrReplaceChild("hood", CubeListBuilder.create().texOffs(384,128).addBox(-4.5f,-6,-4.6f,9,8,2), PartPose.ZERO);
        r.addOrReplaceChild("pauldron", CubeListBuilder.create().texOffs(384,192).addBox(-2.8f,-2,-2.8f,5.6f,3.5f,5.6f), PartPose.offset(5,2,0));
        r.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(128,384).addBox(-4.4f,8.8f,-2.5f,8.8f,1.7f,5), PartPose.ZERO);
        r.addOrReplaceChild("satchel", CubeListBuilder.create().texOffs(384,320).addBox(-2,0,-1.5f,4,6,3), PartPose.offset(-5.5f,6,1));
        r.addOrReplaceChild("chestRig", CubeListBuilder.create().texOffs(192,384).addBox(-4.2f,1,-2.45f,8.4f,1.2f,.8f), PartPose.ZERO);
        r.addOrReplaceChild("bootL", CubeListBuilder.create().texOffs(128,256).addBox(-2.2f,8.5f,-2.3f,4.4f,3.8f,4.6f), PartPose.offset(-2,12,0));
        r.addOrReplaceChild("bootR", CubeListBuilder.create().texOffs(128,256).mirror().addBox(-2.2f,8.5f,-2.3f,4.4f,3.8f,4.6f), PartPose.offset(2,12,0));
        r.addOrReplaceChild("facePlate", CubeListBuilder.create().texOffs(448,0).addBox(-4.4f,-5.2f,-4.8f,8.8f,5.8f,.5f), PartPose.ZERO);
        r.addOrReplaceChild("antenna", CubeListBuilder.create().texOffs(448,128).addBox(-.4f,-12,-.4f,.8f,4,.8f), PartPose.ZERO);
        r.addOrReplaceChild("weaponGuard", CubeListBuilder.create().texOffs(448,192).addBox(-2.5f,-1,-.6f,5,1.2f,1.2f), PartPose.offset(5,12,0));
        return LayerDefinition.create(m,512,512);
    }

    public SpecialNPCModel(ModelPart r) {
        head=r.getChild("head"); body=r.getChild("body"); armL=r.getChild("armL"); armR=r.getChild("armR");
        legL=r.getChild("legL"); legR=r.getChild("legR"); back=r.getChild("back"); weapon=r.getChild("weapon");
        hood=r.getChild("hood"); pauldron=r.getChild("pauldron"); belt=r.getChild("belt"); satchel=r.getChild("satchel");
        chestRig=r.getChild("chestRig"); bootL=r.getChild("bootL"); bootR=r.getChild("bootR"); facePlate=r.getChild("facePlate");
        antenna=r.getChild("antenna"); weaponGuard=r.getChild("weaponGuard");
    }

    @Override public void setupAnim(SpecialNPC e, float limb, float amt, float age, float yaw, float pitch) {
        head.yRot=yaw*Mth.DEG_TO_RAD; head.xRot=pitch*Mth.DEG_TO_RAD;
        float s=Mth.cos(limb*.6662f)*1.12f*amt; legL.xRot=s; legR.xRot=-s; armL.xRot=-s*.62f; armR.xRot=s*.62f;
        float b=Mth.sin(age*.09f)*.035f; body.xRot=b; weapon.yRot=0f; pauldron.yRot=0f; back.yRot=0f; satchel.xRot=0f; belt.yRot=0f;
        float attack=e.getAttackAnim(0.0F);
        if(attack>0.01F){
            float eased=Mth.sin(attack*Mth.PI);
            body.yRot=Mth.sin(attack*Mth.PI)*.08F;
            armR.xRot-=eased*1.45F; armL.xRot+=eased*.24F;
            head.xRot+=eased*.06F;
        } else {
            body.yRot=Mth.sin(age*.045F)*.01F;
        }
        String r=e.role();
        hood.visible=r.equals("ghoul_scavenger")||r.equals("ghoul_medic");
        pauldron.visible=r.equals("ccg_heavy");
        weapon.visible=r.equals("ccg_heavy")||r.equals("ccg_sniper");
        back.visible=r.equals("ccg_quartermaster")||r.equals("ghoul_medic");
        satchel.visible=r.equals("ghoul_medic")||r.equals("ccg_quartermaster");
        chestRig.visible=r.startsWith("ccg_");
        facePlate.visible=r.equals("ccg_heavy")||r.equals("ccg_sniper");
        antenna.visible=r.equals("ccg_sniper");
        weaponGuard.visible=weapon.visible;
        if(r.equals("ccg_sniper")){armR.xRot=-1.1f+Mth.sin(age*.18f)*.08f;armL.xRot=-.75f;weapon.yRot=.08f*Mth.sin(age*.12f);}
        if(r.equals("ccg_heavy")){body.xRot=.02f*Mth.sin(age*.11f);pauldron.yRot=.04f*Mth.sin(age*.13f);}
        if(r.equals("ghoul_scavenger")){body.yRot=.08f*Mth.sin(age*.12f);hood.y=Mth.sin(age*.12f)*.04f;}
        if(r.equals("ghoul_medic")){back.yRot=.15f*Mth.sin(age*.1f);satchel.xRot=.05f*Mth.sin(age*.14f);}
        if(r.equals("ccg_quartermaster")){belt.yRot=.05f*Mth.sin(age*.1f);}
        bootL.visible=true; bootR.visible=true; belt.visible=true;
    }

    @Override public void renderToBuffer(PoseStack p, VertexConsumer v, int l, int o, float r, float g, float b, float a){
        head.render(p,v,l,o,r,g,b,a); body.render(p,v,l,o,r,g,b,a); armL.render(p,v,l,o,r,g,b,a); armR.render(p,v,l,o,r,g,b,a);
        legL.render(p,v,l,o,r,g,b,a); legR.render(p,v,l,o,r,g,b,a); back.render(p,v,l,o,r,g,b,a); weapon.render(p,v,l,o,r,g,b,a);
        hood.render(p,v,l,o,r,g,b,a); pauldron.render(p,v,l,o,r,g,b,a); belt.render(p,v,l,o,r,g,b,a); satchel.render(p,v,l,o,r,g,b,a);
        chestRig.render(p,v,l,o,r,g,b,a); bootL.render(p,v,l,o,r,g,b,a); bootR.render(p,v,l,o,r,g,b,a); facePlate.render(p,v,l,o,r,g,b,a);
        antenna.render(p,v,l,o,r,g,b,a); weaponGuard.render(p,v,l,o,r,g,b,a);
    }
}
