package com.tokyoghoul.rpg.entity;
import net.minecraft.world.entity.EntityType; import net.minecraft.world.level.Level;
public class GhoulMedic extends SpecialNPC { public GhoulMedic(EntityType<? extends GhoulMedic> t,Level l){super(t,l);setCustomName(net.minecraft.network.chat.Component.literal("Медик Гулей"));} @Override public String role(){return "ghoul_medic";} }
