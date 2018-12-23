package li.cil.tis3d.common.capabilities;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import javax.annotation.Nullable;

public final class CapabilityInfraredReceiver {
    // TODO
/*    public static final ResourceLocation INFRARED_RECEIVER_LOCATION = new ResourceLocation(API.MOD_ID, "infrared_receiver");

    @CapabilityInject(InfraredReceiver.class)
    public static Capability<InfraredReceiver> INFRARED_RECEIVER_CAPABILITY;

    private static final CapabilityInfraredReceiver INSTANCE = new CapabilityInfraredReceiver();

    private static final InfraredReceiver DEFAULT_INFRARED_RECEIVER = new DefaultInfraredReceiver();

    public static void register() {
        CapabilityManager.INSTANCE.register(InfraredReceiver.class, new Capability.IStorage<InfraredReceiver>() {
            @Override
            @Nullable
            public NBTBase writeNBT(final Capability<InfraredReceiver> capability, final InfraredReceiver instance, final EnumFacing side) {
                return null;
            }

            @Override
            public void readNBT(final Capability<InfraredReceiver> capability, final InfraredReceiver instance, final EnumFacing side, final NBTBase nbt) {
            }
        }, () -> DEFAULT_INFRARED_RECEIVER);

        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    @SubscribeEvent
    public void onAttachTileEntityCapabilities(final AttachCapabilitiesEvent<TileEntity> event) {
        final TileEntity tileEntity = event.getObject();
        if (tileEntity instanceof InfraredReceiver) {
            event.addCapability(INFRARED_RECEIVER_LOCATION, new InfraredReceiverCapabilityProvider((InfraredReceiver) tileEntity));
        }
    }

    @SubscribeEvent
    public void onAttachEntityCapabilities(final AttachCapabilitiesEvent<Entity> event) {
        final Entity entity = event.getObject();
        if (entity instanceof InfraredReceiver) {
            event.addCapability(INFRARED_RECEIVER_LOCATION, new InfraredReceiverCapabilityProvider((InfraredReceiver) entity));
        }
    }

    private static final class InfraredReceiverCapabilityProvider implements ICapabilityProvider {
        private final InfraredReceiver receiver;

        InfraredReceiverCapabilityProvider(final InfraredReceiver receiver) {
            this.receiver = receiver;
        }

        @Override
        public boolean hasCapability(final Capability<?> capability, @Nullable final EnumFacing facing) {
            return capability == INFRARED_RECEIVER_CAPABILITY;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public <T> T getCapability(final Capability<T> capability, @Nullable final EnumFacing facing) {
            return capability == INFRARED_RECEIVER_CAPABILITY ? (T) receiver : null;
        }
    }

    private static class DefaultInfraredReceiver implements InfraredReceiver {
        @Override
        public void onInfraredPacket(final InfraredPacket packet, final RayTraceResult hit) {
        }
    }

    private CapabilityInfraredReceiver() {
    } */
}
