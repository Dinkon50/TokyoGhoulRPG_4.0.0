package com.tokyoghoul.rpg;

import com.tokyoghoul.rpg.client.ClientSetup;
import com.tokyoghoul.rpg.entity.CCGNPC;
import com.tokyoghoul.rpg.entity.GhoulNPC;
import com.tokyoghoul.rpg.entity.GhoulBoss;
import com.tokyoghoul.rpg.entity.ModEntities;
import com.tokyoghoul.rpg.entity.SpecialNPC;
import com.tokyoghoul.rpg.init.ModItems;
import com.tokyoghoul.rpg.init.ModCreativeTabs;
import com.tokyoghoul.rpg.loot.ModLootModifiers;
import com.tokyoghoul.rpg.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TokyoGhoulRPG.MODID)
public class TokyoGhoulRPG {
    public static final String MODID = "tokyoghoulrpg";
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final RegistryObject<SoundEvent> SKILL_TREE_TENSION = SOUNDS.register("skill_tree_tension",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "skill_tree_tension")));
    public static final RegistryObject<SoundEvent> JAW_SNAP = SOUNDS.register("jaw_snap",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "jaw_snap")));
    public static final RegistryObject<SoundEvent> FINGER_CRACK = SOUNDS.register("finger_crack",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "finger_crack")));
    public static final RegistryObject<SoundEvent> KAGUNE_APPEAR = SOUNDS.register("kagune_appear", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "kagune_appear")));
    public static final RegistryObject<SoundEvent> KAGUNE_RETRACT = SOUNDS.register("kagune_retract", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "kagune_retract")));
    public static final RegistryObject<SoundEvent> KAGUNE_SWISH = SOUNDS.register("kagune_swish", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "kagune_swish")));
    public static final RegistryObject<SoundEvent> KAGUNE_HIT = SOUNDS.register("kagune_hit", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "kagune_hit")));
    public static final RegistryObject<SoundEvent> GRAPPLE_WHOOSH = SOUNDS.register("grapple_whoosh", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "grapple_whoosh")));
    public static final RegistryObject<SoundEvent> DASH_WHOOSH = SOUNDS.register("dash_whoosh", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "dash_whoosh")));
    public static final RegistryObject<SoundEvent> RAGE_ACTIVATE = SOUNDS.register("rage_activate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "rage_activate")));
    public static final RegistryObject<SoundEvent> QUINQUE_SWING = SOUNDS.register("quinque_swing", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "quinque_swing")));

    public TokyoGhoulRPG() {
        var bus=FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        ModCreativeTabs.TABS.register(bus);
        ModLootModifiers.register(bus);
        SOUNDS.register(bus);
        NetworkHandler.init();
        bus.addListener(TokyoGhoulRPG::registerAttributes);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientSetup::init);
    }

    private static void registerAttributes(EntityAttributeCreationEvent e){
        e.put(ModEntities.GHOUL_NPC.get(), GhoulNPC.createAttributes().build());
        e.put(ModEntities.CCG_NPC.get(), CCGNPC.createAttributes().build());
        e.put(ModEntities.GHOUL_BOSS.get(), GhoulBoss.createAttributes().build());
        e.put(ModEntities.CCG_QUARTERMASTER.get(), SpecialNPC.createAttributes().build());
        e.put(ModEntities.CCG_HEAVY.get(), SpecialNPC.createAttributes().build());
        e.put(ModEntities.CCG_SNIPER.get(), SpecialNPC.createAttributes().build());
        e.put(ModEntities.GHOUL_SCAVENGER.get(), SpecialNPC.createAttributes().build());
        e.put(ModEntities.GHOUL_MEDIC.get(), SpecialNPC.createAttributes().build());
    }
}
