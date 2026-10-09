package com.tokyoghoul.rpg.init;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TokyoGhoulRPG.MODID);

    public static final RegistryObject<CreativeModeTab> ALL_ITEMS = TABS.register("all_items", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("item_group.tokyoghoulrpg.all_items"))
            .icon(() -> new ItemStack(ModItems.QUINQUE_IXA.get()))
            .displayItems((params, output) -> ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build()
    );

    public static final RegistryObject<CreativeModeTab> INJECTIONS = TABS.register("injections", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("item_group.tokyoghoulrpg.injections"))
            .icon(() -> new ItemStack(ModItems.HUMANIZING_INJECTION.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.RC_INJECTOR.get());
                output.accept(ModItems.HUMANIZING_INJECTION.get());
            })
            .build()
    );


    public static final RegistryObject<CreativeModeTab> QUINQUES = TABS.register("quinques", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("item_group.tokyoghoulrpg.quinques"))
            .icon(() -> new ItemStack(ModItems.QUINQUE_IXA.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.QUINQUE_IXA.get());
                output.accept(ModItems.QUINQUE_NARUKAMI.get());
                output.accept(ModItems.QUINQUE_SCORPION.get());
                output.accept(ModItems.QUINQUE_13S_JASON.get());
                output.accept(ModItems.QUINQUE_FUEGUCHI_ONE.get());
                output.accept(ModItems.QUINQUE_FUEGUCHI_TWO.get());
                output.accept(ModItems.QUINQUE_YUKIMURA_1_3.get());
                output.accept(ModItems.QUINQUE_OWL.get());
            })
            .build()
    );

    public static final RegistryObject<CreativeModeTab> CLOTHING = TABS.register("clothing", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("item_group.tokyoghoulrpg.clothing"))
            .icon(() -> new ItemStack(ModItems.KANEKI_COAT.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.KANEKI_MASK.get());
                output.accept(ModItems.KANEKI_COAT.get());
                output.accept(ModItems.KANEKI_PANTS.get());
                output.accept(ModItems.KANEKI_BOOTS.get());
                output.accept(ModItems.GHOUL_MASK.get());
                output.accept(ModItems.GHOUL_COAT.get());
                output.accept(ModItems.GHOUL_PANTS.get());
                output.accept(ModItems.GHOUL_BOOTS.get());
                output.accept(ModItems.INVESTIGATOR_COAT.get());
                output.accept(ModItems.CCG_HELMET.get());
                output.accept(ModItems.CCG_LEGS.get());
                output.accept(ModItems.CCG_BOOTS.get());
            })
            .build()
    );

    public static final RegistryObject<CreativeModeTab> CHARACTERS = TABS.register("characters", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("item_group.tokyoghoulrpg.characters"))
            .icon(() -> new ItemStack(ModItems.YOSHIMURA_SPAWN_EGG.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.GHOUL_SPAWN_EGG.get());
                output.accept(ModItems.GHOUL_REFUGE_EGG.get());
                output.accept(ModItems.GHOUL_MASKMAKER_EGG.get());
                output.accept(ModItems.GHOUL_ELITE_EGG.get());
                output.accept(ModItems.GHOUL_SCAVENGER_EGG.get());
                output.accept(ModItems.GHOUL_MEDIC_EGG.get());
                output.accept(ModItems.CCG_SPAWN_EGG.get());
                output.accept(ModItems.CCG_HUNTER_EGG.get());
                output.accept(ModItems.CCG_MEDIC_EGG.get());
                output.accept(ModItems.CCG_ELITE_EGG.get());
                output.accept(ModItems.CCG_QUARTERMASTER_EGG.get());
                output.accept(ModItems.CCG_HEAVY_EGG.get());
                output.accept(ModItems.CCG_SNIPER_EGG.get());
                output.accept(ModItems.YOSHIMURA_SPAWN_EGG.get());
            })
            .build()
    );

    private ModCreativeTabs() {}
}
