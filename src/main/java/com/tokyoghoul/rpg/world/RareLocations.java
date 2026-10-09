package com.tokyoghoul.rpg.world;
import com.tokyoghoul.rpg.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.tokyoghoul.rpg.init.ModItems;
import java.util.*;
public final class RareLocations {
 private static final String ID="tokyoghoulrpg_rare_locations"; private RareLocations(){}
 private static RareLocationData data(ServerLevel l){return l.getDataStorage().computeIfAbsent(RareLocationData::load,RareLocationData::new,ID);}
 public static void tick(ServerLevel l){if(l.getGameTime()%2400L!=0L)return;RareLocationData d=data(l);if(d.generated>=3||l.players().isEmpty()||l.random.nextInt(180)!=0)return;ServerPlayer base=l.players().get(l.random.nextInt(l.players().size()));double a=l.random.nextDouble()*Math.PI*2,dist=220+l.random.nextDouble()*220;int x=base.blockPosition().getX()+(int)(Math.cos(a)*dist),z=base.blockPosition().getZ()+(int)(Math.sin(a)*dist),y=l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);if(y<l.getMinBuildHeight()+4)return;BlockPos c=new BlockPos(x,y,z);for(int i=0;i<d.generated;i++)if(new BlockPos(d.xs[i],d.ys[i],d.zs[i]).distSqr(c)<90*90)return;build(l,c,d.generated);d.xs[d.generated]=x;d.ys[d.generated]=y;d.zs[d.generated]=z;d.generated++;d.setDirty();base.sendSystemMessage(net.minecraft.network.chat.Component.literal("§8[РЕДКАЯ ЛОКАЦИЯ] §cОбнаружена новая территория: "+name(d.generated-1)));}
 private static String name(int t){return switch(t){case 0->"Архив CCG";case 1->"Заброшенная палата Гулей";default->"Подпольная кузница Квинке";};}
 private static void build(ServerLevel l,BlockPos c,int t){int r=10;for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){double q=Math.sqrt(dx*dx+dz*dz);if(q<=r)l.setBlock(c.offset(dx,-1,dz),t==1?Blocks.DEEPSLATE_BRICKS.defaultBlockState():Blocks.POLISHED_BLACKSTONE.defaultBlockState(),3);if(q<=r-1)l.setBlock(c.offset(dx,0,dz),Blocks.AIR.defaultBlockState(),3);}for(int dx=-r;dx<=r;dx+=5)for(int dz=-r;dz<=r;dz+=5){if(Math.abs(dx)==r||Math.abs(dz)==r){l.setBlock(c.offset(dx,1,dz),t==0?Blocks.IRON_BLOCK.defaultBlockState():Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),3);l.setBlock(c.offset(dx,2,dz),t==0?Blocks.IRON_BARS.defaultBlockState():Blocks.SOUL_LANTERN.defaultBlockState(),3);}}
  if(t==0){spawn(l,ModEntities.CCG_QUARTERMASTER.get(),c.offset(-4,0,-3));spawn(l,ModEntities.CCG_HEAVY.get(),c.offset(4,0,-3));spawn(l,ModEntities.CCG_SNIPER.get(),c.offset(0,0,5));chest(l,c.offset(-6,1,5));chest(l,c.offset(6,1,5));}
  else if(t==1){spawn(l,ModEntities.GHOUL_SCAVENGER.get(),c.offset(-3,0,0));spawn(l,ModEntities.GHOUL_MEDIC.get(),c.offset(3,0,0));chest(l,c.offset(0,1,-6));chest(l,c.offset(0,1,6));}
  else {spawn(l,ModEntities.CCG_HEAVY.get(),c.offset(-4,0,0));spawn(l,ModEntities.GHOUL_SCAVENGER.get(),c.offset(4,0,0));chest(l,c.offset(-6,1,-5));chest(l,c.offset(6,1,-5));chest(l,c.offset(0,1,7));}
 }
 private static void spawn(ServerLevel l,net.minecraft.world.entity.EntityType<?> t,BlockPos p){Entity e=t.create(l);if(e!=null){e.moveTo(p.getX()+.5,p.getY()+1,p.getZ()+.5,0,0);l.addFreshEntity(e);}}
 private static void chest(ServerLevel l,BlockPos p){l.setBlock(p,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,net.minecraft.core.Direction.NORTH),3);if(l.getBlockEntity(p)instanceof ChestBlockEntity c){int n=2+l.random.nextInt(4);for(int i=0;i<n;i++){ItemStack s=switch(l.random.nextInt(10)){case 0->new ItemStack(ModItems.QUINQUE_IXA.get());case 1->new ItemStack(ModItems.QUINQUE_NARUKAMI.get());case 2->new ItemStack(ModItems.QUINQUE_SCORPION.get());case 3->new ItemStack(ModItems.RC_CELL.get(),1+l.random.nextInt(3));case 4->new ItemStack(Items.DIAMOND);case 5->new ItemStack(Items.GOLD_INGOT,2);default->new ItemStack(Items.IRON_INGOT,2+l.random.nextInt(4));};int slot=l.random.nextInt(c.getContainerSize());if(c.getItem(slot).isEmpty())c.setItem(slot,s);else i--;}c.setChanged();}}
}
