package com.tokyoghoul.rpg.entity;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TokyoGhoulRPG.MODID);

    public static final RegistryObject<EntityType<GhoulNPC>> GHOUL_NPC =
        ENTITIES.register("ghoul_npc", () ->
            EntityType.Builder.of(GhoulNPC::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F).clientTrackingRange(8).build("ghoul_npc"));

    public static final RegistryObject<EntityType<GhoulBoss>> GHOUL_BOSS =
        ENTITIES.register("ghoul_boss", () ->
            EntityType.Builder.of(GhoulBoss::new, MobCategory.MONSTER)
                .sized(1.20F, 3.90F).clientTrackingRange(12).build("ghoul_boss"));

    public static final RegistryObject<EntityType<CCGNPC>> CCG_NPC =
        ENTITIES.register("ccg_npc", () ->
            EntityType.Builder.of(CCGNPC::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F).clientTrackingRange(8).build("ccg_npc"));

    public static final RegistryObject<EntityType<CCGQuartermaster>> CCG_QUARTERMASTER = ENTITIES.register("ccg_quartermaster", () -> EntityType.Builder.of(CCGQuartermaster::new, MobCategory.CREATURE).sized(.62F,1.98F).clientTrackingRange(8).build("ccg_quartermaster"));
    public static final RegistryObject<EntityType<CCGHeavy>> CCG_HEAVY = ENTITIES.register("ccg_heavy", () -> EntityType.Builder.of(CCGHeavy::new, MobCategory.MONSTER).sized(.72F,2.08F).clientTrackingRange(8).build("ccg_heavy"));
    public static final RegistryObject<EntityType<CCGSniper>> CCG_SNIPER = ENTITIES.register("ccg_sniper", () -> EntityType.Builder.of(CCGSniper::new, MobCategory.MONSTER).sized(.58F,1.92F).clientTrackingRange(10).build("ccg_sniper"));
    public static final RegistryObject<EntityType<GhoulScavenger>> GHOUL_SCAVENGER = ENTITIES.register("ghoul_scavenger", () -> EntityType.Builder.of(GhoulScavenger::new, MobCategory.MONSTER).sized(.65F,2.0F).clientTrackingRange(8).build("ghoul_scavenger"));
    public static final RegistryObject<EntityType<GhoulMedic>> GHOUL_MEDIC = ENTITIES.register("ghoul_medic", () -> EntityType.Builder.of(GhoulMedic::new, MobCategory.CREATURE).sized(.60F,1.95F).clientTrackingRange(8).build("ghoul_medic"));

    private ModEntities(){}
}
