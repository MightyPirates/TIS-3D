/**
 * This is the Lightweight Module API, designed for server owners who just want that bit of customization,
 * without actually having to force their players to install custom mods.
 * Or for people who like connecting Minecraft to the real world in some way.
 * The general idea is to be cross-Minecraft-version, and avoid a Lightweight module having to use Minecraft components.
 * However, this leads to certain limitations.
 * Lightweight modules only run on the server, and currently have no documentation in the Manual.
 * To avoid depending on a specific version of Minecraft, they don't allow in-world access.
 * The following are part of the Lightweight module API:
 * module.lightweight.* (contains ModuleLightweight, the interface to implement)
 * module.ModuleBase (Common between full-fledged Modules and Lightweight modules)
 * machine.CasingBase (Casing goes here!)
 * machine.Face, machine.Port, machine.Pipe, machine.HaltAndCatchFireException
 */
package li.cil.tis3d.api.module.lightweight;