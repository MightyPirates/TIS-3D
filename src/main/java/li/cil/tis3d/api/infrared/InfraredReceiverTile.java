package li.cil.tis3d.api.infrared;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public interface InfraredReceiverTile {
	@Nullable InfraredReceiver getInfraredReceiver(EnumFacing facing);
}
