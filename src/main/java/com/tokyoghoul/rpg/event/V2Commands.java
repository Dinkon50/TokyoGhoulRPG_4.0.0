package com.tokyoghoul.rpg.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.entity.ModEntities;
import com.tokyoghoul.rpg.world.KurokawaDistrictBuilder;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public final class V2Commands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent e){
        e.getDispatcher().register(Commands.literal("tg")
            .then(Commands.literal("skillpoints")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("amount", IntegerArgumentType.integer(1,45))
                    .executes(ctx -> {
                        var player=ctx.getSource().getPlayerOrException();
                        int amount=IntegerArgumentType.getInteger(ctx,"amount");
                        GhoulData.get(player).ifPresent(d -> d.addPoints(amount));
                        GhoulData.get(player).ifPresent(d -> d.sync(player));
                        ctx.getSource().sendSuccess(() -> Component.literal("§aTokyoGhoulRPG: добавлено очков: "+amount), true);
                        return 1;
                    })))
            .then(Commands.literal("level")
                .executes(ctx -> {
                    var player=ctx.getSource().getPlayerOrException();
                    int level=GhoulData.get(player).map(GhoulData::getLevel).orElse(1);
                    ctx.getSource().sendSuccess(() -> Component.literal("§eУровень TokyoGhoulRPG: "+level+"/30"), false);
                    return level;
                }))
            .then(Commands.literal("quests")
                .executes(ctx -> { ctx.getSource().sendSuccess(() -> Component.literal("§6Сюжет района: исследуй Kurokawa, найди приют гуля и станцию CCG."), false); return 1; }))
            .then(Commands.literal("district")
                .requires(s -> s.hasPermission(2))
                .executes(ctx -> {
                    var player=ctx.getSource().getPlayerOrException();
                    KurokawaDistrictBuilder.build(ctx.getSource().getLevel(), player);
                    return 1;
                }))
            .then(Commands.literal("boss")
                .requires(s -> s.hasPermission(2))
                .executes(ctx -> {
                    var player=ctx.getSource().getPlayerOrException();
                    var level=ctx.getSource().getLevel();
                    var e1=ModEntities.GHOUL_NPC.get().create(level);
                    if(e1==null) return 0;
                    e1.moveTo(player.getX()+4,player.getY(),player.getZ()+4,0,0);
                    e1.addTag("elite");
                    e1.setCustomName(Component.literal("§4Элитный гуль"));
                    e1.setCustomNameVisible(true);
                    var hp=e1.getAttribute(Attributes.MAX_HEALTH); if(hp!=null) hp.setBaseValue(80);
                    var dmg=e1.getAttribute(Attributes.ATTACK_DAMAGE); if(dmg!=null) dmg.setBaseValue(14);
                    e1.setHealth(80);
                    level.addFreshEntity(e1);
                    ctx.getSource().sendSuccess(() -> Component.literal("§4Элитный гуль появился рядом."), true);
                    return 1;
                }))
            .then(Commands.literal("reset")
                .requires(s -> s.hasPermission(2))
                .executes(ctx -> {
                    var player=ctx.getSource().getPlayerOrException();
                    GhoulData.get(player).ifPresent(d -> { d.resetProgress(); d.sync(player); });
                    ctx.getSource().sendSuccess(() -> Component.literal("§cПрогресс TokyoGhoulRPG сброшен. Выбор происхождения нужно пройти заново."), true);
                    return 1;
                }))
        );
    }
    private V2Commands(){}
}
