package li.cil.tis3d.data.forge;

import li.cil.tis3d.api.API;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

import static li.cil.tis3d.common.block.Blocks.CASING;
import static li.cil.tis3d.common.block.Blocks.CONTROLLER;
import static li.cil.tis3d.common.tags.BlockTags.COMPUTERS;

public final class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable final ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, API.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        tag(COMPUTERS).add(
            CASING.get(),
            CONTROLLER.get()
        );
    }
}
