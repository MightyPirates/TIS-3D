package li.cil.tis3d.util.datafix;

import li.cil.tis3d.api.API;
import li.cil.tis3d.util.datafix.fixes.TileEntityLeadingWhitespace;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;

public final class Fixes {
    public static void init() {
        final ModFixs fixer = FMLCommonHandler.instance().getDataFixer().init(API.MOD_ID, 1);

        fixer.registerFix(FixTypes.BLOCK_ENTITY, new TileEntityLeadingWhitespace());
    }

    private Fixes() {
    }
}
