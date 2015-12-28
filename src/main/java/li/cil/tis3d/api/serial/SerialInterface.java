package li.cil.tis3d.api.serial;

/**
 * Provides serial input and output on an arbitrary block position.
 * <p>
 * This is used by the serial port module to allow communication with specific
 * blocks where a separate module might be overkill. Used for easier
 * integration with other mods.
 * <p>
 * A serial interface always acts passively, i.e. it is always called from
 * the serial port module, and never expected to take action autonomously
 * (e.g. from the wrapped block's tile entity's update).
 * <p>
 * Read and write operations of the serial port module are generally
 * uncoupled, i.e. it not being able to write to the serial interface
 * will not cause it to block and not read from it.
 */
public interface SerialInterface {
    /**
     * Whether the interface can currently be written to.
     * <p>
     * If this is <tt>true</tt>, the serial port module will call {@link #write(short)}
     * when it has something to write. If it is <tt>false</tt>, the serial port module
     * won't read any values. It is legal change the returned value between ticks to
     * cancel an active transfer.
     * <p>
     * This method is called in each update of the serial port module, which
     * may be multiple times per tick, so this method should be decently
     * efficient and not perform expensive computations.
     *
     * @return <tt>true</tt> if the interface can currently be written to.
     */
    boolean canWrite();

    /**
     * Called to write a single value to the serial interface.
     * <p>
     * This is only called when {@link #canWrite()} is <tt>true</tt>, but when
     * this is the case, this must not fail. If at all, the value shall be
     * silently dropped.
     *
     * @param value the value written to the serial interface.
     */
    void write(short value);

    /**
     * Whether the interface can currently be read from.
     * <p>
     * If this is <tt>true</tt>, the serial port module will call {@link #peek()}
     * to begin writing that that value to its ports. Once a write on any port
     * completes, the serial port module will call {@link #skip()} to finish
     * the operation. It is also legal change the returned value between ticks
     * to cancel an active transfer.
     * <p>
     * This method is called in each update of the serial port module, which
     * may be multiple times per tick, so this method should be decently
     * efficient and not perform expensive computations.
     *
     * @return <tt>true</tt> if the interface can currently be read from.
     */
    boolean canRead();

    /**
     * Called to read the current value from the serial interface.
     * <p>
     * This is only called when {@link #canRead()} is <tt>true</tt>, but when
     * this is the case, this must not fail.
     * <p>
     * This must not consume the current value. It is legal to change the value
     * returned from this between ticks; the serial port module will notice
     * this and reset any active writes.
     *
     * @return the current value readable from the interface.
     */
    short peek();

    /**
     * Called to finish a read from this module.
     * <p>
     * This is only called when {@link #canRead()} is <tt>true</tt>, but when
     * this is the case, this must not fail.
     */
    void skip();

    /**
     * Reset the state of the serial interface.
     * <p>
     * This is called when the TIS-3D computer the serial port module is part
     * of is shut down, either regularly by the controller losing redstone
     * power, or the casing housing the serial port module getting disconnected
     * from its controller. This is also called when the serial port module is
     * removed from its casing.
     */
    void reset();
}
