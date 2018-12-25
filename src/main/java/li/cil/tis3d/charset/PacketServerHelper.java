/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of ProtoCharset.
 *
 * ProtoCharset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtoCharset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ProtoCharset.  If not, see <http://www.gnu.org/licenses/>.
 */

package li.cil.tis3d.charset;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;

public final class PacketServerHelper {
    private PacketServerHelper() {

    }

    public static void forEachWatching(World world, BlockPos pos, Consumer<ServerPlayerEntity> consumer) {
        // TODO NORELEASE
        if (world instanceof ServerWorld) {
            world.players.forEach((e) -> {
                consumer.accept((ServerPlayerEntity) e);
            });
        }
    }
}
