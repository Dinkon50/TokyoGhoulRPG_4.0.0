package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tokyoghoul.rpg.item.QuinqueItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * High-detail first-person Quinque animation controller.
 * Each weapon has a distinct wind-up, impact and recovery profile.
 * The model remains the normal item model; this controller animates the whole weapon
 * in the hand without changing server-authoritative damage logic.
 */
public final class QuinqueHandAnimation {
    private record Profile(float preX,float preY,float preZ,float hitX,float hitY,float hitZ,
                           float tx,float ty,float tz,float weight,float recoil) {}

    @SubscribeEvent
    public static void renderHand(RenderHandEvent e) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null) return;
        ItemStack stack=mc.player.getItemInHand(e.getHand());
        if(!(stack.getItem() instanceof QuinqueItem)) return;
        String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        PoseStack p=e.getPoseStack();
        float swing=Mth.clamp(e.getSwingProgress(),0f,1f);
        float equip=e.getEquipProgress();
        float age=mc.player.tickCount+e.getPartialTick();
        float idle=Mth.sin(age*0.085f)*0.010f;
        boolean charged=stack.getOrCreateTag().getBoolean("TG_StrikeReady");
        Profile q=profile(id,charged);

        // Natural breathing and hand stabilization.
        p.translate(0.0D, -0.025D*equip+idle, 0.015D*Mth.cos(age*0.07f));
        p.mulPose(Axis.ZP.rotationDegrees(idle*30f));
        p.mulPose(Axis.XP.rotationDegrees(Mth.sin(age*0.06f)*0.55f));

        if(swing>0.001f) animateStrike(p,swing,q);
        else {
            // Tiny weapon-specific idle offset so every Quinque has a distinct stance.
            p.mulPose(Axis.XP.rotationDegrees(q.preX*0.06f));
            p.mulPose(Axis.YP.rotationDegrees(q.preY*0.045f));
            p.mulPose(Axis.ZP.rotationDegrees(q.preZ*0.045f));
        }
    }

    private static void animateStrike(PoseStack p,float s,Profile q){
        float pre=Mth.clamp(s/0.28f,0f,1f);
        float hit=Mth.clamp((s-0.18f)/0.44f,0f,1f);
        float rec=Mth.clamp((s-0.55f)/0.45f,0f,1f);
        pre=1f-(float)Math.pow(1f-pre,2.2f);
        hit=(float)(1.0-Math.pow(1.0-hit,3.0));
        rec=rec*rec*(3f-2f*rec);

        // Pull back before impact.
        p.mulPose(Axis.XP.rotationDegrees(q.preX*pre + q.hitX*hit*(1f-rec) + q.hitX*0.22f*rec));
        p.mulPose(Axis.YP.rotationDegrees(q.preY*pre + q.hitY*hit*(1f-rec) + q.hitY*0.12f*rec));
        p.mulPose(Axis.ZP.rotationDegrees(q.preZ*pre + q.hitZ*hit*(1f-rec) + q.hitZ*0.16f*rec));
        p.translate(q.tx*pre + q.tz*0.18f*hit,
                    q.ty*pre - q.ty*0.35f*hit,
                    q.tz*pre + q.tz*0.42f*hit);
        float squash=1f+q.weight*0.035f*hit;
        p.scale(squash,1f+q.weight*0.012f*hit,1f+q.weight*0.025f*hit);

        // Impact recoil: a very short reverse snap followed by recovery.
        float impact=(float)Math.sin(hit*Math.PI);
        p.mulPose(Axis.XP.rotationDegrees(-q.recoil*impact));
        p.translate(0.0D,0.0D,-0.025D*q.recoil*impact);
    }

    private static Profile profile(String id,boolean charged){
        return switch(id){
            case "quinque_ixa" -> new Profile(-32,-12,-8,70,12,32,0.0f,-0.06f,0.20f,1.16f,5f);
            case "quinque_narukami" -> new Profile(-12,-8,-5,22,14,-18,0.0f,0.0f,0.28f,0.94f,2.5f);
            case "quinque_scorpion" -> new Profile(-8,-22,-24,38,58,64,0.02f,-0.02f,0.05f,0.86f,2.5f);
            case "quinque_13s_jason" -> new Profile(-38,-24,-18,86,38,82,0.0f,-0.10f,0.10f,1.20f,6f);
            case "quinque_fueguchi_one" -> new Profile(-10,-34,-24,44,82,98,0.03f,-0.01f,0.10f,0.96f,4f);
            case "quinque_fueguchi_two" -> new Profile(-18,-12,-28,36,28,46,0.0f,-0.08f,0.04f,1.12f,3.5f);
            case "quinque_yukimura_1_3" -> new Profile(-10,-10,-8,30,18,42,0.0f,0.0f,0.24f,0.88f,2f);
            case "quinque_owl" -> new Profile(-42,-18,-24,96,24,74,0.0f,-0.12f,0.12f,1.26f,7f);
            default -> new Profile(-18,-18,-12,58,36,72,0,-0.05f,0.08f,1f,3f);
        };
    }
    private QuinqueHandAnimation(){}
}
