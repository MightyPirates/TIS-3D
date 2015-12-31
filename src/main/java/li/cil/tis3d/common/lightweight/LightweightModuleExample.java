package li.cil.tis3d.common.lightweight;

import li.cil.tis3d.api.machine.CasingBase;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.lightweight.ModuleLightweight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An example of a simple Lightweight Module.
 * Only sends the value 0xFFFF to the right as a test.
 * just shows a scrolling tile pattern, because it looks cool enough that people might want to use it.
 */
public class LightweightModuleExample implements ModuleLightweight {
    // Camera and ticks for advancing camera
    private int cX, cY, ticks;
    private CasingBase casingBase;
    private Face placedFace;

    @Override
    public void initialize(CasingBase casing, Face face) {
        casingBase = casing;
        placedFace = face;
    }

    @Override
    public void readFromData(DataInputStream stream) throws IOException {
        cX = stream.readInt();
        cY = stream.readInt();
        ticks = stream.readInt();
    }

    @Override
    public void writeToData(DataOutputStream stream) throws IOException {
        stream.writeInt(cX);
        stream.writeInt(cY);
        stream.writeInt(ticks);
    }

    @Override
    public void getScreen(byte[] storage) {
        byte[] tile = {
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 3, 3, 3, 3, 3, 3, 3,
                0, 3, 9, 9, 9, 9, 9, 3,
                0, 3, 9, 11, 11, 11, 9, 3,
                0, 3, 9, 11, 11, 11, 9, 3,
                0, 3, 9, 11, 11, 11, 9, 3,
                0, 3, 9, 9, 9, 9, 9, 3,
                0, 3, 3, 3, 3, 3, 3, 3,
        };
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                int gX = x - cX;
                int gY = y - cY;
                int tX = gX & 0x7;
                int tY = gY & 0x7;
                storage[x + (y * 64)] = tile[tX + (tY * 8)];
            }
        }
    }

    @Override
    public CasingBase getCasing() {
        return casingBase;
    }

    @Override
    public Face getFace() {
        return placedFace;
    }

    @Override
    public void step() {
        ticks++;
        cX++;
        if ((ticks & 0x01) == 0)
            cY++;
        Pipe outPipe = getCasing().getSendingPipe(getFace(), Port.RIGHT);
        if (!outPipe.isWriting())
            outPipe.beginWrite((short) 0xFFFF);
    }

    @Override
    public void onEnabled() {
        ticks = 0;
        cX = 0;
        cY = 0;
    }

    @Override
    public void onWriteComplete(Port port) {
        // nothing to do here
    }

    @Override
    public void onDisabled() {

    }

    @Override
    public void onDisposed() {

    }
}
