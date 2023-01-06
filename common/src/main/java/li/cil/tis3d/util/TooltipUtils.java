package li.cil.tis3d.util;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class TooltipUtils {
    public static void tryAddDescription(final ItemStack stack, final List<Component> tooltip) {
        if (stack.isEmpty()) {
            return;
        }

        final String translationKey = stack.getDescriptionId() + ".desc";
        final Language language = Language.getInstance();
        if (language.has(translationKey)) {
            final MutableComponent description = Component.translatable(translationKey);
            tooltip.add(makeGray(description));
        }
    }

    private static MutableComponent makeGray(final MutableComponent text) {
        return text.withStyle(s -> s.withColor(ChatFormatting.GRAY));
    }
}
