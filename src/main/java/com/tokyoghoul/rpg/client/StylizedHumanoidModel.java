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
import net.minecraft.world.entity.Mob;

/**
 * Detailed Tokyo Ghoul humanoid NPC model.  The extra straps, belt, coat panels,
 * gloves, boots and insignia are deliberately separate parts so role textures
 * have real 3D silhouettes instead of looking like flat skins.
 */
public class StylizedHumanoidModel<T extends Mob> extends EntityModel<T> {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8)
                .texOffs(32, 0).addBox(-4.05f, -8.05f, -4.05f, 8.1f, 8.1f, 8.1f), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4), PartPose.ZERO);
        root.addOrReplaceChild("coat", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-5, 1, 1.8f, 10, 13, 2.5f), PartPose.ZERO);
        root.addOrReplaceChild("coat_tail_l", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-4.5f, 10, 2.0f, 4.5f, 8, 1.8f), PartPose.ZERO);
        root.addOrReplaceChild("coat_tail_r", CubeListBuilder.create()
                .texOffs(0, 48).addBox(0, 10, 2.0f, 4.5f, 8, 1.8f), PartPose.ZERO);
        root.addOrReplaceChild("belt", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-4.2f, 8.8f, -2.45f, 8.4f, 1.6f, 4.9f), PartPose.ZERO);
        root.addOrReplaceChild("chest_strap", CubeListBuilder.create()
                .texOffs(24, 48).addBox(-0.8f, 0.8f, -2.35f, 1.6f, 8.4f, 0.9f), PartPose.ZERO);
        root.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-2, -1, -2, 4, 12, 4), PartPose.offset(-5, 2, 0));
        PartDefinition armRDef = root.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(40, 16).mirror().addBox(-2, -1, -2, 4, 12, 4), PartPose.offset(5, 2, 0));
        armRDef.addOrReplaceChild("weapon", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-0.6f, -1, -1.0f, 1.2f, 1.8f, 11), PartPose.offset(0, 10, -0.2f));
        root.addOrReplaceChild("glove_l", CubeListBuilder.create()
                .texOffs(40, 32).addBox(-2.1f, 8.3f, -2.1f, 4.2f, 3.8f, 4.2f), PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("glove_r", CubeListBuilder.create()
                .texOffs(40, 32).mirror().addBox(-2.1f, 8.3f, -2.1f, 4.2f, 3.8f, 4.2f), PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(-2, 12, 0));
        root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(0, 32).mirror().addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(2, 12, 0));
        root.addOrReplaceChild("boot_l", CubeListBuilder.create()
                .texOffs(16, 32).addBox(-2.15f, 8.5f, -2.3f, 4.3f, 3.8f, 4.6f), PartPose.offset(-2, 12, 0));
        root.addOrReplaceChild("boot_r", CubeListBuilder.create()
                .texOffs(16, 32).mirror().addBox(-2.15f, 8.5f, -2.3f, 4.3f, 3.8f, 4.6f), PartPose.offset(2, 12, 0));
        root.addOrReplaceChild("mask", CubeListBuilder.create()
                .texOffs(48, 0).addBox(-4.5f, -5, -4.7f, 9, 7, 1.4f), PartPose.ZERO);
        root.addOrReplaceChild("collar", CubeListBuilder.create()
                .texOffs(32, 32).addBox(-4.5f, -1.5f, -2.5f, 9, 2.5f, 5), PartPose.ZERO);
        root.addOrReplaceChild("shoulder_l", CubeListBuilder.create()
                .texOffs(48, 24).addBox(-2.5f, -1.5f, -2.8f, 5, 3, 5.5f), PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("shoulder_r", CubeListBuilder.create()
                .texOffs(48, 24).mirror().addBox(-2.5f, -1.5f, -2.8f, 5, 3, 5.5f), PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("insignia", CubeListBuilder.create()
                .texOffs(56, 32).addBox(-1.5f, 1.5f, -2.55f, 3, 3, 0.35f), PartPose.ZERO);
        root.addOrReplaceChild("pouch_l", CubeListBuilder.create()
                .texOffs(48, 40).addBox(-1.8f, -0.5f, -2.8f, 3.6f, 4, 2), PartPose.offset(-4.2f, 9, 0));
        root.addOrReplaceChild("pouch_r", CubeListBuilder.create()
                .texOffs(48, 40).addBox(-1.8f, -0.5f, -2.8f, 3.6f, 4, 2), PartPose.offset(4.2f, 9, 0));
        return LayerDefinition.create(mesh, 512, 512);
    }

    private final ModelPart head, body, coat, coatTailL, coatTailR, belt, chestStrap;
    private final ModelPart armL, armR, weapon, gloveL, gloveR, legL, legR, bootL, bootR;
    private final ModelPart mask, collar, shoulderL, shoulderR, insignia, pouchL, pouchR;
    private final boolean ccg;

    public StylizedHumanoidModel(ModelPart root, boolean ccg) {
        this.head = root.getChild("head"); this.body = root.getChild("body"); this.coat = root.getChild("coat");
        this.coatTailL = root.getChild("coat_tail_l"); this.coatTailR = root.getChild("coat_tail_r");
        this.belt = root.getChild("belt"); this.chestStrap = root.getChild("chest_strap");
        this.armL = root.getChild("arm_l"); this.armR = root.getChild("arm_r"); this.weapon = this.armR.getChild("weapon");
        this.gloveL = root.getChild("glove_l"); this.gloveR = root.getChild("glove_r");
        this.legL = root.getChild("leg_l"); this.legR = root.getChild("leg_r");
        this.bootL = root.getChild("boot_l"); this.bootR = root.getChild("boot_r");
        this.mask = root.getChild("mask"); this.collar = root.getChild("collar");
        this.shoulderL = root.getChild("shoulder_l"); this.shoulderR = root.getChild("shoulder_r");
        this.insignia = root.getChild("insignia"); this.pouchL = root.getChild("pouch_l"); this.pouchR = root.getChild("pouch_r");
        this.ccg = ccg;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        float swing = Mth.cos(limbSwing * 0.6662f) * 1.20f * limbSwingAmount;
        legL.xRot = swing; legR.xRot = -swing;
        armL.xRot = -swing * 0.68f; armR.xRot = swing * 0.68f;
        float breathe = Mth.sin(ageInTicks * 0.085f) * 0.035f;
        head.y = breathe;
        body.xRot = breathe * 0.35f;
        // Short, eased attack motion keeps NPCs from looking like rigid Minecraft dummies.
        float attack = entity.getAttackAnim(0.0F);
        if (attack > 0.01F) {
            float eased = Mth.sin(attack * Mth.PI);
            body.yRot = Mth.sin(attack * Mth.PI) * 0.10F;
            armR.xRot -= eased * 1.55F;
            armL.xRot += eased * 0.30F;
            head.xRot += eased * 0.08F;
        } else {
            body.yRot = Mth.sin(ageInTicks * 0.045F) * 0.012F;
        }
        coat.xRot = 0.06f * Mth.sin(ageInTicks * 0.12f) + 0.12f * limbSwingAmount;
        coatTailL.xRot = -0.06f * limbSwingAmount + 0.04f * Mth.sin(ageInTicks * 0.10f);
        coatTailR.xRot = 0.06f * limbSwingAmount - 0.04f * Mth.sin(ageInTicks * 0.10f);
        belt.yRot = 0.015f * Mth.sin(ageInTicks * 0.07f);
        mask.visible = !ccg;
        shoulderL.visible = ccg; shoulderR.visible = ccg;
        weapon.visible = ccg && !entity.getTags().contains("medic");
        insignia.visible = ccg;
        pouchL.visible = ccg; pouchR.visible = ccg;
        if (ccg && entity.getTags().contains("hunter")) {
            shoulderL.zRot = Mth.sin(ageInTicks * 0.10F) * 0.025F;
            shoulderR.zRot = -Mth.sin(ageInTicks * 0.10F) * 0.025F;
        } else {
            shoulderL.zRot = 0F; shoulderR.zRot = 0F;
        }
        if (entity.getTags().contains("elite")) {
            shoulderL.visible = true; shoulderR.visible = true;
            collar.xRot = 0.03f * Mth.sin(ageInTicks * 0.16f);
        }
        collar.visible = true;
        chestStrap.visible = ccg;
        gloveL.visible = true; gloveR.visible = true;
        bootL.visible = true; bootR.visible = true;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        head.render(pose, vc, light, overlay, r, g, b, a); body.render(pose, vc, light, overlay, r, g, b, a);
        coat.render(pose, vc, light, overlay, r, g, b, a); coatTailL.render(pose, vc, light, overlay, r, g, b, a);
        coatTailR.render(pose, vc, light, overlay, r, g, b, a); belt.render(pose, vc, light, overlay, r, g, b, a);
        chestStrap.render(pose, vc, light, overlay, r, g, b, a); armL.render(pose, vc, light, overlay, r, g, b, a);
        armR.render(pose, vc, light, overlay, r, g, b, a); gloveL.render(pose, vc, light, overlay, r, g, b, a);
        gloveR.render(pose, vc, light, overlay, r, g, b, a); legL.render(pose, vc, light, overlay, r, g, b, a);
        legR.render(pose, vc, light, overlay, r, g, b, a); bootL.render(pose, vc, light, overlay, r, g, b, a);
        bootR.render(pose, vc, light, overlay, r, g, b, a); mask.render(pose, vc, light, overlay, r, g, b, a);
        collar.render(pose, vc, light, overlay, r, g, b, a); shoulderL.render(pose, vc, light, overlay, r, g, b, a);
        shoulderR.render(pose, vc, light, overlay, r, g, b, a); insignia.render(pose, vc, light, overlay, r, g, b, a);
        pouchL.render(pose, vc, light, overlay, r, g, b, a); pouchR.render(pose, vc, light, overlay, r, g, b, a);
    }
}
