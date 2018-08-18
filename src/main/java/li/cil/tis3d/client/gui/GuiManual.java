package li.cil.tis3d.client.gui;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.client.manual.Document;
import li.cil.tis3d.client.manual.segment.InteractiveSegment;
import li.cil.tis3d.client.manual.segment.Segment;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.common.api.ManualAPIImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")

public final class GuiManual extends GuiScreen {
    private static final int documentMaxWidth = 220;
    private static final int documentMaxHeight = 176;
    private static final int scrollPosX = 250;
    private static final int scrollPosY = 48;
    private static final int scrollWidth = 26;
    private static final int scrollHeight = 180;
    private static final int tabPosX = -52;
    private static final int tabPosY = 40;
    private static final int tabWidth = 64;
    private static final int tabHeight = 32;
    private static final int tabOverlap = 8;
    private static final int maxTabsPerSide = 7;
    private static final int windowWidth = 256;
    private static final int windowHeight = 256;

    private int guiLeft = 0;
    private int guiTop = 0;
    private int xSize = 0;
    private int ySize = 0;

    private boolean isDragging = false;
    private Segment document = null;
    private int documentHeight = 0;
    private Optional<InteractiveSegment> currentSegment = Optional.empty();

    private ImageButton scrollButton = null;

    public void pushPage(final String path) {
        if (!ManualAPIImpl.peekPath().equals(path)) {
            ManualAPIImpl.pushPath(path);
            refreshPage();
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        final ScaledResolution screenSize = new ScaledResolution(mc.mainWindow.getWidth(), mc.mainWindow.getHeight());
        final ScaledResolution guiSize = new ScaledResolution(windowWidth, windowHeight);
        final int midX = screenSize.scaledWidth / 2;
        final int midY = screenSize.scaledHeight / 2;
        guiLeft = midX - guiSize.scaledWidth / 2;
        guiTop = midY - guiSize.scaledHeight / 2;
        xSize = guiSize.scaledWidth;
        ySize = guiSize.scaledHeight;

        for (int i = 0; i < ManualAPIImpl.getTabs().size() && i < maxTabsPerSide; i++) {
            final int x = guiLeft + tabPosX;
            final int y = guiTop + tabPosY + i * (tabHeight - tabOverlap);
            this.addButton(new ImageButton(i, x, y, tabWidth, tabHeight - tabOverlap - 1, TextureLoader.LOCATION_GUI_MANUAL_TAB) {
                @Override
                public void mousePressed(double p_mouseClicked_1_, double p_mouseClicked_3_) {
                    ManualAPI.navigate(ManualAPIImpl.getTabs().get(id).path);
                }
            }.setImageHeight(tabHeight).setVerticalImageOffset(-tabOverlap / 2));
        }

        scrollButton = new ImageButton(-1, guiLeft + scrollPosX, guiTop + scrollPosY, 26, 13, TextureLoader.LOCATION_GUI_MANUAL_SCROLL) {
            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                return false; // Handled in parent mouseClicked
            }
        };
        this.addButton(scrollButton);

        refreshPage();
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        GlStateManager.enableBlend();

        super.drawScreen(mouseX, mouseY, partialTicks);

        mc.getTextureManager().bindTexture(TextureLoader.LOCATION_GUI_MANUAL_BACKGROUND);
        Gui.drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, windowWidth, windowHeight);

        scrollButton.enabled = canScroll();
        scrollButton.hoverOverride = isDragging;

        for (int i = 0; i < ManualAPIImpl.getTabs().size() && i < maxTabsPerSide; i++) {
            final ManualAPIImpl.Tab tab = ManualAPIImpl.getTabs().get(i);
            final ImageButton button = (ImageButton) buttonList.get(i);
            GlStateManager.pushMatrix();
            GlStateManager.translate(button.x + 30, button.y + 4 - tabOverlap / 2, zLevel);
            tab.renderer.render();
            GlStateManager.popMatrix();
        }

        currentSegment = Document.render(document, guiLeft + 16, guiTop + 48, documentMaxWidth, documentMaxHeight, offset(), getFontRenderer(), mouseX, mouseY);

        if (!isDragging) {
            currentSegment.ifPresent(s -> s.tooltip().ifPresent(t -> drawHoveringText(Collections.singletonList(I18n.format(t)), mouseX, mouseY)));

            for (int i = 0; i < ManualAPIImpl.getTabs().size() && i < maxTabsPerSide; i++) {
                final ManualAPIImpl.Tab tab = ManualAPIImpl.getTabs().get(i);
                final ImageButton button = (ImageButton) buttonList.get(i);
                if (mouseX > button.x && mouseX < button.x + button.getWidth() && mouseY > button.y && mouseY < button.y + button.getHeight()) {
                    if (tab.tooltip != null) {
                        drawHoveringText(Collections.singletonList(I18n.format(tab.tooltip)), mouseX, mouseY);
                    }
                }
            }
        }

        if (canScroll() && (isCoordinateOverScrollBar(mouseX - guiLeft, mouseY - guiTop) || isDragging)) {
            drawHoveringText(Collections.singletonList(100 * offset() / maxOffset() + "%"), guiLeft + scrollPosX + scrollWidth, scrollButton.y + scrollButton.getHeight() + 1);
        }
    }

    @Override
    public boolean mouseScrolled(double value) {
        if (value != 0) {
            scroll(-value);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int code, int scancode, int mods) {
        if (mc.gameSettings.keyBindJump.func_197976_a(code, scancode)) {
            popPage();
            return true;
        } else if (mc.gameSettings.keyBindInventory.func_197976_a(code, scancode)) {
            mc.player.closeScreen();
            return true;
        } else {
            return super.keyPressed(code, scancode, mods);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseXd, final double mouseYd, final int button) {
        if (super.mouseClicked(mouseXd, mouseYd, button)) {
            return true;
        }

        int mouseX = (int) Math.round(mouseXd);
        int mouseY = (int) Math.round(mouseYd);

        if (canScroll() && button == 0 && isCoordinateOverScrollBar(mouseX - guiLeft, mouseY - guiTop)) {
            isDragging = true;
            scrollMouse(mouseYd);
            return true;
        } else if (button == 0) {
            currentSegment.ifPresent(s -> s.onMouseClick(mouseX, mouseY));
            return true;
        } else if (button == 1) {
            popPage();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseDragged(final double mouseXd, final double mouseYd, final int clickedMouseButton, final double da, final double db) {
        if (super.mouseDragged(mouseXd, mouseYd, clickedMouseButton, da, db)) {
            return true;
        }

        if (isDragging) {
            scrollMouse(mouseYd);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        super.mouseReleased(mouseX, mouseY, button);

        if (button == 0) {
            isDragging = false;
        }

        return true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // --------------------------------------------------------------------- //

    private FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    private boolean canScroll() {
        return maxOffset() > 0;
    }

    private int offset() {
        return ManualAPIImpl.peekOffset();
    }

    private int maxOffset() {
        return documentHeight - documentMaxHeight;
    }

    private void refreshPage() {
        final Iterable<String> content = ManualAPI.contentFor(ManualAPIImpl.peekPath());
        document = Document.parse(content != null ? content : Collections.singletonList("Document not found: " + ManualAPIImpl.peekPath()));
        documentHeight = Document.height(document, documentMaxWidth, getFontRenderer());
        scrollTo(offset());
    }

    private void popPage() {
        if (ManualAPIImpl.getHistorySize() > 1) {
            ManualAPIImpl.popPath();
            refreshPage();
        } else {
            mc.player.closeScreen();
        }
    }

    private void scrollMouse(final double mouseY) {
        scrollTo((int) Math.round((mouseY - guiTop - scrollPosY - 6.5) * maxOffset() / (scrollHeight - 13.0)));
    }

    private void scroll(double amount) {
        scrollTo(offset() + (int) Math.round(Document.lineHeight(getFontRenderer()) * 3 * amount));
    }

    private void scrollTo(final int row) {
        ManualAPIImpl.setOffset(Math.max(0, Math.min(maxOffset(), row)));
        final int yMin = guiTop + scrollPosY;
        if (maxOffset() > 0) {
            scrollButton.y = yMin + (scrollHeight - 13) * offset() / maxOffset();
        } else {
            scrollButton.y = yMin;
        }
    }

    private boolean isCoordinateOverScrollBar(final int x, final int y) {
        return x > scrollPosX && x < scrollPosX + scrollWidth &&
            y >= scrollPosY && y < scrollPosY + scrollHeight;
    }

    private static class ImageButton extends GuiButton {
        private final ResourceLocation image;
        private boolean hoverOverride = false;
        private int verticalImageOffset = 0;
        private int imageHeightOverride = 0;

        ImageButton(final int id, final int x, final int y, final int w, final int h, final ResourceLocation image) {
            super(id, x, y, w, h, "");
            this.image = image;
        }

        ImageButton setImageHeight(final int height) {
            this.imageHeightOverride = height;
            return this;
        }

        ImageButton setVerticalImageOffset(final int offset) {
            this.verticalImageOffset = offset;
            return this;
        }

        public int getWidth() {
        	return width;
        }

        public int getHeight() {
        	return height;
        }

        @Override
        public void drawButton(final int mouseX, final int mouseY, final float partialTicks) {
            if (visible) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(image);
                GlStateManager.color(1, 1, 1, 1);

                hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

                final int x0 = x;
                final int x1 = x + width;
                final int y0 = y + verticalImageOffset;
                final int y1 = y + verticalImageOffset + ((imageHeightOverride > 0) ? imageHeightOverride : height);

                final double u0 = 0;
                final double u1 = u0 + 1;
                final double v0 = (hoverOverride || getHoverState(hovered) == 2) ? 0.5 : 0;
                final double v1 = v0 + 0.5;

                final Tessellator t = Tessellator.getInstance();
                final BufferBuilder b = t.getBuffer();
                b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                b.pos(x0, y1, zLevel).tex(u0, v1).endVertex();
                b.pos(x1, y1, zLevel).tex(u1, v1).endVertex();
                b.pos(x1, y0, zLevel).tex(u1, v0).endVertex();
                b.pos(x0, y0, zLevel).tex(u0, v0).endVertex();
                t.draw();
            }
        }
    }

    private static class ScaledResolution {
        final int scaledWidth;
        final int scaledHeight;

        ScaledResolution(final int width, final int height) {
            int scaleFactor = 1;
            int guiScale = Minecraft.getMinecraft().gameSettings.guiScale;

            if (guiScale == 0) {
                guiScale = 1000;
            }

            while (scaleFactor < guiScale && width / (scaleFactor + 1) >= 320 && height / (scaleFactor + 1) >= 240) {
                ++scaleFactor;
            }

            if (Minecraft.getMinecraft().getForceUnicodeFont() && scaleFactor % 2 != 0 && scaleFactor != 1) {
                --scaleFactor;
            }

            this.scaledWidth = MathHelper.ceil(width / (double) scaleFactor);
            this.scaledHeight = MathHelper.ceil(height / (double) scaleFactor);
        }
    }
}
