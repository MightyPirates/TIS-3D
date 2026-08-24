/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.gametest.Invocations.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.HitResult;

import java.util.Arrays;

public final class TestModule extends AbstractModule implements InfraredReceiver {
    private static final int PORTS = Port.VALUES.length;

    private static final String TAG_STEP_COUNT = "stepCount";

    // --------------------------------------------------------------------- //

    private final Invocations invocations = new Invocations();

    private final short[] writeValues = new short[PORTS];
    private final boolean[] writePending = new boolean[PORTS];
    private final boolean[] cancelAfterWrite = new boolean[PORTS];
    private final int[] cancelStep = new int[PORTS];
    private final boolean[] readPending = new boolean[PORTS];

    private int stepCount;

    // --------------------------------------------------------------------- //

    public TestModule(final Casing casing, final Face face) {
        super(casing, face);
        Arrays.fill(cancelStep, -1);
    }

    // --------------------------------------------------------------------- //
    // Test setup.

    public TestModule writeOn(final Port port, final int value) {
        writeValues[port.ordinal()] = (short) value;
        writePending[port.ordinal()] = true;
        return this;
    }

    public TestModule writeAndCancelOn(final Port port, final int value) {
        cancelAfterWrite[port.ordinal()] = true;
        return writeOn(port, value);
    }

    public TestModule readOn(final Port port) {
        readPending[port.ordinal()] = true;
        return this;
    }

    // --------------------------------------------------------------------- //
    // Assertions.

    public Invocations invocations() {
        return invocations;
    }

    public int stepCount() {
        return stepCount;
    }

    // --------------------------------------------------------------------- //

    @Override
    public void step() {
        stepCount++;
        for (final Port port : Port.VALUES) {
            final int index = port.ordinal();

            if (cancelStep[index] == stepCount) {
                getCasing().getSendingPipe(getFace(), port).cancelWrite();
                invocations.record(stepCount, Kind.CANCEL, port, writeValues[index]);
            } else if (writePending[index]) {
                final Pipe sending = getCasing().getSendingPipe(getFace(), port);
                if (!sending.isWriting()) {
                    sending.beginWrite(writeValues[index]);
                    writePending[index] = false;
                    invocations.record(stepCount, Kind.WRITE, port, writeValues[index]);
                    if (cancelAfterWrite[index]) {
                        cancelStep[index] = stepCount + 1;
                    }
                }
            }

            if (readPending[index]) {
                final Pipe receiving = getCasing().getReceivingPipe(getFace(), port);
                if (!receiving.isReading()) {
                    receiving.beginRead();
                }
                if (receiving.canTransfer()) {
                    invocations.record(stepCount, Kind.READ, port, receiving.read());
                }
            }
        }
    }

    @Override
    public void onEnabled() {
        invocations.record(stepCount, Kind.ENABLED);
    }

    @Override
    public void onDisabled() {
        invocations.record(stepCount, Kind.DISABLED);
    }

    @Override
    public void onWriteComplete(final Port port) {
        invocations.record(stepCount, Kind.WRITE_COMPLETE, port, 0);
    }

    @Override
    public void onInfraredPacket(final InfraredPacket packet, final HitResult hit) {
        invocations.record(stepCount, Kind.INFRARED, null, packet.getPacketValue());
    }

    @Override
    public void save(final CompoundTag tag) {
        super.save(tag);
        tag.putInt(TAG_STEP_COUNT, stepCount);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        stepCount = tag.getIntOr(TAG_STEP_COUNT, 0);
    }
}
