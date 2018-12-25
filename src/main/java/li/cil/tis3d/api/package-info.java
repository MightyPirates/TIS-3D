/**
 * Welcome to the TIS-3D API, where some dreams come true, and others go to die.
 * <p>
 * Anyway. This is the place to go if you'd like to register custom modules for
 * TIS-3D, extending its functionality. To do so, implement a module using the
 * {@link li.cil.tis3d.api.module.Module} interface, as well as a provider
 * using the {@link li.cil.tis3d.api.module.ModuleProvider} interface, then
 * register the provider with TIS-3D via {@link li.cil.tis3d.api.ModuleAPI#addProvider(li.cil.tis3d.api.module.ModuleProvider)}.
 * <p>
 * The provider will then be queried by TIS-3D when an item is used on a
 * casing, and if it works the module created by the provider will be used.
 * See the module's interface and the provider's interface to see how you
 * can interact with the casing and other modules.
 */
@ParametersAreNonnullByDefault
package li.cil.tis3d.api;

import javax.annotation.ParametersAreNonnullByDefault;