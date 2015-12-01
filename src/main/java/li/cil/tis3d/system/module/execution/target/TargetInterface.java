package li.cil.tis3d.system.module.execution.target;

public interface TargetInterface {
    void beginWrite(final int value);

    void cancelWrite();

    boolean isWriting();

    boolean isOutputTransferring();

    void beginRead();

    boolean isReading();

    boolean isInputTransferring();

    int read();
}
