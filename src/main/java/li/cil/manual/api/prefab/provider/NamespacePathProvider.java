package li.cil.manual.api.prefab.provider;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.provider.PathProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class NamespacePathProvider extends ForgeRegistryEntry<PathProvider> implements PathProvider {
    private static final String NAMESPACE = "%NAMESPACE%";
    private static final String PATH = "%PATH%";
    private static final String BLOCK_PATH_WITH_NAMESPACE = ManualModel.LANGUAGE_KEY + "/%NAMESPACE%/block/%PATH%.md";
    private static final String BLOCK_PATH = ManualModel.LANGUAGE_KEY + "/block/%PATH%.md";
    private static final String ITEM_PATH_WITH_NAMESPACE = ManualModel.LANGUAGE_KEY + "/%NAMESPACE%/item/%PATH%.md";
    private static final String ITEM_PATH = ManualModel.LANGUAGE_KEY + "/item/%PATH%.md";

    private final String namespace;
    private final boolean keepNamespaceInPath;

    public NamespacePathProvider(final String namespace) {
        this(namespace, false);
    }

    public NamespacePathProvider(final String namespace, final boolean keepNamespaceInPath) {
        this.namespace = namespace;
        this.keepNamespaceInPath = keepNamespaceInPath;
    }

    @Override
    public Optional<String> pathFor(final ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        final Item item = stack.getItem();
        final Block block = Block.byItem(item);
        if (block != Blocks.AIR) {
            final ResourceLocation blockId = block.getRegistryName();
            if (blockId == null) {
                return Optional.empty();
            }

            final String blockNamespace = blockId.getNamespace();
            if (namespace.equals(blockNamespace)) {
                final String template = keepNamespaceInPath ? BLOCK_PATH_WITH_NAMESPACE : BLOCK_PATH;
                return Optional.of(template
                    .replace(NAMESPACE, namespace)
                    .replace(PATH, blockId.getPath()));
            }
        } else {
            final ResourceLocation itemId = item.getRegistryName();
            if (itemId == null) {
                return Optional.empty();
            }

            final String itemNamespace = itemId.getNamespace();
            if (namespace.equals(itemNamespace)) {
                final String template = keepNamespaceInPath ? ITEM_PATH_WITH_NAMESPACE : ITEM_PATH;
                return Optional.of(template
                    .replace(NAMESPACE, namespace)
                    .replace(PATH, itemId.getPath()));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> pathFor(final World world, final BlockPos pos, final Direction face) {
        final Block block = world.getBlockState(pos).getBlock();
        final ResourceLocation blockId = block.getRegistryName();
        if (blockId == null) {
            return Optional.empty();
        }

        final String blockNamespace = blockId.getNamespace();
        if (namespace.equals(blockNamespace)) {
            final String template = keepNamespaceInPath ? BLOCK_PATH_WITH_NAMESPACE : BLOCK_PATH;
            return Optional.of(template
                .replace(NAMESPACE, this.namespace)
                .replace(PATH, blockId.getPath()));
        }

        return Optional.empty();
    }
}
