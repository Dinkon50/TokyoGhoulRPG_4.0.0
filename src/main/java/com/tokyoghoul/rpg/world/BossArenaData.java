package com.tokyoghoul.rpg.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class BossArenaData extends SavedData {
    public boolean generated = false;
    public int x;
    public int y;
    public int z;
    public long respawnAt = -1L;

    public BossArenaData() {}

    public static BossArenaData load(CompoundTag tag) {
        BossArenaData d = new BossArenaData();
        d.generated = tag.getBoolean("Generated");
        d.x = tag.getInt("X");
        d.y = tag.getInt("Y");
        d.z = tag.getInt("Z");
        d.respawnAt = tag.getLong("RespawnAt");
        return d;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("Generated", generated);
        tag.putInt("X", x);
        tag.putInt("Y", y);
        tag.putInt("Z", z);
        tag.putLong("RespawnAt", respawnAt);
        return tag;
    }

    public void markDirty() { setDirty(); }
}
