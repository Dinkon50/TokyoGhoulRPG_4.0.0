package com.tokyoghoul.rpg.init;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.entity.ModEntities;
import com.tokyoghoul.rpg.item.GhoulCoatItem;
import com.tokyoghoul.rpg.item.HalfGhoulOrganItem;
import com.tokyoghoul.rpg.item.HumanizingInjectionItem;
import com.tokyoghoul.rpg.item.RCInjectorItem;
import com.tokyoghoul.rpg.item.RoleSpawnEggItem;
import com.tokyoghoul.rpg.item.QuinqueItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TokyoGhoulRPG.MODID);

    public static final RegistryObject<Item> RC_CELL = ITEMS.register("rc_cell",()->new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> RC_INJECTOR = ITEMS.register("rc_injector",()->new RCInjectorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HUMANIZING_INJECTION = ITEMS.register("humanizing_injection",()->new HumanizingInjectionItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HALF_GHOUL_ORGAN = ITEMS.register("half_ghoul_organ",()->new HalfGhoulOrganItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SKILL_TOKEN = ITEMS.register("skill_token",()->new Item(new Item.Properties().stacksTo(64)));

    // Clothing / masks. Vanilla armor renderer supplies the equipped 3D silhouette; textures are custom.
    public static final RegistryObject<Item> GHOUL_MASK = ITEMS.register("ghoul_mask",()->new ArmorItem(TokyoArmorMaterials.GHOUL, ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_COAT = ITEMS.register("ghoul_coat",()->new ArmorItem(TokyoArmorMaterials.GHOUL,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_PANTS = ITEMS.register("ghoul_pants",()->new ArmorItem(TokyoArmorMaterials.GHOUL,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_BOOTS = ITEMS.register("ghoul_boots",()->new ArmorItem(TokyoArmorMaterials.GHOUL,ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> INVESTIGATOR_COAT = ITEMS.register("investigator_coat",()->new ArmorItem(TokyoArmorMaterials.CCG,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> CCG_HELMET = ITEMS.register("ccg_helmet",()->new ArmorItem(TokyoArmorMaterials.CCG,ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> CCG_LEGS = ITEMS.register("ccg_legs",()->new ArmorItem(TokyoArmorMaterials.CCG,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> CCG_BOOTS = ITEMS.register("ccg_boots",()->new ArmorItem(TokyoArmorMaterials.CCG,ArmorItem.Type.BOOTS,new Item.Properties()));

    // Kaneki-inspired one-eyed ghoul outfit. It is an original fan-made texture set, not a ripped anime asset.
    public static final RegistryObject<Item> KANEKI_MASK = ITEMS.register("kaneki_mask",()->new ArmorItem(TokyoArmorMaterials.KANEKI,ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> KANEKI_COAT = ITEMS.register("kaneki_coat",()->new ArmorItem(TokyoArmorMaterials.KANEKI,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> KANEKI_PANTS = ITEMS.register("kaneki_pants",()->new ArmorItem(TokyoArmorMaterials.KANEKI,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> KANEKI_BOOTS = ITEMS.register("kaneki_boots",()->new ArmorItem(TokyoArmorMaterials.KANEKI,ArmorItem.Type.BOOTS,new Item.Properties()));

    // Canonical Quinque names shown in the anime; the CCG starter receives mass-produced Scorpion 1/56.
    public static final RegistryObject<Item> QUINQUE_IXA = ITEMS.register("quinque_ixa",()->new QuinqueItem(4, QuinqueItem.Effect.NONE, 0.45f, -2.15f));
    public static final RegistryObject<Item> QUINQUE_NARUKAMI = ITEMS.register("quinque_narukami",()->new QuinqueItem(3, QuinqueItem.Effect.CRIT, 0.45f, -1.85f));
    public static final RegistryObject<Item> QUINQUE_SCORPION = ITEMS.register("quinque_scorpion",()->new QuinqueItem(1, QuinqueItem.Effect.NONE, 0.45f, -1.65f));
    public static final RegistryObject<Item> QUINQUE_13S_JASON = ITEMS.register("quinque_13s_jason",()->new QuinqueItem(4, QuinqueItem.Effect.BLEED, 0.45f, -2.20f));
    public static final RegistryObject<Item> QUINQUE_FUEGUCHI_ONE = ITEMS.register("quinque_fueguchi_one",()->new QuinqueItem(2, QuinqueItem.Effect.NONE, 0.45f, -1.95f));
    public static final RegistryObject<Item> QUINQUE_FUEGUCHI_TWO = ITEMS.register("quinque_fueguchi_two",()->new QuinqueItem(3, QuinqueItem.Effect.KNOCK, 0.45f, -2.05f));
    public static final RegistryObject<Item> QUINQUE_YUKIMURA_1_3 = ITEMS.register("quinque_yukimura_1_3",()->new QuinqueItem(2, QuinqueItem.Effect.NONE, 0.45f, -1.90f));
    public static final RegistryObject<Item> QUINQUE_OWL = ITEMS.register("quinque_owl",()->new QuinqueItem(5, QuinqueItem.Effect.CRIT, 0.45f, -2.35f));



    public static final RegistryObject<Item> GHOUL_SPAWN_EGG = ITEMS.register("ghoul_spawn_egg",()->new ForgeSpawnEggItem(ModEntities.GHOUL_NPC,0x5B0A12,0xD7193F,new Item.Properties()));
    public static final RegistryObject<Item> CCG_SPAWN_EGG = ITEMS.register("ccg_spawn_egg",()->new ForgeSpawnEggItem(ModEntities.CCG_NPC,0x173B5E,0x55B7E8,new Item.Properties()));

    // Role eggs: same entity logic, different 3D/texture role in the renderer.
    public static final RegistryObject<Item> GHOUL_REFUGE_EGG = ITEMS.register("ghoul_refuge_spawn_egg",()->new RoleSpawnEggItem(ModEntities.GHOUL_NPC,"refuge",new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_MASKMAKER_EGG = ITEMS.register("ghoul_maskmaker_spawn_egg",()->new RoleSpawnEggItem(ModEntities.GHOUL_NPC,"maskmaker",new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_ELITE_EGG = ITEMS.register("ghoul_elite_spawn_egg",()->new RoleSpawnEggItem(ModEntities.GHOUL_NPC,"elite",new Item.Properties()));
    public static final RegistryObject<Item> CCG_MEDIC_EGG = ITEMS.register("ccg_medic_spawn_egg",()->new RoleSpawnEggItem(ModEntities.CCG_NPC,"medic",new Item.Properties()));
    public static final RegistryObject<Item> CCG_HUNTER_EGG = ITEMS.register("ccg_hunter_spawn_egg",()->new RoleSpawnEggItem(ModEntities.CCG_NPC,"hunter",new Item.Properties()));
    public static final RegistryObject<Item> CCG_ELITE_EGG = ITEMS.register("ccg_elite_spawn_egg",()->new RoleSpawnEggItem(ModEntities.CCG_NPC,"elite",new Item.Properties()));
    public static final RegistryObject<Item> CCG_QUARTERMASTER_EGG = ITEMS.register("ccg_quartermaster_spawn_egg",()->new ForgeSpawnEggItem(ModEntities.CCG_QUARTERMASTER,0x24303A,0xBFC7D0,new Item.Properties()));
    public static final RegistryObject<Item> CCG_HEAVY_EGG = ITEMS.register("ccg_heavy_spawn_egg",()->new ForgeSpawnEggItem(ModEntities.CCG_HEAVY,0x1A1E24,0x8A96A3,new Item.Properties()));
    public static final RegistryObject<Item> CCG_SNIPER_EGG = ITEMS.register("ccg_sniper_spawn_egg",()->new ForgeSpawnEggItem(ModEntities.CCG_SNIPER,0x18202A,0xB9C2CC,new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_SCAVENGER_EGG = ITEMS.register("ghoul_scavenger_spawn_egg",()->new ForgeSpawnEggItem(ModEntities.GHOUL_SCAVENGER,0x2A1718,0x8E1B2D,new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_MEDIC_EGG = ITEMS.register("ghoul_medic_spawn_egg",()->new ForgeSpawnEggItem(ModEntities.GHOUL_MEDIC,0x28191B,0xC04A4A,new Item.Properties()));
    public static final RegistryObject<Item> YOSHIMURA_SPAWN_EGG = ITEMS.register("yoshimura_spawn_egg",()->new ForgeSpawnEggItem(ModEntities.GHOUL_BOSS,0x161719,0xB51D2B,new Item.Properties()));

    private ModItems(){}
}
