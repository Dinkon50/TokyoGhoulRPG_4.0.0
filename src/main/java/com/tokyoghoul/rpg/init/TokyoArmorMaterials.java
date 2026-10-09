package com.tokyoghoul.rpg.init;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public enum TokyoArmorMaterials implements ArmorMaterial {
    GHOUL("ghoul", 18, new int[]{2,5,6,2}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 1.5f, 0.0f, Ingredient.of(net.minecraft.world.item.Items.LEATHER)),
    CCG("ccg", 22, new int[]{2,6,7,2}, 18, SoundEvents.ARMOR_EQUIP_IRON, 1.5f, 0.0f, Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)),
    KANEKI("kaneki", 20, new int[]{2,5,6,2}, 16, SoundEvents.ARMOR_EQUIP_LEATHER, 1.8f, 0.0f, Ingredient.of(net.minecraft.world.item.Items.LEATHER));

    private static final int[] DURABILITY={13,15,16,11};
    private final String name; private final int durability; private final int[] defense; private final int enchant;
    private final SoundEvent sound; private final float toughness; private final float knockback; private final Ingredient repair;
    TokyoArmorMaterials(String name,int durability,int[] defense,int enchant,SoundEvent sound,float toughness,float knockback,Ingredient repair){
        this.name=name;this.durability=durability;this.defense=defense;this.enchant=enchant;this.sound=sound;this.toughness=toughness;this.knockback=knockback;this.repair=repair;
    }
    @Override public int getDurabilityForType(ArmorItem.Type type){return DURABILITY[type.getSlot().getIndex()]*durability;}
    @Override public int getDefenseForType(ArmorItem.Type type){return defense[type.getSlot().getIndex()];}
    @Override public int getEnchantmentValue(){return enchant;}
    @Override public SoundEvent getEquipSound(){return sound;}
    @Override public Ingredient getRepairIngredient(){return repair;}
    @Override public String getName(){return "tokyoghoulrpg:"+name;}
    @Override public float getToughness(){return toughness;}
    @Override public float getKnockbackResistance(){return knockback;}
}
