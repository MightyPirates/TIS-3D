package li.cil.tis3d.api;

import li.cil.tis3d.api.module.Module;

/**
 * A pipe used to move data across {@link Port}s between {@link Module}s.
 * <p>
 * Each {@link Casing} provides eight ports to each installed module, each
 * pair of modules sharing two pipes, going in both directions. This abstracts
 * data transfer between modules.
 * <p>
 * Pipes operate in a synchronized manner, ensuring that when two modules
 * start reading and writing on a pipe between them in the same update step,
 * there is no execution-order-dependent behavior: the pipe requires a read
 * and write operation to be started via {@link #beginRead()} and
 * {@link #beginWrite(int)}, respectively, and will then, <em>after</em> the
 * modules finished their update for the current step switch to a transferring
 * state, so that the modules can finish the operation in the next update.
 * <p>
 * A transfer is completed by the receiver calling {@link #read()}, which will
 * reset the pipe and call {@link Module#onWriteComplete(Port)} on the writing
 * module.
 * <p>
 * So in short, a full transfer cycle looks like this:
 * <ul>
 * <li>Module A starts writing (or reading).</li>
 * <li>Module B starts reading (or writing) (in the same or a later update).</li>
 * <li>At the end of the update step, the pipe will enter transfer mode,
 * indicated by {@link #canTransfer()} being <tt>true</tt>.</li>
 * <li>Module B finishes the transfer by calling {@link #read()}.</li>
 * <li>Module A has it's {@link Module#onWriteComplete(Port)} method called.</li>
 * </ul>
 * <p>
 * Note that at any time during the transfer cycle, one of the two parties may
 * cancel their reading or writing operation by calling {@link #cancelRead()}
 * or {@link #cancelWrite()}, respectively. The other part of the transfer will
 * <em>not</em> be explicitly notified of this change, but this can be checked
 * using the {@link #isReading()} and {@link #isWriting()} methods.
 */
public interface Pipe {
    /**
     * Begin a writing operation on the pipe.
     * <p>
     * Make sure not to call this if the pipe is already being written to, or
     * an exception will be thrown. Use {@link #isWriting()} to check for this.
     *
     * @param value the value to write to the pipe.
     * @throws IllegalStateException if the pipe is already being written to.
     */
    void beginWrite(int value) throws IllegalStateException;

    /**
     * Cancel an active write operation.
     * <p>
     * If the pipe is not currently being written to this does nothing.
     */
    void cancelWrite();

    /**
     * Whether the pipe is currently being written to.
     * <p>
     * This is <tt>true</tt> as soon as {@link #beginWrite(int)} was called and
     * until {@link #read()} or {@link #cancelWrite()} was called to finish or
     * cancel the operation.
     *
     * @return whether the pipe is currently being written to.
     */
    boolean isWriting();

    // --------------------------------------------------------------------- //

    /**
     * Begin a reading operation on the pipe.
     * <p>
     * Make sure not to call this if the pipe is already being read from, or
     * an exception will be thrown. Use {@link #isReading()} to check for this.
     *
     * @throws IllegalStateException if the pipe is already being read from.
     */
    void beginRead() throws IllegalStateException;

    /**
     * Cancel an active read operation.
     * <p>
     * If the pipe is not currently being read from this does nothing.
     */
    void cancelRead();

    /**
     * Whether the pipe is currently being read from.
     * <p>
     * This is <tt>true</tt> as soon as {@link #beginRead()} was called and
     * until {@link #read()} or {@link #cancelRead()} was called to finish or
     * cancel the operation.
     *
     * @return whether the pipe is currently being read from.
     */
    boolean isReading();

    // --------------------------------------------------------------------- //

    /**
     * Whether the pipe can transfer data, i.e. {@link #read()} can be called.
     * <p>
     * Note that this is <em>not</em> equivalent to <tt>{@link #isReading()} &amp;&amp;
     * {@link #isWriting()}</tt>. This is only true in the first update
     * <em>after</em> that has been true and later.
     *
     * @return whether the pipe can now transfer data.
     */
    boolean canTransfer();

    /**
     * Finish a read operation by fetching the data that is written by the
     * write operation running at the same time.
     * <p>
     * Make sure not to call this if the pipe is not in the correct state, or
     * an exception will be thrown. use {@link #canTransfer()} to check for this.
     * <p>
     * This will cause {@link Module#onWriteComplete(Port)} to be called on the
     * module currently writing to the pipe, then reset the pipe (i.e. call
     * {@link #cancelRead()} and {@link #cancelWrite()}) prior to returning the
     * read value.
     *
     * @return the value transferred through the pipe.
     * @throws IllegalStateException if the pipe is in an incorrect state.
     */
    int read() throws IllegalStateException;
}
