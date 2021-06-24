package li.cil.tis3d.common.module.execution.compiler;

import net.minecraft.util.text.TranslationTextComponent;

public final class Strings {
    public static final TranslationTextComponent MESSAGE_TOO_MANY_LINES = new TranslationTextComponent("tis3d.compiler.too_many_lines");
    public static final TranslationTextComponent MESSAGE_TOO_MANY_COLUMNS = new TranslationTextComponent("tis3d.compiler.too_many_columns");
    public static final TranslationTextComponent MESSAGE_INVALID_FORMAT = new TranslationTextComponent("tis3d.compiler.invalid_format");
    public static final TranslationTextComponent MESSAGE_LABEL_DUPLICATE = new TranslationTextComponent("tis3d.compiler.label_duplicate");
    public static final TranslationTextComponent MESSAGE_PARAMETER_OVERFLOW = new TranslationTextComponent("tis3d.compiler.parameter_overflow");
    public static final TranslationTextComponent MESSAGE_PARAMETER_UNDERFLOW = new TranslationTextComponent("tis3d.compiler.parameter_underflow");
    public static final TranslationTextComponent MESSAGE_PARAMETER_INVALID = new TranslationTextComponent("tis3d.compiler.parameter_invalid");
    public static final TranslationTextComponent MESSAGE_LABEL_NOT_FOUND = new TranslationTextComponent("tis3d.compiler.label_not_found");
    public static final TranslationTextComponent MESSAGE_INVALID_INSTRUCTION = new TranslationTextComponent("tis3d.compiler.invalid_instruction");

    public static TranslationTextComponent getCompileError(final ParseException e) {
        return new TranslationTextComponent("tis3d.compiler.error", e.getLineNumber(), e.getStart(), e.getEnd(), e.getDisplayMessage());
    }

    // --------------------------------------------------------------------- //

    private Strings() {
    }
}
