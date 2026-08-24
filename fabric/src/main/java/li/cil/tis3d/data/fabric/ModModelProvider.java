/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.data.fabric;

import com.mojang.math.Quadrant;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.client.renderer.block.fabric.ModuleUnbakedModel;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.common.item.Items;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.client.renderer.block.model.multipart.KeyValueCondition;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ModModelProvider extends FabricModelProvider {
    private static final Identifier FULL_CASING_MODEL = modelLocation("block/casing_all");
    private static final Identifier EMPTY_CASING_MODEL = modelLocation("block/casing_empty");
    private static final Identifier MODULE_IN_CASING_MODEL = modelLocation("block/casing_module");
    private static final Identifier CONTROLLER_MODEL = modelLocation("block/controller");


    public ModModelProvider(final FabricDataOutput output) {
        super(output);
    }

    // --------------------------------------------------------------------- //

    @Override
    public void generateBlockStateModels(final BlockModelGenerators blocks) {
        blocks.blockStateOutput.accept(casingDefinition());
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
            Blocks.CONTROLLER.get(), BlockModelGenerators.plainVariant(CONTROLLER_MODEL)));

        // The generator would otherwise default these to a model that does not exist.
        blocks.itemModelOutput.accept(Items.CASING.get(), ItemModelUtils.plainModel(FULL_CASING_MODEL));
        blocks.itemModelOutput.accept(Items.CONTROLLER.get(), ItemModelUtils.plainModel(CONTROLLER_MODEL));
    }

    @Override
    public void generateItemModels(final ItemModelGenerators items) {
        // Item models come from the NeoForge datagen; see fabric/build.gradle.kts.
    }

    // --------------------------------------------------------------------- //

    private static BlockModelDefinitionGenerator casingDefinition() {
        final List<Selector> selectors = new ArrayList<>();
        CasingBlock.FACE_TO_PROPERTY.forEach((face, property) -> {
            final Direction direction = Face.toDirection(face);
            final VariantMutator rotation = rotationFor(direction);

            selectors.add(new Selector(Optional.of(condition(property.getName(), "false")),
                BlockModelGenerators.plainVariant(EMPTY_CASING_MODEL).with(rotation).toUnbaked()));

            final MultiVariant proxy = BlockModelGenerators.plainVariant(MODULE_IN_CASING_MODEL).with(rotation);
            selectors.add(new Selector(Optional.of(condition(property.getName(), "true")),
                new ModuleUnbakedModel(direction, proxy.toUnbaked())));
        });

        return new BlockModelDefinitionGenerator() {
            @Override
            public Block block() {
                return Blocks.CASING.get();
            }

            @Override
            public BlockModelDefinition create() {
                return new BlockModelDefinition(Optional.empty(),
                    Optional.of(new BlockModelDefinition.MultiPartDefinition(selectors)));
            }
        };
    }

    private static Condition condition(final String property, final String value) {
        return new KeyValueCondition(Map.of(property, new KeyValueCondition.Terms(List.of(new KeyValueCondition.Term(value, false)))));
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
