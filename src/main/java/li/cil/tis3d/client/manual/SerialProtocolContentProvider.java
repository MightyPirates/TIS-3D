package li.cil.tis3d.client.manual;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.content.Document;
import li.cil.manual.api.prefab.provider.NamespaceDocumentProvider;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import li.cil.tis3d.common.provider.SerialInterfaceProviders;

import java.util.*;

import static java.util.Arrays.asList;

/**
 * This content provider kicks in to serve one specific page, the one
 * listing the known serial protocols. It does this by only providing
 * content for that path, and when queried for it, gets the base content
 * which is the "template" for the page, and then populates it with the
 * list of known protocols.
 */
public final class SerialProtocolContentProvider extends NamespaceDocumentProvider {
    private static final String SERIAL_PROTOCOLS_PATH = ManualModel.LANGUAGE_KEY + "/protocols/index.md";

    private static final String PATTERN_LIST = "%PROTOCOLS%";
    private static final String PATTERN_LINE_END = "\r?\n";

    // --------------------------------------------------------------------- //

    SerialProtocolContentProvider() {
        super(API.MOD_ID, "doc");
    }

    // --------------------------------------------------------------------- //

    @Override
    public int sortOrder() {
        return -100; // Run before regular content provider to inject our processing.
    }

    @Override
    public Optional<Document> getDocument(final String path, final String language) {
        final String localizedProtocolsPath = SERIAL_PROTOCOLS_PATH.replaceAll(ManualModel.LANGUAGE_KEY, language);
        if (localizedProtocolsPath.equals(path)) {
            return super.getDocument(localizedProtocolsPath, language).
                map(document -> {
                    final List<String> expandedLines = new ArrayList<>(document.getLines().size());
                    for (final String line : document.getLines()) {
                        expandedLines.addAll(asList(line.replaceAll(PATTERN_LIST, compileLinkList()).split(PATTERN_LINE_END)));
                    }
                    return new Document(expandedLines, document.getLocation());
                });
        }
        return Optional.empty();
    }

    // --------------------------------------------------------------------- //

    private String compileLinkList() {
        final StringBuilder sb = new StringBuilder();
        final Set<SerialProtocolDocumentationReference> references = new HashSet<>();
        for (final SerialInterfaceProvider provider : SerialInterfaceProviders.MODULE_PROVIDER_REGISTRY.get()) {
            final Optional<SerialProtocolDocumentationReference> reference = provider.getDocumentationReference();
            reference.ifPresent(references::add);
        }
        references.stream()
            .sorted(Comparator.comparing(reference -> reference.getName().getString()))
            .forEachOrdered(reference -> {
                final String name = reference.getName().getString();
                final String link = reference.getLink();
                sb.append("- [").append(name).append("](").append(link).append(")\n");
            });
        return sb.toString();
    }
}
