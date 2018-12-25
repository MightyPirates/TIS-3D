package li.cil.tis3d.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TIS3D {
    private static Logger log;

    /**
     * Get the logger to be used by the mod.
     *
     * @return the mod's logger.
     */
    public static Logger getLog() {
        if (log == null) {
            log = LogManager.getLogger();
        }
        return log;
    }
}
