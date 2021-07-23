package li.cil.tis3d.common.tags;

import li.cil.tis3d.api.API;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

public final class BlockTags {
    public static final Tags.IOptionalNamedTag<Block> COMPUTERS = tag("computers");

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }

    // --------------------------------------------------------------------- //

    private static Tags.IOptionalNamedTag<Block> tag(final String name) {
        return net.minecraft.tags.BlockTags.createOptional(new ResourceLocation(API.MOD_ID, name));
    }
}
