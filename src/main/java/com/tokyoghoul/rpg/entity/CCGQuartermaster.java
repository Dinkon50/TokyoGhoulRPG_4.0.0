package com.tokyoghoul.rpg.entity;
import net.minecraft.world.entity.EntityType; import net.minecraft.world.level.Level;
public class CCGQuartermaster extends SpecialNPC { public CCGQuartermaster(EntityType<? extends CCGQuartermaster> t,Level l){super(t,l);setCustomName(net.minecraft.network.chat.Component.literal("Интендант CCG"));} @Override public String role(){return "ccg_quartermaster";} }
