package li.cil.tis3d.common;

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

    public static li.cil.tis3d.api.detail.InfraredAPI infrared;
    public static li.cil.tis3d.api.detail.ManualAPI manual;
    public static li.cil.tis3d.api.detail.ModuleAPI module;
    public static SerialAPI serial;

    @Environment(EnvType.CLIENT)
    public static li.cil.tis3d.api.detail.FontRendererAPI fontRenderer;
}
