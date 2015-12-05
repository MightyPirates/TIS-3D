package li.cil.tis3d.system.module;

import com.google.common.base.Strings;
import li.cil.tis3d.TIS3D;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.prefab.AbstractModuleRotatable;
import li.cil.tis3d.client.TextureLoader;
import li.cil.tis3d.client.render.font.FontRendererTextureMonospace;
import li.cil.tis3d.system.module.execution.MachineImpl;
import li.cil.tis3d.system.module.execution.MachineState;
import li.cil.tis3d.system.module.execution.compiler.Compiler;
import li.cil.tis3d.system.module.execution.compiler.ParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The programmable execution module.
 */
public final class ModuleExecution extends AbstractModuleRotatable {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final MachineImpl machine;
    private ParseException compileError;
    private State state = State.IDLE;

    // --------------------------------------------------------------------- //
    // Computed data

    private enum State {
        IDLE,
        ERROR,
        RUNNING,
        WAITING
    }

    private static final String[] STATE_LOCATIONS = new String[]{
            null,
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_ERROR.toString(),
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING.toString(),
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_WAITING.toString()
    };

    // --------------------------------------------------------------------- //

    public ModuleExecution(final Casing casing, final Face face) {
        super(casing, face);
        machine = new MachineImpl(this, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        final State prevState = state;

        if (compileError != null) {
            state = State.ERROR;
        } else if (machine.getState().instructions.isEmpty()) {
            state = State.IDLE;
        } else {
            final int prevInstruction = machine.getState().pc;

            machine.step();

            if (machine.getState().pc != prevInstruction) {
                state = State.RUNNING;
                getCasing().markDirty();
                sendData(false);
                return; // Don't send data twice.
            } else {
                state = State.WAITING;
            }
        }

        if (prevState != state) {
            getCasing().markDirty();
            sendData(false);
        }
    }

    @Override
    public void onEnabled() {
        if (!getCasing().getCasingWorld().isRemote) {
            sendData(true);
        }
    }

    @Override
    public void onDisabled() {
        machine.getState().reset();
        state = State.IDLE;

        if (!getCasing().getCasingWorld().isRemote) {
            sendData(false);
        }
    }

    @Override
    public void onWriteComplete(final Port port) {
        if (compileError == null) {
            machine.onWriteCompleted(port);
        }
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return false;
        }

        final ItemStack stack = player.getHeldItem();
        if (!isCodeSource(stack)) {
            return false;
        }

        final String code = getSourceCode(stack);
        if (Strings.isNullOrEmpty(code)) {
            return true; // Handled, but does nothing.
        }

        if (!getCasing().getCasingWorld().isRemote) {
            compile(code, player);
            sendData(true);
        }

        return true;
    }

    @Override
    public void onData(final NBTTagCompound nbt) {
        if (nbt.hasKey("pc")) {
            machine.getState().pc = nbt.getInteger("pc");
            machine.getState().acc = nbt.getInteger("acc");
            machine.getState().bak = nbt.getInteger("bak");
            if (nbt.hasKey("state")) {
                try {
                    state = Enum.valueOf(State.class, nbt.getString("state"));
                } catch (final IllegalArgumentException ignored) {
                }
            }
        } else {
            readFromNBT(nbt);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled) {
            return;
        }

        final MachineState machineState = machine.getState();
        if (state == State.IDLE || machineState.code == null) {
            return;
        }

        rotateForRendering();

        RenderHelper.disableStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 0 / 1.0F);

        // Draw status texture.
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        final TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(STATE_LOCATIONS[state.ordinal()]);
        drawQuad(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());

        // Render detailed state when player is close.
        if (Minecraft.getMinecraft().thePlayer.getDistanceSqToCenter(getCasing().getPosition()) < 64) {
            renderState(machineState);
        }

        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey("compileError")) {
            final NBTTagCompound errorNbt = nbt.getCompoundTag("compileError");
            compileError = new ParseException(errorNbt.getString("message"), errorNbt.getInteger("lineNumber"), errorNbt.getInteger("column"));
        } else {
            try {
                if (nbt.hasKey("state")) {
                    state = Enum.valueOf(State.class, nbt.getString("state"));
                }

                // This way around to not even load the machine if the state is invalid.
                final NBTTagCompound machineNbt = nbt.getCompoundTag("machine");
                machine.getState().readFromNBT(machineNbt);
            } catch (final IllegalArgumentException e) {
                // This can only happen if someone messes with the save.
                TIS3D.getLog().warn("Broken save, execution module state is invalid.", e);
            }
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (compileError != null) {
            final NBTTagCompound errorNbt = new NBTTagCompound();
            errorNbt.setString("message", compileError.getMessage());
            errorNbt.setInteger("lineNumber", compileError.getLineNumber());
            errorNbt.setInteger("column", compileError.getColumn());
            nbt.setTag("compileError", errorNbt);
        } else {
            final NBTTagCompound machineNbt = new NBTTagCompound();
            machine.getState().writeToNBT(machineNbt);
            nbt.setTag("machine", machineNbt);
            nbt.setString("state", state.name());
        }
    }

    // --------------------------------------------------------------------- //

    private boolean isCodeSource(final ItemStack stack) {
        if (stack != null) {
            if (stack.getItem() == Items.written_book) {
                return true;
            }
            if (stack.getItem() == Items.writable_book) {
                return true;
            }
        }

        return false;
    }

    private String getSourceCode(final ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return null;
        }

        final NBTTagCompound nbt = stack.getTagCompound();
        final NBTTagList pages = nbt.getTagList("pages", Constants.NBT.TAG_STRING);
        if (pages.tagCount() < 1) {
            return null;
        }

        return pages.getStringTagAt(0);
    }

    /**
     * Compile the specified lines of code, assuming this was issued by the
     * specified player (for notifications on errors). The code will be
     * compiled into the module's machine state. On errors, the state will
     * be left in a reset state.
     *
     * @param code   the code to compile.
     * @param player the player that issued the compile, or <tt>null</tt>.
     */
    private void compile(final String code, final EntityPlayer player) {
        compileError = null;
        try {
            machine.getState().clear();
            Compiler.compile(code, machine.getState());
        } catch (final ParseException e) {
            compileError = e;
            if (player != null) {
                player.addChatMessage(new ChatComponentText(e.toString()));
            }
        }
    }

    /**
     * Send the current execution state to the client.
     * <p>
     * May be used to send the full state in case of larger changes, such as
     * the program we're running being changed.
     *
     * @param full if <tt>true</tt>, the full machine state will be sent.
     */
    private void sendData(final boolean full) {
        final NBTTagCompound nbt = new NBTTagCompound();
        if (full) {
            writeToNBT(nbt);
        } else {
            nbt.setInteger("pc", machine.getState().pc);
            nbt.setInteger("acc", machine.getState().acc);
            nbt.setInteger("bak", machine.getState().bak);
            nbt.setString("state", state.name());
        }
        getCasing().sendData(getFace(), nbt);
    }

    @SideOnly(Side.CLIENT)
    private void renderState(final MachineState machineState) {
        // Offset to start drawing at top left of inner area, slightly inset.
        GlStateManager.translate(4 / 16f, 4 / 16f, 0);
        GlStateManager.scale(1 / 128f, 1 / 128f, 1);
        GlStateManager.translate(1, 1, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);

        // Draw register info on top.
        final String registers = String.format("ACC:%3d BAK:%3d", machineState.acc, machineState.bak);
        FontRendererTextureMonospace.drawString(registers);
        GlStateManager.translate(0, FontRendererTextureMonospace.CHAR_HEIGHT + 4, 0);
        drawLine(1);
        GlStateManager.translate(0, 5, 0);

        // If we have more lines than fit on our "screen", offset so that the
        // current line is in the middle, but don't let last line scroll in.
        final int maxLines = 50 / (FontRendererTextureMonospace.CHAR_HEIGHT + 1);
        final int totalLines = machineState.code.length;
        final int currentLine = machineState.lineNumbers.get(machineState.pc);
        final int page = currentLine / maxLines;
        final int offset = page * maxLines;
        for (int lineNumber = offset; lineNumber < Math.min(totalLines, offset + maxLines); lineNumber++) {
            final String line = machineState.code[lineNumber];
            if (lineNumber == currentLine) {
                if (state == State.WAITING) {
                    GlStateManager.color(0.66f, 0.66f, 0.66f);
                }

                drawLine(FontRendererTextureMonospace.CHAR_HEIGHT);

                GlStateManager.color(0f, 0f, 0f);
            } else {
                GlStateManager.color(1f, 1f, 1f);
            }

            FontRendererTextureMonospace.drawString(line, 15);

            GlStateManager.translate(0, FontRendererTextureMonospace.CHAR_HEIGHT + 1, 0);
        }
    }

    /**
     * Draws a horizontal line of the specified height.
     *
     * @param height the height of the line to draw.
     */
    @SideOnly(Side.CLIENT)
    private static void drawLine(final int height) {
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();

        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(-0.5f, height + 0.5f, 0).endVertex();
        worldRenderer.pos(62.5f, height + 0.5f, 0).endVertex();
        worldRenderer.pos(62.5f, -0.5f, 0).endVertex();
        worldRenderer.pos(-0.5f, -0.5f, 0).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
    }
}
