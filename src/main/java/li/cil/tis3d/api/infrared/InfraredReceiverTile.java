package li.cil.tis3d.api.infrared;

import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

public interface InfraredReceiverTile {
    @Nullable
    InfraredReceiver getInfraredReceiver(Direction facing);
}
