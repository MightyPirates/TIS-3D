package li.cil.tis3d.system.module;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Pipe;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.prefab.AbstractModule;
import li.cil.tis3d.system.module.execution.MachineState;

public final class ModuleRandom extends AbstractModule {
    public ModuleRandom(final Casing casing, final Face face) {
        super(casing, face);
    }

    private void stepOutput(final Port port) {
        // For reading redstone values, wait for readers to provide up-to-date
        // values, instead of an old value from when we started writing.
        final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
        if (sendingPipe.isReading()) {
            if (!sendingPipe.isWriting()) {
                sendingPipe.beginWrite(getCasing().getWorld().rand.nextInt(MachineState.MAX_VALUE * 2 + 1) - MachineState.MAX_VALUE);
            }
        }
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        for (final Port port : Port.VALUES) {
            stepOutput(port);
        }
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Start writing again right away to write as fast as possible.
        stepOutput(port);
    }
}
