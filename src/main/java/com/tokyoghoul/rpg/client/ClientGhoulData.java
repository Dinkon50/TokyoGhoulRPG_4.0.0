package com.tokyoghoul.rpg.client;

import com.tokyoghoul.rpg.network.SyncDataPacket;
import com.tokyoghoul.rpg.capability.GhoulData;

public final class ClientGhoulData {
    public record EntityKaguneState(GhoulData.Race race, boolean active, int kaguneType, boolean rageActive) {}
    public record GrabState(java.util.UUID targetId, boolean active) {}
    private static final java.util.Map<java.util.UUID, EntityKaguneState> ENTITY_KAGUNE = new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, GrabState> GRAB_STATES = new java.util.HashMap<>();
    private static int race,points,level,rc,strength,speed,kagune,regen,stealth;
    private static int investigation,quinque,tactics,armor,discipline,quinqueMastery,rage,rageTicks; private static boolean quinqueBleed;
    private static boolean rageUnlocked,kaguneActive;
    private static int kaguneType, hunger, bleedingTicks, comboStage;
    private static boolean kaguneTechnique1, kaguneTechnique2, quinqueTechnique1, quinqueTechnique2;

    public static void setEntityKagune(java.util.UUID id, int raceOrdinal, boolean active, int kaguneType, boolean rageActive) {
        GhoulData.Race[] races = GhoulData.Race.values();
        GhoulData.Race safeRace = raceOrdinal >= 0 && raceOrdinal < races.length ? races[raceOrdinal] : GhoulData.Race.HUMAN;
        ENTITY_KAGUNE.put(id, new EntityKaguneState(safeRace, active, kaguneType, rageActive));
    }
    public static EntityKaguneState entityKagune(java.util.UUID id) { return ENTITY_KAGUNE.get(id); }
    public static void removeEntityKagune(java.util.UUID id) { ENTITY_KAGUNE.remove(id); }
    public static java.util.Set<java.util.UUID> entityKaguneIds() { return java.util.Set.copyOf(ENTITY_KAGUNE.keySet()); }
    public static void setGrabState(java.util.UUID ownerId, java.util.UUID targetId, boolean active) {
        if (active) GRAB_STATES.put(ownerId, new GrabState(targetId, true));
        else {
            GrabState old = GRAB_STATES.get(ownerId);
            GRAB_STATES.put(ownerId, new GrabState(targetId != null ? targetId : old == null ? null : old.targetId(), false));
        }
    }
    public static GrabState grabState(java.util.UUID ownerId) { return GRAB_STATES.get(ownerId); }
    public static void removeGrabState(java.util.UUID ownerId) { GRAB_STATES.remove(ownerId); }
    public static java.util.Set<java.util.UUID> grabStateOwners() { return java.util.Set.copyOf(GRAB_STATES.keySet()); }

    public static void set(SyncDataPacket p){
        race=p.race(); points=p.points(); level=p.level(); rc=p.rc();
        strength=p.strength(); speed=p.speed(); kagune=p.kagune(); regen=p.regen(); stealth=p.stealth();
        investigation=p.investigation(); quinque=p.quinque(); tactics=p.tactics(); armor=p.armor(); discipline=p.discipline(); quinqueMastery=p.quinqueMastery(); quinqueBleed=p.quinqueBleed();
        rage=p.rage(); rageTicks=p.rageTicks(); rageUnlocked=p.rageUnlocked(); kaguneActive=p.kaguneActive();
        kaguneType=p.kaguneType(); hunger=p.hunger(); bleedingTicks=p.bleedingTicks(); kaguneTechnique1=p.kaguneTechnique1(); kaguneTechnique2=p.kaguneTechnique2(); quinqueTechnique1=p.quinqueTechnique1(); quinqueTechnique2=p.quinqueTechnique2();
    }

    public static GhoulData.Race race(){
        GhoulData.Race[] values=GhoulData.Race.values();
        return race>=0 && race<values.length ? values[race] : GhoulData.Race.HUMAN;
    }
    public static int points(){return points;}
    public static int level(){return level;}
    public static int rc(){return rc;}
    public static int rage(){return rage;}
    public static int rageTicks(){return rageTicks;}
    public static boolean rageUnlocked(){return rageUnlocked;}
    public static boolean rageActive(){return rageTicks>0;}
    public static boolean kaguneActive(){return kaguneActive;}
    public static int kaguneType(){return kaguneType;}
    public static int comboStage(){return comboStage;}
    public static void setComboStage(int value){comboStage=Math.max(0,Math.min(2,value));}
    public static int hunger(){return hunger;}
    public static int bleedingTicks(){return bleedingTicks;}
    public static boolean kaguneTechnique1(){return kaguneTechnique1;} public static boolean kaguneTechnique2(){return kaguneTechnique2;}
    public static boolean quinqueTechnique1(){return quinqueTechnique1;} public static boolean quinqueTechnique2(){return quinqueTechnique2;}
    public static int strength(){return strength;}
    public static int speed(){return speed;}
    public static int kagune(){return kagune;}
    public static int regen(){return regen;}
    public static int stealth(){return stealth;}
    public static int investigation(){return investigation;}
    public static int quinque(){return quinque;}
    public static int tactics(){return tactics;}
    public static int armor(){return armor;}
    public static int discipline(){return discipline;}
    public static int quinqueMastery(){return quinqueMastery;} public static boolean quinqueBleed(){return quinqueBleed;}

    private ClientGhoulData(){}
}
