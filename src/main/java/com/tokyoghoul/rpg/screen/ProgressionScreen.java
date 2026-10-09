package com.tokyoghoul.rpg.screen;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.client.ClientGhoulData;
import com.tokyoghoul.rpg.v2.KaguneType;
import com.tokyoghoul.rpg.client.TokyoGuiStyle;
import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.network.UpgradePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.List;

/**
 * Tokyo Ghoul progression UI. Inspired by the branching/page structure of popular RPG skill-tree mods,
 * but all visuals, labels and mechanics are native to this project.
 */
public class ProgressionScreen extends Screen {
    private record Node(String id, String title, String desc, int x, int y, int tier, boolean technique) {}
    private final List<Node> nodes=new ArrayList<>();
    private int page=0, hovered=-1;
    private int left,top,w,h;

    public ProgressionScreen(){super(TokyoGuiStyle.ui("RC // ДРЕВО ПРОКАЧКИ"));}

    @Override protected void init(){
        w=Math.min(960,width-20); h=Math.min(600,height-14); left=(width-w)/2; top=(height-h)/2; buildNodes();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forMusic(TokyoGhoulRPG.SKILL_TREE_TENSION.get()));
    }

    private void buildNodes(){
        nodes.clear();
        GhoulData.Race r=ClientGhoulData.race();
        int cx=left+w/2; int base=top+142;
        int tierGap=Math.min(58,Math.max(34,(h-368)/4));
        if(page==0){
            String[][] b=r==GhoulData.Race.CCG?new String[][]{{"investigation","РАССЛЕДОВАНИЕ","Следы и точность"},{"quinque","КВИНКЕ","Урон и техника"},{"tactics","ТАКТИКА","Контратаки"},{"armor","ЗАЩИТА","Стойкость"},{"discipline","ДИСЦИПЛИНА","Контроль"},{"quinque_mastery","МАСТЕРСТВО","Уровни Квинке"}}:
                r==GhoulData.Race.HUMAN?new String[][]{{"strength","СИЛА","Ближний бой"},{"speed","СКОРОСТЬ","Рывок и уклонение"},{"stealth","СКРЫТНОСТЬ","Засада"},{"regen","ВЫЖИВАНИЕ","Лечение"}}:
                new String[][]{{"kagune","КАГУНЕ","Контроль органа"},{"strength","СИЛА","Удар"},{"speed","СКОРОСТЬ","Подвижность"},{"regen","РЕГЕНЕРАЦИЯ","Восстановление"},{"stealth","СКРЫТНОСТЬ","Контроль боя"}};
            int gap=Math.min(150,(w-120)/Math.max(1,b.length-1)); int start=cx-(b.length-1)*gap/2;
            for(int i=0;i<b.length;i++) for(int tier=1;tier<=5;tier++) nodes.add(new Node(b[i][0],b[i][1]+"  •  "+tier,b[i][2],start+i*gap,base+(tier-1)*tierGap,tier,false));
            if(r==GhoulData.Race.GHOUL||r==GhoulData.Race.HALF_GHOUL){
                nodes.add(new Node("kagune_technique_1",technique1Name(),"Игровой приём по мотивам формы кагуне; это не официальное название из аниме",cx-110,top+h-158,3,true));
                nodes.add(new Node("kagune_technique_2",technique2Name(),"Продвинутый игровой приём • требуется КАГУНЕ 5",cx+110,top+h-158,5,true));
                nodes.add(new Node("rage","ЯРОСТЬ","Предельный режим • 60 секунд",cx,top+h-104,8,true));
            } else if(r==GhoulData.Race.CCG){
                nodes.add(new Node("quinque_technique_1","ТЕХНИКА КВИНКЕ","Усиленный удар выбранной квинке",cx-110,top+h-158,3,true));
                nodes.add(new Node("quinque_technique_2","МАСТЕР-ТЕХНИКА","Продвинутая техника обращения с квинке",cx+110,top+h-158,5,true));
                nodes.add(new Node("rage","БОЕВОЙ ДУХ","Предельный режим CCG",cx,top+h-104,8,true));
            }
        }else{
            // Page 2: dedicated technique tree. It uses the same node language as the first page, but the connections are ability-focused.
            String[] ids=r==GhoulData.Race.CCG?new String[]{"quinque_mastery","quinque_technique_1","quinque_technique_2","quinque_bleed"}:new String[]{"kagune","kagune_technique_1","kagune_technique_2","strength","speed","regen","stealth"};
            int rows=ids.length; int y=base; int rowGap=Math.min(58,Math.max(38,(h-180)/Math.max(1,rows)));
            for(int i=0;i<rows;i++){String id=ids[i]; nodes.add(new Node(id,label(id),description(id),cx,y+i*rowGap,Math.min(8,i+1),id.contains("technique")));}
        }
    }

    private String label(String id){return switch(id){case "kagune"->"КАГУНЕ";case "kagune_technique_1"->technique1Name();case "kagune_technique_2"->technique2Name();case "quinque_mastery"->"МАСТЕРСТВО КВИНКЕ";case "quinque_technique_1"->"ТЕХНИКА КВИНКЕ";case "quinque_technique_2"->"МАСТЕР-ТЕХНИКА";case "strength"->"СИЛА";case "speed"->"СКОРОСТЬ";case "regen"->"РЕГЕНЕРАЦИЯ";case "stealth"->"СКРЫТНОСТЬ";case "quinque_bleed"->"КРОВОТЕЧЕНИЕ";default->id.toUpperCase();};}
    private String description(String id){return switch(id){case "kagune_technique_1"->"Специальный приём выбранного кагуне";case "kagune_technique_2"->"Продвинутая форма контроля кагуне";case "quinque_technique_1"->"Заряженный удар квинке";case "quinque_technique_2"->"Продвинутая техника обращения с квинке";case "quinque_mastery"->"Повышает эффективность квинке CCG";default->"Пассивное усиление профиля";};}
    private String technique1Name(){return switch(kaguneType()){
        case KANEKI_RINKAKU -> "РИНКАКУ: ПРОКОЛ ЩУПАЛЬЦАМИ";
        case TAKIZAWA_UKAKU -> "УКАКУ: ЗАЛП RC-ОСКОЛКАМИ";
        case RIZE_RINKAKU -> "РИНКАКУ: СЕРИЯ УДАРОВ";
        case TOUKA_UKAKU -> "УКАКУ: ЗАЛП RC-ОСКОЛКАМИ";
        case AYATO_UKAKU -> "АЯТО: КРИСТАЛЛИЧЕСКИЙ ЗАЛП";
        case TSUKIYAMA_KOUKAKU -> "КОУКАКУ: ТЯЖЁЛЫЙ УДАР";
        case NISHIKI_BIKAKU -> "БИКАКУ: УДАР ХВОСТОМ";
        case NORO_BIKAKU -> "НЕУТОЧНЁННЫЙ ТИП: МАССИВНЫЙ УДАР";
        default -> "ТЕХНИКА КАГУНЕ";};}
    private String technique2Name(){return switch(kaguneType()){
        case KANEKI_RINKAKU -> "РИНКАКУ: УДЛИНЁННЫЙ УДАР";
        case TAKIZAWA_UKAKU -> "УКАКУ: РАСШИРЕННЫЙ ЗАЛП";
        case RIZE_RINKAKU -> "РИНКАКУ: УСИЛЕННЫЙ ПРОКОЛ";
        case TOUKA_UKAKU -> "УКАКУ: ВОЗДУШНЫЙ ЗАЛП";
        case AYATO_UKAKU -> "УКАКУ: СЕРИЯ УДАРОВ";
        case TSUKIYAMA_KOUKAKU -> "КОУКАКУ: ПРОБИВАЮЩИЙ УДАР";
        case NISHIKI_BIKAKU -> "БИКАКУ: КОНТРАТАКА ХВОСТОМ";
        case NORO_BIKAKU -> "НЕУТОЧНЁННЫЙ ТИП: ТЯЖЁЛЫЙ УДАР";
        default -> "ПРОДВИНУТАЯ ТЕХНИКА";};}


    private KaguneType kaguneType(){
        KaguneType[] values = KaguneType.values();
        int index = ClientGhoulData.kaguneType();
        return index >= 0 && index < values.length ? values[index] : KaguneType.NONE;
    }

    private int value(String s){return switch(s){case "strength"->ClientGhoulData.strength();case "speed"->ClientGhoulData.speed();case "kagune"->ClientGhoulData.kagune();case "regen"->ClientGhoulData.regen();case "stealth"->ClientGhoulData.stealth();case "investigation"->ClientGhoulData.investigation();case "quinque"->ClientGhoulData.quinque();case "tactics"->ClientGhoulData.tactics();case "armor"->ClientGhoulData.armor();case "discipline"->ClientGhoulData.discipline();case "quinque_mastery"->ClientGhoulData.quinqueMastery();case "quinque_bleed"->ClientGhoulData.quinqueBleed()?1:0;default->0;};}
    private boolean active(Node n){return switch(n.id){case "rage"->ClientGhoulData.rageUnlocked();case "kagune_technique_1"->ClientGhoulData.kaguneTechnique1();case "kagune_technique_2"->ClientGhoulData.kaguneTechnique2();case "quinque_technique_1"->ClientGhoulData.quinqueTechnique1();case "quinque_technique_2"->ClientGhoulData.quinqueTechnique2();default->value(n.id)>=n.tier;};}
    private boolean available(Node n){
        if(n.id.equals("rage")){int core=Math.max(Math.max(ClientGhoulData.strength(),ClientGhoulData.speed()),ClientGhoulData.kagune());return !ClientGhoulData.rageUnlocked()&&ClientGhoulData.points()>=5&&ClientGhoulData.level()>=8&&core>=3;}
        if(n.id.equals("kagune_technique_1")) return !ClientGhoulData.kaguneTechnique1()&&ClientGhoulData.kagune()>=3&&ClientGhoulData.points()>=2;
        if(n.id.equals("kagune_technique_2")) return !ClientGhoulData.kaguneTechnique2()&&ClientGhoulData.kagune()>=5&&ClientGhoulData.kaguneTechnique1()&&ClientGhoulData.points()>=2;
        if(n.id.equals("quinque_technique_1")) return !ClientGhoulData.quinqueTechnique1()&&ClientGhoulData.quinqueMastery()>=3&&ClientGhoulData.points()>=2;
        if(n.id.equals("quinque_technique_2")) return !ClientGhoulData.quinqueTechnique2()&&ClientGhoulData.quinqueMastery()>=5&&ClientGhoulData.quinqueTechnique1()&&ClientGhoulData.points()>=2;
        if(n.id.startsWith("quinque_")&& !n.id.equals("quinque_mastery")) return value(n.id)==0&&ClientGhoulData.quinqueMastery()>=3&&ClientGhoulData.points()>0;
        return value(n.id)==n.tier-1&&ClientGhoulData.points()>0;
    }
    private void learn(Node n){if(available(n))NetworkHandler.CHANNEL.sendToServer(new UpgradePacket(n.id));}

    @Override public void render(GuiGraphics g,int mx,int my,float pt){
        TokyoGuiStyle.background(g,width,height); int accent=ClientGhoulData.race()==GhoulData.Race.CCG?TokyoGuiStyle.CCG:TokyoGuiStyle.RED;
        TokyoGuiStyle.panel(g,left,top,w,h,accent);
        TokyoGuiStyle.title(g,font,"RC // ДРЕВО ПРОКАЧКИ",raceName()+"  •  уровень "+ClientGhoulData.level(),width/2,top+14);
        // page tabs
        tab(g,left+18,top+58,180,"ХАРАКТЕРИСТИКИ",page==0,mx,my);
        tab(g,left+204,top+58,180,"ТЕХНИКИ",page==1,mx,my);
        g.fill(left+18,top+101,left+w-18,top+102,0xFF2A3038);
        g.drawString(font,TokyoGuiStyle.ui("ОЧКИ  "+ClientGhoulData.points()),left+28,top+112,TokyoGuiStyle.GOLD,true);
        g.drawString(font,TokyoGuiStyle.ui("RC  "+ClientGhoulData.rc()),left+145,top+112,TokyoGuiStyle.STEEL,false);
        drawConnections(g);
        for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);boolean a=active(n),ok=available(n),hot=mx>=n.x-22&&mx<=n.x+22&&my>=n.y-22&&my<=n.y+22; if(hot)hovered=i; drawNode(g,n,a,ok,hot);}
        drawInfo(g); super.render(g,mx,my,pt);
    }
    private void tab(GuiGraphics g,int x,int y,int tw,String text,boolean selected,int mx,int my){
        boolean hot=mx>=x&&mx<=x+tw&&my>=y&&my<=y+30;
        g.fill(x,y,x+tw,y+30,selected?0xDD211018:0xBB0B0F14);
        g.fill(x,y,x+tw,y+2,selected?TokyoGuiStyle.RED_BRIGHT:TokyoGuiStyle.LINE);
        g.drawCenteredString(font,TokyoGuiStyle.ui(text),x+tw/2,y+10,hot?TokyoGuiStyle.WHITE:TokyoGuiStyle.STEEL);
    }
    private void drawConnections(GuiGraphics g){for(int i=0;i<nodes.size();i++)for(int j=i+1;j<nodes.size();j++){Node a=nodes.get(i),b=nodes.get(j);if(a.id.equals(b.id)&&b.tier==a.tier+1)line(g,a.x,a.y+19,b.x,b.y-19,active(b)?0xFF7E2438:0xFF252C35);}}
    private void line(GuiGraphics g,int x1,int y1,int x2,int y2,int c){int steps=Math.max(Math.abs(x2-x1),Math.abs(y2-y1));for(int i=0;i<=steps;i++){int x=x1+(x2-x1)*i/Math.max(1,steps),y=y1+(y2-y1)*i/Math.max(1,steps);g.fill(x,y,x+2,y+2,c);}}
    private void drawNode(GuiGraphics g,Node n,boolean a,boolean ok,boolean hot){int r=n.technique?22:16;int border=a?TokyoGuiStyle.RED_BRIGHT:ok?TokyoGuiStyle.GOLD:0xFF39414B;g.fill(n.x-r-3,n.y-r-3,n.x+r+3,n.y+r+3,0xFF050608);g.fill(n.x-r,n.y-r,n.x+r,n.y+r,border);g.fill(n.x-r+2,n.y-r+2,n.x+r-2,n.y+r-2,a?0xFF301019:0xFF10151B);String icon=n.id.contains("technique")?"✦":n.id.equals("rage")?"R":"•";g.drawCenteredString(font,icon,n.x,n.y-4,a?TokyoGuiStyle.WHITE:TokyoGuiStyle.STEEL);if(hot){g.fill(n.x-r-5,n.y-r-5,n.x+r+5,n.y-r-3,TokyoGuiStyle.WHITE);}}
    private void drawInfo(GuiGraphics g){int x=left+18,y=top+h-68,ww=w-36;g.fill(x,y,x+ww,y+52,0xD30A0E13);if(hovered>=0&&hovered<nodes.size()){Node n=nodes.get(hovered);g.drawString(font,TokyoGuiStyle.ui(n.title),x+12,y+9,TokyoGuiStyle.WHITE,true);g.drawString(font,TokyoGuiStyle.ui(n.desc),x+12,y+26,TokyoGuiStyle.STEEL,false);g.drawString(font,TokyoGuiStyle.ui(available(n)?"Можно изучить • клик":active(n)?"Изучено":"Требуется предыдущий узел"),x+12,y+40,available(n)?TokyoGuiStyle.GOLD:TokyoGuiStyle.STEEL,false);}else g.drawString(font,TokyoGuiStyle.ui("Наведите на узел, чтобы увидеть эффект и требования."),x+12,y+18,TokyoGuiStyle.STEEL,false);}
    private String raceName(){return switch(ClientGhoulData.race()){case GHOUL->"ГУЛЬ";case HALF_GHOUL->"ПОЛУГУЛЬ";case CCG->"CCG";default->"ЧЕЛОВЕК";};}
    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0)return super.mouseClicked(mx,my,button);if(my>=top+58&&my<=top+88){if(mx>=left+18&&mx<=left+198){page=0;buildNodes();return true;}if(mx>=left+204&&mx<=left+384){page=1;buildNodes();return true;}}for(Node n:nodes)if(mx>=n.x-24&&mx<=n.x+24&&my>=n.y-24&&my<=n.y+24){learn(n);return true;}return super.mouseClicked(mx,my,button);}
    @Override public void onClose(){Minecraft.getInstance().getSoundManager().stop(TokyoGhoulRPG.SKILL_TREE_TENSION.get().getLocation(),SoundSource.MUSIC);super.onClose();}
}
