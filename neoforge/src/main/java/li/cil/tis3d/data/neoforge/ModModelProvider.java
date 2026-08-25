/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.data.neoforge;

import com.mojang.math.Quadrant;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.api.API;
import li.cil.tis3d.client.renderer.block.neoforge.ModuleUnbakedModel;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.common.item.Items;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.List;
import java.util.stream.Stream;

public final class ModModelProvider extends ModelProvider {
    private static final Identifier FULL_CASING_MODEL = modelLocation("block/casing_all");
    private static final Identifier EMPTY_CASING_MODEL = modelLocation("block/casing_empty");
    private static final Identifier MODULE_IN_CASING_MODEL = modelLocation("block/casing_module");
    private static final Identifier CONTROLLER_MODEL = modelLocation("block/controller");
    private static final Identifier MODULE_ITEM_MODEL = modelLocation("item/module");


    public ModModelProvider(final PackOutput output) {
        super(output, API.MOD_ID);
    }

    // --------------------------------------------------------------------- //

    @Override
    protected void registerModels(final BlockModelGenerators blocks, final ItemModelGenerators items) {
        registerCasing(blocks);
        registerController(blocks);
        registerSimpleItems(blocks);
        registerModuleItems(blocks);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of(Blocks.CASING, Blocks.CONTROLLER).map(supplier -> BuiltInRegistries.BLOCK.wrapAsHolder(supplier.get()));
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.<RegistrySupplier<? extends Item>>of(
            Items.CASING, Items.CONTROLLER,
            Items.BOOK_CODE, Items.BOOK_MANUAL, Items.KEY, Items.KEY_CREATIVE, Items.PRISM,
            Items.AUDIO_MODULE, Items.DISPLAY_MODULE, Items.EXECUTION_MODULE, Items.FACADE_MODULE,
            Items.INFRARED_MODULE, Items.KEYPAD_MODULE, Items.QUEUE_MODULE, Items.RANDOM_MODULE,
            Items.RANDOM_ACCESS_MEMORY_MODULE, Items.READ_ONLY_MEMORY_MODULE, Items.REDSTONE_MODULE,
            Items.SEQUENCER_MODULE, Items.SERIAL_PORT_MODULE, Items.STACK_MODULE, Items.TERMINAL_MODULE,
            Items.TIMER_MODULE
        ).map(supplier -> BuiltInRegistries.ITEM.wrapAsHolder(supplier.get()));
    }

    // --------------------------------------------------------------------- //

    private void registerCasing(final BlockModelGenerators blocks) {
        final MultiPartGenerator casing = MultiPartGenerator.multiPart(Blocks.CASING.get());
        CasingBlock.DIRECTION_TO_PROPERTY.forEach((direction, property) -> {
            final VariantMutator rotation = rotationFor(direction);

            casing.with(new ConditionBuilder().term(property, false),
                BlockModelGenerators.plainVariant(EMPTY_CASING_MODEL).with(rotation));

            // The module face is the custom model, so an installed module can replace its geometry.
            final MultiVariant proxy = BlockModelGenerators.plainVariant(MODULE_IN_CASING_MODEL).with(rotation);
            casing.with(new ConditionBuilder().term(property, true),
                MultiVariant.of(new CustomBlockStateModelBuilder.Simple(
                    new ModuleUnbakedModel(direction, proxy.toUnbaked()))));
        });
        blocks.blockStateOutput.accept(casing);

        blocks.itemModelOutput.accept(Items.CASING.get(), ItemModelUtils.plainModel(FULL_CASING_MODEL));
    }

    private void registerController(final BlockModelGenerators blocks) {
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
            Blocks.CONTROLLER.get(), BlockModelGenerators.plainVariant(CONTROLLER_MODEL)));
        blocks.itemModelOutput.accept(Items.CONTROLLER.get(), ItemModelUtils.plainModel(CONTROLLER_MODEL));
    }

    private void registerSimpleItems(final BlockModelGenerators blocks) {
        for (final RegistrySupplier<? extends Item> item : List.of(
            Items.BOOK_CODE, Items.BOOK_MANUAL, Items.KEY, Items.KEY_CREATIVE, Items.PRISM)) {
            final Identifier model = ModelTemplates.FLAT_ITEM.create(
                modelLocation("item/" + item.getId().getPath()),
                new TextureMapping().put(TextureSlot.LAYER0, modelLocation("item/" + item.getId().getPath())),
                blocks.modelOutput);
            blocks.itemModelOutput.accept(item.get(), ItemModelUtils.plainModel(model));
        }
    }

    private void registerModuleItems(final BlockModelGenerators blocks) {
        moduleTemplate().build().create(MODULE_ITEM_MODEL, new TextureMapping(), blocks.modelOutput);

        module(blocks, Items.AUDIO_MODULE, modelLocation("block/overlay/audio_module"));
        module(blocks, Items.DISPLAY_MODULE, modelLocation("block/overlay/item/display_module"));
        module(blocks, Items.EXECUTION_MODULE, modelLocation("block/overlay/execution_module_running"));
        module(blocks, Items.FACADE_MODULE, Identifier.withDefaultNamespace("block/iron_block"));
        module(blocks, Items.INFRARED_MODULE, modelLocation("block/overlay/infrared_module"));
        module(blocks, Items.KEYPAD_MODULE, modelLocation("block/overlay/keypad_module"));
        module(blocks, Items.QUEUE_MODULE, modelLocation("block/overlay/item/queue_module"));
        module(blocks, Items.RANDOM_MODULE, modelLocation("block/overlay/random_module"));
        module(blocks, Items.RANDOM_ACCESS_MEMORY_MODULE, modelLocation("block/overlay/item/random_access_memory_module"));
        module(blocks, Items.READ_ONLY_MEMORY_MODULE, modelLocation("block/overlay/item/read_only_memory_module"));
        module(blocks, Items.REDSTONE_MODULE, modelLocation("block/overlay/item/redstone_module"));
        module(blocks, Items.SEQUENCER_MODULE, modelLocation("block/overlay/item/sequencer_module"));
        module(blocks, Items.SERIAL_PORT_MODULE, modelLocation("block/overlay/serial_port_module"));
        module(blocks, Items.STACK_MODULE, modelLocation("block/overlay/item/stack_module"));
        module(blocks, Items.TERMINAL_MODULE, modelLocation("block/overlay/item/terminal_module"));
        module(blocks, Items.TIMER_MODULE, modelLocation("block/overlay/item/timer_module"));
    }

    private void module(final BlockModelGenerators blocks, final RegistrySupplier<? extends Item> item, final Identifier overlayTexture) {
        final Identifier model = MODULE_TEMPLATE.create(
            modelLocation("item/" + item.getId().getPath()),
            new TextureMapping()
                .put(TextureSlot.LAYER0, modelLocation("block/casing_module"))
                .put(TextureSlot.LAYER1, overlayTexture)
                .put(TextureSlot.PARTICLE, modelLocation("block/casing_module")),
            blocks.modelOutput);
        blocks.itemModelOutput.accept(item.get(), ItemModelUtils.plainModel(model));
    }

    // --------------------------------------------------------------------- //

    private static final ModelTemplate MODULE_TEMPLATE = new ModelTemplate(
        java.util.Optional.of(MODULE_ITEM_MODEL), java.util.Optional.empty(), TextureSlot.LAYER0, TextureSlot.LAYER1, TextureSlot.PARTICLE);

    private static ExtendedModelTemplateBuilder moduleTemplate() {
        return ExtendedModelTemplateBuilder.builder()
            .guiLight(UnbakedModel.GuiLight.SIDE)
            .element(element -> element
                .from(0, 0, 7)
                .to(16, 16, 8)
                .cube(TextureSlot.LAYER0)
                .face(Direction.DOWN, face -> face.uvs(0, 0, 16, 1))
                .face(Direction.UP, face -> face.uvs(0, 0, 16, 1))
                .face(Direction.WEST, face -> face.uvs(0, 0, 1, 16))
                .face(Direction.EAST, face -> face.uvs(0, 0, 1, 16)))
            .element(element -> element
                .from(0, 0, 7)
                .to(16, 16, 8)
                .face(Direction.NORTH, face -> face.texture(TextureSlot.LAYER1).cullface(Direction.NORTH)))
            .transform(ItemDisplayContext.GUI, t -> t.rotation(30, 135, 0).scale(0.625f))
            .transform(ItemDisplayContext.GROUND, t -> t.translation(0, 3, 0).scale(0.625f))
            .transform(ItemDisplayContext.FIXED, t -> t.scale(1f))
            .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(0, 180, 20).translation(0, 2.5f, 0).scale(0.375f))
            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(-70, 160, 0).scale(0.4f));
    }

    private static VariantMutator rotationFor(final Direction direction) {
        final Quadrant x = switch (direction) {
            case UP -> Quadrant.R90;
            case DOWN -> Quadrant.R270;
            default -> Quadrant.R0;
        };
        final Quadrant y = Quadrant.values()[Math.floorMod((int) direction.toYRot() / 90, 4)];
        return VariantMutator.X_ROT.withValue(x).then(VariantMutator.Y_ROT.withValue(y));
    }

    private static Identifier modelLocation(final String path) {
        return Identifier.fromNamespaceAndPath(API.MOD_ID, path);
    }
}
