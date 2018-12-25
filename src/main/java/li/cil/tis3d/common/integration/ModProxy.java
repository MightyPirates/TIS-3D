package li.cil.tis3d.common.integration;

public interface ModProxy {
    boolean isAvailable();

    default void init() {
    }
}
