/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.data.neoforge;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.block.Blocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput,
            requiredTables(),
            List.of(new SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)),
            registries);
    }

    private static Set<ResourceKey<LootTable>> requiredTables() {
        return BuiltInLootTables
            .all()
            .stream()
            .filter(table -> Objects.equals(table.location().getNamespace(), API.MOD_ID))
            .collect(Collectors.toSet());
    }

    public static final class ModBlockLootTables extends BlockLootSubProvider {
        public ModBlockLootTables(final HolderLookup.Provider registries) {
            super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            dropSelf(Blocks.CASING.get());
            dropSelf(Blocks.CONTROLLER.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return StreamSupport.stream(super.getKnownBlocks().spliterator(), false)
                .filter(block -> {
                    final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
                    return Objects.equals(blockId.getNamespace(), API.MOD_ID);
                })
                .collect(Collectors.toSet());
        }
    }
}
