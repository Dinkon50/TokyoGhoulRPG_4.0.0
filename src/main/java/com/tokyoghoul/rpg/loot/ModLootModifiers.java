package com.tokyoghoul.rpg.loot;

import com.mojang.serialization.Codec;
import com.tokyoghoul.rpg.TokyoGhoulRPG;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TokyoGhoulRPG.MODID);

    public static final RegistryObject<Codec<HumanizingInjectionLootModifier>> HUMANIZING_INJECTION =
        SERIALIZERS.register("humanizing_injection", () -> HumanizingInjectionLootModifier.CODEC);

    public static void register(IEventBus bus) { SERIALIZERS.register(bus); }
    private ModLootModifiers() {}
}
