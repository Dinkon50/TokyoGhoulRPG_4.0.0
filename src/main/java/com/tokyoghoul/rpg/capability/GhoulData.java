package com.tokyoghoul.rpg.capability;

import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.network.SyncDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import com.tokyoghoul.rpg.v2.KaguneType;
import com.tokyoghoul.rpg.v2.V2Constants;
import com.tokyoghoul.rpg.v2.CriticalState;

import java.util.Optional;

public class GhoulData {
    public enum Race { HUMAN, GHOUL, HALF_GHOUL, CCG }

    private Race race = Race.HUMAN;
    private KaguneType kaguneType = KaguneType.NONE;
    private int hunger = 100;
    private int bleedingTicks = 0;
    private int points = 0, level = 1, rc = 0;
    private int strength = 1, speed = 1, kagune = 1, regen = 1, stealth = 1;
    private int investigation = 1, quinque = 1, tactics = 1, armor = 1, discipline = 1, quinqueMastery = 1, quinqueBleed = 0;
    private int rage = 0, rageTicks = 0, abilityCooldown = 0;
    private boolean originChosen = false, kaguneActive = false, rageUnlocked = false;
    private boolean kaguneTechnique1 = false, kaguneTechnique2 = false, quinqueTechnique1 = false, quinqueTechnique2 = false;

    public Race getRace(){ return race; }
    public void setRace(Race r){ race = r; }
    public int getPoints(){ return points; }
    public int getLevel(){ return level; }
    public boolean isOriginChosen(){ return originChosen; }
    public KaguneType getKaguneType(){ return kaguneType; }
    public int getHunger(){ return hunger; }
    public void setHunger(int value){ hunger = Math.max(0, Math.min(100, value)); }
    public int getBleedingTicks(){ return bleedingTicks; }
    public CriticalState getCriticalState(){
        if(bleedingTicks > 0) return CriticalState.BLEEDING;
        return CriticalState.NORMAL;
    }
    public void setBleedingTicks(int ticks){ bleedingTicks = Math.max(0, ticks); }
    public boolean isKaguneActive(){ return kaguneActive; }
    public void setKaguneActive(boolean v){ kaguneActive = v; }
    public boolean isRageUnlocked(){ return rageUnlocked; }
    public boolean isRageActive(){ return rageTicks > 0; }
    public int getRage(){ return rage; }
    public int getRageTicks(){ return rageTicks; }
    public int getAbilityCooldown(){ return abilityCooldown; }
    public boolean kaguneTechnique1(){ return kaguneTechnique1; }
    public boolean kaguneTechnique2(){ return kaguneTechnique2; }
    public boolean quinqueTechnique1(){ return quinqueTechnique1; }
    public boolean quinqueTechnique2(){ return quinqueTechnique2; }

    public void choose(Race r){ choose(r, KaguneType.NONE); }

    public void choose(Race r, KaguneType type){
        race = r;
        kaguneType = (r == Race.GHOUL || r == Race.HALF_GHOUL) ? type : KaguneType.NONE;
        originChosen = true;
        points = r == Race.CCG ? Math.max(points, V2Constants.CCG_START_POINTS) : 0;
        if(r == Race.GHOUL) rc = Math.max(rc, 25);
        if(r == Race.HALF_GHOUL) rc = Math.max(rc, 40);
        if(r == Race.CCG) rc = Math.max(rc, 10);
        kaguneActive = false;
        hunger = 100;
        bleedingTicks = 0;
    }

    public void addPoints(int n){ points = Math.min(45, points + Math.max(0, n)); }

    public boolean upgrade(String stat){
        if(stat.startsWith("kagune_technique") || stat.startsWith("quinque_technique")){
            if(points < 2) return false;
            boolean ghoul = race == Race.GHOUL || race == Race.HALF_GHOUL;
            if(stat.startsWith("kagune_") && !ghoul) return false;
            if(stat.startsWith("quinque_") && race != Race.CCG) return false;
            switch(stat){
                case "kagune_technique_1" -> { if(kaguneTechnique1 || kagune < 3) return false; kaguneTechnique1 = true; }
                case "kagune_technique_2" -> { if(kaguneTechnique2 || kagune < 5) return false; kaguneTechnique2 = true; }
                case "quinque_technique_1" -> { if(quinqueTechnique1 || quinqueMastery < 3) return false; quinqueTechnique1 = true; }
                case "quinque_technique_2" -> { if(quinqueTechnique2 || quinqueMastery < 5) return false; quinqueTechnique2 = true; }
                default -> { return false; }
            }
            points -= 2;
            return true;
        }

        if("rage".equals(stat)){
            int cost = race == Race.CCG ? 4 : 5;
            if((race == Race.HUMAN) || rageUnlocked || points < cost) return false;
            rageUnlocked = true;
            points -= cost;
            return true;
        }

        if(points <= 0 || level >= V2Constants.MAX_LEVEL) return false;

        if(race == Race.CCG){
            if (stat.startsWith("quinque_") && !"quinque_mastery".equals(stat)) {
                if (quinqueMastery < 3 || points <= 0) return false;
                switch(stat){case "quinque_bleed" -> {if(quinqueBleed>0)return false;quinqueBleed=1;} default -> {return false;}}
                points--; return true;
            }
            if ("quinque_mastery".equals(stat)) {
                if (quinqueMastery >= 8) return false;
                quinqueMastery++; points--;
                level = Math.min(V2Constants.MAX_LEVEL, 1 + (investigation + quinque + tactics + armor + discipline + quinqueMastery - 6) / 3);
                return true;
            }
            switch(stat){
                case "investigation" -> { if(investigation >= V2Constants.MAX_STAT) return false; investigation++; }
                case "quinque" -> { if(quinque >= V2Constants.MAX_STAT) return false; quinque++; }
                case "tactics" -> { if(tactics >= V2Constants.MAX_STAT) return false; tactics++; }
                case "armor" -> { if(armor >= V2Constants.MAX_STAT) return false; armor++; }
                case "discipline" -> { if(discipline >= V2Constants.MAX_STAT) return false; discipline++; }
                default -> { return false; }
            }
            points--;
            level = Math.min(V2Constants.MAX_LEVEL, 1 + (investigation + quinque + tactics + armor + discipline - 5) / 3);
            return true;
        }

        switch(stat){
            case "strength" -> { if(strength >= V2Constants.MAX_STAT) return false; strength++; }
            case "speed" -> { if(speed >= V2Constants.MAX_STAT) return false; speed++; }
            case "kagune" -> { if(kagune >= V2Constants.MAX_STAT) return false; kagune++; }
            case "regen" -> { if(regen >= V2Constants.MAX_STAT) return false; regen++; }
            case "stealth" -> { if(stealth >= V2Constants.MAX_STAT) return false; stealth++; }
            default -> { return false; }
        }
        points--;
        level = Math.min(V2Constants.MAX_LEVEL, 1 + (strength + speed + kagune + regen + stealth - 5) / 3);
        return true;
    }

    public void resetProgress(){
        race = Race.HUMAN;
        kaguneType = KaguneType.NONE;
        hunger = 100; bleedingTicks = 0; points = 0; level = 1; rc = 0;
        strength = speed = kagune = regen = stealth = 1;
        investigation = quinque = tactics = armor = discipline = quinqueMastery = 1; quinqueBleed = 0;
        rage = rageTicks = abilityCooldown = 0;
        originChosen = false; kaguneActive = false; rageUnlocked = false; kaguneTechnique1 = kaguneTechnique2 = quinqueTechnique1 = quinqueTechnique2 = false;
    }

    public int getStrength(){ return strength; }
    public int getSpeed(){ return speed; }
    public int getKagune(){ return kagune; }
    public int getRegen(){ return regen; }
    public int getStealth(){ return stealth; }
    public int getRC(){ return rc; }
    public int getInvestigation(){ return investigation; }
    public int getQuinque(){ return quinque; }
    public int getTactics(){ return tactics; }
    public int getArmor(){ return armor; }
    public int getDiscipline(){ return discipline; }
    public int getQuinqueMastery(){ return quinqueMastery; }
    public boolean quinqueBleed(){return quinqueBleed>0;}

    public void addRC(int n){ rc = Math.max(0, rc + n); }

    public void addRage(int n){
        if(rageTicks > 0) return;
        rage = Math.min(100, Math.max(0, rage + n));
    }

    public boolean activateRage(){
        if(!rageUnlocked || rage < 100 || rageTicks > 0 || race == Race.HUMAN) return false;
        rage = 0;
        rageTicks = race == Race.CCG ? 20 * 30 : 20 * 60;
        return true;
    }

    public void tick(){
        if(rageTicks > 0) rageTicks--;
        if(bleedingTicks > 0) bleedingTicks--;
        if(abilityCooldown > 0) abilityCooldown--;
    }

    public boolean useAbility(){
        if(abilityCooldown > 0 || race == Race.HUMAN) return false;
        abilityCooldown = 20;
        return true;
    }

    public void setAbilityCooldown(int ticks){ abilityCooldown = Math.max(abilityCooldown, ticks); }

    public void save(CompoundTag t){
        t.putString("Race", race.name());
        t.putString("KaguneType", kaguneType.name());
        t.putInt("Hunger", hunger);
        t.putInt("BleedingTicks", bleedingTicks);
        t.putInt("Points", points);
        t.putInt("Level", level);
        t.putInt("RC", rc);
        t.putInt("Strength", strength);
        t.putInt("Speed", speed);
        t.putInt("Kagune", kagune);
        t.putInt("Regen", regen);
        t.putInt("Stealth", stealth);
        t.putInt("Investigation", investigation);
        t.putInt("Quinque", quinque);
        t.putInt("Tactics", tactics);
        t.putInt("Armor", armor);
        t.putInt("Discipline", discipline);
        t.putInt("QuinqueMastery", quinqueMastery); t.putBoolean("QuinqueBleed",quinqueBleed>0);
        t.putInt("Rage", rage);
        t.putInt("RageTicks", rageTicks);
        t.putInt("AbilityCooldown", abilityCooldown);
        t.putBoolean("OriginChosen", originChosen);
        t.putBoolean("KaguneActive", kaguneActive);
        t.putBoolean("RageUnlocked", rageUnlocked);
        t.putBoolean("KaguneTechnique1", kaguneTechnique1); t.putBoolean("KaguneTechnique2", kaguneTechnique2);
        t.putBoolean("QuinqueTechnique1", quinqueTechnique1); t.putBoolean("QuinqueTechnique2", quinqueTechnique2);
    }

    public void load(CompoundTag t){
        try { race = Race.valueOf(t.getString("Race")); } catch(Exception ignored) {}
        try { kaguneType = KaguneType.valueOf(t.getString("KaguneType")); } catch(Exception ignored) { kaguneType = KaguneType.NONE; }
        hunger = Math.max(0, Math.min(100, t.contains("Hunger") ? t.getInt("Hunger") : 100));
        bleedingTicks = Math.max(0, t.getInt("BleedingTicks"));
        points = t.getInt("Points");
        level = Math.max(1, t.getInt("Level"));
        rc = Math.max(0, t.getInt("RC"));
        strength = Math.max(1, t.getInt("Strength"));
        speed = Math.max(1, t.getInt("Speed"));
        kagune = Math.max(1, t.getInt("Kagune"));
        regen = Math.max(1, t.getInt("Regen"));
        stealth = Math.max(1, t.getInt("Stealth"));
        investigation = Math.max(1, t.getInt("Investigation"));
        quinque = Math.max(1, t.getInt("Quinque"));
        tactics = Math.max(1, t.getInt("Tactics"));
        armor = Math.max(1, t.getInt("Armor"));
        discipline = Math.max(1, t.getInt("Discipline"));
        quinqueMastery = Math.max(1, Math.min(8, t.contains("QuinqueMastery") ? t.getInt("QuinqueMastery") : 1)); quinqueBleed=t.getBoolean("QuinqueBleed")?1:0;
        rage = Math.max(0, Math.min(100, t.getInt("Rage")));
        rageTicks = Math.max(0, t.getInt("RageTicks"));
        abilityCooldown = Math.max(0, t.getInt("AbilityCooldown"));
        originChosen = t.getBoolean("OriginChosen");
        kaguneActive = t.getBoolean("KaguneActive");
        rageUnlocked = t.getBoolean("RageUnlocked");
        kaguneTechnique1 = t.getBoolean("KaguneTechnique1"); kaguneTechnique2 = t.getBoolean("KaguneTechnique2");
        quinqueTechnique1 = t.getBoolean("QuinqueTechnique1"); quinqueTechnique2 = t.getBoolean("QuinqueTechnique2");
    }

    public static Optional<GhoulData> get(Player p){
        return Optional.of(p.getPersistentData()).map(n -> {
            GhoulData d = new GhoulData();
            d.load(n);
            return d;
        });
    }

    public void sync(Player p){
        save(p.getPersistentData());
        if(p instanceof ServerPlayer sp){
            NetworkHandler.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                new SyncDataPacket(this)
            );
            NetworkHandler.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sp),
                new com.tokyoghoul.rpg.network.SyncEntityKagunePacket(sp.getUUID(), race.ordinal(), kaguneActive, kaguneType.ordinal(), rageActive)
            );
        }
    }
}
