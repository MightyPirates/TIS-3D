package li.cil.tis3d.client.manual.provider;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import li.cil.tis3d.api.manual.ContentRenderer;
import li.cil.tis3d.api.prefab.manual.AbstractRendererProvider;
import li.cil.tis3d.client.manual.Strings;
import li.cil.tis3d.client.manual.segment.render.ItemStackContentRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingContentRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class BlockRendererProvider extends AbstractRendererProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, BlockState> BLOCK_STATE_CACHE = new HashMap<>();

    public BlockRendererProvider() {
        super("block");
    }

    @Override
    protected Optional<ContentRenderer> doGetRenderer(final String data) {
        final BlockState state = Objects.requireNonNull(BLOCK_STATE_CACHE.computeIfAbsent(data, (string) -> {
            try {
                return new BlockStateParser(new StringReader(string), false)
                    .parse(false)
                    .getState();
            } catch (final CommandSyntaxException e) {
                LOGGER.error("Failed parsing block state.", e);
                return Blocks.AIR.defaultBlockState();
            }
        }));

        if (state.getBlock() != Blocks.AIR) {
            return Optional.of(new ItemStackContentRenderer(new ItemStack(state.getBlock())));
        } else {
            return Optional.of(new MissingContentRenderer(Strings.WARNING_BLOCK_MISSING));
        }
    }
}
