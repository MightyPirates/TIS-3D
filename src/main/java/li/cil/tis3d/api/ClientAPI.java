package li.cil.tis3d.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClientAPI {
    public li.cil.tis3d.api.detail.FontRendererAPI fontRenderer;
}
