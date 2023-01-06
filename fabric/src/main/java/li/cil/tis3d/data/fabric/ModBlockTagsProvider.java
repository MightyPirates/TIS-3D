package li.cil.tis3d.data.fabric;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import static li.cil.tis3d.common.block.Blocks.CASING;
import static li.cil.tis3d.common.block.Blocks.CONTROLLER;
import static li.cil.tis3d.common.tags.BlockTags.COMPUTERS;

public final class ModBlockTagsProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagsProvider(final FabricDataGenerator generator) {
        super(generator);
    }

    @Override
    protected void generateTags() {
        tag(COMPUTERS).add(
            CASING.get(),
            CONTROLLER.get()
        );
    }
}
