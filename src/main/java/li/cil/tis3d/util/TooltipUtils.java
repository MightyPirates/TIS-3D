package li.cil.tis3d.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;

import java.util.List;

public final class TooltipUtils {
    public static void tryAddDescription(final ItemStack stack, final List<ITextComponent> tooltip) {
        if (stack.isEmpty()) {
            return;
        }

        final String translationKey = stack.getDescriptionId() + ".desc";
        final LanguageMap languagemap = LanguageMap.getInstance();
        if (languagemap.has(translationKey)) {
            final TranslationTextComponent description = new TranslationTextComponent(translationKey);
            tooltip.add(makeGray(description));
        }
    }

    private static IFormattableTextComponent makeGray(final IFormattableTextComponent text) {
        return text.withStyle(s -> s.withColor(TextFormatting.GRAY));
    }
}
