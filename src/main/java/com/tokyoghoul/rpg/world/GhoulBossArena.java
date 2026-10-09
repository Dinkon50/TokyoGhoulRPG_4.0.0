package com.tokyoghoul.rpg.world;

import com.tokyoghoul.rpg.entity.GhoulBoss;
import com.tokyoghoul.rpg.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import com.tokyoghoul.rpg.init.ModItems;

import java.util.List;

/** Rare persistent arena. It is generated once and the boss returns to the same center after five minutes. */
public final class GhoulBossArena {
    private static final String DATA_ID = "tokyoghoulrpg_boss_arena";
    private GhoulBossArena() {}

    private static BossArenaData data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BossArenaData::load, BossArenaData::new, DATA_ID);
    }

    public static void tick(ServerLevel level) {
        BossArenaData d = data(level);
        if (!d.generated) {
            if (level.getGameTime() % 1200L != 0L) return;
            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) return;
            // One arena per world, with a deliberately low first-discovery chance.
            if (level.random.nextInt(400) != 0) return;
            Player base = players.get(level.random.nextInt(players.size()));
            int angle = level.random.nextInt(360);
            double distance = 140.0D + level.random.nextDouble() * 110.0D;
            int x = base.blockPosition().getX() + (int)(Math.cos(Math.toRadians(angle)) * distance);
            int z = base.blockPosition().getZ() + (int)(Math.sin(Math.toRadians(angle)) * distance);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y < level.getMinBuildHeight() + 5 || y > level.getMaxBuildHeight() - 10) return;
            BlockPos center = new BlockPos(x, y, z);
            if (!safe(center, level)) return;
            build(level, center);
            fillChests(level, center);
            d.generated = true; d.x = x; d.y = y; d.z = z; d.respawnAt = level.getGameTime() + 40L;
            d.markDirty();
            spawn(level, center);
            base.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4§l[ОПАСНО] §cГде-то в мире пробудилось логово Пожирателя."));
            return;
        }

        boolean alive = !level.getEntities(ModEntities.GHOUL_BOSS.get(),
            new net.minecraft.world.phys.AABB(d.x - 20, d.y - 5, d.z - 20, d.x + 20, d.y + 8, d.z + 20), e -> e.isAlive()).isEmpty();
        if (!alive && d.respawnAt > 0L && level.getGameTime() >= d.respawnAt) {
            spawn(level, new BlockPos(d.x, d.y, d.z));
            d.respawnAt = -1L;
            d.markDirty();
        }
    }

    public static void onBossDeath(ServerLevel level) {
        BossArenaData d = data(level);
        if (d.generated) {
            d.respawnAt = level.getGameTime() + 20L * 60L * 5L;
            d.markDirty();
        }
    }

    private static boolean safe(BlockPos center, ServerLevel level) {
        for (int dx = -8; dx <= 8; dx++) for (int dz = -8; dz <= 8; dz++) {
            BlockPos p = center.offset(dx, 0, dz);
            if (!level.getBlockState(p.below()).isSolid()) return false;
            if (!level.getBlockState(p).isAir() || !level.getBlockState(p.above()).isAir()) return false;
        }
        return true;
    }

    private static void build(ServerLevel level, BlockPos c) {
        for (int dx = -11; dx <= 11; dx++) for (int dz = -11; dz <= 11; dz++) {
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist <= 11.5) level.setBlock(c.offset(dx, -1, dz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
            if (dist <= 10.5) level.setBlock(c.offset(dx, 0, dz), Blocks.BLACKSTONE.defaultBlockState(), 3);
            if (dist > 9.0 && dist <= 10.5) level.setBlock(c.offset(dx, 1, dz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
        }
        for (int dx = -10; dx <= 10; dx += 5) for (int dz = -10; dz <= 10; dz += 5) {
            if (Math.abs(dx) == 10 || Math.abs(dz) == 10) {
                level.setBlock(c.offset(dx, 1, dz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
                level.setBlock(c.offset(dx, 2, dz), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
            }
        }
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            level.setBlock(c.offset(dx, 0, dz), Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState(), 3);
    }

    private static void fillChests(ServerLevel level, BlockPos c) {
        java.util.ArrayList<BlockPos> spots = new java.util.ArrayList<>();
        int[][] candidates = {{-8,-7},{8,-6},{-7,7},{7,8},{0,-9},{-9,0},{9,1},{1,9},{-5,-9},{6,-9},{-9,5},{9,6}};
        java.util.ArrayList<int[]> shuffled = new java.util.ArrayList<>(java.util.Arrays.asList(candidates));
        java.util.Collections.shuffle(shuffled, new java.util.Random(level.random.nextLong()));

        // Five chests, but never closer than 3 blocks to one another.
        for (int[] v : shuffled) {
            if (spots.size() >= 5) break;
            BlockPos pos = c.offset(v[0], 1, v[1]);
            boolean farEnough = true;
            for (BlockPos other : spots) {
                if (pos.distSqr(other) < 9.0D) { farEnough = false; break; }
            }
            if (farEnough && level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,
                    Direction.Plane.HORIZONTAL.getRandomDirection(level.random)), 3);
                spots.add(pos);
            }
        }
        if (spots.isEmpty()) return;

        // Each chest contains 2-5 item stacks total. Exactly one chest receives one Strong Healing II splash potion.
        int potionChest = level.random.nextInt(spots.size());
        for (int i = 0; i < spots.size(); i++) {
            if (!(level.getBlockEntity(spots.get(i)) instanceof ChestBlockEntity chest)) continue;
            int totalStacks = 2 + level.random.nextInt(4);
            int filled = 0;
            if (i == potionChest) {
                int slot = level.random.nextInt(chest.getContainerSize());
                ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_HEALING);
                chest.setItem(slot, potion);
                filled++;
            }
            while (filled < totalStacks) {
                int slot = level.random.nextInt(chest.getContainerSize());
                if (!chest.getItem(slot).isEmpty()) continue;
                chest.setItem(slot, randomArenaLoot(level));
                filled++;
            }
            chest.setChanged();
        }
    }

    private static ItemStack randomArenaLoot(ServerLevel level) {
        // The mod boss arena is a higher-quality structure: this new item is intentionally more common here.
        if (level.random.nextInt(100) < 8) return new ItemStack(ModItems.HUMANIZING_INJECTION.get());
        // Rare loot is intentionally doubled here: a 5% baseline becomes 10% on this arena.
        if (level.random.nextInt(100) < 10) {
            return switch (level.random.nextInt(10)) {
                case 0 -> new ItemStack(ModItems.QUINQUE_YUKIMURA_1_3.get());
                case 1 -> new ItemStack(ModItems.HALF_GHOUL_ORGAN.get());
                case 2 -> new ItemStack(ModItems.RC_INJECTOR.get());
                case 3 -> new ItemStack(ModItems.HUMANIZING_INJECTION.get());
                case 4 -> new ItemStack(ModItems.KANEKI_MASK.get());
                case 5 -> new ItemStack(ModItems.GHOUL_MASK.get());
                case 6 -> new ItemStack(ModItems.QUINQUE_13S_JASON.get());
                case 7 -> new ItemStack(ModItems.QUINQUE_NARUKAMI.get());
                case 8 -> new ItemStack(ModItems.QUINQUE_IXA.get());
                default -> new ItemStack(ModItems.INVESTIGATOR_COAT.get());
            };
        }
        return switch (level.random.nextInt(10)) {
            case 0 -> new ItemStack(Items.GOLD_INGOT, 1 + level.random.nextInt(3));
            case 1 -> new ItemStack(Items.IRON_INGOT, 2 + level.random.nextInt(5));
            case 2 -> new ItemStack(Items.DIAMOND, 1 + level.random.nextInt(2));
            case 3 -> new ItemStack(Items.COOKED_BEEF, 2 + level.random.nextInt(5));
            case 4 -> new ItemStack(Items.ARROW, 4 + level.random.nextInt(9));
            case 5 -> new ItemStack(ModItems.RC_CELL.get(), 1 + level.random.nextInt(4));
            case 6 -> new ItemStack(ModItems.SKILL_TOKEN.get(), 1);
            case 7 -> new ItemStack(Items.GOLDEN_APPLE, 1);
            case 8 -> new ItemStack(Items.EXPERIENCE_BOTTLE, 2 + level.random.nextInt(5));
            default -> new ItemStack(ModItems.QUINQUE_SCORPION.get(), 1);
        };
    }

    private static void spawn(ServerLevel level, BlockPos center) {
        GhoulBoss boss = ModEntities.GHOUL_BOSS.get().create(level);
        if (boss == null) return;
        boss.moveTo(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D, level.random.nextFloat() * 360F, 0F);
        level.addFreshEntity(boss);
    }
}
