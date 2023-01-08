package li.cil.tis3d.client.integration.roughlyenoughitems;

import li.cil.tis3d.common.item.Items;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.loader.api.FabricLoader;

public final class ModREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerBasicEntryFiltering(final BasicFilteringRule<?> rule) {
        if (FabricLoader.getInstance().isModLoaded("sodium")) {
            rule.hide(EntryStacks.of(Items.FACADE_MODULE.get()));
        }
    }
}
