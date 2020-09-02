package li.cil.tis3d.common.api;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.SerialAPI;
import li.cil.tis3d.api.manual.ContentProvider;
import li.cil.tis3d.api.prefab.manual.ResourceContentProvider;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Registry for serial interface providers.
 */
public final class SerialAPIImpl implements SerialAPI {
    public static final SerialAPIImpl INSTANCE = new SerialAPIImpl();

    // --------------------------------------------------------------------- //

    private final List<SerialInterfaceProvider> providers = new ArrayList<>();
    private final SerialProtocolContentProvider serialProtocolContentProvider = new SerialProtocolContentProvider();

    // --------------------------------------------------------------------- //

    public ContentProvider getSerialProtocolContentProvider() {
        return serialProtocolContentProvider;
    }

    // --------------------------------------------------------------------- //

    @Override
    public void addProvider(final SerialInterfaceProvider provider) {
        if (!providers.contains(provider)) {
            providers.add(provider);
            serialProtocolContentProvider.addReference(provider.getDocumentationReference());
        }
    }

    @Override
    @Nullable
    public SerialInterfaceProvider getProviderFor(final World world, final BlockPos position, final Direction side) {
        for (final SerialInterfaceProvider provider : providers) {
            if (provider.worksWith(world, position, side)) {
                return provider;
            }
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    /**
     * This content provider kicks in to serve one specific page, the one
     * listing the known serial protocols. It does this by only providing
     * content for that path, and when queried for it, gets the base content
     * which is the "template" for the page, and then populates it with the
     * list of known protocols.
     */
    private static final class SerialProtocolContentProvider extends ResourceContentProvider {
        private static final String LANGUAGE_KEY = "%LANGUAGE%";
        private static final Pattern PATTERN_LANGUAGE_KEY = Pattern.compile(LANGUAGE_KEY);
        private static final String SERIAL_PROTOCOLS_PATH = "%LANGUAGE%/serial_protocols.md";
        private static final String SERIAL_PROTOCOLS_TEMPLATE = "%LANGUAGE%/template/serial_protocols.md";
        private static final Pattern PATTERN_LIST = Pattern.compile("@PROTOCOLS@");
        private static final Pattern PATTERN_LINE_END = Pattern.compile("\r?\n");

        // --------------------------------------------------------------------- //

        private final List<SerialProtocolDocumentationReference> protocols = new ArrayList<>();

        @Nullable
        private String cachedList = null;

        // --------------------------------------------------------------------- //

        SerialProtocolContentProvider() {
            super(API.MOD_ID, "doc/");
        }

        void addReference(@Nullable final SerialProtocolDocumentationReference reference) {
            if (reference != null && !protocols.contains(reference)) {
                protocols.add(reference);
                cachedList = null;
            }
        }

        // --------------------------------------------------------------------- //

        @Override
        @Environment(EnvType.CLIENT)
        @Nullable
        public Iterable<String> getContent(final String path) {
            final String language = MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode();
            final String localizedProtocolsPath = PATTERN_LANGUAGE_KEY.matcher(SERIAL_PROTOCOLS_PATH).replaceAll(language);
            if (localizedProtocolsPath.equals(path)) {
                final String localizedTemplatePath = PATTERN_LANGUAGE_KEY.matcher(SERIAL_PROTOCOLS_TEMPLATE).replaceAll(language);
                return populateTemplate(super.getContent(localizedTemplatePath));
            }
            return null;
        }

        // --------------------------------------------------------------------- //

        @Environment(EnvType.CLIENT)
        @Nullable
        private Iterable<String> populateTemplate(@Nullable final Iterable<String> template) {
            if (template == null) {
                return null;
            }
            return StreamSupport
                .stream(template.spliterator(), false)
                .flatMap(line -> Arrays.stream(PATTERN_LINE_END.split(PATTERN_LIST.matcher(line).replaceAll(compileLinkList()))))
                .collect(Collectors.toList());
        }

        @Environment(EnvType.CLIENT)
        private String compileLinkList() {
            if (cachedList == null) {
                final StringBuilder sb = new StringBuilder();
                protocols.sort(Comparator.comparing(s -> s.name));
                for (final SerialProtocolDocumentationReference protocol : protocols) {
                    sb.append("- [").append(I18n.translate(protocol.name)).append("](").append(protocol.link).append(")\n");
                }
                cachedList = sb.toString();
            }

            return cachedList;
        }
    }

    // --------------------------------------------------------------------- //

    private SerialAPIImpl() {
    }
}
