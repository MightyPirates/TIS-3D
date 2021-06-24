package li.cil.tis3d.common.capabilities;

import li.cil.tis3d.api.infrared.InfraredReceiver;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public final class Capabilities {
    @CapabilityInject(InfraredReceiver.class)
    public static Capability<InfraredReceiver> INFRARED_RECEIVER = null;

    // --------------------------------------------------------------------- //

    public static void initialize() {
        register(InfraredReceiver.class);
    }

    // --------------------------------------------------------------------- //

    private static <T> void register(final Class<T> type) {
        CapabilityManager.INSTANCE.register(type, new NullStorage<>(), () -> null);
    }
}
