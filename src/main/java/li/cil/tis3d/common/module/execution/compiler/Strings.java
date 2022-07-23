package li.cil.tis3d.common.module.execution.compiler;

import net.minecraft.network.chat.Component;

public final class Strings {
    public static final Component MESSAGE_TOO_MANY_LINES = Component.translatable("tis3d.compiler.too_many_lines");
    public static final Component MESSAGE_TOO_MANY_COLUMNS = Component.translatable("tis3d.compiler.too_many_columns");
    public static final Component MESSAGE_INVALID_FORMAT = Component.translatable("tis3d.compiler.invalid_format");
    public static final Component MESSAGE_LABEL_DUPLICATE = Component.translatable("tis3d.compiler.label_duplicate");
    public static final Component MESSAGE_PARAMETER_OVERFLOW = Component.translatable("tis3d.compiler.parameter_overflow");
    public static final Component MESSAGE_PARAMETER_UNDERFLOW = Component.translatable("tis3d.compiler.parameter_underflow");
    public static final Component MESSAGE_PARAMETER_INVALID = Component.translatable("tis3d.compiler.parameter_invalid");
    public static final Component MESSAGE_LABEL_NOT_FOUND = Component.translatable("tis3d.compiler.label_not_found");
    public static final Component MESSAGE_INVALID_INSTRUCTION = Component.translatable("tis3d.compiler.invalid_instruction");

    public static Component getCompileError(final ParseException e) {
        return Component.translatable("tis3d.compiler.error", e.getLineNumber(), e.getStart(), e.getEnd(), e.getDisplayMessage());
    }

    // --------------------------------------------------------------------- //

    private Strings() {
    }
}
