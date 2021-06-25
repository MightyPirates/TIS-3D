package li.cil.tis3d.api.prefab.manual;

import li.cil.tis3d.api.manual.Tab;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractTab extends ForgeRegistryEntry<Tab> implements Tab {
    private final String path;
    @Nullable private final ITextComponent tooltip;

    public AbstractTab(final String path, @Nullable final ITextComponent tooltip) {
        this.path = path;
        this.tooltip = tooltip;
    }

    @Override
    public void getTooltip(final List<ITextComponent> tooltip) {
        if (this.tooltip != null) {
            tooltip.add(this.tooltip);
        }
    }

    @Override
    public String getPath() {
        return this.path;
    }
}
