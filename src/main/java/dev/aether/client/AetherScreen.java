package dev.aether.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class AetherScreen extends Screen {
    private final boolean playIntro;
    private long openedAt;
    private String category = "Combat";
    private String search = "";
    private final List<ModuleItem> modules = new ArrayList<>();
    private int panelX, panelY, panelW = 610, panelH = 360;
    private boolean dragging = false;
    private double dragX, dragY;

    private static final int BG = 0xD9111830;
    private static final int PANEL = 0xD91B2545;
    private static final int PANEL_HOVER = 0xE0263458;
    private static final int BORDER = 0xAA8296FF;
    private static final int ACCENT = 0xFF8C9DFF;
    private static final int TEXT = 0xFFF0F2FF;
    private static final int MUTED = 0xFFAFBCE6;

    public AetherScreen(boolean playIntro) {
        super(Text.literal("Aether Client"));
        this.playIntro = playIntro;
        modules.add(new ModuleItem("Schematic Preview", "Ghost blocks and layer view", false));
        modules.add(new ModuleItem("Area Planner", "Plan a centered 3×3 tunnel", false));
        modules.add(new ModuleItem("Waypoints", "Mark and manage locations", true));
        modules.add(new ModuleItem("Custom Crosshair", "Color, size and gap controls", true));
        modules.add(new ModuleItem("Fullbright", "Client-side brightness option", false));
        modules.add(new ModuleItem("HUD Editor", "Move and scale widgets", true));
        modules.add(new ModuleItem("Inventory Helper", "Hotbar and item reminders", false));
        modules.add(new ModuleItem("Freelook", "Camera controls", false));
    }

    @Override
    protected void init() {
        super.init();
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        openedAt = System.currentTimeMillis();
    }

    private float introProgress() {
        if (!playIntro) return 1f;
        return Math.min(1f, (System.currentTimeMillis() - openedAt) / 620f);
    }

    private float easeOut(float x) {
        float y = 1f - x;
        return 1f - y * y * y;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float p = easeOut(introProgress());
        int alpha = (int)(105 * p);
        ctx.fill(0, 0, width, height, (alpha << 24) | 0x080B19);

        int x = panelX + (int)((1f - p) * 36);
        int y = panelY + (int)((1f - p) * 18);
        int w = (int)(panelW * (0.97f + 0.03f * p));
        int h = (int)(panelH * (0.97f + 0.03f * p));

        // Layered translucent surfaces mimic a frosted glass panel without external assets.
        rounded(ctx, x + 3, y + 5, w, h, 0x4410152B);
        rounded(ctx, x, y, w, h, BG);
        outline(ctx, x, y, w, h, BORDER);
        ctx.fill(x + 1, y + 39, x + w - 1, y + 40, 0x665F75D8);

        ctx.drawTextWithShadow(textRenderer, "✦ AETHER", x + 16, y + 14, TEXT);
        ctx.drawText(textRenderer, "Client", x + 92, y + 15, MUTED);
        ctx.drawText(textRenderer, "Search: " + (search.isEmpty() ? "type to filter" : search),
            x + 180, y + 15, MUTED);
        ctx.drawText(textRenderer, "RIGHT SHIFT  •  v0.1", x + w - 132, y + 15, MUTED);

        int sideW = 112;
        rounded(ctx, x + 9, y + 49, sideW, h - 60, 0x66202A48);
        String[] cats = {"Combat", "Player", "World", "Visuals", "Movement", "Misc", "Theme", "Configs"};
        for (int i = 0; i < cats.length; i++) {
            int cy = y + 59 + i * 34;
            boolean active = category.equals(cats[i]);
            if (active) rounded(ctx, x + 14, cy - 5, sideW - 10, 27, 0xAA4657A2);
            ctx.drawText(textRenderer, cats[i], x + 24, cy + 3, active ? TEXT : MUTED);
        }

        int contentX = x + sideW + 22;
        int contentY = y + 54;
        ctx.drawTextWithShadow(textRenderer, category, contentX, contentY, TEXT);
        ctx.drawText(textRenderer, "Compact modules  ·  glass theme", contentX, contentY + 15, MUTED);

        int cardW = (w - sideW - 42) / 2;
        int cardY = contentY + 33;
        int shown = 0;
        for (ModuleItem item : modules) {
            if (!search.isEmpty() && !item.name.toLowerCase().contains(search.toLowerCase())) continue;
            int col = shown % 2;
            int row = shown / 2;
            int cx = contentX + col * (cardW + 8);
            int cy = cardY + row * 43;
            if (cy + 37 > y + h - 30) break;
            boolean hover = mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + 36;
            rounded(ctx, cx, cy, cardW, 36, hover ? PANEL_HOVER : PANEL);
            outline(ctx, cx, cy, cardW, 36, hover ? ACCENT : 0x554E649E);
            ctx.drawText(textRenderer, item.name, cx + 9, cy + 7, TEXT);
            ctx.drawText(textRenderer, item.description, cx + 9, cy + 20, MUTED);
            int toggleX = cx + cardW - 25;
            rounded(ctx, toggleX, cy + 10, 17, 12, item.enabled ? 0xFF758DFF : 0xFF47516D);
            ctx.fill(toggleX + (item.enabled ? 8 : 2), cy + 12, toggleX + (item.enabled ? 14 : 8), cy + 20,
                0xFFF7F8FF);
            shown++;
        }

        ctx.drawText(textRenderer, "Session intro: " + (playIntro ? "played once" : "skipped"),
            x + 16, y + h - 19, MUTED);

        // A brief glint crosses the top edge only during the first opening animation.
        if (playIntro && p < 1f) {
            int glintX = x + (int)((w + 100) * p) - 100;
            ctx.fill(glintX, y + 1, glintX + 72, y + 3, 0xCCB8C4FF);
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void rounded(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 3, y, x + w - 3, y + h, color);
        ctx.fill(x, y + 3, x + w, y + h - 3, color);
        ctx.fill(x + 1, y + 2, x + w - 1, y + h - 2, color);
    }

    private void outline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 3, y, x + w - 3, y + 1, color);
        ctx.fill(x + 3, y + h - 1, x + w - 3, y + h, color);
        ctx.fill(x, y + 3, x + 1, y + h - 3, color);
        ctx.fill(x + w - 1, y + 3, x + w, y + h - 3, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = panelX + 10;
            int y = panelY + 54;
            String[] cats = {"Combat", "Player", "World", "Visuals", "Movement", "Misc", "Theme", "Configs"};
            for (int i = 0; i < cats.length; i++) {
                int cy = y + 5 + i * 34;
                if (mouseX >= x && mouseX <= x + 105 && mouseY >= cy && mouseY <= cy + 27) {
                    category = cats[i];
                    return true;
                }
            }
            int contentX = panelX + 134;
            int cardW = (panelW - 112 - 42) / 2;
            int cardY = panelY + 54 + 33 + 18;
            int shown = 0;
            for (ModuleItem item : modules) {
                if (!search.isEmpty() && !item.name.toLowerCase().contains(search.toLowerCase())) continue;
                int cx = contentX + (shown % 2) * (cardW + 8);
                int cy = cardY + (shown / 2) * 43;
                if (mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + 36) {
                    item.enabled = !item.enabled;
                    return true;
                }
                shown++;
            }
        }
        if (button == 1 && mouseY >= panelY && mouseY <= panelY + 39) {
            dragging = true; dragX = mouseX - panelX; dragY = mouseY - panelY; return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            panelX = Math.max(0, Math.min(width - panelW, (int)(mouseX - dragX)));
            panelY = Math.max(0, Math.min(height - panelH, (int)(mouseY - dragY)));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_SLASH) {
            search = "";
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!Character.isISOControl(chr) && search.length() < 28) {
            search += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    private static final class ModuleItem {
        final String name, description;
        boolean enabled;
        ModuleItem(String name, String description, boolean enabled) {
            this.name = name; this.description = description; this.enabled = enabled;
        }
    }
}
