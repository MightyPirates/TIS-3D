package li.cil.tis3d.data.forge;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.common.item.Items;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public final class ModBlockStateProvider extends BlockStateProvider {
    private static final ResourceLocation FULL_CASING_MODEL = new ResourceLocation(API.MOD_ID, "block/casing_all");
    private static final ResourceLocation EMPTY_CASING_MODEL = new ResourceLocation(API.MOD_ID, "block/casing_empty");
    private static final ResourceLocation MODULE_IN_CASING_MODEL = new ResourceLocation(API.MOD_ID, "block/casing_module");
    private static final ResourceLocation CONTROLLER_MODEL = new ResourceLocation(API.MOD_ID, "block/controller");

    public ModBlockStateProvider(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, API.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        final ModelFile.ExistingModelFile casingAll = models().getExistingFile(FULL_CASING_MODEL);
        final ModelFile.ExistingModelFile casingEmpty = models().getExistingFile(EMPTY_CASING_MODEL);
        final ModelFile.ExistingModelFile casingModule = models().getExistingFile(MODULE_IN_CASING_MODEL);

        final MultiPartBlockStateBuilder casingBuilder = getMultipartBuilder(Blocks.CASING.get());
        CasingBlock.FACE_TO_PROPERTY.forEach((face, property) -> {
            final Direction direction = Face.toDirection(face);
            final int rotationY = (int) direction.toYRot();
            final int rotationX;
            if (direction == Direction.UP) {
                rotationX = 90;
            } else if (direction == Direction.DOWN) {
                rotationX = -90;
            } else {
                rotationX = 0;
            }

            casingBuilder.part()
                .modelFile(casingEmpty)
                .rotationX(rotationX)
                .rotationY(rotationY)
                .addModel()
                .condition(property, false)
                .end();
            casingBuilder.part()
                .modelFile(casingModule)
                .rotationX(rotationX)
                .rotationY(rotationY)
                .addModel()
                .condition(property, true)
                .end();
        });
        itemModels()
            .getBuilder(Items.CASING.getId().getPath())
            .parent(casingAll);

        simpleBlock(Blocks.CONTROLLER.get(), models().getExistingFile(CONTROLLER_MODEL));
        itemModels()
            .getBuilder(Items.CONTROLLER.getId().getPath())
            .parent(models().getExistingFile(Blocks.CONTROLLER.getId()));
    }
}
