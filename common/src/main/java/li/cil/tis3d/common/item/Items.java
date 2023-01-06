package li.cil.tis3d.common.item;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Items {
    private static final DeferredRegister<Item> ITEMS = RegistryUtils.get(Registries.ITEM);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<Item> CASING = register(Blocks.CASING);
    public static final RegistrySupplier<Item> CONTROLLER = register(Blocks.CONTROLLER);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<Item> BOOK_CODE = register("code_book", CodeBookItem::new);
    public static final RegistrySupplier<Item> BOOK_MANUAL = register("manual", ManualItem::new);

    public static final RegistrySupplier<Item> KEY = register("key", KeyItem::new);
    public static final RegistrySupplier<Item> KEY_CREATIVE = register("skeleton_key", KeyItem::new);

    public static final RegistrySupplier<Item> PRISM = register("prism");

    public static final RegistrySupplier<ModuleItem> AUDIO_MODULE = register("audio_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> DISPLAY_MODULE = register("display_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> EXECUTION_MODULE = register("execution_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> FACADE_MODULE = register("facade_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> INFRARED_MODULE = register("infrared_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> KEYPAD_MODULE = register("keypad_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> QUEUE_MODULE = register("queue_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> RANDOM_MODULE = register("random_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> RANDOM_ACCESS_MEMORY_MODULE = register("random_access_memory_module", ModuleItem::new);
    public static final RegistrySupplier<ReadOnlyMemoryModuleItem> READ_ONLY_MEMORY_MODULE = register("read_only_memory_module", ReadOnlyMemoryModuleItem::new);
    public static final RegistrySupplier<ModuleItem> REDSTONE_MODULE = register("redstone_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> SEQUENCER_MODULE = register("sequencer_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> SERIAL_PORT_MODULE = register("serial_port_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> STACK_MODULE = register("stack_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> TERMINAL_MODULE = register("terminal_module", ModuleItem::new);
    public static final RegistrySupplier<ModuleItem> TIMER_MODULE = register("timer_module", ModuleItem::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        ITEMS.register();
    }

    public static <T extends Item> boolean is(final ItemStack stack, final RegistrySupplier<T> item) {
        return is(stack, item.get());
    }

    public static <T extends Item> boolean is(final ItemStack stack, final T item) {
        return !stack.isEmpty() && stack.getItem() == item;
    }

    // --------------------------------------------------------------------- //

    private static RegistrySupplier<Item> register(final String name) {
        return register(name, ModItem::new);
    }

    private static <T extends Item> RegistrySupplier<T> register(final String name, final Supplier<T> factory) {
        return ITEMS.register(name, factory);
    }

    private static <T extends Block> RegistrySupplier<Item> register(final RegistrySupplier<T> block) {
        return register(block, ModBlockItem::new);
    }

    private static <TBlock extends Block, TItem extends Item> RegistrySupplier<TItem> register(final RegistrySupplier<TBlock> block, final Function<TBlock, TItem> factory) {
        return register(block.getId().getPath(), () -> factory.apply(block.get()));
    }
}
