package com.tokyoghoul.rpg.client;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Shared visual language for every Tokyo Ghoul RPG screen and HUD element. */
public final class TokyoGuiStyle {
    public static final ResourceLocation UI_FONT = new ResourceLocation(TokyoGhoulRPG.MODID, "ui");
    public static final int INK = 0xFF080A0D;
    public static final int PANEL = 0xF20F1319;
    public static final int PANEL_SOFT = 0xD9161B22;
    public static final int LINE = 0xFF343B45;
    public static final int RED = 0xFF9E243B;
    public static final int RED_BRIGHT = 0xFFD84A61;
    public static final int RED_SOFT = 0xFF6E1A2A;
    public static final int STEEL = 0xFF9CA6B2;
    public static final int WHITE = 0xFFE9ECEF;
    public static final int GOLD = 0xFFD2B46C;
    public static final int CCG = 0xFF4C8FC0;
    public static final int GREEN = 0xFF76B98C;

    private TokyoGuiStyle() {}

    public static Component ui(String text) {
        return Component.literal(text).withStyle(s -> s.withFont(UI_FONT));
    }

    public static void background(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xE906080B);
        g.fill(0, 0, w, 2, 0xFF050608);
        g.fill(0, h - 2, w, h, 0xFF050608);
        // Thin scanline texture. It is deliberately restrained so text remains readable.
        for (int y = 8; y < h; y += 12) g.fill(0, y, w, y + 1, 0x12000000);
        g.fill(0, 0, 3, h, RED);
        g.fill(w - 3, 0, w, h, 0xFF11151B);
    }

    public static void panel(GuiGraphics g, int x, int y, int w, int h, int accent) {
        g.fill(x + 4, y + 5, x + w + 4, y + h + 5, 0x50000000);
        g.fill(x, y, x + w, y + h, PANEL);
        g.fill(x, y, x + w, y + 2, 0xFF222831);
        g.fill(x, y + 2, x + 3, y + h - 2, accent);
        g.fill(x + w - 2, y + 2, x + w, y + h - 2, 0xFF272D35);
        g.fill(x + 10, y + h - 2, x + w - 10, y + h, 0xFF242A32);
    }

    public static void section(GuiGraphics g, int x, int y, int w, String label, int accent) {
        g.fill(x, y, x + w, y + 1, accent);
        g.fill(x, y + 1, x + 32, y + 3, accent);
        g.drawString(Minecraft.getInstance().font, ui(label), x, y + 7, WHITE, false);
    }

    public static void divider(GuiGraphics g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, LINE);
        g.fill(x, y + 1, x + Math.min(28, w), y + 2, RED_SOFT);
    }

    public static void button(GuiGraphics g, int x, int y, int w, int h, Component label, boolean hovered, boolean selected, int accent) {
        int base = hovered ? 0xFF20262E : 0xFF151A20;
        if (selected) base = 0xFF2A1820;
        g.fill(x + 2, y + 3, x + w + 2, y + h + 3, 0x50000000);
        g.fill(x, y, x + w, y + h, base);
        g.fill(x, y, x + w, y + 1, hovered || selected ? accent : LINE);
        g.fill(x, y + h - 2, x + w, y + h, selected ? accent : 0xFF0C0F13);
        g.fill(x, y, x + 2, y + h, hovered || selected ? accent : 0xFF262D36);
        if (hovered) g.fill(x + 4, y + 4, x + 7, y + h - 4, accent);
        int text = selected ? WHITE : hovered ? WHITE : 0xFFD1D6DB;
        g.drawCenteredString(Minecraft.getInstance().font, label.copy().withStyle(s -> s.withFont(UI_FONT)), x + w / 2, y + (h - 8) / 2, text);
    }

    public static void title(GuiGraphics g, Font font, String main, String sub, int center, int top) {
        g.drawCenteredString(font, ui(main), center, top, WHITE);
        g.drawCenteredString(font, ui(sub), center, top + 17, STEEL);
        g.fill(center - 110, top + 34, center + 110, top + 35, LINE);
        g.fill(center - 38, top + 34, center + 38, top + 36, RED);
    }

    public static void cornerMark(GuiGraphics g, int x, int y, int side, int accent) {
        g.fill(x, y, x + side, y + 1, accent);
        g.fill(x, y, x + 1, y + side, accent);
        g.fill(x + side - 8, y + side - 1, x + side, y + side, accent);
    }
}
