package li.cil.tis3d.common.block.property;

import li.cil.tis3d.api.module.traits.CasingFaceQuadOverride;
import net.minecraftforge.common.property.IUnlistedProperty;

public enum PropertyCasingFaceQuadOverrides implements IUnlistedProperty<CasingFaceQuadOverride[]> {
    INSTANCE;

    // --------------------------------------------------------------------- //
    // IUnlistedProperty

    @Override
    public String getName() {
        return "casing_face_quad_overrides";
    }

    @Override
    public boolean isValid(final CasingFaceQuadOverride[] value) {
        return true;
    }

    @Override
    public Class<CasingFaceQuadOverride[]> getType() {
        return CasingFaceQuadOverride[].class;
    }

    @Override
    public String valueToString(final CasingFaceQuadOverride[] value) {
        return String.format("[%s, %s, %s, %s, %s, %s]", value[0], value[1], value[2], value[3], value[4], value[5]);
    }
}
