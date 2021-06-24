package li.cil.tis3d.client.manual;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.prefab.manual.ResourceContentProvider;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import li.cil.tis3d.common.provider.SerialInterfaceProviders;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This content provider kicks in to serve one specific page, the one
 * listing the known serial protocols. It does this by only providing
 * content for that path, and when queried for it, gets the base content
 * which is the "template" for the page, and then populates it with the
 * list of known protocols.
 */
public final class SerialProtocolContentProvider extends ResourceContentProvider {
    private static final String LANGUAGE_KEY = "%LANGUAGE%";
    private static final Pattern PATTERN_LANGUAGE_KEY = Pattern.compile(LANGUAGE_KEY);
    private static final String SERIAL_PROTOCOLS_PATH = "%LANGUAGE%/serial_protocols.md";
    private static final String SERIAL_PROTOCOLS_TEMPLATE = "%LANGUAGE%/template/serial_protocols.md";
    private static final Pattern PATTERN_LIST = Pattern.compile("@PROTOCOLS@");
    private static final Pattern PATTERN_LINE_END = Pattern.compile("\r?\n");

    // --------------------------------------------------------------------- //

    @Nullable
    private String cachedList = null;

    // --------------------------------------------------------------------- //

    SerialProtocolContentProvider() {
        super(API.MOD_ID, "doc/");
    }

    // --------------------------------------------------------------------- //

    @Override
    @Nullable
    public Iterable<String> getContent(final String path) {
        final String language = Minecraft.getInstance().getLanguageManager().getSelected().getCode();
        final String localizedProtocolsPath = PATTERN_LANGUAGE_KEY.matcher(SERIAL_PROTOCOLS_PATH).replaceAll(language);
        if (localizedProtocolsPath.equals(path)) {
            final String localizedTemplatePath = PATTERN_LANGUAGE_KEY.matcher(SERIAL_PROTOCOLS_TEMPLATE).replaceAll(language);
            return populateTemplate(super.getContent(localizedTemplatePath));
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    @Nullable
    private Iterable<String> populateTemplate(@Nullable final Iterable<String> template) {
        if (template == null) {
            return null;
        }
        return StreamSupport.
            stream(template.spliterator(), false).
            flatMap(line -> Arrays.stream(PATTERN_LINE_END.split(PATTERN_LIST.matcher(line).replaceAll(compileLinkList())))).
            collect(Collectors.toList());
    }

    private String compileLinkList() {
        if (cachedList == null) {
            final StringBuilder sb = new StringBuilder();
            final List<SerialProtocolDocumentationReference> references = new ArrayList<>();
            for (final SerialInterfaceProvider provider : SerialInterfaceProviders.MODULE_PROVIDER_REGISTRY.get()) {
                final SerialProtocolDocumentationReference reference = provider.getDocumentationReference();
                if (reference != null && !references.contains(reference)) {
                    references.add(reference);
                }
            }
            references.sort(Comparator.comparing(s -> s.getName().getString()));
            for (final SerialProtocolDocumentationReference protocol : references) {
                sb.append("- [").append(protocol.getName()).append("](").append(protocol.getLink()).append(")\n");
            }
            cachedList = sb.toString();
        }

        return cachedList;
    }
}
