package li.cil.tis3d.common;

import li.cil.tis3d.api.CommonAPI;
import li.cil.tis3d.api.ClientAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class LocalAPI {
    public static CommonAPI common;

    @Environment(EnvType.CLIENT)
    public static ClientAPI client;
}
