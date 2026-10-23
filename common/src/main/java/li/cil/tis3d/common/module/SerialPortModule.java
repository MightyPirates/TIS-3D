/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.ModuleWithBlockChangeListener;
import li.cil.tis3d.api.module.traits.ModuleWithExclusiveWrites;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.api.serial.SerialInterface;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.provider.SerialInterfaceProviders;
import li.cil.tis3d.util.LevelUtils;
import li.cil.tis3d.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

/**
 * The serial port module can provides access to blocks with a {@link SerialInterface}.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class SerialPortModule extends AbstractModule implements ModuleWithBlockChangeListener, ModuleWithExclusiveWrites {
    // --------------------------------------------------------------------- //
    // Persisted data

    private short writing;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT data names.
    private static final String TAG_VALUE = "value";
    private static final String TAG_SERIAL_INTERFACE = "serialInterface";

    private Optional<SerialInterface> serialInterface = Optional.empty();
    private Optional<CompoundTag> serialInterfaceTag = Optional.empty();
    private boolean isScanScheduled = true;

    // --------------------------------------------------------------------- //

    public SerialPortModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // BlockChangeAware

    @Override
    public void onNeighborBlockChange(final BlockPos neighborPos, final boolean isModuleNeighbor) {
        if (isModuleNeighbor) {
            isScanScheduled = true;
        }
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        scan();
        stepOutput();
        stepInput();
    }

    @Override
    public void onDisabled() {
        // Reset serial interface on shutdown.
        serialInterface.ifPresent(SerialInterface::reset);
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        // Consume the read value (the one that was being written).
        serialInterface.ifPresent(SerialInterface::skip);
        getCasing().setChanged();

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Re-cancel in case step() was called after onBeforeWriteComplete() to
        // ensure all our writes are in sync.
        cancelWrite();

        // Start writing again right away to write as fast as possible.
        stepOutput();
    }

    @Override
    public void render(final RenderContext context) {
        if (!getCasing().isEnabled()) {
            return;
        }

        context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_MODULE_SERIAL_PORT);
    }

    @Override
    public void load(final ValueInput input) {
        super.load(input);

        writing = (short) input.getShortOr(TAG_VALUE, (short) 0);

        if (serialInterface.isPresent()) {
            input.child(TAG_SERIAL_INTERFACE).ifPresent(serialInterface.get()::load);
        } else {
            input.read(TAG_SERIAL_INTERFACE, CompoundTag.CODEC).ifPresent(tag -> serialInterfaceTag = Optional.of(tag));
        }
    }

    @Override
    public void save(final ValueOutput output) {
        super.save(output);

        output.putShort(TAG_VALUE, writing);

        if (serialInterface.isPresent()) {
            final ValueOutput serialInterfaceOutput = output.child(TAG_SERIAL_INTERFACE);
            serialInterface.get().save(serialInterfaceOutput);
            if (serialInterfaceOutput.isEmpty()) {
                output.discard(TAG_SERIAL_INTERFACE);
            }
        } else {
            serialInterfaceTag.ifPresent(tag -> output.store(TAG_SERIAL_INTERFACE, CompoundTag.CODEC, tag));
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Check if our serial interface is still valid.
     * <p>
     * Clears an existing interface if there's none available for the neighbor
     * block and creates one if there is and we don't have one yet.
     */
    private void scan() {
        // Only check for interface validity when necessary.
        if (!isScanScheduled) {
            return;
        }
        isScanScheduled = false;

        final Level level = getCasing().getCasingLevel();
        final BlockPos neighborPos = getCasing().getPosition().relative(getWorldSide());
        final Direction neighborSide = getWorldSide().getOpposite();
        if (LevelUtils.isLoaded(level, neighborPos)) {
            final Optional<SerialInterfaceProvider> provider = SerialInterfaceProviders.getProviderFor(level, neighborPos, neighborSide);
            if (provider.isPresent()) {
                if (serialInterface.isEmpty() || !provider.get().stillValid(level, neighborPos, neighborSide, serialInterface.get())) {
                    // Either we didn't have an interface for our neighbor yet,
                    // or the interface has become invalid, so create a new one.
                    reset();
                    serialInterface = provider.get().getInterface(level, neighborPos, neighborSide);
                    if (serialInterface.isPresent() && serialInterfaceTag.isPresent()) {
                        NBTUtils.loadFromTag(ProblemReporter.Collector.EMPTY_ROOT, level.registryAccess(), serialInterfaceTag.get(), serialInterface.get()::load);
                        serialInterfaceTag = Optional.empty(); // Done reading, don't re-use.
                    }
                } // else: interface still valid.
            } else {
                // No provider for neighbor, can't interact.
                reset();
            }
        } else {
            // Neighboring slot is not loaded, can't interact.
            reset();
        }
    }

    /**
     * Reset the state of the module, its ports, and, if there was one, the
     * serial interface, then clears it.
     */
    private void reset() {
        serialInterface.ifPresent(SerialInterface::reset);
        serialInterface = Optional.empty();
        cancelRead();
        cancelWrite();
        getCasing().setChanged();
    }

    /**
     * Update the output of the serial interface.
     */
    private void stepOutput() {
        if (serialInterface.map(SerialInterface::canRead).orElse(false)) {
            final short value = serialInterface.map(SerialInterface::peek).orElse((short) 0);
            if (value != writing) {
                cancelWrite();
                writing = value;
                getCasing().setChanged();
            }
            for (final Port port : Port.VALUES) {
                final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
                if (!sendingPipe.isWriting()) {
                    sendingPipe.beginWrite(writing);
                }
            }
        } else {
            // No interface or can't read from it.
            cancelWrite();
        }
    }

    /**
     * Update the inputs of the serial port, moving values to the present serial interface.
     */
    private void stepInput() {
        if (serialInterface.map(SerialInterface::canWrite).orElse(false)) {
            for (final Port port : Port.VALUES) {
                // Continuously read from all ports, write last received value.
                final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
                if (!receivingPipe.isReading()) {
                    receivingPipe.beginRead();
                }
                if (receivingPipe.canTransfer()) {
                    // Forward the value.
                    serialInterface.ifPresent(s -> {
                        s.write(receivingPipe.read());
                        getCasing().setChanged();
                    });
                }
            }
        } else {
            // No interface or can't write to it.
            cancelRead();
        }
    }
}
