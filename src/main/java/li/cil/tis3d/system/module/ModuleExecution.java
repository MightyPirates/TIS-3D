package li.cil.tis3d.system.module;

import com.google.common.base.Strings;
import li.cil.tis3d.api.FontRendererAPI;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.AbstractModuleRotatable;
import li.cil.tis3d.client.render.TextureLoader;
import li.cil.tis3d.common.TIS3D;
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

import java.util.Optional;

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
        ERR,
        RUN,
        WAIT
    }

    private static final String[] STATE_LOCATIONS = new String[]{
            null,
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_ERROR.toString(),
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING.toString(),
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_WAITING.toString()
    };

    // NBT tag names.
    private static final String TAG_FULL = "full";
    private static final String TAG_STATE = "state";
    private static final String TAG_MACHINE = "machine";
    private static final String TAG_COMPILE_ERROR = "compileError";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_LINE_NUMBER = "lineNumber";
    private static final String TAG_COLUMN = "column";
    private static final String TAG_PC = MachineState.TAG_PC;
    private static final String TAG_ACC = MachineState.TAG_ACC;
    private static final String TAG_BAK = MachineState.TAG_BAK;
    private static final String TAG_LAST = MachineState.TAG_LAST;

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
            state = State.ERR;
        } else if (machine.getState().instructions.isEmpty()) {
            state = State.IDLE;
        } else {
            if (machine.step()) {
                state = State.RUN;
                getCasing().markDirty();
                sendData(false);
                return; // Don't send data twice.
            } else {
                state = State.WAIT;
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
        if (nbt.getBoolean(TAG_FULL)) {
            readFromNBT(nbt);
        } else {
            machine.getState().pc = nbt.getInteger(TAG_PC);
            machine.getState().acc = nbt.getInteger(TAG_ACC);
            machine.getState().bak = nbt.getInteger(TAG_BAK);
            if (nbt.hasKey(TAG_LAST)) {
                try {
                    machine.getState().last = Optional.of(Enum.valueOf(Port.class, nbt.getString(TAG_LAST)));
                } catch (final IllegalArgumentException e) {
                    TIS3D.getLog().warn("Invalid machine state received.", e);
                }
            } else {
                machine.getState().last = Optional.empty();
            }
            if (nbt.hasKey(TAG_STATE)) {
                try {
                    state = Enum.valueOf(State.class, nbt.getString(TAG_STATE));
                } catch (final IllegalArgumentException e) {
                    TIS3D.getLog().warn("Invalid executable module state received.", e);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled && !isPlayerLookingAt()) {
            return;
        }

        rotateForRendering();

        RenderHelper.disableStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 0 / 1.0F);

        final MachineState machineState = machine.getState();

        // Draw status texture.
        if (state != State.IDLE) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            final TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(STATE_LOCATIONS[state.ordinal()]);
            drawQuad(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());
        }

        // Render detailed state when player is close.
        if (machineState.code != null && Minecraft.getMinecraft().thePlayer.getDistanceSqToCenter(getCasing().getPosition()) < 64) {
            renderState(machineState);
        }

        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        try {
            final NBTTagCompound machineNbt = nbt.getCompoundTag(TAG_MACHINE);
            machine.getState().readFromNBT(machineNbt);
            state = Enum.valueOf(State.class, nbt.getString(TAG_STATE));
        } catch (final IllegalArgumentException e) {
            // This can only happen if someone messes with the save.
            TIS3D.getLog().warn("Broken save, execution module state is invalid.", e);
        }

        if (nbt.hasKey(TAG_COMPILE_ERROR)) {
            final NBTTagCompound errorNbt = nbt.getCompoundTag(TAG_COMPILE_ERROR);
            compileError = new ParseException(errorNbt.getString(TAG_MESSAGE), errorNbt.getInteger(TAG_LINE_NUMBER), errorNbt.getInteger(TAG_COLUMN));
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        final NBTTagCompound machineNbt = new NBTTagCompound();
        machine.getState().writeToNBT(machineNbt);
        nbt.setTag(TAG_MACHINE, machineNbt);
        nbt.setString(TAG_STATE, state.name());

        if (compileError != null) {
            final NBTTagCompound errorNbt = new NBTTagCompound();
            errorNbt.setString(TAG_MESSAGE, compileError.getMessage());
            errorNbt.setInteger(TAG_LINE_NUMBER, compileError.getLineNumber());
            errorNbt.setInteger(TAG_COLUMN, compileError.getColumn());
            nbt.setTag(TAG_COMPILE_ERROR, errorNbt);
        }
    }

    // --------------------------------------------------------------------- //

    private static boolean isCodeSource(final ItemStack stack) {
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

    private static String getSourceCode(final ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return null;
        }

        final NBTTagCompound nbt = stack.getTagCompound();
        final NBTTagList pages = nbt.getTagList("pages", Constants.NBT.TAG_STRING);
        if (pages.tagCount() < 1) {
            return null;
        }

        final StringBuilder code = new StringBuilder();
        for (int page = 0; page < pages.tagCount(); page++) {
            code.append(pages.getStringTagAt(page)).append('\n');
        }
        return code.toString();
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
                player.addChatMessage(new ChatComponentText(String.format("Compile error @%s.", e)));
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
        nbt.setBoolean(TAG_FULL, full);
        if (full) {
            writeToNBT(nbt);
        } else {
            nbt.setInteger(TAG_PC, machine.getState().pc);
            nbt.setInteger(TAG_ACC, machine.getState().acc);
            nbt.setInteger(TAG_BAK, machine.getState().bak);
            machine.getState().last.ifPresent(last -> nbt.setString(TAG_LAST, last.name()));
            nbt.setString(TAG_STATE, state.name());
        }
        getCasing().sendData(getFace(), nbt);
    }

    @SideOnly(Side.CLIENT)
    private void renderState(final MachineState machineState) {
        // Offset to start drawing at top left of inner area, slightly inset.
        GlStateManager.translate(3.5f / 16f, 3.5f / 16f, 0);
        GlStateManager.scale(1 / 128f, 1 / 128f, 1);
        GlStateManager.translate(1, 1, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);

        // Draw register info on top.
        final String accLast = String.format("ACC:%4X LAST:%s", (short) machineState.acc, machineState.last.map(Enum::name).orElse("NONE"));
        FontRendererAPI.drawString(accLast);
        GlStateManager.translate(0, FontRendererAPI.getCharHeight() + 4, 0);
        final String bakState = String.format("BAK:%4X MODE:%s", (short) machineState.bak, state.name());
        FontRendererAPI.drawString(bakState);
        GlStateManager.translate(0, FontRendererAPI.getCharHeight() + 4, 0);
        drawLine(1);
        GlStateManager.translate(0, 5, 0);

        // If we have more lines than fit on our "screen", offset so that the
        // current line is in the middle, but don't let last line scroll in.
        final int maxLines = 50 / (FontRendererAPI.getCharHeight() + 1);
        final int totalLines = machineState.code.length;
        final int currentLine;
        if (machineState.lineNumbers.size() > 0) {
            currentLine = machineState.lineNumbers.get(machineState.pc);
        } else if (compileError != null) {
            currentLine = compileError.getLineNumber();
        } else {
            currentLine = -1;
        }
        final int page = currentLine / maxLines;
        final int offset = page * maxLines;
        for (int lineNumber = offset; lineNumber < Math.min(totalLines, offset + maxLines); lineNumber++) {
            final String line = machineState.code[lineNumber];
            if (lineNumber == currentLine) {
                if (state == State.WAIT) {
                    GlStateManager.color(0.66f, 0.66f, 0.66f);
                } else if (state == State.ERR) {
                    GlStateManager.color(1f, 0f, 0f);
                }

                drawLine(FontRendererAPI.getCharHeight());

                GlStateManager.color(0f, 0f, 0f);
            } else {
                GlStateManager.color(1f, 1f, 1f);
            }

            FontRendererAPI.drawString(line, 18);

            GlStateManager.translate(0, FontRendererAPI.getCharHeight() + 1, 0);
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
        worldRenderer.pos(71.5f, height + 0.5f, 0).endVertex();
        worldRenderer.pos(71.5f, -0.5f, 0).endVertex();
        worldRenderer.pos(-0.5f, -0.5f, 0).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
    }
}
