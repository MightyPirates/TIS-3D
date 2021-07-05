package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.util.Side;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class SequencerModule extends AbstractModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final boolean[][] configuration = new boolean[COL_COUNT][ROW_COUNT];
    private int position = -1;
    private int delay = 4;
    private int stepsRemaining = 0;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT data names.
    private static final String TAG_CONFIGURATION = "configuration";
    private static final String TAG_POSITION = "position";
    private static final String TAG_DELAY = "delay";
    private static final String TAG_STEPS_REMAINING = "stepsRemaining";

    // Data packet types.
    private static final byte DATA_TYPE_CONFIGURATION = 0;
    private static final byte DATA_TYPE_POSITION = 1;

    private static final int COL_COUNT = 8;
    private static final int ROW_COUNT = 8;

    // Rendering info.
    private static final float CELLS_U0 = 5 / 32f;
    private static final float CELLS_V0 = 5 / 32f;
    private static final float CELLS_SIZE_U = 1 / 32f;
    private static final float CELLS_SIZE_V = 1 / 32f;
    private static final float CELLS_STEP_U = CELLS_SIZE_U + 2 / 32f;
    private static final float CELLS_STEP_V = CELLS_SIZE_V + 2 / 32f;
    private static final float CELLS_OUTER_U0 = 8 / 64f;
    private static final float CELLS_OUTER_V0 = 8 / 64f;
    private static final float CELLS_OUTER_SIZE_U = 6 / 64f;
    private static final float CELLS_OUTER_SIZE_V = 6 / 64f;
    private static final float CELLS_OUTER_STEP_U = CELLS_OUTER_SIZE_U;
    private static final float CELLS_OUTER_STEP_V = CELLS_OUTER_SIZE_V;
    private static final float BAR_U0 = 8 / 64f;
    private static final float BAR_V0 = 8 / 64f;
    private static final float BAR_SIZE_U = 6 / 64f;
    private static final float BAR_SIZE_V = 48 / 64f;
    private static final float BAR_STEP_U = BAR_SIZE_U;

    private short output;

    // --------------------------------------------------------------------- //

    public SequencerModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        stepInput();
        stepOutput();
    }

    @Override
    public void onDisabled() {
        position = -1;
        stepsRemaining = 0;
    }

    @Override
    public boolean onActivate(final PlayerEntity player, final Hand hand, final Vec3d hit) {
        if (player.isSneaking()) {
            return false;
        }

        // Handle input on the client and send it to the server for higher
        // hit position resolution (MC sends this to the server at a super
        // low resolution for some reason).
        final World world = getCasing().getCasingWorld();
        if (world.isClient) {
            final Vec3d uv = hitToUV(hit);
            final int col = uvToCol((float)uv.x);
            final int row = uvToRow((float)uv.y);
            if (col >= 0 && row >= 0) {
                configuration[col][row] = !configuration[col][row];
                sendConfiguration(Side.SERVER);
            }
        }

        return true;
    }

    @Override
    public void onData(final ByteBuf data) {
        if (getCasing().getCasingWorld().isClient) {
            if (data.readBoolean()) {
                decodeConfiguration(data.readLong(), configuration);
            } else {
                position = data.readByte();
            }
        } else {
            decodeConfiguration(data.readLong(), configuration);
            sendConfiguration(Side.CLIENT);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks,
                       final MatrixStack matrices, final VertexConsumerProvider vcp,
                       final int light, final int overlay) {
        if (!isVisible()) {
            return;
        }

        matrices.push();
        rotateForRendering(matrices);

        final VertexConsumer vc = vcp.getBuffer(RenderLayer.getCutoutMipped());
        final MatrixStack.Entry mat = matrices.peek();
        final boolean enabled = getCasing().isEnabled();

        final int numLayers = 3;
        final float layerBump = 0.005f / numLayers;

        // Draw base grid of sequencer entries.
        final Sprite baseSprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_MODULE_SEQUENCER);
        final int baseBrightness = enabled ? 0xFF : 0x80;
        RenderUtil.drawQuad(baseSprite, mat, vc,
                            0xFF, baseBrightness, baseBrightness, baseBrightness,
                            RenderUtil.maxLight, overlay);

        if (rendererDispatcher.camera.getBlockPos().getSquaredDistance(getCasing().getPosition()) < 64) {
            // Draw configuration of sequencer (white squares for set bits).
            final int confColor = enabled ? 0xFFCCD8DF : 0xFF666C6F;
            for (int col = 0; col < COL_COUNT; col++) {
                for (int row = 0; row < ROW_COUNT; row++) {
                    if (configuration[col][row]) {
                        final float u0 = CELLS_U0 + CELLS_STEP_U * col;
                        final float v0 = CELLS_V0 + CELLS_STEP_V * row;
                        RenderUtil.drawColorQuad(mat, vc, u0, v0, CELLS_SIZE_U, CELLS_SIZE_V,
                                                 confColor, RenderUtil.maxLight, overlay);
                    }
                }
            }
        }

        // Draw selection overlay for focused cell, if any.
        final Vec3d hitPos = getObserverLookAt(rendererDispatcher);
        if (hitPos != null) {
            final Vec3d uv = hitToUV(hitPos);
            final int col = uvToCol((float)uv.x);
            final int row = uvToRow((float)uv.y);
            if (col >= 0 && row >= 0) {
                final float u = CELLS_OUTER_U0 + col * CELLS_OUTER_STEP_U;
                final float v = CELLS_OUTER_V0 + row * CELLS_OUTER_STEP_V;

                matrices.translate(0f, 0f, layerBump);
                RenderUtil.drawColorQuad(mat, vc,
                                         u, v, CELLS_OUTER_SIZE_U, CELLS_OUTER_SIZE_V,
                                         enabled ? 0xFF596673 : 0xFF2C3339,
                                         RenderUtil.maxLight, overlay);
            }
        }

        if (enabled) {
            // Draw bar in background indicating current position in sequence.
            final float barU0 = BAR_U0 + BAR_STEP_U * position;
            final float brightness = 0.75f + 0.25f * (delay == 0 ? 1 : (1 - (delay - stepsRemaining) / (float)delay));
            final int barRed = (int)(0x33 * brightness);
            final int barGreen = (int)(0x4C * brightness);
            final int barBlue = (int)(0x59 * brightness);

            matrices.translate(0f, 0f, layerBump);
            RenderUtil.drawColorQuad(mat, vc, barU0, BAR_V0, BAR_SIZE_U, BAR_SIZE_V,
                                     0xFF, barRed, barGreen, barBlue,
                                     RenderUtil.maxLight, overlay);
        }

        matrices.pop();
    }

    @Override
    public void readFromNBT(final NbtCompound nbt) {
        super.readFromNBT(nbt);

        decodeConfiguration(nbt.getLong(TAG_CONFIGURATION), configuration);
        position = Math.min(Math.max(nbt.getInt(TAG_POSITION), 0), COL_COUNT - 1);
        delay = Math.min(Math.max(nbt.getInt(TAG_DELAY), 0), 0xFFFF);
        stepsRemaining = Math.min(Math.max(nbt.getInt(TAG_STEPS_REMAINING), 0), 0xFFFF);

        initializeOutput();
    }

    @Override
    public void writeToNBT(final NbtCompound nbt) {
        super.writeToNBT(nbt);

        nbt.putLong(TAG_CONFIGURATION, encodeConfiguration(configuration));
        nbt.putInt(TAG_POSITION, position);
        nbt.putInt(TAG_DELAY, delay);
        nbt.putInt(TAG_STEPS_REMAINING, stepsRemaining);
    }

    // --------------------------------------------------------------------- //

    private void stepOutput() {
        if (stepsRemaining-- <= 0) {
            stepsRemaining = delay;
            cancelWrite();
            position = (position + 1) % COL_COUNT;
            sendPosition();

            initializeOutput();
            for (final Port port : Port.VALUES) {
                final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
                if (!sendingPipe.isWriting()) {
                    sendingPipe.beginWrite(output);
                }
            }
        }
    }

    private void stepInput() {
        for (final Port port : Port.VALUES) {
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                delay = receivingPipe.read() & 0xFFFF;
            }
        }
    }

    private void sendConfiguration(final Side toSide) {
        final ByteBuf data = Unpooled.buffer();
        if (toSide == Side.CLIENT) {
            data.writeBoolean(true);
        }
        data.writeLong(encodeConfiguration(configuration));
        getCasing().sendData(getFace(), data, DATA_TYPE_CONFIGURATION);
    }

    private void sendPosition() {
        final ByteBuf data = Unpooled.buffer();
        data.writeBoolean(false);
        data.writeByte(position);
        getCasing().sendData(getFace(), data, DATA_TYPE_POSITION);
    }

    private void initializeOutput() {
        output = 0;
        for (int mask = 1, row = 0; row < ROW_COUNT; row++, mask <<= 1) {
            if (configuration[position][row]) {
                output |= mask;
            }
        }
    }

    private static long encodeConfiguration(final boolean[][] configuration) {
        long encodedConfiguration = 0L;
        long mask = 1;
        for (int col = 0; col < COL_COUNT; col++) {
            for (int row = 0; row < ROW_COUNT; row++, mask <<= 1L) {
                if (configuration[col][row]) {
                    encodedConfiguration |= mask;
                }
            }
        }
        return encodedConfiguration;
    }

    private static void decodeConfiguration(final long encodedConfiguration, final boolean[][] configuration) {
        long mask = 1;
        for (int col = 0; col < COL_COUNT; col++) {
            for (int row = 0; row < ROW_COUNT; row++, mask <<= 1L) {
                configuration[col][row] = (encodedConfiguration & mask) != 0;
            }
        }
    }

    private int uvToCol(final float u) {
        if (u < CELLS_OUTER_U0 || u > CELLS_OUTER_U0 + CELLS_OUTER_STEP_U * COL_COUNT) {
            return -1;
        }

        final float mappedU = (u - CELLS_OUTER_U0) / (CELLS_OUTER_STEP_U * COL_COUNT);
        return (int)(mappedU * COL_COUNT);
    }

    private int uvToRow(final float v) {
        if (v < CELLS_OUTER_V0 || v > CELLS_OUTER_V0 + CELLS_OUTER_STEP_V * ROW_COUNT) {
            return -1;
        }

        final float mappedV = (v - CELLS_OUTER_V0) / (CELLS_OUTER_STEP_V * ROW_COUNT);
        return (int)(mappedV * COL_COUNT);
    }
}
