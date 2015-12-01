package li.cil.tis3d.api;

import li.cil.tis3d.api.module.Module;

/**
 * A port used to move data between {@link Module}s.
 */
public interface Port {
    void beginWrite(int value);

    void cancelWrite();

    boolean isWriting();

    void beginRead();

    void cancelRead();

    boolean isReading();

    boolean isTransferring();

    int read();
}
