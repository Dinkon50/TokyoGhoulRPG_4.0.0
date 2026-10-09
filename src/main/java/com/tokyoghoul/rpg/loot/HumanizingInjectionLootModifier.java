package com.tokyoghoul.rpg.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.init.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class HumanizingInjectionLootModifier extends LootModifier {
    public static final Codec<HumanizingInjectionLootModifier> CODEC = RecordCodecBuilder.create(instance ->
        LootModifier.codecStart(instance).apply(instance, HumanizingInjectionLootModifier::new)
    );

    protected HumanizingInjectionLootModifier(LootItemCondition[] conditions) { super(conditions); }

    @Override protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation table = context.getQueriedLootTableId();
        if (table == null || !table.getPath().startsWith("chests/")) return generatedLoot;

        // Base chance: 2%. Mod-owned chest tables get a higher 8% chance.
        float chance = table.getNamespace().equals(TokyoGhoulRPG.MODID) ? 0.08F : 0.02F;
        if (context.getRandom().nextFloat() < chance) {
            generatedLoot.add(new ItemStack(ModItems.HUMANIZING_INJECTION.get()));
        }
        return generatedLoot;
    }

    @Override public Codec<? extends net.minecraftforge.common.loot.IGlobalLootModifier> codec() { return CODEC; }
}
