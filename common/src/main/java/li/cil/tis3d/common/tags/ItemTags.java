package li.cil.tis3d.common.tags;

import li.cil.tis3d.api.API;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ItemTags {
    public static final TagKey<Item> COMPUTERS = tag("computers");
    public static final TagKey<Item> MODULES = tag("modules");
    public static final TagKey<Item> BOOKS = tag("books");
    public static final TagKey<Item> KEYS = tag("keys");

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }

    // --------------------------------------------------------------------- //

    private static TagKey<Item> tag(final String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(API.MOD_ID, name));
    }
}
