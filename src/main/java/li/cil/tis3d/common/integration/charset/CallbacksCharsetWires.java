package li.cil.tis3d.common.integration.charset;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneEmitter;

public final class CallbacksCharsetWires {
    public static void register() {
        RedstoneIntegration.INSTANCE.addCallback(CallbacksCharsetWires::onBundledOutputChanged);
        RedstoneIntegration.INSTANCE.addRedstoneInputProvider(CallbacksCharsetWires::getInput);
        RedstoneIntegration.INSTANCE.addBundledRedstoneInputProvider(CallbacksCharsetWires::getBundledInput);
    }

    // --------------------------------------------------------------------- //

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

    // --------------------------------------------------------------------- //

    private CallbacksCharsetWires() {
    }
}
