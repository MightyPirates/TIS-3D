package li.cil.tis3d.common.mixin;

import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.util.BlockHitResult;
import net.minecraft.util.HitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CasingBlockEntity.class)
public abstract class CasingInfraredReceiverMixin implements InfraredReceiver {
    @Override
    public void onInfraredPacket(final InfraredPacket packet, final HitResult hitResult) {
        if (hitResult.getType() != HitResult.Type.BLOCK) return;
        assert hitResult instanceof BlockHitResult;
        final CasingBlockEntity self = (CasingBlockEntity)(Object)this;
        final Module module = self.getModule(Face.fromDirection(((BlockHitResult)hitResult).getSide()));
        if (module instanceof InfraredReceiver) {
            ((InfraredReceiver)module).onInfraredPacket(packet, hitResult);
        }
    }
}
