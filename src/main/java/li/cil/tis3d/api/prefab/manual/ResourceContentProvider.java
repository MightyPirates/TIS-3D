package li.cil.tis3d.api.prefab.manual;

import com.google.common.base.Charsets;
import li.cil.tis3d.api.manual.ContentProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Basic implementation of a content provider based on Minecraft's resource
 * loading framework.
 * <p>
 * Beware that the manual is unaware of resource domains. In other words, two
 * paths that are identical except for their resource domain will be the same,
 * as seen from the manual.
 */
public class ResourceContentProvider extends ForgeRegistryEntry<ContentProvider> implements ContentProvider {
    private final String resourceDomain;
    private final String basePath;

    public ResourceContentProvider(final String resourceDomain, final String basePath) {
        this.resourceDomain = resourceDomain;
        this.basePath = basePath;
    }

    public ResourceContentProvider(final String resourceDomain) {
        this(resourceDomain, "");
    }

    @Override
    @Nullable
    public Iterable<String> getContent(final String path) {
        final ResourceLocation location = new ResourceLocation(resourceDomain, basePath + (path.startsWith("/") ? path.substring(1) : path));
        try (final InputStream stream = Minecraft.getInstance().getResourceManager().getResource(location).getInputStream()) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
            final ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (final Throwable ignored) {
            return null;
        }
    }
}
