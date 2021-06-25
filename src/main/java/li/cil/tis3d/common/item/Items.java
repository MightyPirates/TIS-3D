package li.cil.tis3d.common.item;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Items {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<Item> CASING = register(Blocks.CASING);
    public static final RegistryObject<Item> CONTROLLER = register(Blocks.CONTROLLER);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<Item> BOOK_CODE = register("code_book", ItemBookCode::new);
    public static final RegistryObject<Item> BOOK_MANUAL = register("manual", ItemBookManual::new);

    public static final RegistryObject<Item> KEY = register("key", ItemKey::new);
    public static final RegistryObject<Item> KEY_CREATIVE = register("skeleton_key", ItemKey::new);

    public static final RegistryObject<Item> PRISM = register("prism");

    public static final RegistryObject<ItemModule> AUDIO_MODULE = register("audio_module", ItemModule::new);
    public static final RegistryObject<ItemModule> DISPLAY_MODULE = register("display_module", ItemModule::new);
    public static final RegistryObject<ItemModule> EXECUTION_MODULE = register("execution_module", ItemModule::new);
    public static final RegistryObject<ItemModule> FACADE_MODULE = register("facade_module", ItemModule::new);
    public static final RegistryObject<ItemModule> INFRARED_MODULE = register("infrared_module", ItemModule::new);
    public static final RegistryObject<ItemModule> KEYPAD_MODULE = register("keypad_module", ItemModule::new);
    public static final RegistryObject<ItemModule> QUEUE_MODULE = register("queue_module", ItemModule::new);
    public static final RegistryObject<ItemModule> RANDOM_MODULE = register("random_module", ItemModule::new);
    public static final RegistryObject<ItemModule> RANDOM_ACCESS_MEMORY_MODULE = register("random_access_memory_module", ItemModule::new);
    public static final RegistryObject<ItemModuleReadOnlyMemory> READ_ONLY_MEMORY_MODULE = register("read_only_memory_module", ItemModuleReadOnlyMemory::new);
    public static final RegistryObject<ItemModule> REDSTONE_MODULE = register("redstone_module", ItemModule::new);
    public static final RegistryObject<ItemModule> SEQUENCER_MODULE = register("sequencer_module", ItemModule::new);
    public static final RegistryObject<ItemModule> SERIAL_PORT_MODULE = register("serial_port_module", ItemModule::new);
    public static final RegistryObject<ItemModule> STACK_MODULE = register("stack_module", ItemModule::new);
    public static final RegistryObject<ItemModule> TERMINAL_MODULE = register("terminal_module", ItemModule::new);
    public static final RegistryObject<ItemModule> TIMER_MODULE = register("timer_module", ItemModule::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
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
