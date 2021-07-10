package li.cil.manual.api.prefab.provider;

import com.google.common.base.Charsets;
import li.cil.manual.api.provider.ContentProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Basic implementation of a content provider based on Minecraft's resource
 * loading framework.
 * <p>
 * Beware that the manual is unaware of resource domains. In other words, two
 * paths that are identical except for their resource domain will be the same,
 * as seen from the manual.
 */
@OnlyIn(Dist.CLIENT)
public class NamespaceContentProvider extends ForgeRegistryEntry<ContentProvider> implements ContentProvider {
    private final String namespace;
    private final String basePath;

    public NamespaceContentProvider(final String namespace, final String basePath) {
        this.namespace = namespace;
        this.basePath = basePath.endsWith("/") ? basePath : (basePath + "/");
    }

    public NamespaceContentProvider(final String namespace) {
        this(namespace, "");
    }

    @Override
    public Optional<Stream<String>> getContent(final String path, final String language) {
        final IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        final ResourceLocation location = new ResourceLocation(namespace, basePath + path);
        try (final InputStream stream = resourceManager.getResource(location).getInputStream()) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
            final ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return Optional.of(lines.stream());
        } catch (final Throwable ignored) {
            return Optional.empty();
        }
    }
}
