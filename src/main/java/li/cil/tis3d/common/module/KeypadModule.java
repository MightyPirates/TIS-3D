package li.cil.tis3d.common.module;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.init.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class KeypadModule extends AbstractModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * The current value being input.
     */
    private Optional<Short> value = Optional.empty();

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_VALUE = "value";

    // Data packet types.
    private static final byte DATA_TYPE_VALUE = 0;

    // Rendering info.
    private static final float KEYS_U0 = 5 / 32f;
    private static final float KEYS_V0 = 5 / 32f;
    private static final float KEYS_SIZE_U = 5 / 32f;
    private static final float KEYS_SIZE_V = 5 / 32f;
    private static final float KEYS_SIZE_V_LAST = 4 / 32f;
    private static final float KEYS_STEP_U = 6 / 32f;
    private static final float KEYS_STEP_V = 6 / 32f;

    // Pitch lookup for click feedback sound cue per value, 0-9.
    // Roughly based on telephone keypad frequencies, except we have to mush
    // both tones into one, so obviously some fidelity is lost, but eh.
    private static final float[] VALUE_TO_PITCH = new float[]{0.9125f, 0.7f, 0.75f, 0.825f, 0.725f, 0.8f, 0.875f, 0.775f, 0.85f, 0.95f};

    // --------------------------------------------------------------------- //

    public KeypadModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        stepOutput();
    }

    @Override
    public void onDisabled() {
        if (value.isPresent()) {
            // Clear the value (that was being written).
            value = Optional.empty();

            // Tell clients we can input again.
            getCasing().sendData(getFace(), new CompoundTag(), DATA_TYPE_VALUE);
        }
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        // Pop the value (that was being written).
        value = Optional.empty();

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Tell clients we can input again.
        getCasing().sendData(getFace(), new CompoundTag(), DATA_TYPE_VALUE);
    }

    @Override
    public boolean onActivate(final PlayerEntity player, final Hand hand, final Vec3d hit) {
        if (player.isSneaking()) {
            return false;
        }

        // Reasoning: don't remove module from casing while activating the
        // module while the casing is disabled. Could be frustrating.
        if (!getCasing().isEnabled()) {
            return true;
        }

        // Only allow inputting one value.
        if (value.isPresent()) {
            return true;
        }

        // Handle input on the client and send it to the server for higher
        // hit position resolution (MC sends this to the server at a super
        // low resolution for some reason).
        final World world = getCasing().getCasingWorld();
        if (world.isClient) {
            final Vec3d uv = hitToUV(hit);
            final int button = uvToButton((float)uv.x, (float)uv.y);
            if (button == -1) {
                // No button here.
                return true;
            }
            final short number = buttonToNumber(button);

            final CompoundTag nbt = new CompoundTag();
            nbt.putShort(TAG_VALUE, number);
            getCasing().sendData(getFace(), nbt, DATA_TYPE_VALUE);
        }

        return true;
    }

    @Override
    public void onData(final CompoundTag nbt) {
        final World world = getCasing().getCasingWorld();
        if (world.isClient) {
            // Got state on which key is currently 'pressed'.
            if (nbt.contains(TAG_VALUE)) {
                value = Optional.of(nbt.getShort(TAG_VALUE));
            } else {
                value = Optional.empty();
            }
        } else if (!value.isPresent() && nbt.contains(TAG_VALUE)) {
            // Got an input and don't have one yet.
            final short newValue = nbt.getShort(TAG_VALUE);
            value = Optional.of(newValue);
            getCasing().sendData(getFace(), nbt, DATA_TYPE_VALUE);
            getCasing().getCasingWorld().playSound(null, getCasing().getPosition(), SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3f, VALUE_TO_PITCH[newValue]);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks) {
        if (!getCasing().isEnabled() || !isVisible()) {
            return;
        }

        rotateForRendering();
        RenderUtil.ignoreLighting();
        GlStateManager.enableBlend();

        // Draw base texture. Draw half transparent while writing current value,
        // i.e. while no input is possible.
        value.ifPresent(unused -> GlStateManager.color4f(1, 1, 1, 0.5f));
        GlStateManager.depthMask(false);
        RenderUtil.drawQuad(RenderUtil.getSprite(Textures.LOCATION_OVERLAY_MODULE_KEYPAD));
        GlStateManager.depthMask(true);

        // Draw overlay for hovered button if we can currently input a value.
        if (!value.isPresent()) {
            final Vec3d hitPos = getObserverLookAt(rendererDispatcher);
            if (hitPos != null) {
                final Vec3d uv = hitToUV(hitPos);
                final int button = uvToButton((float)uv.x, (float)uv.y);
                if (button >= 0) {
                    drawButtonOverlay(button);
                }
            }
        }

        GlStateManager.disableBlend();
    }

    @Override
    public void readFromNBT(final CompoundTag nbt) {
        super.readFromNBT(nbt);

        if (nbt.contains(TAG_VALUE)) {
            value = Optional.of(nbt.getShort(TAG_VALUE));
        }
    }

    @Override
    public void writeToNBT(final CompoundTag nbt) {
        super.writeToNBT(nbt);

        value.ifPresent(x -> nbt.putShort(TAG_VALUE, x));
    }

    // --------------------------------------------------------------------- //

    private void stepOutput() {
        if (!value.isPresent()) {
            return;
        }

        final short v = value.get();
        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
            if (!sendingPipe.isWriting()) {
                sendingPipe.beginWrite(v);
            }
        }
    }

    private int uvToButton(final float u, final float v) {
        if (u < KEYS_U0 || u > KEYS_U0 + KEYS_STEP_U * 2 + KEYS_SIZE_U) {
            return -1;
        }
        if (v < KEYS_V0 || v > KEYS_V0 + KEYS_STEP_V * 3 + KEYS_SIZE_V) {
            return -1;
        }

        // Pretty meh, but cba to math right now. Mostly because skipping the
        // gaps and floating point modulo and special case for zero. Ugh.
        int row = 0;
        float v0 = v - KEYS_V0;
        while (v0 > ((row == 3) ? KEYS_SIZE_V_LAST : KEYS_SIZE_V)) {
            row++;
            v0 -= KEYS_STEP_V;
        }
        if (v0 < 0) {
            // Looking at a gap.
            return -1;
        }

        int column = row == 3 ? -1 : 0;
        float u0 = u - KEYS_U0;
        while (u0 > KEYS_SIZE_U) {
            column++;
            u0 -= KEYS_STEP_U;
        }
        if (u0 < 0 && row != 3 && column != 1) {
            // Looking at a gap.
            return -1;
        }
        if (column < 0) {
            // Left side of zero button.
            column = 0;
        }

        final int button = row * 3 + column;
        if (button > 9) {
            // Past the last button.
            return -1;
        }

        return button;
    }

    private short buttonToNumber(final int button) {
        return (short)((button + 1) % 10);
    }

    @Environment(EnvType.CLIENT)
    private void drawButtonOverlay(final int button) {
        final int column = button % 3;
        final int row = button / 3;
        final float x = KEYS_U0 + column * KEYS_STEP_U;
        final float y = KEYS_V0 + row * KEYS_STEP_V;
        final float w = buttonToNumber(button) == 0 ? (KEYS_SIZE_U + KEYS_STEP_U) : KEYS_SIZE_U;
        final float h = row == 3 ? KEYS_SIZE_V_LAST : KEYS_SIZE_V;
        GlStateManager.disableTexture();
        GlStateManager.color4f(1, 1, 1, 0.5f);
        RenderUtil.drawUntexturedQuad(x, y, w, h);
        GlStateManager.enableTexture();
    }
}
