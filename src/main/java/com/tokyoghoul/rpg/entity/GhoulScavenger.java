package com.tokyoghoul.rpg.entity;
import net.minecraft.world.entity.EntityType; import net.minecraft.world.level.Level;
public class GhoulScavenger extends SpecialNPC { public GhoulScavenger(EntityType<? extends GhoulScavenger> t,Level l){super(t,l);setCustomName(net.minecraft.network.chat.Component.literal("Собиратель Гулей"));} @Override public String role(){return "ghoul_scavenger";} }
