package li.cil.tis3d.common.integration.redlogic;

import cpw.mods.fml.common.Optional;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.BundledRedstone;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.Redstone;
import mods.immibis.redlogic.api.wiring.IBundledWire;
import mods.immibis.redlogic.api.wiring.IConnectable;
import mods.immibis.redlogic.api.wiring.IWire;

@Optional.Interface(iface = "mods.immibis.redlogic.api.wiring.IConnectable", modid = ProxyRedLogic.MOD_ID)
public interface RedLogicConnectable extends IConnectable {
    Casing getCasing();

    @Optional.Method(modid = ProxyRedLogic.MOD_ID)
    @Override
    default boolean connects(final IWire wire, final int blockFace, final int fromDirection) {
        final Module module = getCasing().getModule(Face.VALUES[fromDirection]);
        if (wire instanceof IBundledWire) {
            return module instanceof BundledRedstone;
        } else {
            return module instanceof Redstone;
        }
    }

    @Override
    default boolean connectsAroundCorner(final IWire wire, final int blockFace, final int fromDirection) {
        return false;
    }
}
