package li.cil.tis3d.system.module;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Pipe;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.prefab.AbstractModule;
import net.minecraft.nbt.NBTTagCompound;
import scala.Array;

/**
 * The stack module can be used to store a number of values to be retrieved
 * later on. It operates as LIFO queue, providing the top element to all ports
 * but a single value can only be read from one port.
 * <p>
 * While it is not full, it will receive data on all ports and push them back.
 */
public final class ModuleStack extends AbstractModule {
    /**
     * The number of elements the stack may store.
     */
    public static final int STACK_SIZE = 15;

    // --------------------------------------------------------------------- //
    // Persisted data

    private final int[] stack = new int[STACK_SIZE];
    private int top = -1;

    // --------------------------------------------------------------------- //

    public ModuleStack(final Casing casing, final Face face) {
        super(casing, face);
    }

    private boolean isEmpty() {
        return top < 0;
    }

    private boolean isFull() {
        return top >= STACK_SIZE - 1;
    }

    private void push(final int value) {
        stack[++top] = value;
    }

    private int peek() {
        return stack[top];
    }

    private void pop() {
        --top;
    }

    private void stepOutput() {
        // Don't try to write if the stack is empty.
        if (isEmpty()) {
            return;
        }

        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
            if (!sendingPipe.isWriting()) {
                sendingPipe.beginWrite(peek());
            }
        }
    }

    private void stepInput() {
        for (final Port port : Port.VALUES) {
            // Stop reading if the stack is full.
            if (isFull()) {
                return;
            }

            // Continuously read from all ports, push back last received value.
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                // Store the value.
                push(receivingPipe.read());

                // Restart all writes to ensure we're outputting the top-most value.
                cancelWrite();

                // Start reading again right away to read as fast as possible.
                receivingPipe.beginRead();
            }
        }
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        stepOutput();
        stepInput();
    }

    @Override
    public void onDisabled() {
        // Clear stack on shutdown.
        top = -1;
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Pop the top value (the one that was being written).
        pop();

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();

        // Start writing again right away to write as fast as possible.
        stepOutput();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        final int[] stackNbt = nbt.getIntArray("stack");
        Array.copy(stackNbt, 0, stack, 0, Math.min(stackNbt.length, stack.length));
        top = Math.max(-1, Math.min(STACK_SIZE - 1, nbt.getInteger("top")));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        nbt.setIntArray("stack", stack);
        nbt.setInteger("top", top);
    }
}
