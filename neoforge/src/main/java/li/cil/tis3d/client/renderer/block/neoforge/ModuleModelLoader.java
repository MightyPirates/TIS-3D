/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.renderer.block.neoforge;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.neoforged.neoforge.client.model.ElementsModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public final class ModuleModelLoader implements IGeometryLoader<ModuleModel> {
    // --------------------------------------------------------------------- //
    // IGeometryLoader

    @Override
    public ModuleModel read(final JsonObject modelContents, final JsonDeserializationContext context) throws JsonParseException {
        return new ModuleModel(ElementsModel.Loader.INSTANCE.read(modelContents, context));
    }
}
