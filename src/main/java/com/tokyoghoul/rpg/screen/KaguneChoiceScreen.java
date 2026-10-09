package com.tokyoghoul.rpg.screen;

import com.tokyoghoul.rpg.client.TokyoGuiStyle;
import com.tokyoghoul.rpg.network.KaguneChoicePacket;
import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class KaguneChoiceScreen extends Screen {
    private final List<Button> choices = new ArrayList<>();
    private final KaguneType[] types = {KaguneType.KANEKI_RINKAKU, KaguneType.TAKIZAWA_UKAKU};

    public KaguneChoiceScreen() { super(TokyoGuiStyle.ui("RC // КАГУНЕ")); }

    @Override protected void init() {
        choices.clear();
        int cx = width / 2;
        int panelH = Math.min(520, height - 18);
        int panelTop = (height - panelH) / 2;
        int y = panelTop + 72;
        for (int i = 0; i < types.length; i++) {
            final int id = i;
            Button b = Button.builder(Component.empty(), ignored -> choose(id)).bounds(cx - 225, y, 450, 38).build();
            b.setAlpha(0.0F);
            choices.add(b);
            addRenderableWidget(b);
            y += 48;
        }
    }

    private void choose(int id) {
        NetworkHandler.CHANNEL.sendToServer(new KaguneChoicePacket(id));
        onClose();
    }

    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        TokyoGuiStyle.background(g, width, height);
        int w = Math.min(720, width - 20), h = Math.min(520, height - 18);
        int x = (width - w) / 2, y0 = (height - h) / 2;
        TokyoGuiStyle.panel(g, x, y0, w, h, TokyoGuiStyle.RED);
        TokyoGuiStyle.title(g, font, "KAGUNE // ORGANIC WEAPON", "Выбери одну форму. Каждая меняет стиль боя.", width / 2, y0 + 18);
        int y = y0 + 72;
        for (int i = 0; i < types.length; i++) {
            Button b = choices.get(i);
            boolean hover = b.isHovered();
            TokyoGuiStyle.button(g, width / 2 - 225, y, 450, 38, TokyoGuiStyle.ui(types[i].displayName()), hover, false, TokyoGuiStyle.RED);
            g.drawString(font, TokyoGuiStyle.ui(types[i].description()), width / 2 - 205, y + 23, hover ? TokyoGuiStyle.WHITE : TokyoGuiStyle.STEEL, false);
            g.drawString(font, TokyoGuiStyle.ui(String.format("%02d", i + 1)), width / 2 + 195, y + 14, TokyoGuiStyle.RED_BRIGHT, true);
            y += 48;
        }
        g.drawCenteredString(font, TokyoGuiStyle.ui("Профиль нельзя сменить после подтверждения."), width / 2, y0 + h - 22, TokyoGuiStyle.GOLD);
        super.render(g, mx, my, pt);
    }
}
