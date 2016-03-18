package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.FontRendererAPI;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.BlockChangeAware;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.render.TextureLoader;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.module.execution.MachineImpl;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The programmable execution module.
 */
public final class ModuleExecution extends AbstractModuleRotatable implements BlockChangeAware {
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
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_IDLE.toString(),
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_ERROR.toString(),
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING.toString(),
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_WAITING.toString()
    };

    // NBT tag names.
    private static final String TAG_STATE = "state";
    private static final String TAG_MACHINE = "machine";
    private static final String TAG_COMPILE_ERROR = "compileError";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_LINE_NUMBER = "lineNumber";
    private static final String TAG_START = "columnStart";
    private static final String TAG_END = "columnEnd";

    // Data packet types.
    private static final byte DATA_TYPE_FULL = 0;
    private static final byte DATA_TYPE_INCREMENTAL = 1;

    // --------------------------------------------------------------------- //

    public ModuleExecution(final Casing casing, final Face face) {
        super(casing, face);
        machine = new MachineImpl(this, face);
    }

    public MachineState getState() {
        return machine.getState();
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        assert (!getCasing().getCasingWorld().isRemote);

        final State prevState = state;

        if (compileError != null) {
            state = State.ERR;
        } else if (getState().instructions.isEmpty()) {
            state = State.IDLE;
        } else {
            if (machine.step()) {
                state = State.RUN;
                getCasing().markDirty();
                sendPartialState();
                return; // Don't send data twice.
            } else {
                state = State.WAIT;
            }
        }

        if (prevState != state) {
            getCasing().markDirty();
            sendPartialState();
        }
    }

    @Override
    public void onEnabled() {
        assert (!getCasing().getCasingWorld().isRemote);

        sendFullState();
    }

    @Override
    public void onDisabled() {
        assert (!getCasing().getCasingWorld().isRemote);

        getState().reset();
        state = State.IDLE;

        sendPartialState();
    }

    @Override
    public void onWriteComplete(final Port port) {
        if (compileError == null) {
            machine.onWriteCompleted(port);
        }
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand hand, final ItemStack heldItem, final float hitX, final float hitY, final float hitZ) {
        // Vanilla book? If so, make that a code book.
        if (heldItem != null && heldItem.getItem() == net.minecraft.init.Items.book) {
            if (!player.getEntityWorld().isRemote) {
                if (!player.capabilities.isCreativeMode) {
                    heldItem.splitStack(1);
                }
                final ItemStack bookCode = new ItemStack(Items.bookCode);
                if (player.inventory.addItemStackToInventory(bookCode)) {
                    player.inventoryContainer.detectAndSendChanges();
                }
                if (bookCode.stackSize > 0) {
                    player.dropItem(bookCode, false, false);
                }
            }

            return true;
        }

        // Code book? Store current program on it if sneaking.
        if (Items.isBookCode(heldItem) && player.isSneaking()) {
            final ItemBookCode.Data data = ItemBookCode.Data.loadFromStack(heldItem);
            if (getState().code != null && getState().code.length > 0) {
                data.addProgram(Arrays.asList(getState().code));
                ItemBookCode.Data.saveToStack(heldItem, data);
            }

            return true;
        }

        // If sneaking otherwise, ignore interaction.
        if (player.isSneaking()) {
            return false;
        }

        // Following is programming. If the casing is locked, don't allow changing code.
        if (getCasing().isLocked()) {
            return false;
        }

        // Get the provider for the item, if any.
        final SourceCodeProvider provider = providerFor(heldItem);
        if (provider == null) {
            return false;
        }

        // Get the code from the item, if any.
        final Iterable<String> code = provider.codeFor(heldItem);
        if (code == null || !code.iterator().hasNext()) {
            return true; // Handled, but does nothing.
        }

        // Compile the code into our machine state.
        if (!getCasing().getCasingWorld().isRemote) {
            compile(code, player);
            sendFullState();
        }

        return true;
    }

    @Override
    public void onData(final NBTTagCompound nbt) {
        readFromNBT(nbt);
    }

    @Override
    public void onData(final ByteBuf data) {
        getState().pc = data.readShort();
        getState().acc = data.readShort();
        getState().bak = data.readShort();
        if (data.readBoolean()) {
            getState().last = Optional.of(Port.values()[data.readByte()]);
        } else {
            getState().last = Optional.empty();
        }
        state = State.values()[data.readByte()];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if ((!enabled || !isVisible()) && !isPlayerLookingAt()) {
            return;
        }

        rotateForRendering();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);

        final MachineState machineState = getState();

        // Draw status texture.
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        final TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(STATE_LOCATIONS[state.ordinal()]);
        RenderUtil.drawQuad(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());

        // Render detailed state when player is close.
        if (machineState.code != null && Minecraft.getMinecraft().thePlayer.getDistanceSqToCenter(getCasing().getPosition()) < 64) {
            renderState(machineState);
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final NBTTagCompound machineNbt = nbt.getCompoundTag(TAG_MACHINE);
        getState().readFromNBT(machineNbt);
        state = EnumUtils.readFromNBT(State.class, TAG_STATE, nbt);

        if (nbt.hasKey(TAG_COMPILE_ERROR)) {
            final NBTTagCompound errorNbt = nbt.getCompoundTag(TAG_COMPILE_ERROR);
            compileError = new ParseException(errorNbt.getString(TAG_MESSAGE), errorNbt.getInteger(TAG_LINE_NUMBER), errorNbt.getInteger(TAG_START), errorNbt.getInteger(TAG_END));
        } else {
            compileError = null;
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        final NBTTagCompound machineNbt = new NBTTagCompound();
        getState().writeToNBT(machineNbt);
        nbt.setTag(TAG_MACHINE, machineNbt);
        EnumUtils.writeToNBT(state, TAG_STATE, nbt);

        if (compileError != null) {
            final NBTTagCompound errorNbt = new NBTTagCompound();
            errorNbt.setString(TAG_MESSAGE, compileError.getMessage());
            errorNbt.setInteger(TAG_LINE_NUMBER, compileError.getLineNumber());
            errorNbt.setInteger(TAG_START, compileError.getStart());
            errorNbt.setInteger(TAG_END, compileError.getEnd());
            nbt.setTag(TAG_COMPILE_ERROR, errorNbt);
        }
    }

    // --------------------------------------------------------------------- //
    // BlockChangeAware

    @Override
    public void onNeighborBlockChange(final Block neighborBlock) {
        if (isVisible()) {
            sendPartialState();
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Compile the specified lines of code, assuming this was issued by the
     * specified player (for notifications on errors). The code will be
     * compiled into the module's machine state. On errors, the state will
     * be left in a reset state.
     *
     * @param code   the code to compile.
     * @param player the player that issued the compile, or <tt>null</tt>.
     */
    public void compile(final Iterable<String> code, final EntityPlayer player) {
        if (getCasing().getCasingWorld().isRemote) {
            return; // When called from ItemBookCode e.g.
        }

        compileError = null;
        try {
            getState().clear();
            Compiler.compile(code, getState());
        } catch (final ParseException e) {
            compileError = e;
            if (player != null) {
                player.addChatMessage(new TextComponentString(String.format("Compile error @%s.", e)));
            }
        }
    }

    /**
     * Send the full state to the client.
     */
    private void sendFullState() {
        final NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        getCasing().sendData(getFace(), nbt, DATA_TYPE_FULL);
    }

    /**
     * Send the current execution state to the client.
     */
    private void sendPartialState() {
        if (!isVisible()) {
            return;
        }

        final ByteBuf data = Unpooled.buffer();

        data.writeShort((short) getState().pc);
        data.writeShort(getState().acc);
        data.writeShort(getState().bak);
        data.writeBoolean(getState().last.isPresent());
        if (getState().last.isPresent()) {
            data.writeByte((byte) getState().last.get().ordinal());
        }
        data.writeByte(state.ordinal());

        getCasing().sendData(getFace(), data, DATA_TYPE_INCREMENTAL);
    }

    @SideOnly(Side.CLIENT)
    private void renderState(final MachineState machineState) {
        // Offset to start drawing at top left of inner area, slightly inset.
        GlStateManager.translate(3.5f / 16f, 3.5f / 16f, 0);
        GlStateManager.scale(1 / 128f, 1 / 128f, 1);
        GlStateManager.translate(1, 1, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);

        // Draw register info on top.
        final String accLast = String.format("ACC:%4X LAST:%s", machineState.acc, machineState.last.map(Enum::name).orElse("NONE"));
        FontRendererAPI.drawString(accLast);
        GlStateManager.translate(0, FontRendererAPI.getCharHeight() + 4, 0);
        final String bakState = String.format("BAK:%4X MODE:%s", machineState.bak, state.name());
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
            currentLine = Optional.ofNullable(machineState.lineNumbers.get(machineState.pc)).orElse(-1);
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
                } else if (state == State.ERR || compileError != null && compileError.getLineNumber() == currentLine) {
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
        final VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.pos(-0.5f, height + 0.5f, 0).endVertex();
        buffer.pos(71.5f, height + 0.5f, 0).endVertex();
        buffer.pos(71.5f, -0.5f, 0).endVertex();
        buffer.pos(-0.5f, -0.5f, 0).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
    }

    // --------------------------------------------------------------------- //

    private interface SourceCodeProvider {
        boolean worksFor(ItemStack stack);

        Iterable<String> codeFor(ItemStack stack);
    }

    private static final class SourceCodeProviderVanilla implements SourceCodeProvider {
        @Override
        public boolean worksFor(final ItemStack stack) {
            return (stack.getItem() == net.minecraft.init.Items.written_book) || (stack.getItem() == net.minecraft.init.Items.writable_book);
        }

        @Override
        public Iterable<String> codeFor(final ItemStack stack) {
            if (!stack.hasTagCompound()) {
                return null;
            }

            final NBTTagCompound nbt = stack.getTagCompound();
            final NBTTagList pages = nbt.getTagList("pages", net.minecraftforge.common.util.Constants.NBT.TAG_STRING);
            if (pages.tagCount() < 1) {
                return null;
            }

            final List<String> code = new ArrayList<>();
            for (int page = 0; page < pages.tagCount(); page++) {
                Collections.addAll(code, ItemBookCode.Data.PATTERN_LINES.split(pages.getStringTagAt(page)));
            }
            return code;
        }
    }

    private static final class SourceCodeProviderBookCode implements SourceCodeProvider {
        @Override
        public boolean worksFor(final ItemStack stack) {
            return stack.getItem() == Items.bookCode;
        }

        @Override
        public Iterable<String> codeFor(final ItemStack stack) {
            final ItemBookCode.Data data = ItemBookCode.Data.loadFromStack(stack);
            if (data.getProgramCount() < 1) {
                return null;
            }
            return data.getProgram(data.getSelectedProgram());
        }
    }

    private static final List<SourceCodeProvider> providers = new ArrayList<>(Arrays.asList(
            new SourceCodeProviderVanilla(),
            new SourceCodeProviderBookCode()
    ));

    private static SourceCodeProvider providerFor(final ItemStack stack) {
        if (stack != null) {
            return providers.stream().filter(p -> p.worksFor(stack)).findFirst().orElse(null);
        }
        return null;
    }
}
