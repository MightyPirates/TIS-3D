package li.cil.manual.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

//@Mod("markdown_manual")
public final class Main {
    public Main() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Bootstrap::initialize);
    }
}
