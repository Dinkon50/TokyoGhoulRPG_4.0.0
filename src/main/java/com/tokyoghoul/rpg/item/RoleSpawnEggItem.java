package com.tokyoghoul.rpg.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** Spawn egg with a visual role tag used by the stylized NPC renderer. */
public class RoleSpawnEggItem extends Item {
    private final Supplier<? extends EntityType<? extends Mob>> type;
    private final String role;

    public RoleSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, String role, Properties props) {
        super(props);
        this.type = type;
        this.role = role;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockPos p = ctx.getClickedPos().relative(ctx.getClickedFace());
        Mob mob = type.get().create(level);
        if (mob == null) return InteractionResult.FAIL;
        mob.moveTo(p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        mob.addTag(role);
        level.addFreshEntity(mob);
        if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative()) ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
