package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.BundledRedstone;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.Redstone;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import pl.asie.charset.api.wires.IConnectable;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;

@Optional.Interface(iface = "pl.asie.charset.api.wires.IConnectable", modid = ModCharsetWires.MOD_ID)
public interface CharsetWiresConnectable extends IConnectable {
    Casing getCasing();

    @Optional.Method(modid = ModCharsetWires.MOD_ID)
    @Override
    default boolean canConnect(final WireType wireType, final WireFace wireFace, final EnumFacing facing) {
        final Module module = getCasing().getModule(Face.fromEnumFacing(facing));
        switch (wireType) {
            case NORMAL:
            case INSULATED:
                return module instanceof Redstone;
            case BUNDLED:
                return module instanceof BundledRedstone;
        }
        return false;
    }
}
