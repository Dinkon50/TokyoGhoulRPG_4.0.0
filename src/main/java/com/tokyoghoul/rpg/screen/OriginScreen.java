package com.tokyoghoul.rpg.screen;

import com.tokyoghoul.rpg.client.TokyoGuiStyle;
import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.network.OriginPacket;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class OriginScreen extends Screen {
    private int stage = 0;
    private int selectedRace = 0;
    private KaguneType selectedKagune = KaguneType.NONE;
    private final List<Button> themedButtons = new ArrayList<>();

    public OriginScreen() { super(TokyoGuiStyle.ui("RC // ПРОИСХОЖДЕНИЕ")); }

    @Override protected void init() { rebuild(); }

    private void rebuild() {
        clearWidgets();
        themedButtons.clear();
        int cx = width / 2;
        int panelH = Math.min(stage == 0 ? 330 : 500, height - 18);
        int panelTop = (height - panelH) / 2;
        if (stage == 0) {
            int y = panelTop + 100;
            addButton(cx - 206, y, 198, 66, "ГУЛЬ", "RC-КЛЕТКИ  •  КАГУНЕ", 1, () -> chooseRace(1));
            addButton(cx + 8, y, 198, 66, "ПОЛУГУЛЬ", "ГИБРИД  •  КАГУНЕ", 2, () -> chooseRace(2));
            addButton(cx - 206, y + 78, 198, 66, "ЧЕЛОВЕК", "ТЕХНИКА  •  ВЫЖИВАНИЕ", 0, () -> chooseRace(0));
            addButton(cx + 8, y + 78, 198, 66, "CCG", "КВИНКЕ  •  СЛУЖБА", 3, () -> chooseRace(3));
        } else {
            KaguneType[] types = kaguneTypes();
            int y = panelTop + 78;
            for (KaguneType type : types) {
                KaguneType picked = type;
                addButton(cx - 205, y, 410, 34, type.displayName(), type.description(), 1, () -> { selectedKagune = picked; finish(); });
                y += 46;
            }
            addButton(cx - 205, panelTop + panelH - 34, 410, 30, "← НАЗАД", "Вернуться к выбору стороны", 0, () -> { stage = 0; rebuild(); });
        }
    }

    private void addButton(int x, int y, int w, int h, String title, String sub, int accentMode, Runnable action) {
        Button b = Button.builder(Component.empty(), ignored -> action.run()).bounds(x, y, w, h).build();
        b.setAlpha(0.0F);
        themedButtons.add(b);
        addRenderableWidget(b);
    }

    private void chooseRace(int race) {
        selectedRace = race;
        if (race == 1 || race == 2) { stage = 1; rebuild(); }
        else { selectedKagune = KaguneType.NONE; finish(); }
    }

    private void finish() {
        NetworkHandler.CHANNEL.sendToServer(new OriginPacket(selectedRace, selectedKagune.ordinal()));
        onClose();
    }

    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        TokyoGuiStyle.background(g, width, height);
        int w = Math.min(660, width - 28);
        int h = Math.min(stage == 0 ? 330 : 390, height - 28);
        int left = (width - w) / 2, top = (height - h) / 2;
        int accent = selectedRace == 3 ? TokyoGuiStyle.CCG : TokyoGuiStyle.RED;
        TokyoGuiStyle.panel(g, left, top, w, h, accent);
        TokyoGuiStyle.cornerMark(g, left + 12, top + 12, 22, accent);
        TokyoGuiStyle.cornerMark(g, left + w - 34, top + 12, 22, accent);

        if (stage == 0) {
            TokyoGuiStyle.title(g, font, "TOKYO GHOUL // ORIGIN", "Выбери сторону. Это определит твою систему развития.", width / 2, top + 20);
            g.drawCenteredString(font, TokyoGuiStyle.ui("РЕШЕНИЕ НЕ ОБРАТИМО БЕЗ ИНЪЕКЦИИ ОЧИЩЕНИЯ"), width / 2, top + 66, TokyoGuiStyle.GOLD);
            drawCards(g, top + 100);
        } else {
            String race = selectedRace == 2 ? "ПОЛУГУЛЬ" : "ГУЛЬ";
            TokyoGuiStyle.title(g, font, "RC ORGAN // KAGUNE", race + "  •  выбери боевой профиль", width / 2, top + 20);
            g.drawCenteredString(font, TokyoGuiStyle.ui("После подтверждения тип нельзя сменить обычным способом."), width / 2, top + 66, TokyoGuiStyle.STEEL);
        }

        super.render(g, mx, my, pt);
        drawButtonSurfaces(g);
    }

    private void drawCards(GuiGraphics g, int y) {
        int cx = width / 2;
        int[][] pos = {{cx - 206, y}, {cx + 8, y}, {cx - 206, y + 78}, {cx + 8, y + 78}};
        String[] title = {"ГУЛЬ", "ПОЛУГУЛЬ", "ЧЕЛОВЕК", "CCG"};
        String[] sub = {"RC-КЛЕТКИ  •  КАГУНЕ", "ГИБРИД  •  КАГУНЕ", "ТЕХНИКА  •  ВЫЖИВАНИЕ", "КВИНКЕ  •  СЛУЖБА"};
        for (int i = 0; i < 4; i++) {
            Button b = themedButtons.get(i);
            int accent = i == 3 ? TokyoGuiStyle.CCG : TokyoGuiStyle.RED;
            TokyoGuiStyle.button(g, pos[i][0], pos[i][1], 198, 66, TokyoGuiStyle.ui(title[i] + "  /  " + sub[i]), b.isHovered(), selectedRace == i, accent);
            g.drawString(font, TokyoGuiStyle.ui(String.format("0%d", i + 1)), pos[i][0] + 12, pos[i][1] + 12, accent, true);
        }
    }

    private KaguneType[] kaguneTypes() {
        if (selectedRace == 2) {
            // Half-Ghoul: two canonical artificial one-eyed ghoul profiles.
            return new KaguneType[]{KaguneType.KANEKI_RINKAKU, KaguneType.TAKIZAWA_UKAKU};
        }
        // Ghoul: six canonical character kagune profiles.
        return new KaguneType[]{
            KaguneType.RIZE_RINKAKU,
            KaguneType.TOUKA_UKAKU,
            KaguneType.AYATO_UKAKU,
            KaguneType.TSUKIYAMA_KOUKAKU,
            KaguneType.NISHIKI_BIKAKU,
            KaguneType.NORO_BIKAKU
        };
    }

    private void drawButtonSurfaces(GuiGraphics g) {
        if (stage == 0) return;
        int cx = width / 2;
        KaguneType[] types = kaguneTypes();
        int panelH = Math.min(selectedRace == 1 ? 500 : 390, height - 18);
        int panelTop = (height - panelH) / 2;
        int y = panelTop + 78;
        for (int i = 0; i < types.length; i++) {
            Button b = themedButtons.get(i);
            TokyoGuiStyle.button(g, cx - 205, y, 410, 34, TokyoGuiStyle.ui(types[i].displayName()), b.isHovered(), selectedKagune == types[i], TokyoGuiStyle.RED);
            g.drawString(font, TokyoGuiStyle.ui(types[i].description()), cx - 184, y + 21, TokyoGuiStyle.STEEL, false);
            y += 46;
        }
        Button back = themedButtons.get(types.length);
        int by = panelTop + panelH - 34;
        TokyoGuiStyle.button(g, cx - 205, by, 410, 30, TokyoGuiStyle.ui("← НАЗАД"), back.isHovered(), false, TokyoGuiStyle.LINE);
    }
}
