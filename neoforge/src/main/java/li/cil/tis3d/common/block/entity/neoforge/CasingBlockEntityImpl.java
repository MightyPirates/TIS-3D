/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.block.entity.neoforge;

import li.cil.tis3d.common.block.entity.CasingBlockEntity;

public final class CasingBlockEntityImpl {
    public static void invalidateModelData(final CasingBlockEntity casing) {
        casing.requestModelDataUpdate();
    }
}
