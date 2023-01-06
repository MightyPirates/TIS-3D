package li.cil.tis3d.data.fabric;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class DataGenerators implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(final FabricDataGenerator generator) {
        final ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(generator);
        generator.addProvider(blockTagsProvider);
        generator.addProvider(new ModItemTagsProvider(generator, blockTagsProvider));
        generator.addProvider(ModRecipesProvider::new);
    }
}
