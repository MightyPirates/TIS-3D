package li.cil.tis3d.common.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.ModuleWithBlockChangeListener;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.item.CodeBookItem;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.module.execution.MachineImpl;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.compiler.Strings;
import li.cil.tis3d.util.Color;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.*;

/**
 * The programmable execution module.
 */
public final class ExecutionModule extends AbstractModuleWithRotation implements ModuleWithBlockChangeListener {
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

    @OnlyIn(Dist.CLIENT)
    private static final class RenderData {
        private static final ResourceLocation[] STATE_LOCATIONS = new ResourceLocation[]{
            Textures.LOCATION_OVERLAY_MODULE_EXECUTION_IDLE,
            Textures.LOCATION_OVERLAY_MODULE_EXECUTION_ERROR,
            Textures.LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING,
            Textures.LOCATION_OVERLAY_MODULE_EXECUTION_WAITING
        };
    }

    // NBT tag names.
    private static final String TAG_STATE = "state";
    private static final String TAG_MACHINE = "machine";

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
            getCasing().setChanged();
            sendPartialState();
            return; // Don't send data twice.
        } else {
            state = State.WAIT;
        }

        if (prevState != state) {
            getCasing().setChanged();
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
    public boolean onActivate(final PlayerEntity player, final Hand hand, final Vector3d hit) {
        final ItemStack heldItem = player.getItemInHand(hand);

        // Vanilla book? If so, make that a code book.
        if (Items.is(heldItem, net.minecraft.item.Items.BOOK)) {
            if (!player.getCommandSenderWorld().isClientSide()) {
                if (!player.abilities.instabuild) {
                    heldItem.split(1);
                }
                final ItemStack bookCode = new ItemStack(Items.BOOK_CODE.get());
                if (player.inventory.add(bookCode)) {
                    player.containerMenu.broadcastChanges();
                }
                if (bookCode.getCount() > 0) {
                    player.drop(bookCode, false, false);
                }
            }

            return true;
        }

        // Code book? Store current program on it if sneaking.
        if (Items.is(heldItem, Items.BOOK_CODE) && player.isShiftKeyDown()) {
            final CodeBookItem.Data data = CodeBookItem.Data.loadFromStack(heldItem);
            if (getState().code != null && getState().code.length > 0) {
                data.addOrSelectProgram(Arrays.asList(getState().code));
                CodeBookItem.Data.saveToStack(heldItem, data);
            }

            return true;
        }

        // If sneaking otherwise, ignore interaction.
        if (player.isShiftKeyDown()) {
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
        final World world = getCasing().getCasingLevel();
        if (!world.isClientSide()) {
            compile(code);
            if (compileError != null) {
                player.displayClientMessage(Strings.getCompileError(compileError), false);
            }
            sendFullState();
        }

        return true;
    }

    @Override
    public void onData(final CompoundNBT nbt) {
        this.load(nbt);
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(final RenderContext context) {
        if ((!getCasing().isEnabled() || !isVisible()) && !this.isHitFace(context.getDispatcher().cameraHitResult)) {
            return;
        }

        final MatrixStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(matrixStack);

        // Draw status texture.
        context.drawAtlasQuadUnlit(RenderData.STATE_LOCATIONS[state.ordinal()]);

        // Render detailed state when player is close.
        final MachineState machineState = getState();
        if (machineState.code != null && context.closeEnoughForDetails(getCasing().getPosition())) {
            renderState(context, machineState);
        }

        matrixStack.popPose();
    }

    @Override
    public void load(final CompoundNBT tag) {
        super.load(tag);

        final CompoundNBT machineNbt = tag.getCompound(TAG_MACHINE);
        getState().readFromNBT(machineNbt);
        state = EnumUtils.readFromNBT(State.class, TAG_STATE, tag);

        if (getState().code != null) {
            compile(Arrays.asList(getState().code));
        }
    }

    @Override
    public void save(final CompoundNBT tag) {
        super.save(tag);

        final CompoundNBT machineNbt = new CompoundNBT();
        getState().writeToNBT(machineNbt);
        tag.put(TAG_MACHINE, machineNbt);
        EnumUtils.writeToNBT(state, TAG_STATE, tag);
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
     * @param code the code to compile.
     */
    private void compile(final Iterable<String> code) {
        compileError = null;
        try {
            getState().clear();
            Compiler.compile(code, getState());
        } catch (final ParseException e) {
            compileError = e;
        }
    }

    /**
     * Send the full state to the client.
     */
    private void sendFullState() {
        final CompoundNBT nbt = new CompoundNBT();
        this.save(nbt);
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
        getState().last.ifPresent(port -> data.writeByte((byte) port.ordinal()));
        data.writeByte(state.ordinal());

        getCasing().sendData(getFace(), data, DATA_TYPE_INCREMENTAL);
    }

    @OnlyIn(Dist.CLIENT)
    private void renderState(final RenderContext context, final MachineState machineState) {
        final MatrixStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();

        // Offset to start drawing at top left of inner area, slightly inset.
        matrixStack.translate(3.5f / 16f, 3.5f / 16f, 0);
        matrixStack.scale(1 / 128f, 1 / 128f, 1);
        matrixStack.translate(1, 1, 0);

        final FontRenderer fontRenderer = API.smallFontRenderer;

        // Draw register info on top.
        final String accLast = String.format("ACC:%4X LAST:%s", machineState.acc, machineState.last.map(Enum::name).orElse("NONE"));
        context.drawString(fontRenderer, accLast, Color.WHITE);
        matrixStack.translate(0, fontRenderer.lineHeight() + 4, 0);

        final String bakState = String.format("BAK:%4X MODE:%s", machineState.bak, state.name());
        context.drawString(fontRenderer, bakState, Color.WHITE);
        matrixStack.translate(0, fontRenderer.lineHeight() + 4, 0);

        drawLine(context, 1, Color.WHITE);

        matrixStack.translate(0, 5, 0);

        // If we have more lines than fit on our "screen", offset so that the
        // current line is in the middle, but don't let last line scroll in.
        final int maxLines = 50 / (fontRenderer.lineHeight() + 1);
        final int totalLines = machineState.code.length;
        final int currentLine;
        if (!machineState.lineNumbers.isEmpty()) {
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
            final CharSequence charSequence = line.subSequence(0, Math.min(line.length(), 18));
            if (lineNumber == currentLine) {
                // Draw current line marker behind text
                if (state == State.WAIT) {
                    drawLine(context, fontRenderer.lineHeight(), Color.LIGHT_GRAY);
                } else if (state == State.ERR || compileError != null && compileError.getLineNumber() == currentLine) {
                    drawLine(context, fontRenderer.lineHeight(), Color.RED);
                } else {
                    drawLine(context, fontRenderer.lineHeight(), Color.WHITE);
                }

                context.drawString(fontRenderer, charSequence, Color.BLACK);
            } else {
                context.drawString(fontRenderer, charSequence, Color.WHITE);
            }

            matrixStack.translate(0, fontRenderer.lineHeight() + 1, 0);
        }

        matrixStack.popPose();
    }

    /**
     * Draws a horizontal line of the specified height.
     *
     * @param height the height of the line to draw.
     */
    @OnlyIn(Dist.CLIENT)
    private static void drawLine(final RenderContext context, final int height, final int color) {
        context.drawQuadUnlit(-0.5f, -0.5f, 72, height + 1, color);
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
            return Items.is(stack, net.minecraft.item.Items.WRITTEN_BOOK) ||
                   Items.is(stack, net.minecraft.item.Items.WRITABLE_BOOK);
        }

        @Override
        public Iterable<String> codeFor(final ItemStack stack) {
            final CompoundNBT nbt = stack.getTag();
            if (nbt == null) {
                return null;
            }

            final ListNBT pages = nbt.getList("pages", NBT.TAG_STRING);
            if (pages.isEmpty()) {
                return null;
            }

            final List<String> code = new ArrayList<>();
            for (int page = 0; page < pages.size(); page++) {
                String line = pages.getString(page);
                if (Items.is(stack, net.minecraft.item.Items.WRITTEN_BOOK)) {
                    try {
                        final ITextProperties stringVisitable = ITextComponent.Serializer.fromJson(line);
                        if (stringVisitable != null) {
                            line = stringVisitable.getString();
                        }
                    } catch (final Exception ignored) {
                    }
                }
                line = line.replaceAll("§[a-z0-9]", "");
                Collections.addAll(code, Constants.PATTERN_LINES.split(line));
            }
            return code;
        }
    }

    private static final class SourceCodeProviderBookCode implements SourceCodeProvider {
        @Override
        public boolean worksFor(final ItemStack stack) {
            return Items.is(stack, Items.BOOK_CODE);
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
