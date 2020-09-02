package li.cil.tis3d.api;

import net.minecraft.item.ItemGroup;

/**
 * Glue / actual references for the TIS-3D API.
 */
public final class API {
    /**
     * The ID of the mod, i.e. the internal string it is identified by.
     */
    public static final String MOD_ID = "tis3d";

    // --------------------------------------------------------------------- //

    public static ItemGroup itemGroup;

    public enum Font {
        SmallFont,
        NormalFont
    }

    // Set in TIS-3D pre-init, prefer using static entry point classes instead.
    public static li.cil.tis3d.api.detail.FontRendererAPI fontRendererAPI;
    public static li.cil.tis3d.api.detail.InfraredAPI infraredAPI;
    public static ManualAPI manualAPI;
    public static ModuleAPI moduleAPI;
    public static SerialAPI serialAPI;

    private API() {
    }
}
