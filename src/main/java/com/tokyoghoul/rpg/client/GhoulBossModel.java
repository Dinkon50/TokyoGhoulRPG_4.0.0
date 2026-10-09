package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tokyoghoul.rpg.entity.GhoulBoss;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Yoshimura / Non-Killing Owl inspired boss silhouette.
 * The animation language is based on the anime's visible Owl-like wing/kagune motion:
 * restrained idle posture, broad wing preparation, forward strike and recovery.
 */
public class GhoulBossModel extends EntityModel<GhoulBoss> {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0,256).addBox(-7,-10,-4,14,20,8)
                .texOffs(0,480).addBox(-9,-4,-5,18,10,2), PartPose.offset(0,12,0));
        r.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(352,192).addBox(-8,-5,-5,16,10,3)
                .texOffs(352,296).addBox(-10,-2,-6,20,5,2), PartPose.offset(0,5,0));
        r.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0,0).addBox(-6,-7,-6,12,14,12)
                .texOffs(288,0).addBox(-3,-2,-8,6,5,3)
                .texOffs(432,0).addBox(-4,-6,-7,8,2,2), PartPose.offset(0,-10,0));
        r.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(512,96).addBox(-2.5f,-1.2f,-3.2f,5,2.4f,5), PartPose.offset(0,-10,-6.5f));
        r.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(512,0).addBox(-3,-2,-3,6,24,6).texOffs(704,0).addBox(-4,20,-4,8,8,8), PartPose.offset(-9,3,0));
        r.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(512,0).mirror().addBox(-3,-2,-3,6,24,6).texOffs(704,0).mirror().addBox(-4,20,-4,8,8,8), PartPose.offset(9,3,0));
        r.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(0,576).addBox(-4,0,-4,8,22,8), PartPose.offset(-4,32,0));
        r.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(0,576).mirror().addBox(-4,0,-4,8,22,8), PartPose.offset(4,32,0));
        r.addOrReplaceChild("spines", CubeListBuilder.create().texOffs(256,576).addBox(-12,-1,2,24,15,3).texOffs(256,736).addBox(-9,-1,5,18,9,3), PartPose.offset(0,5,0));
        r.addOrReplaceChild("wing_l", CubeListBuilder.create().texOffs(512,128).addBox(-2,-2,-1,4,30,3).texOffs(544,128).addBox(-8,3,-1,8,24,2), PartPose.offset(-8,1,2));
        r.addOrReplaceChild("wing_r", CubeListBuilder.create().texOffs(512,128).mirror().addBox(-2,-2,-1,4,30,3).texOffs(544,128).mirror().addBox(0,3,-1,8,24,2), PartPose.offset(8,1,2));
        return LayerDefinition.create(mesh, 1024, 1024);
    }

    private final ModelPart body,chest,head,beak,armL,armR,legL,legR,spines,wingL,wingR;
    public GhoulBossModel(ModelPart root){
        body=root.getChild("body"); chest=root.getChild("chest"); head=root.getChild("head"); beak=root.getChild("beak");
        armL=root.getChild("arm_l"); armR=root.getChild("arm_r"); legL=root.getChild("leg_l"); legR=root.getChild("leg_r");
        spines=root.getChild("spines"); wingL=root.getChild("wing_l"); wingR=root.getChild("wing_r");
    }

    private float ease(float t){ t=Mth.clamp(t,0f,1f); return t*t*(3f-2f*t); }

    @Override public void setupAnim(GhoulBoss e,float limbSwing,float limbSwingAmount,float age,float yaw,float pitch){
        head.yRot=yaw*Mth.DEG_TO_RAD; head.xRot=pitch*Mth.DEG_TO_RAD;
        beak.yRot=head.yRot; beak.xRot=head.xRot;
        float walk=Mth.cos(limbSwing*.55f)*.58f*limbSwingAmount;
        legL.xRot=walk; legR.xRot=-walk; armL.xRot=-walk*.45f; armR.xRot=walk*.45f;
        float breathe=Mth.sin(age*.075f)*.035f;
        body.xRot=breathe*.35f; chest.xRot=breathe;
        wingL.xRot=.10f+Mth.sin(age*.09f)*.035f; wingR.xRot=-.10f-Mth.sin(age*.09f)*.035f;
        wingL.zRot=.12f; wingR.zRot=-.12f;
        spines.zRot=Mth.sin(age*.08f)*.035f;
        int ticks=e.getAnimationTicks(); int kind=e.getAnimationKind();
        if(kind==1){
            float t=ease(1f-ticks/50f);
            float wave=t<.5f?ease(t*2f):ease((1f-t)*2f);
            wingL.zRot=.18f+1.05f*wave; wingR.zRot=-.18f-1.05f*wave;
            wingL.xRot=-.15f*wave; wingR.xRot=.15f*wave;
            body.xRot=.42f*wave; head.xRot+=.10f*wave;
        } else if(kind==2){
            float t=ease(1f-ticks/28f);
            armL.xRot=-1.0f*t; armR.xRot=-1.0f*t; body.xRot=-.24f*t;
            wingL.zRot=.55f*t; wingR.zRot=-.55f*t;
            head.xRot+=.16f*t;
        } else if(kind==4){
            float t=ease(1f-ticks/34f);
            wingL.zRot=1.20f*t; wingR.zRot=-1.20f*t;
            wingL.xRot=-.35f*t; wingR.xRot=.35f*t;
            body.xRot=-.12f*t; head.xRot+=.08f*t;
        } else if(kind==5){
            float t=ease(1f-ticks/30f);
            body.xRot=-.48f*t; head.xRot+=.25f*t;
            armL.xRot=-.72f*t; armR.xRot=-.72f*t;
            wingL.xRot=-.55f*t; wingR.xRot=.55f*t;
        } else if(kind==3){
            float shake=Mth.sin(age*2.0f)*.08f;
            body.zRot=shake; head.zRot=-shake*.65f; wingL.zRot=.16f+shake; wingR.zRot=-.16f-shake;
        }
    }
    @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,float r,float g,float b,float a){
        body.render(p,v,light,overlay,r,g,b,a); chest.render(p,v,light,overlay,r,g,b,a); head.render(p,v,light,overlay,r,g,b,a); beak.render(p,v,light,overlay,r,g,b,a);
        armL.render(p,v,light,overlay,r,g,b,a); armR.render(p,v,light,overlay,r,g,b,a); legL.render(p,v,light,overlay,r,g,b,a); legR.render(p,v,light,overlay,r,g,b,a);
        spines.render(p,v,light,overlay,r,g,b,a); wingL.render(p,v,light,overlay,r,g,b,a); wingR.render(p,v,light,overlay,r,g,b,a);
    }
}
