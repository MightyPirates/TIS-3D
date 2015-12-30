package li.cil.tis3d.common.integration.minecraft;

import li.cil.tis3d.api.serial.SerialInterface;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public final class SerialInterfaceProviderFurnace implements SerialInterfaceProvider {
    @Override
    public boolean worksWith(final World world, final int x, final int y, final int z, final EnumFacing side) {
        return world.getTileEntity(x, y, z) instanceof TileEntityFurnace;
    }

    @Override
    public SerialInterface interfaceFor(final World world, final int x, final int y, final int z, final EnumFacing side) {
        return new SerialInterfaceFurnace((TileEntityFurnace) world.getTileEntity(x, y, z));
    }

    @Override
    public SerialProtocolDocumentationReference getDocumentationReference() {
        return new SerialProtocolDocumentationReference("Minecraft Furnace", "protocols/minecraftFurnace.md");
    }

    @Override
    public boolean isValid(final World world, final int x, final int y, final int z, final EnumFacing side, final SerialInterface serialInterface) {
        return serialInterface instanceof SerialInterfaceFurnace;
    }

    // --------------------------------------------------------------------- //

    private static final class SerialInterfaceFurnace implements SerialInterface {
        private static final String TAG_MODE = "mode";

        private enum FurnaceField {
            /**
             * How many more ticks the furnace will continue operating before
             * another fuel item must be consumed.
             */
            RemainingFuelTicks,

            /**
             * How many ticks in total the current fuel item provided. So the
             * percentage of remaining fuel is <tt>RemainingFuelTicks / TotalFuelTicks</tt>.
             */
            TotalFuelTicks,

            /**
             * How many ticks the current smelting operation has already run.
             */
            AccumulatedSmeltTicks,

            /**
             * How many ticks in total the current smelting operation will take. So
             * the percentage of smelting progress is <tt>AccumulatedSmeltTicks / TotalSmeltTicks</tt>.
             */
            TotalSmeltTicks;

            public int get(final TileEntityFurnace furnace) {
                switch (this) {
                    case RemainingFuelTicks:
                        return furnace.furnaceBurnTime;
                    case TotalFuelTicks:
                        return furnace.currentItemBurnTime;
                    case AccumulatedSmeltTicks:
                        return furnace.furnaceCookTime;
                    case TotalSmeltTicks:
                        return 200; // Weee, magic numbers! \o/
                }
                return 0;
            }
        }

        private enum Mode {
            PercentageFuel,
            PercentageProgress
        }

        private final TileEntityFurnace furnace;
        private Mode mode = Mode.PercentageFuel;

        public SerialInterfaceFurnace(final TileEntityFurnace furnace) {
            this.furnace = furnace;
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public void write(final short value) {
            if (value == 0) {
                mode = Mode.PercentageFuel;
            } else {
                mode = Mode.PercentageProgress;
            }
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public short peek() {
            switch (mode) {
                case PercentageFuel: {
                    final int value = FurnaceField.RemainingFuelTicks.get(furnace);
                    final int total = FurnaceField.TotalFuelTicks.get(furnace);
                    if (total > 0) {
                        return (short) (value * 100 / total);
                    }
                }
                case PercentageProgress: {
                    final int value = FurnaceField.AccumulatedSmeltTicks.get(furnace);
                    final int total = FurnaceField.TotalSmeltTicks.get(furnace);
                    if (total > 0) {
                        return (short) (value * 100 / total);
                    }
                }
            }
            return (short) 0;
        }

        @Override
        public void skip() {
        }

        @Override
        public void reset() {
            mode = Mode.PercentageFuel;
        }

        @Override
        public void readFromNBT(final NBTTagCompound nbt) {
            mode = EnumUtils.readFromNBT(SerialInterfaceFurnace.Mode.class, TAG_MODE, nbt);
        }

        @Override
        public void writeToNBT(final NBTTagCompound nbt) {
            EnumUtils.writeToNBT(mode, TAG_MODE, nbt);
        }
    }
}
