package li.cil.tis3d.common;

import li.cil.tis3d.common.ConfigManager.*;

/**
 * User configurable stuff via config file.
 */
public final class CommonConfig {
    /**
     * The maximum number of packets to allow sending per tick before
     * throttling kicks in, killing duplicate data packets.
     */
    @Path("network") @Min(1) @Max(500)
    @Comment({
        "The maximum number of status packets modules may send per tick.",
        "When this is exceeded, throttling kicks in."})
    @Translation("maxPacketsPerTick")
    public static int maxPacketsPerTick = 10;

    /**
     * The maximum number of particle effects to allow sending per tick
     * before throttling kicks in, killing duplicate effects.
     */
    @Path("network") @Min(1) @Max(500)
    @Comment({
        "The maximum number of particle effects data transfer may trigger per tick.",
        "When this is exceeded, throttling kicks in."})
    @Translation("maxParticlesPerTick")
    public static int maxParticlesPerTick = 5;

    /**
     * The maximum number of casings that may be connected to a controller.
     */
    @Path("controller") @Min(1) @Max(16)
    @Comment("The maximum number of casings a single controller supports.")
    @Translation("maxCasings")
    public static int maxCasingsPerController = 8;

    /**
     * The maximum number of lines a program may have.
     */
    @Path("module.execution") @Min(1) @Max(200)
    @Comment("The maximum number of lines an ASM program for an execution node may have.")
    @Translation("maxLinesPerProgram")
    public static int maxLinesPerProgram = 40;

    /**
     * Maximum number of items stored in our receiver queue.
     * <p>
     * If the queue runs full, additionally received packets will be dropped.
     */
    @Path("module.infrared") @Min(1) @Max(64)
    @Comment("The maximum number of infrared packets that can be stored in the receiver's buffer.")
    @Translation("maxQueueLength")
    public static int maxInfraredQueueLength = 16;
}
