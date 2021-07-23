package li.cil.tis3d.common.module.execution.compiler;

import net.minecraft.network.chat.TranslatableComponent;

public final class Strings {
    public static final TranslatableComponent MESSAGE_TOO_MANY_LINES = new TranslatableComponent("tis3d.compiler.too_many_lines");
    public static final TranslatableComponent MESSAGE_TOO_MANY_COLUMNS = new TranslatableComponent("tis3d.compiler.too_many_columns");
    public static final TranslatableComponent MESSAGE_INVALID_FORMAT = new TranslatableComponent("tis3d.compiler.invalid_format");
    public static final TranslatableComponent MESSAGE_LABEL_DUPLICATE = new TranslatableComponent("tis3d.compiler.label_duplicate");
    public static final TranslatableComponent MESSAGE_PARAMETER_OVERFLOW = new TranslatableComponent("tis3d.compiler.parameter_overflow");
    public static final TranslatableComponent MESSAGE_PARAMETER_UNDERFLOW = new TranslatableComponent("tis3d.compiler.parameter_underflow");
    public static final TranslatableComponent MESSAGE_PARAMETER_INVALID = new TranslatableComponent("tis3d.compiler.parameter_invalid");
    public static final TranslatableComponent MESSAGE_LABEL_NOT_FOUND = new TranslatableComponent("tis3d.compiler.label_not_found");
    public static final TranslatableComponent MESSAGE_INVALID_INSTRUCTION = new TranslatableComponent("tis3d.compiler.invalid_instruction");

    public static TranslatableComponent getCompileError(final ParseException e) {
        return new TranslatableComponent("tis3d.compiler.error", e.getLineNumber(), e.getStart(), e.getEnd(), e.getDisplayMessage());
    }

    // --------------------------------------------------------------------- //

    private Strings() {
    }
}
