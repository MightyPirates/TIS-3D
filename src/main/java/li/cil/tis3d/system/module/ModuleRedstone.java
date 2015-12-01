package li.cil.tis3d.system.module;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.Side;
import li.cil.tis3d.api.module.Redstone;
import li.cil.tis3d.api.prefab.AbstractModule;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public final class ModuleRedstone extends AbstractModule implements Redstone {
    /**
     * The current redstone output.
     */
    private int output = 0;

    public ModuleRedstone(final Casing casing, final Face face) {
        super(casing, face);
    }

    private void beginWrite(final Side side) {
        // For reading redstone values, wait for readers to provide up-to-date
        // values, instead of an old value from when we started writing.
        final Port outputPort = getCasing().getOutputPort(getFace(), side);
        if (outputPort.isReading()) {
            if (!outputPort.isWriting()) {
                outputPort.beginWrite(getRedstoneInput());
            }
        }
    }

    private void beginRead(final Side side) {
        // Continuously read from all ports, set output to last received value.
        final Port inputPort = getCasing().getInputPort(getFace(), side);
        if (!inputPort.isReading()) {
            inputPort.beginRead();
        }
        if (inputPort.isTransferring()) {
            setRedstoneOutput(inputPort.read());
            inputPort.beginRead();
        }
    }

    private int getRedstoneInput() {
        final EnumFacing facing = Face.toEnumFacing(getFace());
        final BlockPos inputPos = getCasing().getPosition().offset(facing);
        return getCasing().getWorld().getRedstonePower(inputPos, facing);
    }

    private void setRedstoneOutput(final int value) {
        output = value;
        if (!getCasing().getWorld().isRemote) {
            final Block blockType = getCasing().getWorld().getBlockState(getCasing().getPosition()).getBlock();
            getCasing().markDirty();
            getCasing().getWorld().notifyNeighborsOfStateChange(getCasing().getPosition(), blockType);
        }
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        for (final Side side : Side.VALUES) {
            beginWrite(side);
            beginRead(side);
        }
    }

    @Override
    public void onWriteComplete(final Side side) {
        beginWrite(side);
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        output = nbt.getInteger("output");
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("output", output);
    }

    @Override
    public int getRedstoneOutput() {
        return output;
    }
}
