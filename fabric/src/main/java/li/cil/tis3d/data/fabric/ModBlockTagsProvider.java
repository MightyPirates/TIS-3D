package li.cil.tis3d.data.fabric;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

import static li.cil.tis3d.common.block.Blocks.CASING;
import static li.cil.tis3d.common.block.Blocks.CONTROLLER;
import static li.cil.tis3d.common.tags.BlockTags.COMPUTERS;

public final class ModBlockTagsProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagsProvider(final FabricDataOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        getOrCreateTagBuilder(COMPUTERS).add(
            CASING.get(),
            CONTROLLER.get()
        );
    }
}
