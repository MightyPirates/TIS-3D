package li.cil.tis3d.system.module.execution.compiler;

/**
 * Thrown by the {@link Compiler} when the specified code contains errors.
 */
public final class ParseException extends Exception {
    private final int lineNumber;
    private final int column;

    public ParseException(final String message, final int lineNumber, final int column) {
        super(message);
        this.lineNumber = lineNumber;
        this.column = column;
    }

    /**
     * The line on which the error occurred.
     *
     * @return the line of the error.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * The column (nth character) on which the error occurred.
     *
     * @return the column of the error.
     */
    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return (lineNumber + 1) + ":" + (column + 1) + ": " + getMessage();
    }
}
