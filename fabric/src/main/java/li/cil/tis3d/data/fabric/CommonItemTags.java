package li.cil.tis3d.data.fabric;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class CommonItemTags {
    public static final TagKey<Item> CHESTS = commonTag("chest");
    public static final TagKey<Item> DIAMOND_GEMS = commonTag("diamonds");
    public static final TagKey<Item> EMERALDS = commonTag("emeralds");
    public static final TagKey<Item> ENDER_PEARLS = commonTag("ender_pearls");
    public static final TagKey<Item> GLASS_PANES = commonTag("glass_panes");
    public static final TagKey<Item> GOLD_INGOTS = commonTag("gold_ingots");
    public static final TagKey<Item> GOLD_NUGGETS = commonTag("gold_nuggets");
    public static final TagKey<Item> IRON_BLOCKS = commonTag("iron_blocks");
    public static final TagKey<Item> IRON_INGOTS = commonTag("iron_ingots");
    public static final TagKey<Item> LAPIS_LAZULIS = commonTag("lapis_lazulis");
    public static final TagKey<Item> QUARTZ_GEMS = commonTag("quartz");
    public static final TagKey<Item> REDSTONE_DUSTS = commonTag("redstone_dusts");
    public static final TagKey<Item> SAND = commonTag("sand");

    // --------------------------------------------------------------------- //

    private static TagKey<Item> commonTag(final String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("c", name));
    }
}
