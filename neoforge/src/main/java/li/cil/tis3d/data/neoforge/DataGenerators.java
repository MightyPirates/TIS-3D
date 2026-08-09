package li.cil.tis3d.data.neoforge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public final class DataGenerators {
    @SubscribeEvent
    public static void gatherServerData(final GatherDataEvent.Server event) {
        event.createProvider(ModLootTableProvider::new);
        event.createBlockAndItemTags(ModBlockTagsProvider::new, ModItemTagsProvider::new);
        event.createProvider(ModRecipesProvider::new);
    }

    @SubscribeEvent
    public static void gatherClientData(final GatherDataEvent.Client event) {
        event.createProvider(ModModelProvider::new);
    }
}
