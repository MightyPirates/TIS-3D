package li.cil.tis3d.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.client.manual.Document;
import li.cil.tis3d.client.manual.segment.InteractiveSegment;
import li.cil.tis3d.client.manual.segment.Segment;
import li.cil.tis3d.common.api.ManualAPIImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Environment(EnvType.CLIENT)
public final class ManualGui extends Screen {
    private static final int DOCUMENT_MAX_WIDTH = 220;
    private static final int DOCUMENT_MAX_HEIGHT = 176;
    private static final int SCROLL_POS_X = 250;
    private static final int SCROLL_POS_Y = 48;
    private static final int SCROLL_WIDTH = 26;
    private static final int SCROLL_HEIGHT = 180;
    private static final int TAB_POS_X = -52;
    private static final int TAB_POS_Y = 40;
    private static final int TAB_WIDTH = 64;
    private static final int TAB_HEIGHT = 32;
    private static final int TAB_OVERLAP = 8;
    private static final int MAX_TABS_PER_SIDE = 7;
    private static final int WINDOW_WIDTH = 256;
    private static final int WINDOW_HEIGHT = 256;

    private int guiLeft = 0;
    private int guiTop = 0;
    private int xSize = 0;
    private int ySize = 0;

    private boolean isDragging = false;
    private Segment document = null;
    private int documentHeight = 0;
    private Optional<InteractiveSegment> currentSegment = Optional.empty();

    private ImageButton scrollButton = null;

    protected ManualGui() {
        super(new LiteralText("Manual"));
    }

    public void pushPage(final String path) {
        if (!ManualAPIImpl.peekPath().equals(path)) {
            ManualAPIImpl.pushPath(path);
            refreshPage();
        }
    }

    @Override
    public void init() {
        super.init();

        //~ final ScaledResolution screenSize = new ScaledResolution(minecraft.window.getScaledWidth(), minecraft.window.getScaledHeight());
        final ScaledResolution screenSize = new ScaledResolution(640, 480); // XXX
        final ScaledResolution guiSize = new ScaledResolution(WINDOW_WIDTH, WINDOW_HEIGHT);
        final int midX = screenSize.scaledWidth / 2;
        final int midY = screenSize.scaledHeight / 2;
        guiLeft = midX - guiSize.scaledWidth / 2;
        guiTop = midY - guiSize.scaledHeight / 2;
        xSize = guiSize.scaledWidth;
        ySize = guiSize.scaledHeight;

        for (int i = 0; i < ManualAPIImpl.getTabs().size() && i < MAX_TABS_PER_SIDE; i++) {
            final int x = guiLeft + TAB_POS_X;
            final int y = guiTop + TAB_POS_Y + i * (TAB_HEIGHT - TAB_OVERLAP);
            final int id = i;
            this.addButton(new ImageButton(x, y, TAB_WIDTH, TAB_HEIGHT - TAB_OVERLAP - 1, Textures.LOCATION_GUI_MANUAL_TAB, (button) -> {
                ManualAPI.navigate(ManualAPIImpl.getTabs().get(id).path);
            }).setImageHeight(TAB_HEIGHT).setVerticalImageOffset(-TAB_OVERLAP / 2));
        }

        scrollButton = new ImageButton(guiLeft + SCROLL_POS_X, guiTop + SCROLL_POS_Y, 26, 13, Textures.LOCATION_GUI_MANUAL_SCROLL, (button) -> {

        }) {
            @Override
            public boolean mouseClicked(final double x, final double y, final int button) {
                return false; // Handled in parent mouseClicked
            }
        };
        this.addButton(scrollButton);

        refreshPage();
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        GlStateManager.enableBlend();

        super.render(mouseX, mouseY, partialTicks);

        minecraft.getTextureManager().bindTexture(Textures.LOCATION_GUI_MANUAL_BACKGROUND);
        DrawableHelper.blit(guiLeft, guiTop, 0, 0, xSize, ySize, WINDOW_WIDTH, WINDOW_HEIGHT);

        scrollButton.active = canScroll();
        scrollButton.hoverOverride = isDragging;

        for (int i = 0; i < ManualAPIImpl.getTabs().size() && i < MAX_TABS_PER_SIDE; i++) {
            final ManualAPIImpl.Tab tab = ManualAPIImpl.getTabs().get(i);
            final ImageButton button = (ImageButton)buttons.get(i);
            GlStateManager.pushMatrix();
            //~ GlStateManager.translatef(button.x + 30, (float)(button.y + 4 - TAB_OVERLAP / 2), (int)blitOffset);
            tab.renderer.render();
            GlStateManager.popMatrix();
        }

        currentSegment = Document.render(document, guiLeft + 16, guiTop + 48, DOCUMENT_MAX_WIDTH, DOCUMENT_MAX_HEIGHT, offset(), getTextRenderer(), mouseX, mouseY);

        if (!isDragging) {
            currentSegment.ifPresent(s -> s.tooltip().ifPresent(t -> renderTooltip(Collections.singletonList(I18n.translate(t)), mouseX, mouseY)));

            for (int i = 0; i < ManualAPIImpl.getTabs().size() && i < MAX_TABS_PER_SIDE; i++) {
                final ManualAPIImpl.Tab tab = ManualAPIImpl.getTabs().get(i);
                final ImageButton button = (ImageButton)buttons.get(i);
                if (mouseX > button.x && mouseX < button.x + button.getWidth() && mouseY > button.y && mouseY < button.y + button.getHeight()) {
                    if (tab.tooltip != null) {
                        renderTooltip(Collections.singletonList(I18n.translate(tab.tooltip)), mouseX, mouseY);
                    }
                }
            }
        }

        if (canScroll() && (isCoordinateOverScrollBar(mouseX - guiLeft, mouseY - guiTop) || isDragging)) {
            renderTooltip(Collections.singletonList(100 * offset() / maxOffset() + "%"), guiLeft + SCROLL_POS_X + SCROLL_WIDTH, scrollButton.y + scrollButton.getHeight() + 1);
        }
    }

    @Override
    public boolean mouseScrolled(final double mx, final double my, final double value) {
        if (value != 0) {
            scroll(-value);
        }
        return true;
    }

    @Override
    public boolean keyPressed(final int code, final int scancode, final int mods) {
        if (minecraft.options.keyJump.matchesKey(code, scancode)) {
            popPage();
            return true;
        } else if (minecraft.options.keyInventory.matchesKey(code, scancode)) {
            minecraft.player.closeScreen();
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

        final int mouseX = (int)Math.round(mouseXd);
        final int mouseY = (int)Math.round(mouseYd);

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
    public boolean isPauseScreen() {
        return false;
    }

    // --------------------------------------------------------------------- //

    private TextRenderer getTextRenderer() {
        return font;
    }

    private boolean canScroll() {
        return maxOffset() > 0;
    }

    private int offset() {
        return ManualAPIImpl.peekOffset();
    }

    private int maxOffset() {
        return documentHeight - DOCUMENT_MAX_HEIGHT;
    }

    private void refreshPage() {
        final Iterable<String> content = ManualAPI.contentFor(ManualAPIImpl.peekPath());
        document = Document.parse(content != null ? content : Collections.singletonList("Document not found: " + ManualAPIImpl.peekPath()));
        documentHeight = Document.height(document, DOCUMENT_MAX_WIDTH, getTextRenderer());
        scrollTo(offset());
    }

    private void popPage() {
        if (ManualAPIImpl.getHistorySize() > 1) {
            ManualAPIImpl.popPath();
            refreshPage();
        } else {
            minecraft.player.closeScreen();
        }
    }

    private void scrollMouse(final double mouseY) {
        scrollTo((int)Math.round((mouseY - guiTop - SCROLL_POS_Y - 6.5) * maxOffset() / (SCROLL_HEIGHT - 13.0)));
    }

    private void scroll(final double amount) {
        scrollTo(offset() + (int)Math.round(Document.lineHeight(getTextRenderer()) * 3 * amount));
    }

    private void scrollTo(final int row) {
        ManualAPIImpl.setOffset(Math.max(0, Math.min(maxOffset(), row)));
        final int yMin = guiTop + SCROLL_POS_Y;
        if (maxOffset() > 0) {
            scrollButton.y = yMin + (SCROLL_HEIGHT - 13) * offset() / maxOffset();
        } else {
            scrollButton.y = yMin;
        }
    }

    private boolean isCoordinateOverScrollBar(final int x, final int y) {
        return x > SCROLL_POS_X && x < SCROLL_POS_X + SCROLL_WIDTH &&
            y >= SCROLL_POS_Y && y < SCROLL_POS_Y + SCROLL_HEIGHT;
    }

    private static class ImageButton extends ButtonWidget {
        private final Identifier image;
        private boolean hoverOverride = false;
        private int verticalImageOffset = 0;
        private int imageHeightOverride = 0;

        ImageButton(final int x, final int y, final int w, final int h, final Identifier image, final PressAction action) {
            super(x, y, w, h, "", action);
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
        public void render(final int mouseX, final int mouseY, final float partialTicks) {
            if (visible) {
                MinecraftClient.getInstance().getTextureManager().bindTexture(image);
                GlStateManager.color4f(1, 1, 1, 1);

                isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

                final int x0 = x;
                final int x1 = x + width;
                final int y0 = y + verticalImageOffset;
                final int y1 = y + verticalImageOffset + ((imageHeightOverride > 0) ? imageHeightOverride : height);

                final double u0 = 0;
                final double u1 = u0 + 1;
                final double v0 = (hoverOverride || isHovered()) ? 0.5 : 0;
                final double v1 = v0 + 0.5;

                final Tessellator t = Tessellator.getInstance();
                final BufferBuilder b = t.getBuffer();
                b.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
                //~ b.vertex(x0, y1, blitOffset).texture(u0, v1).next();
                //~ b.vertex(x1, y1, blitOffset).texture(u1, v1).next();
                //~ b.vertex(x1, y0, blitOffset).texture(u1, v0).next();
                //~ b.vertex(x0, y0, blitOffset).texture(u0, v0).next();
                t.draw();
            }
        }
    }

    private static class ScaledResolution {
        final int scaledWidth;
        final int scaledHeight;

        ScaledResolution(final int width, final int height) {
            int scaleFactor = 1;
            int guiScale = MinecraftClient.getInstance().options.guiScale;

            if (guiScale == 0) {
                guiScale = 1000;
            }

            while (scaleFactor < guiScale && width / (scaleFactor + 1) >= 320 && height / (scaleFactor + 1) >= 240) {
                ++scaleFactor;
            }

            if (MinecraftClient.getInstance().forcesUnicodeFont() && scaleFactor % 2 != 0 && scaleFactor != 1) {
                --scaleFactor;
            }

            this.scaledWidth = MathHelper.ceil(width / (double)scaleFactor);
            this.scaledHeight = MathHelper.ceil(height / (double)scaleFactor);
        }
    }
}
