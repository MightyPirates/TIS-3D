package li.cil.tis3d.data;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.item.Items;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public final class ModItemModelProvider extends ItemModelProvider {
    private static final String MODULE_ITEM_MODEL_NAME = "module";

    public ModItemModelProvider(final DataGenerator generator, final ExistingFileHelper existingFileHelper) {
        super(generator, API.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simple(Items.BOOK_CODE);
        simple(Items.BOOK_MANUAL);

        simple(Items.KEY);
        simple(Items.KEY_CREATIVE);

        simple(Items.PRISM);

        this.getBuilder(MODULE_ITEM_MODEL_NAME)
            .guiLight(BlockModel.GuiLight.SIDE)
            .element()
            .from(0, 0, 7)
            .to(16, 16, 8)
            .cube("#layer0")
            .face(Direction.DOWN).uvs(0, 0, 16, 1).end()
            .face(Direction.UP).uvs(0, 0, 16, 1).end()
            .face(Direction.WEST).uvs(0, 0, 1, 16).end()
            .face(Direction.EAST).uvs(0, 0, 1, 16).end()
            .end()
            .element()
            .from(0, 0, 7)
            .to(16, 16, 8)
            .face(Direction.NORTH)
            .texture("#layer1")
            .cullface(Direction.NORTH)
            .end()
            .end()
            .transforms()

            .transform(ItemTransforms.TransformType.GUI)
            .rotation(30, 135, 0)
            .scale(0.625f)
            .end()

            .transform(ItemTransforms.TransformType.GROUND)
            .translation(0, 3, 0)
            .scale(0.625f)
            .end()

            .transform(ItemTransforms.TransformType.FIXED)
            .scale(1f)
            .end()

            .transform(ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
            .rotation(0, 180, 20)
            .translation(0, 2.5f, 0)
            .scale(0.375f)
            .end()

            .transform(ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
            .rotation(-70, 160, 0)
            .scale(0.4f)
            .end()
            .end();

        module(Items.AUDIO_MODULE, BLOCK_FOLDER + "/overlay/audio_module");
        module(Items.DISPLAY_MODULE, ITEM_FOLDER + "/display_module");
        module(Items.EXECUTION_MODULE, BLOCK_FOLDER + "/overlay/execution_module_running");
        module(Items.FACADE_MODULE, mcLoc(BLOCK_FOLDER + "/iron_block"));
        module(Items.INFRARED_MODULE, BLOCK_FOLDER + "/overlay/infrared_module");
        module(Items.KEYPAD_MODULE, BLOCK_FOLDER + "/overlay/keypad_module");
        module(Items.QUEUE_MODULE, ITEM_FOLDER + "/queue_module");
        module(Items.RANDOM_MODULE, BLOCK_FOLDER + "/overlay/random_module");
        module(Items.RANDOM_ACCESS_MEMORY_MODULE, ITEM_FOLDER + "/random_access_memory_module");
        module(Items.READ_ONLY_MEMORY_MODULE, ITEM_FOLDER + "/read_only_memory_module");
        module(Items.REDSTONE_MODULE, ITEM_FOLDER + "/redstone_module");
        module(Items.SEQUENCER_MODULE, ITEM_FOLDER + "/sequencer_module");
        module(Items.SERIAL_PORT_MODULE, BLOCK_FOLDER + "/overlay/serial_port_module");
        module(Items.STACK_MODULE, ITEM_FOLDER + "/stack_module");
        module(Items.TERMINAL_MODULE, ITEM_FOLDER + "/terminal_module");
        module(Items.TIMER_MODULE, ITEM_FOLDER + "/timer_module");
    }

    private <T extends Item> void simple(final RegistryObject<T> item) {
        singleTexture(item.getId().getPath(),
            new ResourceLocation(ITEM_FOLDER + "/generated"),
            "layer0", modLoc(ITEM_FOLDER + "/" + item.getId().getPath()));
    }

    private <T extends Item> void module(final RegistryObject<T> item, final String overlayTexture) {
        module(item, modLoc(overlayTexture));
    }

    private <T extends Item> void module(final RegistryObject<T> item, final ResourceLocation overlayTexture) {
        this.withExistingParent(item.getId().getPath(), modLoc(MODULE_ITEM_MODEL_NAME))
            .texture("layer0", BLOCK_FOLDER + "/casing_module")
            .texture("layer1", overlayTexture);
    }
}
