package li.cil.tis3d.data.forge;

import li.cil.tis3d.api.API;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import static li.cil.tis3d.common.block.Blocks.CASING;
import static li.cil.tis3d.common.block.Blocks.CONTROLLER;
import static li.cil.tis3d.common.tags.BlockTags.COMPUTERS;

public final class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(final DataGenerator generator, final ExistingFileHelper existingFileHelper) {
        super(generator, API.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(COMPUTERS).add(
            CASING.get(),
            CONTROLLER.get()
        );
    }
}
