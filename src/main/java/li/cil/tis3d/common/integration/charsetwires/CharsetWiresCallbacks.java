package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneEmitter;

public final class CharsetWiresCallbacks {
    public static final CharsetWiresCallbacks INSTANCE = new CharsetWiresCallbacks();

    @SubscribeEvent
    public void onAttachCapabilities(final AttachCapabilitiesEvent.TileEntity event) {
        if (event.getTileEntity() instanceof TileEntityCasing) {
            final TileEntityCasing tileEntity = (TileEntityCasing) event.getTileEntity();
            event.addCapability(CapabilityRedstoneEmitter.PROVIDER_REDSTONE_EMITTER, new CapabilityRedstoneEmitter.Provider(tileEntity));
            event.addCapability(CapabilityRedstoneReceiver.PROVIDER_REDSTONE_RECEIVER, new CapabilityRedstoneReceiver.Provider(tileEntity));
            event.addCapability(CapabilityBundledEmitter.PROVIDER_BUNDLED_EMITTER, new CapabilityBundledEmitter.Provider(tileEntity));
            event.addCapability(CapabilityBundledReceiver.PROVIDER_BUNDLED_RECEIVER, new CapabilityBundledReceiver.Provider(tileEntity));
        }
    }

    public static void onBundledOutputChanged(final BundledRedstone module, final int channel) {
        final EnumFacing facing = Face.toEnumFacing(module.getFace());
        final World world = module.getCasing().getCasingWorld();
        final BlockPos neighborPos = module.getCasing().getPosition().offset(facing);
        if (world.isBlockLoaded(neighborPos)) {
            final TileEntity tileEntity = world.getTileEntity(neighborPos);
            if (tileEntity != null && tileEntity.hasCapability(CapabilityBundledReceiver.INSTANCE, facing.getOpposite())) {
                final IBundledReceiver receiver = tileEntity.getCapability(CapabilityBundledReceiver.INSTANCE, facing.getOpposite());
                if (receiver != null) {
                    receiver.onBundledInputChange();
                }
            }
        }
    }

    public static int getInput(final Redstone module) {
        final EnumFacing facing = Face.toEnumFacing(module.getFace());
        final World world = module.getCasing().getCasingWorld();
        final BlockPos inputPos = module.getCasing().getPosition().offset(facing);
        if (world.isBlockLoaded(inputPos)) {
            final TileEntity tileEntity = world.getTileEntity(inputPos);
            if (tileEntity != null && tileEntity.hasCapability(CapabilityRedstoneEmitter.INSTANCE, facing.getOpposite())) {
                final IRedstoneEmitter emitter = tileEntity.getCapability(CapabilityRedstoneEmitter.INSTANCE, facing.getOpposite());
                if (emitter != null) {
                    return emitter.getRedstoneSignal() & 0xFFFF;
                }
            }
        }

        return 0;
    }

    public static int getBundledInput(final BundledRedstone module, final int channel) {
        final EnumFacing facing = Face.toEnumFacing(module.getFace());
        final World world = module.getCasing().getCasingWorld();
        final BlockPos inputPos = module.getCasing().getPosition().offset(facing);
        if (world.isBlockLoaded(inputPos)) {
            final TileEntity tileEntity = world.getTileEntity(inputPos);
            if (tileEntity != null && tileEntity.hasCapability(CapabilityBundledEmitter.INSTANCE, facing.getOpposite())) {
                final IBundledEmitter emitter = tileEntity.getCapability(CapabilityBundledEmitter.INSTANCE, facing.getOpposite());
                if (emitter != null) {
                    return emitter.getBundledSignal()[channel];
                }
            }
        }

        return 0;
    }
}
