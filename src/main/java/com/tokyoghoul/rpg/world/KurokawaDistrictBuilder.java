package com.tokyoghoul.rpg.world;

import com.tokyoghoul.rpg.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Builds the original Kurokawa District hub around the command caller. */
public final class KurokawaDistrictBuilder {
    private KurokawaDistrictBuilder() {}

    public static void build(ServerLevel level, Player player) {
        BlockPos origin = player.blockPosition().above();
        int radius = 15;

        // Street and plaza.
        fill(level, origin.offset(-radius, -1, -radius), origin.offset(radius, -1, radius), Blocks.DEEPSLATE);
        fill(level, origin.offset(-8, -1, -8), origin.offset(8, -1, 8), Blocks.POLISHED_DEEPSLATE);
        for (int x = -radius; x <= radius; x += 4) {
            lamp(level, origin.offset(x, 0, -12));
            lamp(level, origin.offset(x, 0, 12));
        }

        // CCG station, ghoul refuge, and alley shops.
        building(level, origin.offset(-13, 0, -9), 7, 7, 5, Blocks.POLISHED_BLACKSTONE, Blocks.IRON_BLOCK);
        sign(level, origin.offset(-10, 5, -9), Blocks.LIGHT_BLUE_CONCRETE);
        building(level, origin.offset(6, 0, -9), 7, 7, 4, Blocks.DARK_OAK_PLANKS, Blocks.RED_WOOL);
        sign(level, origin.offset(9, 4, -9), Blocks.RED_WOOL);
        building(level, origin.offset(-13, 0, 4), 7, 6, 4, Blocks.BRICKS, Blocks.COPPER_BLOCK);
        building(level, origin.offset(6, 0, 4), 7, 6, 4, Blocks.DEEPSLATE_BRICKS, Blocks.AMETHYST_BLOCK);

        // A narrow neon-like alley.
        for (int z = -5; z <= 8; z++) {
            level.setBlock(origin.offset(0, 1, z), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 3);
            if (z % 2 == 0) level.setBlock(origin.offset(-1, 3, z), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
        }

        spawnGhoul(level, origin.offset(9, 1, -5), "refuge");
        spawnGhoul(level, origin.offset(-9, 1, 7), "maskmaker");
        spawnCCG(level, origin.offset(-10, 1, -5), "officer");
        spawnCCG(level, origin.offset(-8, 1, -5), "medic");
        spawnCCG(level, origin.offset(9, 1, 7), "hunter");
        spawnGhoul(level, origin.offset(0, 1, 10), "elite");

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§8[ §cKurokawa District §8] §7Район создан рядом с тобой."));
    }

    private static void building(ServerLevel level, BlockPos base, int w, int d, int h, net.minecraft.world.level.block.Block wall, net.minecraft.world.level.block.Block trim) {
        for (int x = 0; x < w; x++) for (int z = 0; z < d; z++) {
            level.setBlock(base.offset(x, 0, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
            for (int y = 1; y <= h; y++) {
                boolean edge = x == 0 || z == 0 || x == w - 1 || z == d - 1;
                if (edge) level.setBlock(base.offset(x, y, z), y == h ? trim.defaultBlockState() : wall.defaultBlockState(), 3);
            }
        }
        int doorX = w / 2;
        for (int y = 1; y <= 2; y++) level.setBlock(base.offset(doorX, y, 0), Blocks.AIR.defaultBlockState(), 3);
        for (int x = 1; x < w - 1; x += 2) level.setBlock(base.offset(x, h - 1, 0), Blocks.GLASS_PANE.defaultBlockState(), 3);
    }

    private static void lamp(ServerLevel level, BlockPos p) {
        level.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 3);
        level.setBlock(p.above(), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
    }

    private static void sign(ServerLevel level, BlockPos p, net.minecraft.world.level.block.Block block) {
        level.setBlock(p, block.defaultBlockState(), 3);
        level.setBlock(p.above(), Blocks.BLACK_CONCRETE.defaultBlockState(), 3);
    }

    private static void fill(ServerLevel level, BlockPos a, BlockPos b, net.minecraft.world.level.block.Block block) {
        BlockPos.betweenClosed(a, b).forEach(pos -> level.setBlock(pos, block.defaultBlockState(), 3));
    }

    private static void spawnGhoul(ServerLevel level, BlockPos pos, String role) {
        var e = ModEntities.GHOUL_NPC.get().create(level);
        if (e == null) return;
        e.moveTo(pos, 0, 0);
        e.addTag(role);
        e.setCustomNameVisible(true);
        e.setCustomName(net.minecraft.network.chat.Component.literal(roleName(role)));
        if ("elite".equals(role)) {
            e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(70);
            e.setHealth(70);
            e.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12);
        }
        level.addFreshEntity(e);
    }

    private static void spawnCCG(ServerLevel level, BlockPos pos, String role) {
        var e = ModEntities.CCG_NPC.get().create(level);
        if (e == null) return;
        e.moveTo(pos, 0, 0);
        e.addTag(role);
        e.setCustomNameVisible(true);
        e.setCustomName(net.minecraft.network.chat.Component.literal(roleName(role)));
        level.addFreshEntity(e);
    }

    private static String roleName(String role) {
        return switch (role) {
            case "refuge" -> "Приют гуля";
            case "maskmaker" -> "Масочник";
            case "officer" -> "Офицер CCG";
            case "medic" -> "Медик CCG";
            case "hunter" -> "Охотник CCG";
            case "elite" -> "Элитный гуль";
            default -> "Житель района";
        };
    }
}
