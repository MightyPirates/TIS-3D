package li.cil.tis3d.common.integration.projectred;

import li.cil.tis3d.api.module.BundledRedstone;
import mrtjp.projectred.api.ProjectRedAPI;
import net.minecraft.world.World;

public final class ProjectRedCallbacks {
    public static int getBundledInput(final BundledRedstone module, final int channel) {
        final World world = module.getCasing().getCasingWorld();
        final byte[] signal = ProjectRedAPI.transmissionAPI.getBundledInput(world, module.getCasing().getPositionX(), module.getCasing().getPositionY(), module.getCasing().getPositionZ(), module.getFace().ordinal());
        if (signal != null) {
            return signal[channel] & 0xFF;
        }

        return 0;
    }

    private ProjectRedCallbacks() {
    }
}
