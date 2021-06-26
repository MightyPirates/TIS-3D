package li.cil.tis3d.common.provider.serial;

import li.cil.tis3d.api.serial.SerialInterface;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Objects;
import java.util.Optional;

public final class SerialInterfaceProviderFurnace extends ForgeRegistryEntry<SerialInterfaceProvider> implements SerialInterfaceProvider {
    private static final TranslationTextComponent DOCUMENTATION_TITLE = new TranslationTextComponent("tis3d.manual.serial_protocols.furnace");
    private static final String DOCUMENTATION_LINK = "minecraft_furnace.md";
    private static final SerialProtocolDocumentationReference DOCUMENTATION_REFERENCE = new SerialProtocolDocumentationReference(DOCUMENTATION_TITLE, DOCUMENTATION_LINK);

    // --------------------------------------------------------------------- //
    // SerialInterfaceProvider

    @Override
    public boolean matches(final World world, final BlockPos position, final Direction side) {
        return world.getBlockEntity(position) instanceof FurnaceTileEntity;
    }

    @Override
    public Optional<SerialInterface> getInterface(final World world, final BlockPos position, final Direction face) {
        final FurnaceTileEntity furnace = Objects.requireNonNull((FurnaceTileEntity) world.getBlockEntity(position));
        return Optional.of(new SerialInterfaceFurnace(furnace));
    }

    @Override
    public Optional<SerialProtocolDocumentationReference> getDocumentationReference() {
        return Optional.of(DOCUMENTATION_REFERENCE);
    }

    @Override
    public boolean stillValid(final World world, final BlockPos position, final Direction side, final SerialInterface serialInterface) {
        return serialInterface instanceof SerialInterfaceFurnace;
    }

    // --------------------------------------------------------------------- //

    private static final class SerialInterfaceFurnace implements SerialInterface {
        private static final String TAG_MODE = "mode";

        private enum Mode {
            PercentageFuel,
            PercentageProgress
        }

        private final FurnaceTileEntity furnace;
        private Mode mode = Mode.PercentageFuel;

        SerialInterfaceFurnace(final FurnaceTileEntity furnace) {
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
                    final int value = furnace.litTime;
                    final int total = furnace.litDuration;
                    if (total > 0) {
                        return (short) (value * 100 / total);
                    }
                }
                case PercentageProgress: {
                    final int value = furnace.cookingProgress;
                    final int total = furnace.cookingTotalTime;
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
        public void readFromNBT(final CompoundNBT nbt) {
            mode = EnumUtils.readFromNBT(SerialInterfaceFurnace.Mode.class, TAG_MODE, nbt);
        }

        @Override
        public void writeToNBT(final CompoundNBT nbt) {
            EnumUtils.writeToNBT(mode, TAG_MODE, nbt);
        }
    }
}
