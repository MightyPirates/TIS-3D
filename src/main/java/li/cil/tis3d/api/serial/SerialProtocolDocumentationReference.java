package li.cil.tis3d.api.serial;

/**
 * Represents a reference to a manual page describing a protocol used by
 * a {@link SerialInterface}.
 */
public final class SerialProtocolDocumentationReference {
    /**
     * The name of the reference, i.e. the text of the link in the manual.
     * <p>
     * Note that this is run through the {@link net.minecraft.util.StatCollector},
     * so it is generally a good idea to use localizable values here.
     */
    public final String name;

    /**
     * The path to the manual page describing the protocol.
     */
    public final String link;

    // --------------------------------------------------------------------- //

    public SerialProtocolDocumentationReference(final String name, final String link) {
        this.name = name;
        this.link = link;
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + link.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SerialProtocolDocumentationReference that = (SerialProtocolDocumentationReference) o;
        return name.equals(that.name) && link.equals(that.link);

    }

    @Override
    public String toString() {
        return String.format("SerialProtocolDocumentationReference{name='%s', link='%s'}", name, link);
    }
}
