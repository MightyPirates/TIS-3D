package li.cil.tis3d.common.item;

import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Items {
    private static final DeferredRegister<Item> ITEMS = RegistryUtils.create(ForgeRegistries.ITEMS);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<Item> CASING = register(Blocks.CASING);
    public static final RegistryObject<Item> CONTROLLER = register(Blocks.CONTROLLER);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<Item> BOOK_CODE = register("code_book", CodeBookItem::new);
    public static final RegistryObject<Item> BOOK_MANUAL = register("manual", ManualItem::new);

    public static final RegistryObject<Item> KEY = register("key", KeyItem::new);
    public static final RegistryObject<Item> KEY_CREATIVE = register("skeleton_key", KeyItem::new);

    public static final RegistryObject<Item> PRISM = register("prism");

    public static final RegistryObject<ModuleItem> AUDIO_MODULE = register("audio_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> DISPLAY_MODULE = register("display_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> EXECUTION_MODULE = register("execution_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> FACADE_MODULE = register("facade_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> INFRARED_MODULE = register("infrared_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> KEYPAD_MODULE = register("keypad_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> QUEUE_MODULE = register("queue_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> RANDOM_MODULE = register("random_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> RANDOM_ACCESS_MEMORY_MODULE = register("random_access_memory_module", ModuleItem::new);
    public static final RegistryObject<ReadOnlyMemoryModuleItem> READ_ONLY_MEMORY_MODULE = register("read_only_memory_module", ReadOnlyMemoryModuleItem::new);
    public static final RegistryObject<ModuleItem> REDSTONE_MODULE = register("redstone_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> SEQUENCER_MODULE = register("sequencer_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> SERIAL_PORT_MODULE = register("serial_port_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> STACK_MODULE = register("stack_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> TERMINAL_MODULE = register("terminal_module", ModuleItem::new);
    public static final RegistryObject<ModuleItem> TIMER_MODULE = register("timer_module", ModuleItem::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }

    public static <T extends Item> boolean is(final ItemStack stack, final RegistryObject<T> item) {
        return is(stack, item.get());
    }

    public static <T extends Item> boolean is(final ItemStack stack, final T item) {
        return !stack.isEmpty() && stack.getItem() == item;
    }

    // --------------------------------------------------------------------- //

    private static RegistryObject<Item> register(final String name) {
        return register(name, ModItem::new);
    }

    private static <T extends Item> RegistryObject<T> register(final String name, final Supplier<T> factory) {
        return ITEMS.register(name, factory);
    }

    private static <T extends Block> RegistryObject<Item> register(final RegistryObject<T> block) {
        return register(block, ModBlockItem::new);
    }

    private static <TBlock extends Block, TItem extends Item> RegistryObject<TItem> register(final RegistryObject<TBlock> block, final Function<TBlock, TItem> factory) {
        return register(block.getId().getPath(), () -> factory.apply(block.get()));
    }
}
