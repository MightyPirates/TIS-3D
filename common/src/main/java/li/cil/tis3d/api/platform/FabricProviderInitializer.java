/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.api.platform;

/**
 * Fabric specific interface, used to define an entrypoint that is invoked when all
 * TIS-3D owned registries have been created, and entries may be registered with
 * them. This is Fabric's approach to allowing ordered initialization. For Forge this
 * is not required, since it allows declaring mod initialization ordering.
 * <p>
 * Declare an implementation under the {@code tis3d:registration} entrypoint in
 * {@code fabric.mod.json}:
 * <pre>
 * "entrypoints": {
 *     "tis3d:registration": [
 *         "com.example.MyProviderInitializer"
 *     ]
 * }
 * </pre>
 */
public interface FabricProviderInitializer {
    /**
     * Registers providers with provider registries.
     */
    void registerProviders();
}
