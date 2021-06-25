package li.cil.tis3d.client.manual;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.detail.ManualAPI;
import li.cil.tis3d.api.prefab.manual.NamespaceContentProvider;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import li.cil.tis3d.common.provider.SerialInterfaceProviders;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This content provider kicks in to serve one specific page, the one
 * listing the known serial protocols. It does this by only providing
 * content for that path, and when queried for it, gets the base content
 * which is the "template" for the page, and then populates it with the
 * list of known protocols.
 */
public final class SerialProtocolContentProvider extends NamespaceContentProvider {
    private static final String SERIAL_PROTOCOLS_PATH = ManualAPI.LANGUAGE_KEY + "/serial_protocols.md";
    private static final String SERIAL_PROTOCOLS_TEMPLATE = ManualAPI.LANGUAGE_KEY + "/template/serial_protocols.md";

    private static final String PATTERN_LIST = "%PROTOCOLS%";
    private static final String PATTERN_LINE_END = "\r?\n";

    // --------------------------------------------------------------------- //

    SerialProtocolContentProvider() {
        super(API.MOD_ID, "doc/");
    }

    // --------------------------------------------------------------------- //

    @Override
    public Optional<Iterable<String>> getContent(final String path, final String language) {
        final String localizedProtocolsPath = SERIAL_PROTOCOLS_PATH.replaceAll(ManualAPI.LANGUAGE_KEY, language);
        if (localizedProtocolsPath.equals(path)) {
            final String localizedTemplatePath = SERIAL_PROTOCOLS_TEMPLATE.replaceAll(ManualAPI.LANGUAGE_KEY, language);
            return super.getContent(localizedTemplatePath, language).
                map(lines -> StreamSupport.
                    stream(lines.spliterator(), false).
                    map(line -> line.replaceAll(PATTERN_LIST, compileLinkList())).
                    flatMap(expandedLine -> Arrays.stream(expandedLine.split(PATTERN_LINE_END))).
                    collect(Collectors.toList())
                );
        }
        return Optional.empty();
    }

    // --------------------------------------------------------------------- //

    private String compileLinkList() {
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
            final String name = protocol.getName().getString();
            final String link = protocol.getLink();
            sb.append("- [").append(name).append("](").append(link).append(")\n");
        }
        return sb.toString();
    }
}
