package li.cil.tis3d.common.module.execution.target;

import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;

/**
 * Provides an abstracted way of interacting with {@link Target}s, similar to
 * how {@link Pipe}s operate.
 * <p>
 * Depending on the represented {@link Target} this may operate on none, one or
 * multiple pipes (when operating on registers, a port or a virtual port like
 * {@link Target#ANY}, respectively).
 * <p>
 * Note that unlike for pipes, write operations may finish instantly here, when
 * writing to a register, for example. In that case {@link #beginWrite(short)}
 * will return <tt>true</tt> to indicate this.
 */
public interface TargetInterface {
    /**
     * Begin a writing operation on the target.
     * <p>
     * Make sure not to call this if the target is already being written to, or
     * an exception will be thrown. Use {@link #isWriting()} to check for this.
     *
     * @param value the value to write to the target.
     * @return <tt>true</tt> if the operation finished synchronously, <tt>false</tt> otherwise.
     * @throws IllegalStateException if the target is already being written to.
     */
    boolean beginWrite(final short value);

    /**
     * Whether the target is currently being written to.
     * <p>
     * This is <tt>true</tt> as soon as {@link #beginWrite(short)} was called and
     * until {@link #read()} was called to finish or cancel the operation,
     * <em>unless</em> the operation was completed synchronously, in which case
     * {@link #beginWrite(short)} returned <tt>true</tt>.
     *
     * @return whether the target is currently being written to.
     */
    boolean isWriting();

    // --------------------------------------------------------------------- //

    /**
     * Begin a reading operation on the target.
     * <p>
     * Make sure not to call this if the target is already being read from, or
     * an exception will be thrown. Use {@link #isReading()} to check for this.
     *
     * @throws IllegalStateException if the target is already being read from.
     */
    void beginRead();

    // cancelRead was not needed anywhere yet, so it has been omitted.

    /**
     * Whether the target is currently being read from.
     * <p>
     * This is <tt>true</tt> as soon as {@link #beginRead()} was called and
     * until {@link #read()} was called to finish the operation.
     *
     * @return whether the target is currently being read from.
     */
    boolean isReading();

    // --------------------------------------------------------------------- //

    /**
     * Whether the target can transfer data, i.e. {@link #read()} can be called.
     * <p>
     * Note that this is <em>not</em> equivalent to <tt>{@link #isReading()} &amp;&amp;
     * {@link #isWriting()}</tt>. Depending on the target type, this may be
     * only true in the first update <em>after</em> that has been true and
     * later, but it may also be true right away or always.
     *
     * @return whether the target can now transfer data.
     */
    boolean canTransfer();

    /**
     * Finish a read operation by fetching the data made available from the
     * represented target.
     * <p>
     * Make sure not to call this if the target is not in the correct state, or
     * an exception will be thrown. use {@link #canTransfer()} to check for this.
     *
     * @return the value transferred through the target.
     * @throws IllegalStateException if the target is in an incorrect state.
     */
    short read();

    /**
     * Finish a write operation started by the instruction, usually by
     * advancing the program counter.
     * <p>
     * Instructions must <em>always</em> await or cancel a write operation they
     * started.
     *
     * @param port the port the interface was writing to.
     */
    default void onWriteComplete(final Port port) {
    }
}
