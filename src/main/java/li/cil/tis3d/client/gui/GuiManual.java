package li.cil.tis3d.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.client.manual.Document;
import li.cil.tis3d.client.manual.segment.InteractiveSegment;
import li.cil.tis3d.client.manual.segment.Segment;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.common.api.ManualAPIImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@SideOnly(Side.CLIENT)
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

        final ScaledResolution screenSize = new ScaledResolution(mc.displayWidth, mc.displayHeight);
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
            buttonList.add(new ImageButton(i, x, y, tabWidth, tabHeight - tabOverlap - 1, TextureLoader.LOCATION_MANUAL_TAB).setImageHeight(tabHeight).setVerticalImageOffset(-tabOverlap / 2));
        }

        scrollButton = new ImageButton(-1, guiLeft + scrollPosX, guiTop + scrollPosY, 26, 13, TextureLoader.LOCATION_MANUAL_SCROLL);
        buttonList.add(scrollButton);

        refreshPage();
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        GL11.glEnable(GL11.GL_BLEND);

        super.drawScreen(mouseX, mouseY, partialTicks);

        mc.renderEngine.bindTexture(TextureLoader.LOCATION_MANUAL_BACKGROUND);
        Gui.func_146110_a(guiLeft, guiTop, 0, 0, xSize, ySize, windowWidth, windowHeight);

        scrollButton.enabled = canScroll();
        scrollButton.hoverOverride = isDragging;

        for (int i = 0; i < ManualAPIImpl.getTabs().size() && i < maxTabsPerSide; i++) {
            final ManualAPIImpl.Tab tab = ManualAPIImpl.getTabs().get(i);
            final ImageButton button = (ImageButton) buttonList.get(i);
            GL11.glPushMatrix();
            GL11.glTranslated(button.xPosition + 30, button.yPosition + 4 - tabOverlap / 2, zLevel);
            tab.renderer.render();
            GL11.glPopMatrix();
        }

        currentSegment = Document.render(document, guiLeft + 16, guiTop + 48, documentMaxWidth, documentMaxHeight, offset(), fontRendererObj, mouseX, mouseY);

        if (!isDragging) {
            currentSegment.ifPresent(s -> s.tooltip().ifPresent(t -> drawHoveringText(Collections.singletonList(StatCollector.translateToLocal(t)), mouseX, mouseY, fontRendererObj)));

            for (int i = 0; i < ManualAPIImpl.getTabs().size() && i < maxTabsPerSide; i++) {
                final ManualAPIImpl.Tab tab = ManualAPIImpl.getTabs().get(i);
                final ImageButton button = (ImageButton) buttonList.get(i);
                if (mouseX > button.xPosition && mouseX < button.xPosition + button.width && mouseY > button.yPosition && mouseY < button.yPosition + button.height) {
                    if (tab.tooltip != null) {
                        drawHoveringText(Collections.singletonList(StatCollector.translateToLocal(tab.tooltip)), mouseX, mouseY, fontRendererObj);
                    }
                }
            }
        }

        if (canScroll() && (isCoordinateOverScrollBar(mouseX - guiLeft, mouseY - guiTop) || isDragging)) {
            drawHoveringText(Collections.singletonList(100 * offset() / maxOffset() + "%"), guiLeft + scrollPosX + scrollWidth, scrollButton.yPosition + scrollButton.height + 1, fontRendererObj);
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        if (Mouse.hasWheel() && Mouse.getEventDWheel() != 0) {
            if (Math.signum(Mouse.getEventDWheel()) < 0) {
                scrollDown();
            } else {
                scrollUp();
            }
        }
    }

    @Override
    protected void keyTyped(final char ch, final int code) {
        if (code == mc.gameSettings.keyBindJump.getKeyCode()) {
            popPage();
        } else if (code == mc.gameSettings.keyBindInventory.getKeyCode()) {
            mc.thePlayer.closeScreen();
        } else {
            super.keyTyped(ch, code);
        }
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (canScroll() && button == 0 && isCoordinateOverScrollBar(mouseX - guiLeft, mouseY - guiTop)) {
            isDragging = true;
            scrollMouse(mouseY);
        } else if (button == 0) {
            currentSegment.ifPresent(s -> s.onMouseClick(mouseX, mouseY));
        } else if (button == 1) {
            popPage();
        }
    }

    @Override
    protected void mouseClickMove(final int mouseX, final int mouseY, final int lastButtonClicked, final long timeSinceMouseClick) {
        super.mouseClickMove(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        if (isDragging) {
            scrollMouse(mouseY);
        }
    }

    @Override
    protected void mouseMovedOrUp(final int mouseX, final int mouseY, final int button) {
        super.mouseMovedOrUp(mouseX, mouseY, button);
        if (button == 0) {
            isDragging = false;
        }
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (button.id >= 0 && button.id < ManualAPIImpl.getTabs().size()) {
            ManualAPI.navigate(ManualAPIImpl.getTabs().get(button.id).path);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // --------------------------------------------------------------------- //

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
        documentHeight = Document.height(document, documentMaxWidth, fontRendererObj);
        scrollTo(offset());
    }

    private void popPage() {
        if (ManualAPIImpl.getHistorySize() > 1) {
            ManualAPIImpl.popPath();
            refreshPage();
        } else {
            Minecraft.getMinecraft().thePlayer.closeScreen();
        }
    }

    private void scrollMouse(final int mouseY) {
        scrollTo((int) Math.round((mouseY - guiTop - scrollPosY - 6.5) * maxOffset() / (scrollHeight - 13.0)));
    }

    private void scrollUp() {
        scrollTo(offset() - Document.lineHeight(fontRendererObj) * 3);
    }

    private void scrollDown() {
        scrollTo(offset() + Document.lineHeight(fontRendererObj) * 3);
    }

    private void scrollTo(final int row) {
        ManualAPIImpl.setOffset(Math.max(0, Math.min(maxOffset(), row)));
        final int yMin = guiTop + scrollPosY;
        if (maxOffset() > 0) {
            scrollButton.yPosition = yMin + (scrollHeight - 13) * offset() / maxOffset();
        } else {
            scrollButton.yPosition = yMin;
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

        public ImageButton(final int id, final int x, final int y, final int w, final int h, final ResourceLocation image) {
            super(id, x, y, w, h, "");
            this.image = image;
        }

        public ImageButton setImageHeight(final int height) {
            this.imageHeightOverride = height;
            return this;
        }

        public ImageButton setVerticalImageOffset(final int offset) {
            this.verticalImageOffset = offset;
            return this;
        }

        @Override
        public void drawButton(final Minecraft mc, final int mouseX, final int mouseY) {
            if (visible) {
                mc.getTextureManager().bindTexture(image);
                GL11.glColor4f(1, 1, 1, 1);

                field_146123_n = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

                final int x0 = xPosition;
                final int x1 = xPosition + width;
                final int y0 = yPosition + verticalImageOffset;
                final int y1 = yPosition + verticalImageOffset + ((imageHeightOverride > 0) ? imageHeightOverride : height);

                final double u0 = 0;
                final double u1 = u0 + 1;
                final double v0 = (hoverOverride || getHoverState(field_146123_n) == 2) ? 0.5 : 0;
                final double v1 = v0 + 0.5;

                final Tessellator t = Tessellator.instance;
                t.startDrawing(GL11.GL_QUADS);
                t.addVertexWithUV(x0, y1, zLevel, u0, v1);
                t.addVertexWithUV(x1, y1, zLevel, u1, v1);
                t.addVertexWithUV(x1, y0, zLevel, u1, v0);
                t.addVertexWithUV(x0, y0, zLevel, u0, v0);
                t.draw();
            }
        }
    }

    private static class ScaledResolution {
        public final int scaledWidth;
        public final int scaledHeight;

        public ScaledResolution(final int width, final int height) {
            int scaleFactor = 1;
            int guiScale = Minecraft.getMinecraft().gameSettings.guiScale;

            if (guiScale == 0) {
                guiScale = 1000;
            }

            while (scaleFactor < guiScale && width / (scaleFactor + 1) >= 320 && height / (scaleFactor + 1) >= 240) {
                ++scaleFactor;
            }

            if (Minecraft.getMinecraft().func_152349_b() && scaleFactor % 2 != 0 && scaleFactor != 1) {
                --scaleFactor;
            }

            this.scaledWidth = MathHelper.ceiling_double_int(width / (double) scaleFactor);
            this.scaledHeight = MathHelper.ceiling_double_int(height / (double) scaleFactor);
        }
    }
}
