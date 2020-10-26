package li.cil.tis3d.common;

import li.cil.tis3d.api.FontRendererAPI;
import li.cil.tis3d.api.InfraredAPI;
import li.cil.tis3d.api.ManualClientAPI;
import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.SerialAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;

public class API {
    /**
     * The ID of the mod, i.e. the internal string it is identified by.
     */
    public static final String MOD_ID = "tis3d";

    public static ItemGroup itemGroup;

    public static InfraredAPI infrared;
    public static ManualClientAPI manual;
    public static ModuleAPI module;
    public static SerialAPI serial;

    @Environment(EnvType.CLIENT)
    public static FontRendererAPI fontRenderer;
}
