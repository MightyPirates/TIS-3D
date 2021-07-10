package li.cil.tis3d.common.integration.minecraft;

import li.cil.tis3d.api.serial.SerialInterface;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import li.cil.tis3d.common.mixin.AbstractFurnaceBlockEntityAccessors;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class FurnaceSerialInterfaceProvider implements SerialInterfaceProvider {
    // --------------------------------------------------------------------- //
    // SerialInterfaceProvider

    @Override
    public boolean worksWith(final World world, final BlockPos position, final Direction side) {
        return world.getBlockEntity(position) instanceof AbstractFurnaceBlockEntity;
    }

    @Override
    public SerialInterface interfaceFor(final World world, final BlockPos position, final Direction side) {
        final AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity)world.getBlockEntity(position);
        if (furnace == null) {
            throw new IllegalArgumentException("Provided location does not contain a furnace. Check via worksWith first.");
        }
        return new SerialInterfaceFurnace(furnace);
    }

    @Override
    public SerialProtocolDocumentationReference getDocumentationReference() {
        return new SerialProtocolDocumentationReference("Minecraft Furnace", "protocols/minecraft_furnace.md");
    }

    @Override
    public boolean isValid(final World world, final BlockPos position, final Direction side, final SerialInterface serialInterface) {
        return serialInterface instanceof SerialInterfaceFurnace;
    }

    // --------------------------------------------------------------------- //

    private static final class SerialInterfaceFurnace implements SerialInterface {
        private static final String TAG_MODE = "mode";

        public enum FurnaceField {
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

            public int get(final AbstractFurnaceBlockEntity furnace) {
                final AbstractFurnaceBlockEntityAccessors accessors = (AbstractFurnaceBlockEntityAccessors)furnace;
                switch (this) {
                    case RemainingFuelTicks:
                        return accessors.getBurnTime();
                    case TotalFuelTicks:
                        return accessors.getFuelTime();
                    case AccumulatedSmeltTicks:
                        return accessors.getCookTime();
                    case TotalSmeltTicks:
                        return accessors.getCookTimeTotal();
                }
                return 0;
            }
        }

        public enum Mode {
            PercentageFuel,
            PercentageProgress
        }

        private final AbstractFurnaceBlockEntity furnace;
        private Mode mode = Mode.PercentageFuel;

        SerialInterfaceFurnace(final AbstractFurnaceBlockEntity furnace) {
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
                        return (short)(value * 100 / total);
                    }
                }
                case PercentageProgress: {
                    final int value = FurnaceField.AccumulatedSmeltTicks.get(furnace);
                    final int total = FurnaceField.TotalSmeltTicks.get(furnace);
                    if (total > 0) {
                        return (short)(value * 100 / total);
                    }
                }
            }
            return (short)0;
        }

        @Override
        public void skip() {
        }

        @Override
        public void reset() {
            mode = Mode.PercentageFuel;
        }

        @Override
        public void readFromNBT(final NbtCompound nbt) {
            mode = EnumUtils.readFromNBT(SerialInterfaceFurnace.Mode.class, TAG_MODE, nbt);
        }

        @Override
        public void writeToNBT(final NbtCompound nbt) {
            EnumUtils.writeToNBT(mode, TAG_MODE, nbt);
        }
    }
}
