package com.tokyoghoul.rpg.entity;
import net.minecraft.world.entity.EntityType; import net.minecraft.world.level.Level;
public class CCGSniper extends SpecialNPC { public CCGSniper(EntityType<? extends CCGSniper> t,Level l){super(t,l);setCustomName(net.minecraft.network.chat.Component.literal("Стрелок CCG"));} @Override public String role(){return "ccg_sniper";} }
