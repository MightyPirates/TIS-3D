package li.cil.tis3d.common.tags;

import li.cil.tis3d.api.API;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public final class ItemTags {
    public static final Tags.IOptionalNamedTag<Item> COMPUTERS = tag("computers");
    public static final Tags.IOptionalNamedTag<Item> MODULES = tag("modules");
    public static final Tags.IOptionalNamedTag<Item> BOOKS = tag("books");
    public static final Tags.IOptionalNamedTag<Item> KEYS = tag("keys");

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }

    // --------------------------------------------------------------------- //

    private static Tags.IOptionalNamedTag<Item> tag(final String name) {
        return net.minecraft.tags.ItemTags.createOptional(new ResourceLocation(API.MOD_ID, name));
    }
}
