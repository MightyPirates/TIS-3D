package li.cil.tis3d.data;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import li.cil.tis3d.api.API;
import li.cil.tis3d.common.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.*;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(final DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void validate(final Map<ResourceLocation, LootTable> map, final ValidationTracker validationtracker) {
        final Set<ResourceLocation> modLootTableIds =
            LootTables
                .all()
                .stream()
                .filter(lootTable -> Objects.equals(lootTable.getNamespace(), API.MOD_ID))
                .collect(Collectors.toSet());

        for (final ResourceLocation id : Sets.difference(modLootTableIds, map.keySet()))
            validationtracker.reportProblem("Missing mod loot table: " + id);

        map.forEach((location, table) -> LootTableManager.validate(validationtracker, location, table));
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return Collections.singletonList(Pair.of(ModBlockLootTables::new, LootParameterSets.BLOCK));
    }

    public static final class ModBlockLootTables extends BlockLootTables {
        @Override
        protected void addTables() {
            dropSelf(Blocks.CASING.get());
            dropSelf(Blocks.CONTROLLER.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return StreamSupport.stream(super.getKnownBlocks().spliterator(), false)
                .filter(block -> block.getRegistryName() != null && Objects.equals(block.getRegistryName().getNamespace(), API.MOD_ID))
                .collect(Collectors.toSet());
        }
    }
}
