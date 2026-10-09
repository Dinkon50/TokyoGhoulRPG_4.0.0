package com.tokyoghoul.rpg.entity;
import net.minecraft.world.entity.EntityType; import net.minecraft.world.level.Level;
public class CCGHeavy extends SpecialNPC { public CCGHeavy(EntityType<? extends CCGHeavy> t,Level l){super(t,l);setCustomName(net.minecraft.network.chat.Component.literal("Тяжёлый следователь"));} @Override public String role(){return "ccg_heavy";} }
