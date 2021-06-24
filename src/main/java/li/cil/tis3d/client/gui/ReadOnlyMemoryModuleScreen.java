package li.cil.tis3d.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.API;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.container.ReadOnlyMemoryModuleContainer;
import li.cil.tis3d.common.module.ModuleRandomAccessMemory;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.ClientReadOnlyMemoryModuleDataMessage;
import li.cil.tis3d.util.Color;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public final class ReadOnlyMemoryModuleScreen extends ContainerScreen<ReadOnlyMemoryModuleContainer> {
    private static final int GRID_LEFT = 25;
    private static final int GRID_TOP = 13;
    private static final int CELL_WIDTH = 10;
    private static final int CELL_HEIGHT = 7;
    private static final String LABEL_INITIALIZING = "INITIALIZING...";

    private final byte[] data = new byte[ModuleRandomAccessMemory.MEMORY_SIZE];

    private static int selectedCell = 0;
    private boolean highNibble = true;
    private boolean receivedData;
    private long initTime;

    public ReadOnlyMemoryModuleScreen(final ReadOnlyMemoryModuleContainer container, final PlayerInventory playerInventory, final ITextComponent title) {
        super(container, playerInventory, title);

        xSize = 190;
        ySize = 130;
    }

    public void setData(final byte[] data) {
        System.arraycopy(data, 0, this.data, 0, Math.min(data.length, this.data.length));
        receivedData = true;
        initTime = System.currentTimeMillis();
    }

    // --------------------------------------------------------------------- //
    // GuiScreen

    @Override
    public void init() {
        super.init();

        getMinecraft().keyboardListener.enableRepeatEvents(true);
    }

    @Override
    public void onClose() {
        super.onClose();

        // Only send to server if our data is actually based on the old server
        // data to avoid erasing ROM when closing UI again too quickly.
        if (receivedData) {
            // Save any changes made and send them to the server.
            Network.INSTANCE.sendToServer(new ClientReadOnlyMemoryModuleDataMessage(container.getHand(), data));
        }

        getMinecraft().keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        renderBackground(matrixStack);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        final IRenderTypeBuffer.Impl bufferFactory = IRenderTypeBuffer.getImpl(buffer);

        // Draw row and column headers.
        drawHeaders(matrixStack, bufferFactory);

        // Draw/fade out initializing info text.
        drawInitializing(matrixStack, bufferFactory);

        if (!receivedData) {
            bufferFactory.finish();
            return;
        }

        // Draw memory cells being edited.
        drawMemory(matrixStack, bufferFactory);

        bufferFactory.finish();

        // Draw marker around currently selected memory cell.
        drawSelectionBox(matrixStack);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final MatrixStack matrixStack, final float partialTicks, final int x, final int y) {
        GlStateManager.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(Textures.LOCATION_GUI_MEMORY);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final MatrixStack matrixStack, final int x, final int y) {
        // Suppress rendering of labels.
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        selectCellAt(mouseX, mouseY);

        return true;
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double dragX, final double dragY) {
        selectCellAt(mouseX, mouseY);

        return true;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeScreen();
            return true;
        }

        if (!receivedData) {
            return false;
        }

        final int digit = Character.digit(keyCode, 16);
        if (digit >= 0) {
            byte value = data[selectedCell];
            if (highNibble) {
                value &= 0x0F;
                value |= (digit & 0x0F) << 4;
            } else {
                value &= 0xF0;
                value |= digit & 0x0F;
            }
            data[selectedCell] = value;
            highNibble = !highNibble;
            if (highNibble) {
                selectedCell = (selectedCell + 1) % data.length;
            }
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            data[selectedCell] = 0;
            highNibble = true;
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (highNibble) {
                selectedCell = (selectedCell - 1 + data.length) % data.length;
            }
            data[selectedCell] = 0;
            highNibble = true;
        } else {
            int col = selectedCell & 0x0F;
            int row = (selectedCell & 0xF0) >> 4;

            switch (keyCode) {
                case GLFW.GLFW_KEY_LEFT:
                case GLFW.GLFW_KEY_H:
                    if (col == 0) {
                        col = 15;
                        row = (row - 1 + 16) % 16;
                    } else {
                        --col;
                    }
                    break;
                case GLFW.GLFW_KEY_RIGHT:
                case GLFW.GLFW_KEY_L:
                    if (col == 15) {
                        col = 0;
                        row = (row + 1) % 16;
                    } else {
                        ++col;
                    }
                    break;
                case GLFW.GLFW_KEY_UP:
                case GLFW.GLFW_KEY_K:
                    row = (row - 1 + 16) % 16;
                    break;
                case GLFW.GLFW_KEY_DOWN:
                case GLFW.GLFW_KEY_J:
                    row = (row + 1) % 16;
                    break;
                default:
                    return false;
            }

            selectedCell = (row << 4) | col;
            highNibble = true;
        }

        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // --------------------------------------------------------------------- //

    private void selectCellAt(final double mouseX, final double mouseY) {
        if (!receivedData) {
            return;
        }

        final int col = (int) ((mouseX + 1 - guiLeft - GRID_LEFT) / CELL_WIDTH);
        final int row = (int) ((mouseY + 1 - guiTop - GRID_TOP) / CELL_HEIGHT);

        if (isInGridArea(col, row)) {
            selectedCell = (row << 4) | col;
            highNibble = true;
        }
    }

    private boolean isInGridArea(final int col, final int row) {
        return col >= 0 && row >= 0 && col <= 0xF && row <= 0xF;
    }

    private void drawHeaders(final MatrixStack matrixStack, final IRenderTypeBuffer.Impl bufferFactory) {
        // Columns headers (top).
        matrixStack.push();
        matrixStack.translate(guiLeft + GRID_LEFT + 3, guiTop + 6, 0);
        for (int col = 0; col < 16; col++) {
            API.smallFontRenderer.drawString(matrixStack, bufferFactory, String.format("%X", col), Color.GUI_TEXT, 2);
            matrixStack.translate(CELL_WIDTH, 0, 0);
        }
        matrixStack.pop();

        // Row headers (left).
        matrixStack.push();
        matrixStack.translate(guiLeft + 7, guiTop + 14, 0);
        for (int row = 0; row < 16; row++) {
            API.smallFontRenderer.drawString(matrixStack, bufferFactory, String.format("0X%X0", row), Color.GUI_TEXT, 4);
            matrixStack.translate(0, CELL_HEIGHT, 0);
        }
        matrixStack.pop();
    }

    private void drawInitializing(final MatrixStack matrixStack, final IRenderTypeBuffer.Impl bufferFactory) {
        final float sinceInitialized = (System.currentTimeMillis() - initTime) / 1000f;
        if (receivedData && sinceInitialized > 0.5f) {
            return;
        }

        final float alpha = 1 - sinceInitialized / 0.5f;
        final int color = Color.withAlpha(Color.WHITE, alpha);

        final int labelWidth = API.smallFontRenderer.getCharWidth() * LABEL_INITIALIZING.length();

        matrixStack.push();
        matrixStack.translate((float) (guiLeft + GRID_LEFT + 3 + 7 * CELL_WIDTH - labelWidth / 2), guiTop + GRID_TOP + 1 + 7 * CELL_HEIGHT, 0);
        API.smallFontRenderer.drawString(matrixStack, bufferFactory, LABEL_INITIALIZING, color, LABEL_INITIALIZING.length());
        matrixStack.pop();
    }

    private void drawMemory(final MatrixStack matrixStack, final IRenderTypeBuffer.Impl bufferFactory) {
        final int visibleCells = (int) (System.currentTimeMillis() - initTime);

        final int selectedX = selectedCell & 0x0F;
        final int selectedY = selectedCell / 0x0F;

        matrixStack.push();
        matrixStack.translate(guiLeft + GRID_LEFT + 1, guiTop + GRID_TOP + 1, 0);
        for (int i = 0, count = Math.min(visibleCells, data.length); i < count; i++) {
            final int col = i & 0x0F;
            final int row = i / 0x0F;

            final int dx = Math.min(Math.min(
                Math.abs(col - selectedX),
                Math.abs(col - selectedX + 0x0F)),
                Math.abs(col - selectedX - 0x0F)
            );
            final int dy = Math.min(Math.min(
                Math.abs(row - selectedY),
                Math.abs(row - selectedY + 0x0F)),
                Math.abs(row - selectedY - 0x0F));
            final double distance = Math.sqrt(dx * dx + dy * dy);
            final float brightness = (float) Math.min(1, Math.max(0.8, 1 - distance / 32));
            final int color = Color.monochrome(brightness);

            API.smallFontRenderer.drawString(matrixStack, bufferFactory, String.format("%02X", data[i]), color, 2);

            if (col < 0x0F) {
                matrixStack.translate(CELL_WIDTH, 0, 0);
            } else {
                matrixStack.translate(-CELL_WIDTH * 0x0F, CELL_HEIGHT, 0);
            }
        }
        matrixStack.pop();
    }

    private void drawSelectionBox(final MatrixStack matrixStack) {
        final int visibleCells = (int) (System.currentTimeMillis() - initTime) * 2;
        if (selectedCell > visibleCells) {
            return;
        }

        final int col = selectedCell & 0x0F;
        final int row = (selectedCell & 0xF0) >> 4;

        final int x = guiLeft + GRID_LEFT + CELL_WIDTH * col - 1;
        final int y = guiTop + GRID_TOP + CELL_HEIGHT * row - 1;

        matrixStack.push();
        matrixStack.translate(x, y, 0);

        getMinecraft().getTextureManager().bindTexture(Textures.LOCATION_GUI_MEMORY);
        final int vPos = (int) (getMinecraft().world.getGameTime() % 16) * 8;
        blit(matrixStack, 0, 0, 256 - (CELL_WIDTH + 1), vPos, 11, 8);

        matrixStack.pop();
    }
}
