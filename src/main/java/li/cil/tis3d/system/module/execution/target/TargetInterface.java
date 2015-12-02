package li.cil.tis3d.system.module.execution.target;

public interface TargetInterface {
    boolean beginWrite(final int value);

    void cancelWrite();

    boolean isWriting();

    void beginRead();

    boolean isReading();

    boolean canRead();

    int read();
}
