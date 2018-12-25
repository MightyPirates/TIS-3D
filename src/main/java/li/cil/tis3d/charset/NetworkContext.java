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

import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ThreadTaskQueue;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Optional;

public class NetworkContext {
    private final PacketContext context;

    public NetworkContext(PacketContext context) {
        this.context = context;
    }

    public ThreadTaskQueue getListener() {
        return context.getTaskQueue();
    }

    public PlayerEntity getPlayer() {
        return context.getPlayer();
    }

    public Optional<World> getWorld(DimensionType dimension) {
        PlayerEntity player = getPlayer();
        if (player instanceof ServerPlayerEntity) {
            return Optional.of(player.getServer().getWorld(dimension));
        } else {
            return player.getEntityWorld().getDimension().getType() == dimension ? Optional.of(player.getEntityWorld()) : Optional.empty();
        }
    }
}
