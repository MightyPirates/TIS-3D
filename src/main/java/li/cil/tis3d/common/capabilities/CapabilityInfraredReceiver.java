package li.cil.tis3d.common.capabilities;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.Callable;

public final class CapabilityInfraredReceiver {
    public static final ResourceLocation INFRARED_RECEIVER_LOCATION = new ResourceLocation(API.MOD_ID, "infrared_receiver");

    @CapabilityInject(InfraredReceiver.class)
    public static Capability<InfraredReceiver> INFRARED_RECEIVER_CAPABILITY;

    private static final CapabilityInfraredReceiver INSTANCE = new CapabilityInfraredReceiver();

    private static final InfraredReceiver DEFAULT_INFRARED_RECEIVER = new DefaultInfraredReceiver();

    public static void register() {
        CapabilityManager.INSTANCE.register(InfraredReceiver.class, new Capability.IStorage<InfraredReceiver>() {
            @Override
            public NBTBase writeNBT(final Capability<InfraredReceiver> capability, final InfraredReceiver instance, final EnumFacing side) {
                return null;
            }

            @Override
            public void readNBT(final Capability<InfraredReceiver> capability, final InfraredReceiver instance, final EnumFacing side, final NBTBase nbt) {
            }
        }, new Callable<InfraredReceiver>() {
            @Override
            public InfraredReceiver call() throws Exception {
                return DEFAULT_INFRARED_RECEIVER;
            }
        });

        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    @SubscribeEvent
    public void onAttachCapabilities(final AttachCapabilitiesEvent.TileEntity event) {
        final TileEntity tileEntity = event.getTileEntity();
        if (tileEntity instanceof InfraredReceiver) {
            event.addCapability(INFRARED_RECEIVER_LOCATION, new InfraredReceiverCapabilityProvider((InfraredReceiver) tileEntity));
        }
    }

    @SubscribeEvent
    public void onAttachCapabilities(final AttachCapabilitiesEvent.Entity event) {
        final Entity entity = event.getEntity();
        if (entity instanceof InfraredReceiver) {
            event.addCapability(INFRARED_RECEIVER_LOCATION, new InfraredReceiverCapabilityProvider((InfraredReceiver) entity));
        }
    }

    private static final class InfraredReceiverCapabilityProvider implements ICapabilityProvider {
        private final InfraredReceiver receiver;

        public InfraredReceiverCapabilityProvider(final InfraredReceiver receiver) {
            this.receiver = receiver;
        }

        @Override
        public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
            return capability == INFRARED_RECEIVER_CAPABILITY;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
            return capability == INFRARED_RECEIVER_CAPABILITY ? (T) receiver : null;
        }
    }

    private static class DefaultInfraredReceiver implements InfraredReceiver {
        @Override
        public void onInfraredPacket(final InfraredPacket packet, final MovingObjectPosition hit) {
        }
    }

    private CapabilityInfraredReceiver() {
    }
}
