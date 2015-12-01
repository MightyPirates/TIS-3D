package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.system.module.execution.Machine;

public final class LastInterface extends AbstractSideInterface {
    public LastInterface(final Machine machine, final Casing casing, final Face face) {
        super(machine, casing, face);
    }

    @Override
    public void beginWrite(final int value) {
        getState().last.ifPresent(side -> beginWrite(side, value));
    }

    @Override
    public void cancelWrite() {
        getState().last.ifPresent(this::cancelWrite);
    }

    @Override
    public boolean isWriting() {
        return getState().last.map(this::isWriting).orElse(false);
    }

    @Override
    public boolean isOutputTransferring() {
        return getState().last.map(this::isOutputTransferring).orElse(true);
    }

    @Override
    public void beginRead() {
        getState().last.ifPresent(this::beginRead);
    }

    @Override
    public boolean isReading() {
        return getState().last.map(this::isReading).orElse(false);
    }

    @Override
    public boolean isInputTransferring() {
        return getState().last.map(this::isInputTransferring).orElse(true);
    }

    @Override
    public int read() {
        return getState().last.map(this::read).orElse(0);
    }
}
