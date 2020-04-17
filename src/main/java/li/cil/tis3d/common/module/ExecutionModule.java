package li.cil.tis3d.common.module;

import com.mojang.blaze3d.platform.GlStateManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.FontRendererAPI;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.BlockChangeAware;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.CodeBookItem;
import li.cil.tis3d.common.module.execution.MachineImpl;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.util.EnumUtils;
import li.cil.tis3d.util.NBTIds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.*;

/**
 * The programmable execution module.
 */
public final class ExecutionModule extends AbstractModuleWithRotation implements BlockChangeAware {
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

    private static final Identifier[] STATE_LOCATIONS = new Identifier[]{
        Textures.LOCATION_OVERLAY_MODULE_EXECUTION_IDLE,
        Textures.LOCATION_OVERLAY_MODULE_EXECUTION_ERROR,
        Textures.LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING,
        Textures.LOCATION_OVERLAY_MODULE_EXECUTION_WAITING
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

    public ExecutionModule(final Casing casing, final Face face) {
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
        final State prevState = state;

        if (compileError != null) {
            state = State.ERR;
        } else if (getState().instructions.isEmpty()) {
            state = State.IDLE;
        } else if (machine.step()) {
            state = State.RUN;
            getCasing().markDirty();
            sendPartialState();
            return; // Don't send data twice.
        } else {
            state = State.WAIT;
        }

        if (prevState != state) {
            getCasing().markDirty();
            sendPartialState();
        }
    }

    @Override
    public void onEnabled() {
        sendFullState();
    }

    @Override
    public void onDisabled() {
        getState().reset();
        state = State.IDLE;

        sendPartialState();
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        if (compileError == null) {
            machine.onBeforeWriteComplete(port);
        }
    }

    @Override
    public void onWriteComplete(final Port port) {
        if (compileError == null) {
            machine.onWriteCompleted(port);
        }
    }

    @Override
    public boolean onActivate(final PlayerEntity player, final Hand hand, final Vec3d hit) {
        final ItemStack heldItem = player.getStackInHand(hand);

        // Vanilla book? If so, make that a code book.
        if (heldItem.getItem() == net.minecraft.item.Items.BOOK) {
            if (!player.getEntityWorld().isClient) {
                if (!player.abilities.creativeMode) {
                    heldItem.split(1);
                }
                final ItemStack bookCode = new ItemStack(Items.BOOK_CODE);
                if (player.inventory.insertStack(bookCode)) {
                    player.playerContainer.sendContentUpdates();
                }
                if (bookCode.getCount() > 0) {
                    player.dropItem(bookCode, false, false);
                }
            }

            return true;
        }

        // Code book? Store current program on it if sneaking.
        if (Items.isBookCode(heldItem) && player.isSneaking()) {
            final CodeBookItem.Data data = CodeBookItem.Data.loadFromStack(heldItem);
            if (getState().code != null && getState().code.length > 0) {
                data.addOrSelectProgram(Arrays.asList(getState().code));
                CodeBookItem.Data.saveToStack(heldItem, data);
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
        final World world = getCasing().getCasingWorld();
        if (!world.isClient) {
            compile(code, player);
            sendFullState();
        }

        return true;
    }

    @Override
    public void onData(final CompoundTag nbt) {
        readFromNBT(nbt);
    }

    @Override
    public void onData(final ByteBuf data) {
        final MachineState machineState = getState();
        machineState.pc = data.readShort();
        machineState.acc = data.readShort();
        machineState.bak = data.readShort();
        if (data.readBoolean()) {
            machineState.last = Optional.of(Port.values()[data.readByte()]);
        } else {
            machineState.last = Optional.empty();
        }
        state = State.values()[data.readByte()];
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks) {
        if ((!getCasing().isEnabled() || !isVisible()) && !isObserverLookingAt(rendererDispatcher)) {
            return;
        }

        rotateForRendering();
        RenderUtil.ignoreLighting();

        // Draw status texture.
        RenderUtil.drawQuad(RenderUtil.getSprite(STATE_LOCATIONS[state.ordinal()]));

        // Render detailed state when player is close.
        final MachineState machineState = getState();
        if (machineState.code != null && rendererDispatcher.camera.getBlockPos().getSquaredDistance(getCasing().getPosition()) < 64) {
            renderState(machineState);
        }
    }

    @Override
    public void readFromNBT(final CompoundTag nbt) {
        super.readFromNBT(nbt);

        final CompoundTag machineNbt = nbt.getCompound(TAG_MACHINE);
        getState().readFromNBT(machineNbt);
        state = EnumUtils.readFromNBT(State.class, TAG_STATE, nbt);

        if (nbt.contains(TAG_COMPILE_ERROR)) {
            final CompoundTag errorNbt = nbt.getCompound(TAG_COMPILE_ERROR);
            compileError = new ParseException(errorNbt.getString(TAG_MESSAGE), errorNbt.getInt(TAG_LINE_NUMBER), errorNbt.getInt(TAG_START), errorNbt.getInt(TAG_END));
        } else {
            compileError = null;
        }
    }

    @Override
    public void writeToNBT(final CompoundTag nbt) {
        super.writeToNBT(nbt);

        final CompoundTag machineNbt = new CompoundTag();
        getState().writeToNBT(machineNbt);
        nbt.put(TAG_MACHINE, machineNbt);
        EnumUtils.writeToNBT(state, TAG_STATE, nbt);

        if (compileError != null) {
            final CompoundTag errorNbt = new CompoundTag();
            errorNbt.putString(TAG_MESSAGE, compileError.getMessage());
            errorNbt.putInt(TAG_LINE_NUMBER, compileError.getLineNumber());
            errorNbt.putInt(TAG_START, compileError.getStart());
            errorNbt.putInt(TAG_END, compileError.getEnd());
            nbt.put(TAG_COMPILE_ERROR, errorNbt);
        }
    }

    // --------------------------------------------------------------------- //
    // BlockChangeAware

    @Override
    public void onNeighborBlockChange(final BlockPos neighborPos, final boolean isModuleNeighbor) {
        if (isModuleNeighbor && isVisible()) {
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
     * @param player the player that issued the compile.
     */
    private void compile(final Iterable<String> code, final PlayerEntity player) {
        compileError = null;
        try {
            getState().clear();
            Compiler.compile(code, getState());
        } catch (final ParseException e) {
            compileError = e;
            player.addChatMessage(new TranslatableText(Constants.MESSAGE_COMPILE_ERROR, e.getLineNumber(), e.getStart(), e.getEnd()).append(new TranslatableText(e.getMessage())), false);
        }
    }

    /**
     * Send the full state to the client.
     */
    private void sendFullState() {
        final CompoundTag nbt = new CompoundTag();
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

        data.writeShort((short)getState().pc);
        data.writeShort(getState().acc);
        data.writeShort(getState().bak);
        data.writeBoolean(getState().last.isPresent());
        getState().last.ifPresent(port -> data.writeByte((byte)port.ordinal()));
        data.writeByte(state.ordinal());

        getCasing().sendData(getFace(), data, DATA_TYPE_INCREMENTAL);
    }

    @Environment(EnvType.CLIENT)
    private void renderState(final MachineState machineState) {
        // Offset to start drawing at top left of inner area, slightly inset.
        GlStateManager.translatef(3.5f / 16f, 3.5f / 16f, 0);
        GlStateManager.scalef(1 / 128f, 1 / 128f, 1);
        GlStateManager.translatef(1, 1, 0);
        GlStateManager.color4f(1f, 1f, 1f, 1f);

        // Draw register info on top.
        final String accLast = String.format("ACC:%4X LAST:%s", machineState.acc, machineState.last.map(Enum::name).orElse("NONE"));
        FontRendererAPI.drawString(accLast);
        GlStateManager.translatef(0, FontRendererAPI.getCharHeight() + 4, 0);
        final String bakState = String.format("BAK:%4X MODE:%s", machineState.bak, state.name());
        FontRendererAPI.drawString(bakState);
        GlStateManager.translatef(0, FontRendererAPI.getCharHeight() + 4, 0);
        drawLine(1);
        GlStateManager.translatef(0, 5, 0);

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
                    GlStateManager.color3f(0.66f, 0.66f, 0.66f);
                } else if (state == State.ERR || compileError != null && compileError.getLineNumber() == currentLine) {
                    GlStateManager.color3f(1f, 0f, 0f);
                }

                drawLine(FontRendererAPI.getCharHeight());

                GlStateManager.color3f(0f, 0f, 0f);
            } else {
                GlStateManager.color3f(1f, 1f, 1f);
            }

            FontRendererAPI.drawString(line, 18);

            GlStateManager.translatef(0, FontRendererAPI.getCharHeight() + 1, 0);
        }
    }

    /**
     * Draws a horizontal line of the specified height.
     *
     * @param height the height of the line to draw.
     */
    @Environment(EnvType.CLIENT)
    private static void drawLine(final int height) {
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture();

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION);
        buffer.vertex(-0.5f, height + 0.5f, 0).next();
        buffer.vertex(71.5f, height + 0.5f, 0).next();
        buffer.vertex(71.5f, -0.5f, 0).next();
        buffer.vertex(-0.5f, -0.5f, 0).next();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
    }

    // --------------------------------------------------------------------- //

    private interface SourceCodeProvider {
        boolean worksFor(ItemStack stack);

        @Nullable
        Iterable<String> codeFor(final ItemStack stack);
    }

    private static final class SourceCodeProviderVanilla implements SourceCodeProvider {
        @Override
        public boolean worksFor(final ItemStack stack) {
            return Items.isItem(stack, net.minecraft.item.Items.WRITTEN_BOOK) || Items.isItem(stack, net.minecraft.item.Items.WRITABLE_BOOK);
        }

        @Override
        public Iterable<String> codeFor(final ItemStack stack) {
            final CompoundTag nbt = stack.getTag();
            if (nbt == null) {
                return null;
            }

            final ListTag pages = nbt.getList("pages", NBTIds.TAG_STRING);
            if (pages.size() < 1) {
                return null;
            }

            final List<String> code = new ArrayList<>();
            for (int page = 0; page < pages.size(); page++) {
                Collections.addAll(code, Constants.PATTERN_LINES.split(pages.getString(page)));
            }
            return code;
        }
    }

    private static final class SourceCodeProviderBookCode implements SourceCodeProvider {
        @Override
        public boolean worksFor(final ItemStack stack) {
            return Items.isBookCode(stack);
        }

        @Override
        public Iterable<String> codeFor(final ItemStack stack) {
            final CodeBookItem.Data data = CodeBookItem.Data.loadFromStack(stack);
            if (data.getPageCount() < 1) {
                return null;
            }
            return data.getProgram();
        }
    }

    private static final List<SourceCodeProvider> providers = new ArrayList<>(Arrays.asList(
        new SourceCodeProviderVanilla(),
        new SourceCodeProviderBookCode()
    ));

    @Nullable
    private static SourceCodeProvider providerFor(final ItemStack stack) {
        if (!stack.isEmpty()) {
            return providers.stream().filter(p -> p.worksFor(stack)).findFirst().orElse(null);
        }
        return null;
    }
}
