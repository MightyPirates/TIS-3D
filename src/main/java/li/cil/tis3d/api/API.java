package li.cil.tis3d.api;

import li.cil.tis3d.api.util.FontRenderer;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    public static ItemGroup itemGroup;
    public static li.cil.tis3d.api.detail.InfraredAPI infraredAPI;

    @OnlyIn(Dist.CLIENT) public static FontRenderer normalFontRenderer;
    @OnlyIn(Dist.CLIENT) public static FontRenderer smallFontRenderer;

    private API() {
    }
}
