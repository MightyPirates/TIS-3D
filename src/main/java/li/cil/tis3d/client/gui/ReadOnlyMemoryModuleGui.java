package li.cil.tis3d.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.FontRendererAPI;
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.module.RandomAccessMemoryModule;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.ReadOnlyMemoryModuleDataMessage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public final class ReadOnlyMemoryModuleGui extends Screen {
    private static final int GUI_WIDTH = 190;
    private static final int GUI_HEIGHT = 130;

    private static final int GRID_LEFT = 25;
    private static final int GRID_TOP = 13;
    private static final int CELL_WIDTH = 10;
    private static final int CELL_HEIGHT = 7;
    private static final String LABEL_INITIALIZING = "INITIALIZING...";

    private final PlayerEntity player;
    private final Hand hand;
    private final byte[] data = new byte[RandomAccessMemoryModule.MEMORY_SIZE];

    private int guiX = 0;
    private int guiY = 0;
    private static int selectedCell = 0;
    private boolean highNibble = true;
    private boolean receivedData;
    private long initTime;

    ReadOnlyMemoryModuleGui(final PlayerEntity player, final Hand hand) {
        super(new LiteralText("Read-Only Module"));
        this.player = player;
        this.hand = hand;
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
        guiX = (width - GUI_WIDTH) / 2;
        guiY = (height - GUI_HEIGHT) / 2;

        minecraft.keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onClose() {
        super.onClose();

        // Only send to server if our data is actually based on the old server
        // data to avoid erasing ROM when closing UI again too quickly.
        if (receivedData) {
            // Save any changes made and send them to the server.
            Network.INSTANCE.sendToServer(new ReadOnlyMemoryModuleDataMessage(data, hand));
        }

        minecraft.keyboard.enableRepeatEvents(false);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        if (player.removed || !Items.isModuleReadOnlyMemory(player.getStackInHand(hand))) {
            minecraft.openScreen(null);
            return;
        }

        // Background.
        GlStateManager.color4f(1, 1, 1, 1);
        minecraft.getTextureManager().bindTexture(Textures.LOCATION_GUI_MEMORY);
        blit(guiX, guiY, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        // Draw row and column headers.
        drawHeaders();

        // Draw/fade out initializing info text.
        drawInitializing();

        if (!receivedData) {
            return;
        }

        // Draw memory cells being edited.
        drawMemory();

        // Draw marker around currently selected memory cell.
        drawSelectionBox();
    }

    @Override
    public boolean mouseClicked(final double mouseXd, final double mouseYd, final int mouseButton) {
        if (super.mouseClicked(mouseXd, mouseYd, mouseButton)) {
            return true;
        }

        final int mouseX = (int)Math.round(mouseXd);
        final int mouseY = (int)Math.round(mouseYd);

        selectCellAt(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean mouseDragged(final double mouseXd, final double mouseYd, final int clickedMouseButton, final double da, final double db) {
        if (super.mouseDragged(mouseXd, mouseYd, clickedMouseButton, da, db)) {
            return true;
        }

        final int mouseX = (int)Math.round(mouseXd);
        final int mouseY = (int)Math.round(mouseYd);

        selectCellAt(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scancode, final int mods) {
        if (super.keyPressed(keyCode, scancode, mods)) {
            return true;
        }

        if (!receivedData) {
            return false;
        }

        final int digit = Character.digit(keyCode, 16);
        if (digit >= 0) {
            if (highNibble) {
                byte value = data[selectedCell];
                value &= 0x0F;
                value |= (digit & 0x0F) << 4;
                data[selectedCell] = value;
            } else {
                byte value = data[selectedCell];
                value &= 0xF0;
                value |= digit & 0x0F;
                data[selectedCell] = value;
            }
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

    private void selectCellAt(final int mouseX, final int mouseY) {
        if (!receivedData) {
            return;
        }

        final int col = (mouseX + 1 - guiX - GRID_LEFT) / CELL_WIDTH;
        final int row = (mouseY + 1 - guiY - GRID_TOP) / CELL_HEIGHT;

        if (isInGridArea(col, row)) {
            selectedCell = (row << 4) | col;
            highNibble = true;
        }
    }

    private boolean isInGridArea(final int col, final int row) {
        return col >= 0 && row >= 0 && col <= 0xF && row <= 0xF;
    }

    private void drawHeaders() {
        GlStateManager.color4f(0.25f, 0.25f, 0.25f, 1);

        // Columns headers (top).
        GlStateManager.pushMatrix();
        GlStateManager.translatef(guiX + GRID_LEFT + 3, guiY + 6, 0);
        for (int col = 0; col < 16; col++) {
            FontRendererAPI.drawString(String.format("%X", col));
            GlStateManager.translatef(CELL_WIDTH, 0, 0);
        }
        GlStateManager.popMatrix();

        // Row headers (left).
        GlStateManager.pushMatrix();
        GlStateManager.translatef(guiX + 7, guiY + 14, 0);
        for (int row = 0; row < 16; row++) {
            FontRendererAPI.drawString(String.format("0X%X0", row));
            GlStateManager.translatef(0, CELL_HEIGHT, 0);
        }
        GlStateManager.popMatrix();
    }

    private void drawInitializing() {
        final float sinceInitialized = (System.currentTimeMillis() - initTime) / 1000f;
        if (receivedData && sinceInitialized > 0.5f) {
            return;
        }

        GlStateManager.color4f(1, 1, 1, 1 - sinceInitialized / 0.5f);
        GlStateManager.enableBlend();

        final int labelWidth = FontRendererAPI.getCharWidth() * LABEL_INITIALIZING.length();

        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)(guiX + GRID_LEFT + 3 + 7 * CELL_WIDTH - labelWidth / 2), guiY + GRID_TOP + 1 + 7 * CELL_HEIGHT, 0);
        FontRendererAPI.drawString(LABEL_INITIALIZING);
        GlStateManager.popMatrix();
    }

    private void drawMemory() {
        GlStateManager.color4f(1, 1, 1, 1);

        final int visibleCells = (int)(System.currentTimeMillis() - initTime);

        GlStateManager.pushMatrix();
        GlStateManager.translatef(guiX + GRID_LEFT + 1, guiY + GRID_TOP + 1, 0);
        for (int i = 0, count = Math.min(visibleCells, data.length); i < count; i++) {
            FontRendererAPI.drawString(String.format("%02X", data[i]));

            final int col = i & 0x0F;
            if (col < 0x0F) {
                GlStateManager.translatef(CELL_WIDTH, 0, 0);
            } else {
                GlStateManager.translatef(-CELL_WIDTH * 0x0F, CELL_HEIGHT, 0);
            }
        }
        GlStateManager.popMatrix();
    }

    private void drawSelectionBox() {
        final int visibleCells = (int)(System.currentTimeMillis() - initTime) * 2;
        if (selectedCell > visibleCells) {
            return;
        }

        final int col = selectedCell & 0x0F;
        final int row = (selectedCell & 0xF0) >> 4;

        final int x = guiX + GRID_LEFT + CELL_WIDTH * col - 1;
        final int y = guiY + GRID_TOP + CELL_HEIGHT * row - 1;

        GlStateManager.pushMatrix();
        GlStateManager.translatef(x, y, 0);

        minecraft.getTextureManager().bindTexture(Textures.LOCATION_GUI_MEMORY);
        final int vPos = (int)(minecraft.world.getTime() % 16) * 8;
        blit(0, 0, 256 - (CELL_WIDTH + 1), vPos, 11, 8);

        GlStateManager.popMatrix();
    }
}
