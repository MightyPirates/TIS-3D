/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.gui;

import li.cil.tis3d.api.API;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.container.ReadOnlyMemoryModuleContainer;
import li.cil.tis3d.common.module.RandomAccessMemoryModule;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.ClientReadOnlyMemoryModuleDataMessage;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public final class ReadOnlyMemoryModuleScreen extends AbstractContainerScreen<ReadOnlyMemoryModuleContainer> {
    private static final int GRID_LEFT = 25;
    private static final int GRID_TOP = 13;
    private static final int CELL_WIDTH = 10;
    private static final int CELL_HEIGHT = 7;
    private static final String LABEL_INITIALIZING = "INITIALIZING...";

    private final byte[] data = new byte[RandomAccessMemoryModule.MEMORY_SIZE];

    private static int selectedCell;
    private boolean highNibble = true;
    private boolean receivedData;
    private long initTime;

    public ReadOnlyMemoryModuleScreen(final ReadOnlyMemoryModuleContainer container, final Inventory playerInventory, final Component title) {
        super(container, playerInventory, title);

        imageWidth = 190;
        imageHeight = 130;
    }

    public void setData(final byte[] data) {
        System.arraycopy(data, 0, this.data, 0, Math.min(data.length, this.data.length));
        receivedData = true;
        initTime = System.currentTimeMillis();
    }

    // --------------------------------------------------------------------- //
    // GuiScreen

    @Override
    public void removed() {
        super.removed();

        // Only send to server if our data is actually based on the old server
        // data to avoid erasing ROM when closing UI again too quickly.
        if (receivedData) {
            // Save any changes made and send them to the server.
            Network.sendToServer(new ClientReadOnlyMemoryModuleDataMessage(menu.getHand(), data));
        }
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        // Draw row and column headers.
        drawHeaders(graphics);

        // Draw/fade out initializing info text.
        drawInitializing(graphics);

        if (!receivedData) {
            return;
        }

        // Draw memory cells being edited.
        drawMemory(graphics);

        // Draw marker around currently selected memory cell.
        drawSelectionBox(graphics);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int x, final int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, Textures.LOCATION_GUI_MEMORY,
            leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int x, final int y) {
        // Suppress rendering of labels.
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean isDoubleClick) {
        selectCellAt(event.x(), event.y());

        return true;
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double dragX, final double dragY) {
        selectCellAt(event.x(), event.y());

        return true;
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        final int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }

        if (!receivedData) {
            return false;
        }

        final int digit = Character.digit(keyCode, 16);
        if (digit >= 0) {
            int value = data[selectedCell];
            if (highNibble) {
                value &= 0x0F;
                value |= (digit & 0x0F) << 4;
            } else {
                value &= 0xF0;
                value |= digit & 0x0F;
            }
            data[selectedCell] = (byte) value;
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
                case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_H -> {
                    if (col == 0) {
                        col = 15;
                        row = (row - 1 + 16) % 16;
                    } else {
                        --col;
                    }
                }
                case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_L -> {
                    if (col == 15) {
                        col = 0;
                        row = (row + 1) % 16;
                    } else {
                        ++col;
                    }
                }
                case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_K -> row = (row - 1 + 16) % 16;
                case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_J -> row = (row + 1) % 16;
                default -> {
                    return false;
                }
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

    private Minecraft getMinecraft() {
        return Objects.requireNonNull(minecraft);
    }

    private void selectCellAt(final double mouseX, final double mouseY) {
        if (!receivedData) {
            return;
        }

        final int col = (int) ((mouseX + 1 - leftPos - GRID_LEFT) / CELL_WIDTH);
        final int row = (int) ((mouseY + 1 - topPos - GRID_TOP) / CELL_HEIGHT);

        if (isInGridArea(col, row)) {
            selectedCell = (row << 4) | col;
            highNibble = true;
        }
    }

    private boolean isInGridArea(final int col, final int row) {
        return col >= 0 && row >= 0 && col <= 0xF && row <= 0xF;
    }

    private void drawHeaders(final GuiGraphics graphics) {
        // Columns headers (top).
        final var matrixStack = graphics.pose();
        matrixStack.pushMatrix();
        matrixStack.translate(leftPos + GRID_LEFT + 3, topPos + 6);
        for (int col = 0; col < 16; col++) {
            API.smallFontRenderer.draw(graphics, String.format("%X", col), Color.GUI_TEXT);
            matrixStack.translate(CELL_WIDTH, 0);
        }
        matrixStack.popMatrix();

        // Row headers (left).
        matrixStack.pushMatrix();
        matrixStack.translate(leftPos + 7, topPos + 14);
        for (int row = 0; row < 16; row++) {
            API.smallFontRenderer.draw(graphics, String.format("0X%X0", row), Color.GUI_TEXT);
            matrixStack.translate(0, CELL_HEIGHT);
        }
        matrixStack.popMatrix();
    }

    private void drawInitializing(final GuiGraphics graphics) {
        final float sinceInitialized = (System.currentTimeMillis() - initTime) / 1000f;
        if (receivedData && sinceInitialized > 0.5f) {
            return;
        }

        final float alpha = 1 - sinceInitialized / 0.5f;
        final int color = Color.withAlpha(Color.WHITE, alpha);

        final int labelWidth = API.smallFontRenderer.width(LABEL_INITIALIZING);

        final var matrixStack = graphics.pose();
        matrixStack.pushMatrix();
        matrixStack.translate((float) (leftPos + GRID_LEFT + 3 + 7 * CELL_WIDTH - labelWidth / 2), topPos + GRID_TOP + 1 + 7 * CELL_HEIGHT);
        API.smallFontRenderer.draw(graphics, LABEL_INITIALIZING, color);
        matrixStack.popMatrix();
    }

    private void drawMemory(final GuiGraphics graphics) {
        final int visibleCells = (int) (System.currentTimeMillis() - initTime);

        final int selectedX = selectedCell & 0x0F;
        final int selectedY = selectedCell / 0x0F;

        final var matrixStack = graphics.pose();
        matrixStack.pushMatrix();
        matrixStack.translate(leftPos + GRID_LEFT + 1, topPos + GRID_TOP + 1);
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

            API.smallFontRenderer.draw(graphics, String.format("%02X", data[i]), color);

            if (col < 0x0F) {
                matrixStack.translate(CELL_WIDTH, 0);
            } else {
                matrixStack.translate(-CELL_WIDTH * 0x0F, CELL_HEIGHT);
            }
        }
        matrixStack.popMatrix();
    }

    private void drawSelectionBox(final GuiGraphics graphics) {
        final int visibleCells = (int) (System.currentTimeMillis() - initTime) * 2;
        if (selectedCell > visibleCells) {
            return;
        }

        final int col = selectedCell & 0x0F;
        final int row = (selectedCell & 0xF0) >> 4;

        final int x = leftPos + GRID_LEFT + CELL_WIDTH * col - 1;
        final int y = topPos + GRID_TOP + CELL_HEIGHT * row - 1;

        final var matrixStack = graphics.pose();
        matrixStack.pushMatrix();
        matrixStack.translate(x, y);

        final var level = getMinecraft().level;
        final int vPos = level != null ? (int) (level.getGameTime() % 16) * 8 : 0;
        graphics.blit(RenderPipelines.GUI_TEXTURED, Textures.LOCATION_GUI_MEMORY,
            0, 0, 256 - (CELL_WIDTH + 1), vPos, 11, 8, 256, 256);

        matrixStack.popMatrix();
    }
}
