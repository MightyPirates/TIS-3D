package li.cil.tis3d.api.infrared;

import javax.annotation.Nullable;
import net.minecraft.util.math.Direction;

public interface InfraredReceiverTile {
	@Nullable InfraredReceiver getInfraredReceiver(Direction facing);
}
