package li.cil.tis3d.api.machine;

import li.cil.tis3d.api.module.Module;

/**
 * May be thrown during the execution of {@link Module#step()}.
 * <p>
 * When thrown, will cause the controller of the containing TIS-3D computer to
 * reset and restart after a short delay (and shortly catch on fire).
 */
public final class HaltAndCatchFireException extends RuntimeException {
}
