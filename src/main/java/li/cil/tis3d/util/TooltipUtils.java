package li.cil.tis3d.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;

import java.util.List;

public final class TooltipUtils {
    public static void tryAddDescription(final ItemStack stack, final List<ITextComponent> tooltip) {
        if (stack.isEmpty()) {
            return;
        }

        final String translationKey = stack.getTranslationKey() + ".desc";
        final LanguageMap languagemap = LanguageMap.getInstance();
        if (languagemap.func_230506_b_(translationKey)) {
            final TranslationTextComponent description = new TranslationTextComponent(translationKey);
            tooltip.add(makeGray(description));
        }
    }

    private static IFormattableTextComponent makeGray(final IFormattableTextComponent text) {
        return text.modifyStyle(s -> s.setColor(net.minecraft.util.text.Color.fromTextFormatting(TextFormatting.GRAY)));
    }
}
