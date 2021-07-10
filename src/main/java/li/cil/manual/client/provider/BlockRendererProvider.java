package li.cil.manual.client.provider;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import li.cil.manual.api.render.ContentRenderer;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.prefab.provider.AbstractRendererProvider;
import li.cil.manual.client.document.Strings;
import li.cil.manual.client.document.segment.render.ItemStackContentRenderer;
import li.cil.manual.client.document.segment.render.MissingContentRenderer;
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

    // --------------------------------------------------------------------- //

    public BlockRendererProvider() {
        super("block");
    }

    // --------------------------------------------------------------------- //

    @Override
    public boolean matches(final ManualModel manual) {
        return true;
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
            return Optional.of(new MissingContentRenderer(Strings.NO_SUCH_BLOCK));
        }
    }
}
