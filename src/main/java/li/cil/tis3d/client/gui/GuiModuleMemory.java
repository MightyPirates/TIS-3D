package li.cil.tis3d.client.gui;

import li.cil.tis3d.api.FontRendererAPI;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.common.module.ModuleRandomAccessMemory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Created by TheCodeWarrior
 */
public class GuiModuleMemory extends GuiScreen {
    private static final int GUI_WIDTH = 182;
    private static final int GUI_HEIGHT = 122;

    private static final int GRID_LEFT = 21;
    private static final int GRID_TOP = 9;
    private static final int CELL_WIDTH = 9;
    private static final int CELL_HEIGHT = 6;

    private final ModuleRandomAccessMemory  module;

    private int guiX = 0;
    private int guiY = 0;

    private int selectedCell = -1;
    private boolean highBit = true;

    public GuiModuleMemory(final ModuleRandomAccessMemory module) {
        this.module = module;
    }

    public boolean isFor(final ModuleRandomAccessMemory that) {
        return that == module;
    }

    @Override
    public void initGui() {
        super.initGui();
        guiX = (width - GUI_WIDTH) / 2;
        guiY = (height - GUI_HEIGHT) / 2;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int col = (mouseX - guiX - GRID_LEFT) / (CELL_WIDTH+1);
        int row = (mouseY - guiY - GRID_TOP) / (CELL_HEIGHT+1);

        selectedCell = -1;
        if(col >= 0 && row >= 0 && col <= 0xF && row <= 0xF) { // not outside the grid entirely

            int insideX = (mouseX - guiX - GRID_LEFT) - (CELL_WIDTH+1)*col;
            int insideY = (mouseY - guiY - GRID_TOP) - (CELL_HEIGHT+1)*row;

            if(insideX < CELL_WIDTH && insideY < CELL_HEIGHT) { // not in the 1px border between cells
                selectedCell = (row << 4) | col;
                highBit = true;
            }

        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if(selectedCell >= 0) {
            int digit = Character.digit(typedChar, 16);
            if(digit >= 0) {
                if(highBit) {
                    int value = module.get((byte)selectedCell);
                    value &= 0x0F;
                    value |= (digit & 0x0F) << 4;
                    module.set((byte)selectedCell, value);
                    module.sendSingle((byte)selectedCell);
                } else {
                    int value = module.get((byte)selectedCell);
                    value &= 0xF0;
                    value |= digit & 0x0F;
                    module.set((byte)selectedCell, value);
                    module.sendSingle((byte)selectedCell);
                }
                highBit = !highBit;
            } else if(keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                module.set((byte)selectedCell, 0);
                module.sendSingle((byte)selectedCell);
                highBit = true;
            } else {
                int col = selectedCell & 0x0F;
                int row = (selectedCell & 0xF0) >> 4;
                int prevCol = col, prevRow = row;

                switch (keyCode) {
                    case Keyboard.KEY_LEFT:
                    case Keyboard.KEY_H: // cause you gotta have vim keybinds. Absolutely required.
                        col--;
                        break;
                    case Keyboard.KEY_RIGHT:
                    case Keyboard.KEY_L:
                        col++;
                        break;
                    case Keyboard.KEY_UP:
                    case Keyboard.KEY_K:
                        row--;
                        break;
                    case Keyboard.KEY_DOWN:
                    case Keyboard.KEY_J:
                        row++;
                        break;
                }
                if(col < 0) col = 0;
                if(col > 15) col = 15;
                if(row < 0) row = 0;
                if(row > 15) row = 15;

                if(row != prevRow || col != prevCol) {
                    selectedCell = (row << 4) | col;
                    highBit = true;
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.translate(guiX, guiY, 0);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureLoader.LOCATION_GUI_MEMORY);
        this.drawTexturedModalRect(0, 0, 0, 0, GUI_WIDTH, GUI_HEIGHT);


        int prevX = 0, prevY = 0; // only translate by difference to avoid having to untranslate each time. For performance
        for(int i = 0; i < module.size(); i++) {
            int value = module.get((byte)i);
            int col = i & 0x0F;
            int row = (i & 0xF0) >> 4;

            int x = GRID_LEFT + (CELL_WIDTH+1)*col + 1; // +1 for text offset. w/o +1 it is the outer box pos.
            int y = GRID_TOP + (CELL_HEIGHT+1)*row + 1;

            GlStateManager.translate(x-prevX, y-prevY, 0);

            // replace because the 'O' character is clearer than the '0' character.
            FontRendererAPI.drawString(String.format("%02X", value).replace('0','O'));

            if(i == selectedCell) {
                drawSelectionBox();
            }

            prevX = x;
            prevY = y;
        }

    }

    private void drawSelectionBox() {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureLoader.LOCATION_GUI_MEMORY);
        int vPos = (int) (Minecraft.getMinecraft().world.getTotalWorldTime() % 16) * 8;
        this.drawTexturedModalRect(-2, -2, 185, vPos, 11, 8);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
