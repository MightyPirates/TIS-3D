package li.cil.tis3d.api.module.lightweight;

import li.cil.tis3d.api.machine.CasingBase;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.ModuleBase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A "lightweight module".
 * These modules have no access to Minecraft classes, so they should be portable across MC versions.
 * The module is initialized with initialize(Casing, ).
 * Note that ModuleLightweight only *pretends* to be like other modules - a ModuleLightweight reading itself will find the host module supporting it.
 * ModuleLightweightHost is where the actual abstraction happens.
 */
public interface ModuleLightweight extends ModuleBase {

    /**
     * Initializes the ModuleLightweight (in lieu of interfaceized constructors)
     *
     * @param casing The casing which this instance will use.
     * @param face   The face which this instance is placed on.
     */
    void initialize(final CasingBase casing, final Face face);

    /**
     * Reads the serialized data from writeToData.
     *
     * @param stream The data input.
     * @throws IOException If, and only if, the stream throws an IOException.
     */
    void readFromData(DataInputStream stream) throws IOException;

    /**
     * Writes a serialized form of the module to a DataOutputStream.
     *
     * @param stream The stream to which the module should write data.
     * @throws IOException If, and only if, the stream throws an IOException.
     */
    void writeToData(DataOutputStream stream) throws IOException;

    /**
     * Draws the screen onto a 64x64 image area, one byte per pixel
     * Assuming a 5x5 character set, of which 1 is spacing, you can show a 13x13 text grid if needed.
     * The colour palette is, to reiterate:
     * `0`: White
     * `1`: Orange
     * `2`: Magenta
     * `3`: Light Blue
     * `4`: Yellow
     * `5`: Lime
     * `6`: Pink
     * `7`: Gray
     * `8`: Silver
     * `9`: Cyan
     * `10`: Purple
     * `11`: Blue
     * `12`: Brown
     * `13`: Green
     * `14`: Red
     * `15`: Black
     * All colours are ANDed with 0x0F.
     */
    void getScreen(byte[] storage);

}
