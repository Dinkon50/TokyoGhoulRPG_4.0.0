package com.tokyoghoul.rpg.event;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.item.QuinqueItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber public final class QuinqueEvents {
 private static boolean q(ItemStack s){return s.getItem() instanceof QuinqueItem;}
 @SubscribeEvent public static void attack(AttackEntityEvent e){if(q(e.getEntity().getMainHandItem())&&GhoulData.get(e.getEntity()).map(d->d.getRace()==GhoulData.Race.GHOUL||d.getRace()==GhoulData.Race.HALF_GHOUL).orElse(false))e.setCanceled(true);}
 @SubscribeEvent public static void block(PlayerInteractEvent.LeftClickBlock e){if(q(e.getEntity().getMainHandItem())&&GhoulData.get(e.getEntity()).map(d->d.getRace()==GhoulData.Race.GHOUL||d.getRace()==GhoulData.Race.HALF_GHOUL).orElse(false))e.setCanceled(true);}
 @SubscribeEvent public static void hurt(LivingHurtEvent e){if(!(e.getSource().getEntity() instanceof Player p)||!q(p.getMainHandItem()))return;GhoulData.get(p).ifPresent(d->{if(d.getRace()==GhoulData.Race.GHOUL||d.getRace()==GhoulData.Race.HALF_GHOUL)e.setCanceled(true);else if(d.getRace()==GhoulData.Race.HUMAN)e.setAmount(e.getAmount()*.45f);else if(d.getRace()==GhoulData.Race.CCG){float m=d.getQuinqueMastery();e.setAmount(e.getAmount()*(1f+m*.035f));if(m>=2&&d.quinqueBleed()&&p.getRandom().nextFloat()<.30f)e.getEntity().getPersistentData().putInt("TG_QuinqueBleed",60+(int)m*10);}});}
}
