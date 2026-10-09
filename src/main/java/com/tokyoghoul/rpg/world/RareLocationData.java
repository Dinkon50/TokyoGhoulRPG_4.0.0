package com.tokyoghoul.rpg.world;
import net.minecraft.nbt.CompoundTag; import net.minecraft.world.level.saveddata.SavedData;
public class RareLocationData extends SavedData {
 public int generated=0; public int[] xs=new int[3],ys=new int[3],zs=new int[3];
 public static RareLocationData load(CompoundTag t){RareLocationData d=new RareLocationData();d.generated=t.getInt("Generated");for(int i=0;i<3;i++){d.xs[i]=t.getInt("X"+i);d.ys[i]=t.getInt("Y"+i);d.zs[i]=t.getInt("Z"+i);}return d;}
 @Override public CompoundTag save(CompoundTag t){t.putInt("Generated",generated);for(int i=0;i<3;i++){t.putInt("X"+i,xs[i]);t.putInt("Y"+i,ys[i]);t.putInt("Z"+i,zs[i]);}return t;}
}
