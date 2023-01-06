package li.cil.tis3d.api;

import li.cil.manual.api.render.FontRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Glue / actual references for the TIS-3D API.
 */
public final class API {
    /**
     * The ID of the mod, i.e. the internal string it is identified by.
     */
    public static final String MOD_ID = "tis3d";

    // --------------------------------------------------------------------- //

    // Set in TIS-3D constructor, prefer using static entry point classes instead where possible.
    public static CreativeModeTab itemGroup;
    public static li.cil.tis3d.api.detail.InfraredAPI infraredAPI;

    @Environment(EnvType.CLIENT)
    public static FontRenderer normalFontRenderer;
    @Environment(EnvType.CLIENT)
    public static FontRenderer smallFontRenderer;

    private API() {
    }
}
