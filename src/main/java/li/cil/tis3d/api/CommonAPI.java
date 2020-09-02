package li.cil.tis3d.api;

import net.minecraft.item.ItemGroup;

public final class CommonAPI {
    /**
     * The ID of the mod, i.e. the internal string it is identified by.
     */
    public static final String MOD_ID = "tis3d";

    public ItemGroup itemGroup;

    public InfraredAPI infraredAPI;
    public ManualAPI manualAPI;
    public ModuleAPI moduleAPI;
    public SerialAPI serialAPI;
}
