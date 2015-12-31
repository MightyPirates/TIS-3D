package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.*;
import li.cil.tis3d.api.module.lightweight.ModuleLightweight;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.util.ColorUtils;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public final class ModuleLightweightHost extends AbstractModuleRotatable {

    // --------------------------------------------------------------------- //
    // Persisted data

    /*
     * Server only, initial data, null if none
     */
    private byte[] initData = null;

    /**
     * Serverside-only. If null, then this hasn't been initialized yet, and will do nothing until then.
     */
    private ModuleLightweight guestModule = null;
    private String guestModuleName = "";

    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * The uncompressed image.
     * Also kept on the server to allow sending
     * current state to newly connected/coming closer clients.
     * (NOTE: Why is this byte[]? Because using int[] wastes four times the bandwidth.)
     */
    private final byte[] image = new byte[RESOLUTION * RESOLUTION];
    /**
     * For delta calculation. Only update in the delta calculation code.
     */
    private final byte[] backImage = new byte[RESOLUTION * RESOLUTION];

    // Resolution of the screen in pixels, width = height.
    private static final int RESOLUTION = 64;

    // NBT tag names.
    private static final String TAG_MODULENAME = "modulename";
    private static final String TAG_MODULEDATA = "moduledata";
    private static final String TAG_IMAGE = "image";

    // Data packet sub-types.
    // The format is just continuous subpackets until the pointer goes past the end.
    // The upper nibble is the opcode, though right now 15 of these are IGNRUN derivatives,
    // The lower nibble is extra data.
    // To add an opcode, remove the first or last IGNRUN derivative, make sure they're all in order,
    // and make sure that it's a solid block and IGNRUN0 is at the start.
    // Then set PROTOCOL_IGNRUN_MAX to reflect the highest value encodable using IGNRUNs.
    private static final int PROTOCOL_TYPE_SET = 0x00;
    private static final int PROTOCOL_TYPE_IGNRUN0 = 0x10;
    private static final int PROTOCOL_TYPE_IGNRUN1 = 0x20;
    private static final int PROTOCOL_TYPE_IGNRUN2 = 0x30;
    private static final int PROTOCOL_TYPE_IGNRUN3 = 0x40;
    private static final int PROTOCOL_TYPE_IGNRUN4 = 0x50;
    private static final int PROTOCOL_TYPE_IGNRUN5 = 0x60;
    private static final int PROTOCOL_TYPE_IGNRUN6 = 0x70;
    private static final int PROTOCOL_TYPE_IGNRUN7 = 0x80;
    private static final int PROTOCOL_TYPE_IGNRUN8 = 0x90;
    private static final int PROTOCOL_TYPE_IGNRUN9 = 0xA0;
    private static final int PROTOCOL_TYPE_IGNRUNA = 0xB0;
    private static final int PROTOCOL_TYPE_IGNRUNB = 0xC0;
    private static final int PROTOCOL_TYPE_IGNRUNC = 0xD0;
    private static final int PROTOCOL_TYPE_IGNRUND = 0xE0;
    private static final int PROTOCOL_TYPE_IGNRUNE = 0xF0;

    // maximum value encodable using IGNRUN, lower if you add another nibble type
    // also remove the corrisponding PROTOCOL_TYPE_IGNRUN
    private static final int PROTOCOL_IGNRUN_MAX = 0xEF;

    /**
     * The ID of the uploaded texture on the GPU (client only).
     */
    private int glTextureId;

    /**
     * Server only, did we try initializing the guest module
     */
    private boolean triedInitGuestModule = false;

    // --------------------------------------------------------------------- //

    public ModuleLightweightHost(final Casing casing, final Face face) {
        super(casing, face);
    }

    public ModuleLightweightHost(final Casing casing, final Face face, final String guestModuleName) {
        super(casing, face);
        this.guestModuleName = guestModuleName;
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        assert (!getCasing().getCasingWorld().isRemote);
        initializeModule();
        if (guestModule != null)
            guestModule.step();
        sendDelta();
    }

    @Override
    public void onDisabled() {
        assert (!getCasing().getCasingWorld().isRemote);
        initializeModule();
        if (guestModule != null)
            guestModule.onDisabled();
        sendDelta();
    }

    @Override
    public void onDisposed() {
        if (getCasing().getCasingWorld().isRemote) {
            deleteTexture();
        } else {
            initializeModule();
            if (guestModule != null)
                guestModule.onDisposed();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled) {
            return;
        }

        rotateForRendering();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);

        GlStateManager.bindTexture(getGlTextureId());

        RenderUtil.drawQuad();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        guestModuleName = nbt.getString(TAG_MODULENAME);
        initData = nbt.getByteArray(TAG_MODULEDATA);

        final byte[] nbtImage = nbt.getByteArray(TAG_IMAGE);
        System.arraycopy(nbtImage, 0, image, 0, Math.min(nbtImage.length, image.length));
    }

    @Override
    public void onWriteComplete(final Port port) {
        initializeModule();
        if (guestModule != null)
            guestModule.onWriteComplete(port);
    }


    public void initializeModule() {
        assert (!getCasing().getCasingWorld().isRemote);
        if (!triedInitGuestModule) {
            triedInitGuestModule = true;
            if (guestModuleName.isEmpty()) {
                guestModule = null;
                fillInSecretImage(new String[]{
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        "0  000 000000   0 0 0  000  0   0   00 00  000000   00  00  0000",
                        "0 0 0 0 00000  00 0 0 0 0 0000 000 00 0 0 0 00000  00 000 000000",
                        "0 0 00 000000 0000 00 0 00  00 00   00 00 0 00000   0 000 000  0",
                        "0000000000000000000000000000000000000000000000000000000000000000",
                });
            } else {
                // Let's refer to modules by class name. What could possibly go wrong?
                // TBH, since the modules aren't in properly formatted standardized mods,
                // there's not much that can be done AFAIK.
                try {
                    Class<ModuleLightweight> moduleClass = (Class<ModuleLightweight>) Class.forName(guestModuleName);
                    guestModule = moduleClass.newInstance();
                    guestModule.initialize(getCasing(), getFace());
                    if (initData != null) {
                        guestModule.readFromData(new DataInputStream(new ByteArrayInputStream(initData)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    guestModule = null;
                    fillInSecretImage(new String[]{
                            "0000000000000000000000000000000000000000000000000000000000000000",
                            "0  00000 000000000 00000000000000 00000 000000000 00000000000000",
                            "0 0 0000 00  00000 00000 000 0000  000  00  000   0 00 0 000  00",
                            "0 00 000 0 00 0000 00000 0 0 0000 0 0 0 0 00 0 00 0 00 0 00 00 0",
                            "0 000 00 0 00 0000 00000 0 0 0000 00 00 0 00 0 00 0 00 0 00   00",
                            "0 0000 0 0 00 0000 00000 0 0 0000 00 00 0 00 0 00 0 00 0 00 0000",
                            "0 00000  00  00000     00 0 00000 00 00 00  000   00  000 00  00",
                            "0000000000000000000000000000000000000000000000000000000000000000",
                            "000    0000000000000000000000000000 0000000000000     000     00",
                            "00 00000000000000000000000000000000 000000000000 00000 0 00000 0",
                            "0 0000000  000  00000 000  000  000 000000000000 0 0 0 0 0 0 0 0",
                            "00    00 00 0 00 0000 00 00 0 00 00 000000000000 00 00 0 00 00 0",
                            "000000 0   00   00000 00 00 00   00 000000000000 0 0 0 0 0 0 0 0",
                            "00000 00 0000 0000000 00 00 0000 000000000000000 00000 0 00000 0",
                            "0    0000  000  000000 00  00   000 0000000000000     000     00",
                            "0000000000000000000000000000000000000000000000000000000000000000",
                            "00000000000000000000000000000000000000000000000000           000",
                            "0000000000000000000000000000000000000000000000000 00000000000 00",
                            "0000000000000000000000000000000000000000000000000000000000000000",
                    });
                }
            }
        }
    }

    private void fillInSecretImage(String[] lines) {
        int pos = 0;
        for (String s : lines) {
            for (int i = 0; i < RESOLUTION; i++) {
                if (s.charAt(i) == '0') {
                    image[pos] = 14;
                } else {
                    image[pos] = 15;
                }
                pos++;
            }
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        // Let the module write itself out to an array...
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            initializeModule();
            if (guestModule != null)
                guestModule.writeToData(new DataOutputStream(baos));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        // If a module is uninstalled, the world loaded and saved, then reinstalled,
        // then we'll only lose the module's data. Could be worse.
        nbt.setString(TAG_MODULENAME, guestModuleName);
        nbt.setByteArray(TAG_MODULEDATA, baos.toByteArray());

        nbt.setByteArray(TAG_IMAGE, image);
    }

    // --------------------------------------------------------------------- //

    /**
     * Getter for the ID of the texture on the GPU we're using, creates one if necessary.
     *
     * @return the texture ID we're currently using.
     */
    private int getGlTextureId() {
        if (glTextureId == 0) {
            glTextureId = GlStateManager.generateTexture();
            TextureUtil.allocateTexture(glTextureId, RESOLUTION, RESOLUTION);
            TextureUtil.uploadTexture(glTextureId, createIntImage(), RESOLUTION, RESOLUTION);
        }
        return glTextureId;
    }

    /**
     * Deletes our texture from the GPU, if we have one.
     */
    private void deleteTexture() {
        if (glTextureId != 0) {
            TextureUtil.deleteTexture(glTextureId);
            glTextureId = 0;
        }
    }

    // -------------------NETWORKING CODE BELOW----------------------------- //

    @Override
    public void onData(final ByteBuf data) {
        int i = 0;
        while (i < (RESOLUTION * RESOLUTION)) {
            int val = data.readUnsignedByte();
            switch (val & 0x10) {
                case PROTOCOL_TYPE_IGNRUN0:
                case PROTOCOL_TYPE_IGNRUN1:
                case PROTOCOL_TYPE_IGNRUN2:
                case PROTOCOL_TYPE_IGNRUN3:
                case PROTOCOL_TYPE_IGNRUN4:
                case PROTOCOL_TYPE_IGNRUN5:
                case PROTOCOL_TYPE_IGNRUN6:
                case PROTOCOL_TYPE_IGNRUN7:
                case PROTOCOL_TYPE_IGNRUN8:
                case PROTOCOL_TYPE_IGNRUN9:
                case PROTOCOL_TYPE_IGNRUNA:
                case PROTOCOL_TYPE_IGNRUNB:
                case PROTOCOL_TYPE_IGNRUNC:
                case PROTOCOL_TYPE_IGNRUND:
                case PROTOCOL_TYPE_IGNRUNE:
                    i += (byte) (val - PROTOCOL_TYPE_IGNRUN0);
                    break;
                case PROTOCOL_TYPE_SET:
                    image[i++] = (byte) (val & 0xF);
                    break;
                default:
                    throw new RuntimeException("Unknown data type.");
            }
        }
        int[] img = createIntImage();
        TextureUtil.uploadTexture(getGlTextureId(), createIntImage(), RESOLUTION, RESOLUTION);
    }

    private int[] createIntImage() {
        int[] img = new int[image.length];
        for (int i2 = 0; i2 < img.length; i2++)
            img[i2] = ColorUtils.getColorByIndex(image[i2]);
        return img;
    }

    private void sendDelta() {
        initializeModule();
        if (guestModule != null)
            guestModule.getScreen(image);
        final ByteBuf data = Unpooled.buffer();
        int ignSize = 0; // 0: haven't started encoding an IGN yet
        for (int y = 0; y < RESOLUTION; y++) {
            // I've already made sure IGN is always line-aligned, maybe consider a copy-from-other-source-line
            for (int x = 0; x < RESOLUTION; x++) {
                int i = x + (y * RESOLUTION);
                if (image[i] == backImage[i]) {
                    if (ignSize == PROTOCOL_IGNRUN_MAX) {
                        data.writeByte(PROTOCOL_TYPE_IGNRUN0 + ignSize);
                        ignSize = 0;
                    }
                    ignSize++;
                } else {
                    if (ignSize != 0) {
                        data.writeByte(PROTOCOL_TYPE_IGNRUN0 + ignSize);
                        ignSize = 0;
                    }
                    data.writeByte(PROTOCOL_TYPE_SET | (image[i] & 0xF));
                    backImage[i] = image[i];
                }
            }
            if (ignSize != 0) {
                data.writeByte(PROTOCOL_TYPE_IGNRUN0 + ignSize);
                ignSize = 0;
            }
        }
        getCasing().sendOrderedData(getFace(), data);
    }

}
