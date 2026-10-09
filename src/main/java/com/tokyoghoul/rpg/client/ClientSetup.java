package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.entity.ModEntities;
import com.tokyoghoul.rpg.network.AbilityPacket;
import com.tokyoghoul.rpg.network.GrapplePacket;
import com.tokyoghoul.rpg.v2.KaguneType;
import com.tokyoghoul.rpg.network.KaguneAttackPacket;
import com.tokyoghoul.rpg.network.KaguneGrabPacket;
import com.tokyoghoul.rpg.network.KagunePacket;
import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.network.RagePacket;
import com.tokyoghoul.rpg.network.QuinqueAbilityPacket;
import com.tokyoghoul.rpg.network.BitePacket;
import com.tokyoghoul.rpg.screen.ProgressionScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import com.tokyoghoul.rpg.entity.GhoulNPC;
import com.tokyoghoul.rpg.entity.CCGNPC;
import com.tokyoghoul.rpg.entity.SpecialNPC;
import com.tokyoghoul.rpg.entity.GhoulBoss;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class ClientSetup {
    public static KeyMapping KAGUNE, PROGRESSION, RAGE, ABILITY_ONE, ABILITY_TWO, ABILITY_THREE, SPECIAL_ABILITY;
    private static int dashEffectTicks = 0;
    public static final ModelLayerLocation GHOUL_LAYER=new ModelLayerLocation(new ResourceLocation(TokyoGhoulRPG.MODID,"ghoul"),"main");
    public static final ModelLayerLocation CCG_LAYER=new ModelLayerLocation(new ResourceLocation(TokyoGhoulRPG.MODID,"ccg"),"main");
    public static final ModelLayerLocation KAGUNE_LAYER=new ModelLayerLocation(new ResourceLocation(TokyoGhoulRPG.MODID,"kagune"),"main");
    public static final ModelLayerLocation GHOUL_BOSS_LAYER=new ModelLayerLocation(new ResourceLocation(TokyoGhoulRPG.MODID,"ghoul_boss"),"main");
    public static final ModelLayerLocation SPECIAL_NPC_LAYER=new ModelLayerLocation(new ResourceLocation(TokyoGhoulRPG.MODID,"special_npc"),"main");
    private static final RandomSource HALF_GHOUL_SOUND_RANDOM = RandomSource.create();
    private static int halfGhoulSoundTicks = 0;
    private static GhoulData.Race lastClientRace = GhoulData.Race.HUMAN;
    private static float jawAnimation = 0f;
    private static float jawPrevious = 0f;
    private static boolean jawClosing = false;
    private static int jawReopenDelay = 0;
    private static boolean hudMetersInitialized = false;
    private static float hudHealth = 1f, hudHunger = 1f, hudRage = 0f, hudLevel = 0.03f;
    private static float grappleReticle = 0f;
    private static float grappleReticlePrevious = 0f;
    private static boolean grappleReticleSnap = false;

    public static void init(){
        var modBus=net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ClientSetup::renderers);
        modBus.addListener(ClientSetup::layers);
        modBus.addListener(ClientSetup::keys);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ClientSetup.class);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(PlayerKaguneRenderer.class); net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(NPCKaguneRenderer.class);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(QuinqueHandAnimation.class);
    }

    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){
        e.registerLayerDefinition(GHOUL_LAYER,StylizedHumanoidModel::createBodyLayer);
        e.registerLayerDefinition(CCG_LAYER,StylizedHumanoidModel::createBodyLayer);
        e.registerLayerDefinition(KAGUNE_LAYER,KaguneModel::createLayer);
        e.registerLayerDefinition(GHOUL_BOSS_LAYER,GhoulBossModel::createBodyLayer);
        e.registerLayerDefinition(SPECIAL_NPC_LAYER,SpecialNPCModel::createBodyLayer);
    }

    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){
        e.registerEntityRenderer(ModEntities.GHOUL_NPC.get(),ctx -> new StylizedHumanoidRenderer<>(ctx,GHOUL_LAYER,new ResourceLocation(TokyoGhoulRPG.MODID,"textures/entity/ghoul_npc.png"),false));
        e.registerEntityRenderer(ModEntities.CCG_NPC.get(),ctx -> new StylizedHumanoidRenderer<>(ctx,CCG_LAYER,new ResourceLocation(TokyoGhoulRPG.MODID,"textures/entity/ccg_npc.png"),true));
        e.registerEntityRenderer(ModEntities.GHOUL_BOSS.get(),ctx -> new GhoulBossRenderer(ctx,GHOUL_BOSS_LAYER));
        e.registerEntityRenderer(ModEntities.CCG_QUARTERMASTER.get(),SpecialNPCRenderer::new); e.registerEntityRenderer(ModEntities.CCG_HEAVY.get(),SpecialNPCRenderer::new); e.registerEntityRenderer(ModEntities.CCG_SNIPER.get(),SpecialNPCRenderer::new); e.registerEntityRenderer(ModEntities.GHOUL_SCAVENGER.get(),SpecialNPCRenderer::new); e.registerEntityRenderer(ModEntities.GHOUL_MEDIC.get(),SpecialNPCRenderer::new);
    }

    @SubscribeEvent public static void kaguneAttack(InputEvent.InteractionKeyMappingTriggered e){
        if(Minecraft.getInstance().player==null || !ClientGhoulData.kaguneActive()) return;
        // Shift + RMB is reserved for the long-range kagune grapple.
        if(e.isUseItem() && Minecraft.getInstance().options.keyShift.isDown()){
            KaguneType type = currentKaguneType();
            if(type == KaguneType.KANEKI_RINKAKU){
                double maxRange = 15.0D;
                NetworkHandler.CHANNEL.sendToServer(new GrapplePacket());
                var p = Minecraft.getInstance().player;
                if (p != null) {
                    Vec3 target = grappleTarget(p, maxRange);
                    if (target != null) grappleReticleSnap = true;
                    PlayerKaguneRenderer.triggerGrapple(target);
                }
                if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.playSound(TokyoGhoulRPG.GRAPPLE_WHOOSH.get(), 0.72f, 0.88f + Minecraft.getInstance().player.getRandom().nextFloat()*0.10f);
                e.setCanceled(true);
                e.setSwingHand(false);
                return;
            }
            // Shooting and other short single-projection types cannot grapple.
            e.setCanceled(true);
            return;
        }
        if(e.isAttack() && Minecraft.getInstance().options.keyShift.isDown()) {
            NetworkHandler.CHANNEL.sendToServer(new KaguneGrabPacket());
            e.setCanceled(true);
            e.setSwingHand(false);
            return;
        }
        if(e.isAttack()){PlayerKaguneRenderer.triggerAttack(ClientGhoulData.comboStage());NetworkHandler.CHANNEL.sendToServer(new KaguneAttackPacket(0));e.setCanceled(true);e.setSwingHand(true);}
        else if(e.isUseItem()){PlayerKaguneRenderer.triggerAttack(2);NetworkHandler.CHANNEL.sendToServer(new KaguneAttackPacket(1));e.setCanceled(true);e.setSwingHand(true);}
    }

    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){
        KAGUNE=new KeyMapping("key.tokyoghoulrpg.kagune",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G,"key.categories.tokyoghoulrpg");
        PROGRESSION=new KeyMapping("key.tokyoghoulrpg.progression",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_K,"key.categories.tokyoghoulrpg");
        RAGE=new KeyMapping("key.tokyoghoulrpg.rage",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_U,"key.categories.tokyoghoulrpg");
        ABILITY_ONE=new KeyMapping("key.tokyoghoulrpg.ability_one",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_Q,"key.categories.tokyoghoulrpg");
        ABILITY_TWO=new KeyMapping("key.tokyoghoulrpg.ability_two",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_E,"key.categories.tokyoghoulrpg");
        ABILITY_THREE=new KeyMapping("key.tokyoghoulrpg.ability_three",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_F,"key.categories.tokyoghoulrpg");
        SPECIAL_ABILITY=new KeyMapping("key.tokyoghoulrpg.special_ability",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V,"key.categories.tokyoghoulrpg");
        e.register(KAGUNE);e.register(PROGRESSION);e.register(RAGE);e.register(ABILITY_ONE);e.register(ABILITY_TWO);e.register(ABILITY_THREE);e.register(SPECIAL_ABILITY);
    }

    @SubscribeEvent public static void input(InputEvent.Key e){
        if(e.getAction()!=GLFW.GLFW_PRESS)return;
        if(KAGUNE!=null&&KAGUNE.matches(e.getKey(),e.getScanCode())) { PlayerKaguneRenderer.triggerTransition(); NetworkHandler.CHANNEL.sendToServer(new KagunePacket()); }
        if(PROGRESSION!=null&&PROGRESSION.matches(e.getKey(),e.getScanCode())) Minecraft.getInstance().setScreen(new ProgressionScreen());
        if(RAGE!=null&&RAGE.matches(e.getKey(),e.getScanCode())) { PlayerKaguneRenderer.triggerRage(); NetworkHandler.CHANNEL.sendToServer(new RagePacket()); }
        if(ABILITY_ONE!=null&&ABILITY_ONE.matches(e.getKey(),e.getScanCode())) { PlayerKaguneRenderer.triggerAbility(); NetworkHandler.CHANNEL.sendToServer(new AbilityPacket(0)); }
        // E remains a configurable ability key; grapple is deliberately Shift + RMB now.
        if(ABILITY_TWO!=null&&ABILITY_TWO.matches(e.getKey(),e.getScanCode())) {
            dashEffectTicks = 12;
            PlayerKaguneRenderer.triggerDash();
            PlayerKaguneRenderer.triggerAbility();
            if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.playSound(TokyoGhoulRPG.DASH_WHOOSH.get(), 0.58f, 0.92f + Minecraft.getInstance().player.getRandom().nextFloat()*0.12f);
            if (Minecraft.getInstance().player != null) {
                var input = Minecraft.getInstance().player.input;
                NetworkHandler.CHANNEL.sendToServer(new AbilityPacket(1, input.forwardImpulse, input.leftImpulse));
            }
        }
        if(ABILITY_THREE!=null&&ABILITY_THREE.matches(e.getKey(),e.getScanCode())) { PlayerKaguneRenderer.triggerAbility(); NetworkHandler.CHANNEL.sendToServer(new AbilityPacket(2)); }
        if(SPECIAL_ABILITY!=null&&SPECIAL_ABILITY.matches(e.getKey(),e.getScanCode())) {
            GhoulData.Race race = ClientGhoulData.race();
            if(race==GhoulData.Race.GHOUL || race==GhoulData.Race.HALF_GHOUL) {
                if (hasJawTarget(Minecraft.getInstance())) {
                    jawClosing = true;
                    jawAnimation = 1f;
                    if(Minecraft.getInstance().player != null) Minecraft.getInstance().player.playSound(TokyoGhoulRPG.JAW_SNAP.get(), 0.55f, 0.94f + Minecraft.getInstance().player.getRandom().nextFloat()*0.10f);
                }
                NetworkHandler.CHANNEL.sendToServer(new BitePacket());
            } else if(race==GhoulData.Race.CCG) {
                // CCG always keeps the normal Minecraft crosshair.
                NetworkHandler.CHANNEL.sendToServer(new QuinqueAbilityPacket());
            }
        }
    }

    @SubscribeEvent public static void mouse(InputEvent.MouseButton e){
        if(e.getAction()!=GLFW.GLFW_PRESS || e.getButton()!=GLFW.GLFW_MOUSE_BUTTON_LEFT || Minecraft.getInstance().screen!=null) return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null)return;
        double sx=mc.getWindow().getGuiScaledWidth()/(double)mc.getWindow().getScreenWidth();
        double sy=mc.getWindow().getGuiScaledHeight()/(double)mc.getWindow().getScreenHeight();
        int x=(int)(mc.mouseHandler.xpos()*sx), y=(int)(mc.mouseHandler.ypos()*sy);
        int w=96,h=24,gx=mc.getWindow().getGuiScaledWidth()-w-10,gy=mc.getWindow().getGuiScaledHeight()-92;
        if(x>=gx&&x<=gx+w&&y>=gy&&y<=gy+h && (ClientGhoulData.race()==GhoulData.Race.GHOUL||ClientGhoulData.race()==GhoulData.Race.HALF_GHOUL))
            NetworkHandler.CHANNEL.sendToServer(new KagunePacket());
    }

    private static Vec3 grappleTarget(Player player, double range) {
        Vec3 from = player.getEyePosition(1f);
        Vec3 look = player.getViewVector(1f);
        Vec3 to = from.add(look.scale(range));
        HitResult block = player.level().clip(new net.minecraft.world.level.ClipContext(from, to, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        double blockDist = block.getType() == HitResult.Type.BLOCK ? from.distanceTo(block.getLocation()) : Double.MAX_VALUE;
        AABB search = player.getBoundingBox().expandTowards(to.subtract(from)).inflate(0.55D);
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(player, from, to, search, x -> x != player && x.isAlive() && !x.isSpectator(), 9.0D);
        double entityDist = entity != null ? from.distanceTo(entity.getLocation()) : Double.MAX_VALUE;
        if (blockDist == Double.MAX_VALUE && entityDist == Double.MAX_VALUE) return null;
        if (entityDist < blockDist) return entity.getEntity().getBoundingBox().getCenter();
        if (block instanceof BlockHitResult bh && bh.getDirection().getAxis().isVertical()) return bh.getLocation();
        return null;
    }

    private static KaguneType currentKaguneType(){
        int i=ClientGhoulData.kaguneType(); KaguneType[] v=KaguneType.values();
        return i>=0&&i<v.length?v[i]:KaguneType.KANEKI_RINKAKU;
    }

    @SubscribeEvent
    public static void hideVanillaMeters(RenderGuiOverlayEvent.Pre e) {
        if (e.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type()
            || e.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()
            || e.getOverlay() == VanillaGuiOverlay.ARMOR_LEVEL.type()) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void jawCrosshair(RenderGuiOverlayEvent.Pre e) {
        if (e.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        GhoulData.Race race = ClientGhoulData.race();
        if (race != GhoulData.Race.GHOUL && race != GhoulData.Race.HALF_GHOUL) return;

        // The jaw belongs ONLY to the V bite ability.
        // Kagune Leap keeps the normal Minecraft crosshair.

        if (!hasJawTarget(mc)) return;
        e.setCanceled(true);
        GuiGraphics g = e.getGuiGraphics();
        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;
        float partial = e.getPartialTick();
        float open = jawPrevious + (jawAnimation - jawPrevious) * partial;
        float angle = 12f * open;
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(0.72f, 0.72f, 1f);
        g.pose().pushPose();
        g.pose().translate(-32, -1, 0);
        g.pose().translate(32, 2, 0);
        g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-angle));
        g.pose().translate(-32, -2, 0);
        g.blit(new ResourceLocation(TokyoGhoulRPG.MODID, "textures/gui/jaw_upper.png"), -32, -27, 64, 32, 0, 0, 64, 32, 64, 32);
        g.pose().popPose();
        g.pose().pushPose();
        g.pose().translate(32, 2, 0);
        g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        g.pose().translate(-32, -2, 0);
        g.blit(new ResourceLocation(TokyoGhoulRPG.MODID, "textures/gui/jaw_lower.png"), -32, -5, 64, 32, 0, 0, 64, 32, 64, 32);
        g.pose().popPose();
        g.pose().popPose();
    }

    private static boolean hasJawTarget(Minecraft mc) {
        if (mc.player == null || mc.level == null) return false;
        // Match the server bite check: proximity to a humanoid target within 3 blocks.
        AABB search = mc.player.getBoundingBox().inflate(3.0D);
        for (LivingEntity living : mc.level.getEntitiesOfClass(LivingEntity.class, search,
                e -> isHumanoidTarget(e, mc.player) && mc.player.hasLineOfSight(e))) {
            if (mc.player.distanceToSqr(living) <= 9.0D) return true;
        }
        return false;
    }

    private static boolean isHumanoidTarget(net.minecraft.world.entity.Entity entity, Player player) {
        if (!(entity instanceof LivingEntity living) || entity == player || !living.isAlive()) return false;
        // Animals and other non-humanoid mobs never get the jaw crosshair.
        if (living instanceof Animal) return false;
        // Explicitly allow player/NPC/villager-style humanoid targets.
        return living instanceof Player
            || living instanceof AbstractVillager
            || living instanceof GhoulNPC
            || living instanceof CCGNPC
            || living instanceof SpecialNPC
            || living instanceof GhoulBoss;
    }

    @SubscribeEvent
    public static void overlay(RenderGuiOverlayEvent.Post e) {
        if (e.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        GuiGraphics g = e.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        GhoulData.Race race = ClientGhoulData.race();
        int accent = race == GhoulData.Race.CCG ? TokyoGuiStyle.CCG : TokyoGuiStyle.RED;

        // Compact anime HUD. Vanilla HP/food/armor meters are hidden above, so this is the single source of truth.
        int x = 12, y = 12, w = 214, h = race == GhoulData.Race.HUMAN ? 104 : 120;
        float targetHealth = mc.player.getHealth() / Math.max(1f, mc.player.getMaxHealth());
        float targetHunger = ClientGhoulData.hunger() / 100f;
        float targetRage = ClientGhoulData.rageActive() ? 1f : ClientGhoulData.rage() / 100f;
        float targetLevel = ClientGhoulData.level() / 30f;
        if (!hudMetersInitialized) {
            hudHealth = targetHealth; hudHunger = targetHunger; hudRage = targetRage; hudLevel = targetLevel;
            hudMetersInitialized = true;
        } else {
            // Smooth interpolation keeps meter changes animated without extra tick tasks.
            hudHealth = Mth.lerp(0.18f, hudHealth, targetHealth);
            hudHunger = Mth.lerp(0.14f, hudHunger, targetHunger);
            hudRage = Mth.lerp(0.12f, hudRage, targetRage);
            hudLevel = Mth.lerp(0.10f, hudLevel, targetLevel);
        }
        g.fill(x + 3, y + 4, x + w + 3, y + h + 4, 0x55000000);
        g.fill(x, y, x + w, y + h, 0xCC090D12);
        g.fill(x, y, x + 3, y + h, accent);
        g.fill(x + 12, y + 2, x + w - 12, y + 3, 0xFF2C333C);
        g.drawString(mc.font, TokyoGuiStyle.ui(raceName(race) + "  //  LVL " + ClientGhoulData.level()), x + 12, y + 10, TokyoGuiStyle.WHITE, true);
        g.drawString(mc.font, TokyoGuiStyle.ui("RC  " + ClientGhoulData.rc() + "   •   " + (ClientGhoulData.kaguneActive() ? "КАГУНЕ: ВЫПУЩЕНО" : "КАГУНЕ: СКРЫТО")), x + 12, y + 24, TokyoGuiStyle.STEEL, false);

        drawMeter(g, mc, x + 12, y + 42, w - 24, 9, hudHealth, TokyoGuiStyle.RED_BRIGHT, "HP", formatHealth(mc.player), true);
        drawMeter(g, mc, x + 12, y + 58, w - 24, 8, hudHunger, TokyoGuiStyle.GOLD, "ГОЛОД", ClientGhoulData.hunger() + "%", false);
        if (race != GhoulData.Race.HUMAN) {
            float rage = hudRage;
            int rageColor = race == GhoulData.Race.CCG ? TokyoGuiStyle.CCG : TokyoGuiStyle.RED_BRIGHT;
            String rageText = ClientGhoulData.rageActive() ? (race == GhoulData.Race.CCG ? "БОЕВОЙ ДУХ" : "ЯРОСТЬ") + "  " + Math.max(0, ClientGhoulData.rageTicks() / 20) + "с" : "ЯРОСТЬ  " + ClientGhoulData.rage() + "%";
            drawMeter(g, mc, x + 12, y + 74, w - 24, 8, rage, rageColor, "", rageText, true);
        }
        drawMeter(g, mc, x + 12, y + 90, w - 24, 7, hudLevel, TokyoGuiStyle.STEEL, "УРОВЕНЬ", ClientGhoulData.level() + "/30", false);

        if (ClientGhoulData.bleedingTicks() > 0) {
            int bx = x, by = y + h + 8, bw = 210;
            float bleed = Math.max(0f, Math.min(1f, ClientGhoulData.bleedingTicks() / (30f * 20f)));
            g.fill(bx + 3, by + 3, bx + bw + 3, by + 15, 0x55000000);
            g.fill(bx, by, bx + bw, by + 12, 0xCC12070A);
            g.fill(bx, by, bx + (int)(bw * bleed), by + 12, 0xFFB01828);
            g.drawString(mc.font, TokyoGuiStyle.ui("КРОВОТЕЧЕНИЕ  " + (ClientGhoulData.bleedingTicks() / 20 + 1) + "с"), bx + 8, by + 2, TokyoGuiStyle.WHITE, true);
        }

        {
            // Simple origin label only; no bottom-right ability-key clutter.
            int bx = sw - 174, by = sh - 96;
            g.fill(bx + 3, by + 3, bx + 162, by + 32, 0x55000000);
            g.fill(bx, by, bx + 162, by + 29, 0xCC090D12);
            g.fill(bx, by, bx + 3, by + 29, accent);
            g.drawCenteredString(mc.font, TokyoGuiStyle.ui("ТИП: " + raceName(race)), bx + 81, by + 10, TokyoGuiStyle.WHITE);
        }

        // Low-frequency pulse gives the meters life without becoming distracting.
        float pulse = 0.5f + 0.5f * (float)Math.sin((mc.player.tickCount + 0.0f) * 0.10f);
        if (ClientGhoulData.rageActive()) {
            int alpha = 12 + (int)(pulse * 14);
            g.fill(0, 0, sw, 2, (alpha << 24) | (accent & 0x00FFFFFF));
        }
    }

    private static void drawMeter(GuiGraphics g, Minecraft mc, int x, int y, int w, int h, float value, int color, String label, String text, boolean bright) {
        value = Math.max(0f, Math.min(1f, value));
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF05070A);
        g.fill(x, y, x + w, y + h, 0xFF242A31);
        int fill = (int)(w * value);
        if (fill > 0) {
            g.fill(x, y, x + fill, y + h, color);
            if (bright && fill > 3) g.fill(x + 2, y + 1, x + fill - 1, y + 2, 0x55FFFFFF);
        }
        if (!label.isEmpty()) g.drawString(mc.font, TokyoGuiStyle.ui(label), x + 5, y + 1, TokyoGuiStyle.WHITE, true);
        g.drawString(mc.font, TokyoGuiStyle.ui(text), x + w - mc.font.width(text) - 5, y + 1, TokyoGuiStyle.WHITE, true);
    }

    private static String formatHealth(Player p) {
        return String.format(java.util.Locale.ROOT, "%.1f / %.0f", p.getHealth(), p.getMaxHealth());
    }

    private static String raceName(GhoulData.Race race) {
        return switch (race) { case GHOUL -> "ГУЛЬ"; case HALF_GHOUL -> "ПОЛУГУЛЬ"; case CCG -> "CCG"; default -> "ЧЕЛОВЕК"; };
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        jawPrevious = jawAnimation;
        Minecraft mc = Minecraft.getInstance();
        if (dashEffectTicks > 0) {
            dashEffectTicks--;
            if (mc.player != null && mc.level != null && mc.screen == null) {
                Vec3 look = mc.player.getLookAngle();
                for (int i = 0; i < 3; i++) {
                    double spread = (mc.player.getRandom().nextDouble() - 0.5) * 0.8;
                    mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,
                        mc.player.getX() - look.x * (0.25 + i * 0.18) + spread,
                        mc.player.getY() + 0.15 + mc.player.getRandom().nextDouble() * 0.9,
                        mc.player.getZ() - look.z * (0.25 + i * 0.18) + spread,
                        -look.x * 0.035, 0.01, -look.z * 0.035);
                }
            }
        }
        boolean grappleTargeting = false;
        if (mc.player != null && mc.screen == null && mc.options.keyShift.isDown() && ClientGhoulData.kaguneActive()) {
            KaguneType gt = currentKaguneType();
            if (gt == KaguneType.KANEKI_RINKAKU) {
                double range = 15.0D;
                grappleTargeting = grappleTarget(mc.player, range) != null;
            }
        }
        grappleReticlePrevious = grappleReticle;
        if (grappleReticleSnap) {
            grappleReticle = Math.max(0f, grappleReticle - 0.34f);
            if (grappleReticle <= 0.02f) grappleReticleSnap = false;
        } else if (grappleTargeting) {
            grappleReticle = Math.min(1f, grappleReticle + 0.22f);
        } else {
            grappleReticle = Math.max(0f, grappleReticle - 0.26f);
        }
        boolean target = mc.player != null && mc.screen == null && hasJawTarget(mc);
        if (jawClosing) {
            jawAnimation = Math.max(0f, jawAnimation - 0.24f);
            if (jawAnimation <= 0.001f) { jawAnimation = 0f; jawClosing = false; jawReopenDelay = 8; }
        } else if (jawReopenDelay > 0) {
            jawReopenDelay--;
        } else if (target) {
            jawAnimation = Math.min(1f, jawAnimation + 0.18f);
        } else {
            jawAnimation = Math.max(0f, jawAnimation - 0.18f);
        }
        if (mc.player == null || mc.level == null) { halfGhoulSoundTicks = 0; lastClientRace = GhoulData.Race.HUMAN; return; }
        GhoulData.Race race = ClientGhoulData.race();
        if (race != lastClientRace) { halfGhoulSoundTicks = 0; lastClientRace = race; }
        if (race != GhoulData.Race.HALF_GHOUL) return;
        halfGhoulSoundTicks++;
        if (halfGhoulSoundTicks >= 20 * 120) {
            halfGhoulSoundTicks = 0;
            if (HALF_GHOUL_SOUND_RANDOM.nextFloat() < 0.40f && mc.screen == null) {
                mc.player.playSound(TokyoGhoulRPG.FINGER_CRACK.get(), 0.42f, 0.96f + HALF_GHOUL_SOUND_RANDOM.nextFloat() * 0.08f);
            }
        }
    }

    @SubscribeEvent
    public static void globalMenuFrame(ScreenEvent.Render.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || e.getScreen() instanceof ProgressionScreen) return;
        GuiGraphics g = e.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        // A restrained Tokyo Ghoul frame is applied to vanilla screens too, without covering their widgets.
        g.fill(0, 0, w, 1, 0xAA7E1B2D);
        g.fill(0, h - 1, w, h, 0xAA090B0E);
        g.fill(0, 0, 1, h, 0x66000000);
        g.fill(w - 1, 0, w, h, 0x66000000);
        g.drawString(mc.font, TokyoGuiStyle.ui("TOKYO GHOUL // " + mc.screen.getTitle().getString()), 8, 7, 0x88D7DADF, false);
    }
    private ClientSetup(){}
}
