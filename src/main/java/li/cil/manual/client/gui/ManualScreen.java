package li.cil.manual.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualScreenStyle;
import li.cil.manual.api.ManualStyle;
import li.cil.manual.api.Tab;
import li.cil.manual.client.document.Document;
import li.cil.manual.client.document.segment.InteractiveSegment;
import li.cil.tis3d.util.IterableUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@OnlyIn(Dist.CLIENT)
public final class ManualScreen extends Screen {
    private final ManualModel model;
    private final ManualStyle manualStyle;
    private final ManualScreenStyle screenStyle;
    private final Document document;
    private String currentPath;

    private int leftPos = 0;
    private int topPos = 0;
    private float scrollPos = 0;

    private boolean isDragging = false;
    private int documentHeight = 0;
    private Optional<InteractiveSegment> currentSegment = Optional.empty();

    private ScrollButton scrollButton = null;

    public ManualScreen(final ManualModel model, final ManualStyle manualStyle, final ManualScreenStyle screenStyle) {
        super(new StringTextComponent("Manual"));
        this.model = model;
        this.manualStyle = manualStyle;
        this.screenStyle = screenStyle;
        this.document = new Document(manualStyle, model);
    }

    public ManualModel getModel() {
        return model;
    }

    @Override
    public void init() {
        super.init();

        this.leftPos = (width - screenStyle.getWindowRect().getWidth()) / 2 + screenStyle.getWindowRect().getX();
        this.topPos = (height - screenStyle.getWindowRect().getHeight()) / 2 + screenStyle.getWindowRect().getY();

        IterableUtils.forEachWithIndex(model.getTabs(), (i, tab) -> {
            final int x = screenStyle.getTabAreaRect().getX();
            final int y = screenStyle.getTabAreaRect().getY() + i * getTabClickableHeight();
            if (y + screenStyle.getTabRect().getHeight() > screenStyle.getTabAreaRect().getHeight()) return;
            addButton(new TabButton(leftPos + x, topPos + y, tab, (button) -> pushManualPage(tab)));
        });

        scrollButton = addButton(new ScrollButton(
            leftPos + screenStyle.getScrollBarRect().getX() + screenStyle.getScrollButtonRect().getX(),
            topPos + screenStyle.getScrollBarRect().getY() + screenStyle.getScrollButtonRect().getY(),
            screenStyle.getScrollButtonRect().getWidth(),
            screenStyle.getScrollButtonRect().getHeight()));
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        renderBackground(matrixStack);

        if (!Objects.equals(currentPath, model.peek())) {
            refreshPage();
            currentPath = model.peek();

            final SoundHandler soundHandler = Minecraft.getInstance().getSoundManager();
            soundHandler.play(SimpleSound.forUI(manualStyle.getPageChangeSound(), 1));
        }

        scrollPos = MathHelper.lerp(partialTicks * 0.5f, scrollPos, getScrollPosition());

        RenderSystem.enableBlend();

        // Don't render scroll button behind manual.
        scrollButton.active = false;

        // Render tabs behind manual.
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        // Render manual background.
        getMinecraft().getTextureManager().bind(screenStyle.getWindowBackground());
        final Rectangle2d windowRect = screenStyle.getWindowRect();
        blit(matrixStack, leftPos, topPos, 0, 0, windowRect.getWidth(), windowRect.getHeight(), windowRect.getWidth(), windowRect.getHeight());

        // Render scroll button in front of manual.
        scrollButton.active = canScroll();
        scrollButton.render(matrixStack, mouseX, mouseY, partialTicks);

        final Rectangle2d documentRect = screenStyle.getDocumentRect();
        final int documentX = leftPos + documentRect.getX();
        final int documentY = topPos + documentRect.getY();

        matrixStack.pushPose();
        matrixStack.translate(documentX, documentY, 0);

        currentSegment = document.render(matrixStack, getSmoothScrollPosition(),
            documentRect.getWidth(), documentRect.getHeight(),
            mouseX - documentX, mouseY - documentY);

        matrixStack.popPose();

        currentSegment.flatMap(InteractiveSegment::getTooltip).ifPresent(t ->
            renderWrappedToolTip(matrixStack, Collections.singletonList(t), mouseX, mouseY, getFontRenderer()));

        for (final Widget widget : this.buttons) {
            if (widget.active && (!isDragging || widget instanceof ScrollButton)) {
                widget.renderToolTip(matrixStack, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double delta) {
        if (super.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }

        scrollBy(delta);
        return true;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (getMinecraft().options.keyJump.matches(keyCode, scanCode)) {
            popManualPage();
            return true;
        } else if (getMinecraft().options.keyInventory.matches(keyCode, scanCode)) {
            final ClientPlayerEntity player = getMinecraft().player;
            if (player != null) {
                player.closeContainer();
            }
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (canScroll() && button == 0 && isCoordinateOverScrollBar(mouseX, mouseY)) {
            isDragging = true;
            scrollButton.playDownSound(Minecraft.getInstance().getSoundManager());
            scrollTo(mouseY);
            return true;
        } else if (button == 0) {
            return currentSegment.map(InteractiveSegment::mouseClicked).orElse(false);
        } else if (button == 1) {
            popManualPage();
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double dragX, final double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        if (isDragging) {
            scrollTo(mouseY);
            return true;
        }

        return false;
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

    private void pushManualPage(final Tab tab) {
        model.push(tab.getPath());
    }

    private void popManualPage() {
        if (!model.pop()) {
            onClose();
        }
    }

    private FontRenderer getFontRenderer() {
        return font;
    }

    private boolean canScroll() {
        return maxScrollPosition() > 0;
    }

    private int getScrollPosition() {
        return model.getUserData(ScrollOffset.class).
            map(offset -> offset.value).
            orElse(0);
    }

    private void setScrollPosition(final int value) {
        model.setUserData(new ScrollOffset(value));
    }

    private int maxScrollPosition() {
        return Math.max(0, documentHeight - screenStyle.getDocumentRect().getHeight());
    }

    private void refreshPage() {
        final Optional<Iterable<String>> content = model.contentFor(model.peek());
        document.parse(content.orElse(Collections.singleton("Page not found: " + model.peek())));
        documentHeight = document.height(screenStyle.getDocumentRect().getWidth());
        scrollPos = getScrollPosition() - manualStyle.getLineHeight() * 3;
    }

    private void scrollTo(final double mouseY) {
        final int scrollButtonHeight = screenStyle.getScrollButtonRect().getHeight();
        final int halfScrollButtonHeight = (int) Math.ceil(scrollButtonHeight * 0.5);
        final int scrollMinY = topPos + screenStyle.getScrollBarRect().getY() + halfScrollButtonHeight;
        final int scrollHeight = screenStyle.getScrollBarRect().getHeight() - scrollButtonHeight; // Subtract half button top and bottom.
        final int localMouseY = (int) (mouseY - scrollMinY);
        scrollTo(maxScrollPosition() * localMouseY / scrollHeight, true);
    }

    private void scrollBy(final double amount) {
        scrollTo(getScrollPosition() - (int) Math.round(manualStyle.getLineHeight() * 3 * amount), false);
    }

    private void scrollTo(final int y, final boolean immediate) {
        setScrollPosition(Math.max(0, Math.min(maxScrollPosition(), y)));
        if (immediate) {
            scrollPos = getScrollPosition();
        }
    }

    private int getSmoothScrollPosition() {
        if (scrollPos < getScrollPosition()) {
            return (int) Math.ceil(scrollPos);
        } else {
            return (int) Math.floor(scrollPos);
        }
    }

    private int getScrollButtonY() {
        if (maxScrollPosition() > 0) {
            final int yMax = screenStyle.getScrollBarRect().getHeight() - screenStyle.getScrollButtonRect().getHeight();
            return Math.max(0, Math.min(yMax, yMax * getSmoothScrollPosition() / maxScrollPosition()));
        } else {
            return 0;
        }
    }

    private boolean isCoordinateOverScrollBar(final double x, final double y) {
        return screenStyle.getScrollBarRect().contains((int) (x - leftPos), (int) (y - topPos));
    }

    private int getTabClickableHeight() {
        return screenStyle.getTabRect().getHeight() - screenStyle.getTabOverlap();
    }

    private static final class ScrollOffset {
        public final int value;

        public ScrollOffset(final int value) {
            this.value = value;
        }
    }

    private class TabButton extends Button {
        private final Tab tab;
        private final int baseX;
        private float currentX;
        private int targetX;

        TabButton(final int x, final int y, final Tab tab, final IPressable action) {
            super(x, y, screenStyle.getTabRect().getWidth(), getTabClickableHeight(), StringTextComponent.EMPTY, action);
            this.tab = tab;
            this.baseX = x;
            this.currentX = x + screenStyle.getTabHoverShift();
            this.targetX = (int) currentX;
        }

        @Override
        public void renderToolTip(final MatrixStack matrixStack, final int mouseX, final int mouseY) {
            if (!isHovered()) {
                return;
            }

            final List<ITextComponent> tooltip = new ArrayList<>();
            tab.getTooltip(tooltip);
            if (!tooltip.isEmpty()) {
                ManualScreen.this.renderWrappedToolTip(matrixStack, tooltip, mouseX, mouseY, getFontRenderer());
            }
        }

        @Override
        public void renderButton(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
            if (isHovered()) {
                targetX = baseX;
            } else {
                targetX = baseX + screenStyle.getTabHoverShift();
            }

            currentX = MathHelper.lerp(partialTicks * 0.5f, currentX, targetX);

            if (currentX < targetX) {
                x = (int) Math.ceil(currentX);
            } else {
                x = (int) Math.floor(currentX);
            }

            width = screenStyle.getTabAreaRect().getWidth() - (x - baseX);

            final int v0 = isHovered() ? screenStyle.getTabRect().getHeight() : 0;
            final int visualWidth = screenStyle.getTabRect().getWidth();
            final int visualHeight = screenStyle.getTabRect().getHeight();
            final int textureWidth = screenStyle.getTabRect().getWidth();
            final int textureHeight = screenStyle.getTabRect().getHeight() * 2;

            getMinecraft().getTextureManager().bind(screenStyle.getTabButtonTexture());
            blit(matrixStack, x, y, 0, v0, visualWidth, visualHeight, textureWidth, textureHeight);

            matrixStack.pushPose();
            matrixStack.translate(x + 12, (float) (y + (screenStyle.getTabRect().getHeight() - 18) / 2), 0);

            tab.renderIcon(matrixStack);

            matrixStack.popPose();
        }

        @Override
        public void playDownSound(final SoundHandler soundHandler) {
        }
    }

    private class ScrollButton extends Button {
        private static final int TOOLTIP_HEIGHT = 18;

        private final int baseY;

        ScrollButton(final int x, final int y, final int w, final int h) {
            super(x, y, w, h, StringTextComponent.EMPTY, (button) -> {});
            this.baseY = y;
        }

        @Override
        protected boolean clicked(final double mouseX, final double mouseY) {
            if (super.clicked(mouseX, mouseY)) {
                playDownSound(Minecraft.getInstance().getSoundManager());
            }
            return false;
        }

        @Override
        public void renderButton(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
            y = baseY + getScrollButtonY();

            final int x0 = x;
            final int x1 = x0 + width;
            final int y0 = y;
            final int y1 = y0 + height;

            final float u0 = 0;
            final float u1 = u0 + 1;
            final float v0 = (isDragging || isHovered()) ? 0.5f : 0;
            final float v1 = v0 + 0.5f;

            getMinecraft().getTextureManager().bind(screenStyle.getScrollButtonTexture());

            final Tessellator t = Tessellator.getInstance();
            final BufferBuilder builder = t.getBuilder();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            builder.vertex(x0, y1, getBlitOffset()).uv(u0, v1).endVertex();
            builder.vertex(x1, y1, getBlitOffset()).uv(u1, v1).endVertex();
            builder.vertex(x1, y0, getBlitOffset()).uv(u1, v0).endVertex();
            builder.vertex(x0, y0, getBlitOffset()).uv(u0, v0).endVertex();
            t.end();
        }

        @Override
        public void renderToolTip(final MatrixStack matrixStack, final int mouseX, final int mouseY) {
            if (!isDragging && !isHovered() && !isCoordinateOverScrollBar(mouseX, mouseY)) {
                return;
            }
            renderTooltip(matrixStack, new StringTextComponent(100 * getScrollPosition() / maxScrollPosition() + "%"),
                leftPos + screenStyle.getScrollBarRect().getX() + screenStyle.getScrollBarRect().getWidth(),
                y + (getHeight() + TOOLTIP_HEIGHT) / 2);
        }
    }
}
