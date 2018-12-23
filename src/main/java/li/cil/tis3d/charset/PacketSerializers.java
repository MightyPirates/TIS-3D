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

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class PacketSerializers {
	static {
		serializers = Maps.newHashMap();
		deserializers = Maps.newHashMap();
		mapping = Maps.newIdentityHashMap();
		serializerCache = Maps.newIdentityHashMap();
		deserializerCache = Maps.newIdentityHashMap();

		register("s8", (b, o) -> b.writeByte(o.intValue()), (b) -> (Number) b.readByte());
		register("u8", (b, o) -> b.writeByte(o.intValue()), (b) -> (Number) b.readUnsignedByte());
		register("s16", (b, o) -> b.writeShort(o.intValue()), (b) -> (Number) b.readShort());
		register("u16", (b, o) -> b.writeShort(o.intValue()), (b) -> (Number) b.readUnsignedShort());
		register("s24", (b, o) -> b.writeMedium(o.intValue()), (b) -> (Number) b.readMedium());
		register("u24", (b, o) -> b.writeMedium(o.intValue()), (b) -> (Number) b.readUnsignedMedium());
		register("s32", (b, o) -> b.writeInt(o.intValue()), (b) -> (Number) b.readInt());
		register("u32", (b, o) -> b.writeInt(o.intValue()), (b) -> (Number) b.readUnsignedInt());
		register("s64", (b, o) -> b.writeLong(o.longValue()), (b) -> (Number) b.readLong());
		register("f32", (b, o) -> b.writeFloat(o.floatValue()), (b) -> (Number) b.readFloat());
		register("f64", (b, o) -> b.writeDouble(o.doubleValue()), (b) -> (Number) b.readDouble());

		register("var32", (b, o) -> b.writeVarInt(o.intValue()), (b) -> (Number) b.readVarInt());
		register("var64", (b, o) -> b.writeVarLong(o.longValue()), (b) -> (Number) b.readVarLong());

		register("char", Character.class, (BiConsumer<PacketByteBuf, Character>) PacketByteBuf::writeChar, ByteBuf::readChar);
		registerMapping(char.class, "char");

		register("bool", Boolean.class, PacketByteBuf::writeBoolean, PacketByteBuf::readBoolean);
		registerMapping(boolean.class, "bool");

		register("vec3d_32", (b, o) -> {
			b.writeFloat((float) o.x);
			b.writeFloat((float) o.y);
			b.writeFloat((float) o.z);
		}, (b) -> {
			float x = b.readFloat();
			float y = b.readFloat();
			float z = b.readFloat();
			return new Vec3d(x, y, z);
		});
		register("vec3d_64", (b, o) -> {
			b.writeDouble(o.x);
			b.writeDouble(o.y);
			b.writeDouble(o.z);
		}, (b) -> {
			double x = b.readDouble();
			double y = b.readDouble();
			double z = b.readDouble();
			return new Vec3d(x, y, z);
		});
		registerMapping(Vec3d.class, "vec3d_64");

		register(BlockPos.class, PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos);
		register(ItemStack.class, PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack);
		register(Identifier.class, PacketByteBuf::writeIdentifier, PacketByteBuf::readIdentifier);
		register(CompoundTag.class, PacketByteBuf::writeCompoundTag, PacketByteBuf::readCompoundTag);
		register(DimensionType.class, (b, o) -> b.writeVarInt(Registry.DIMENSION.getRawId(o)), (b) -> Registry.DIMENSION.getInt(b.readVarInt()));
		register(String.class, PacketByteBuf::writeString, (b) -> b.readString(32767));
		register(UUID.class, PacketByteBuf::writeUuid, PacketByteBuf::readUuid);
		register(byte[].class, PacketByteBuf::writeByteArray, PacketByteBuf::readByteArray);
		register(int[].class, PacketByteBuf::writeIntArray, PacketByteBuf::readIntArray);

		/* register(IParticleData.class, (b, o) -> {
			Particle
		}, IParticleData::write); */

		registerMapping(Byte.class, "s8");
		registerMapping(Short.class, "s16");
		registerMapping(Integer.class, "var32");
		registerMapping(Long.class, "var64");
		registerMapping(Float.class, "f32");
		registerMapping(Double.class, "f64");
		registerMapping(byte.class, "s8");
		registerMapping(short.class, "s16");
		registerMapping(int.class, "var32");
		registerMapping(long.class, "var64");
		registerMapping(float.class, "f32");
		registerMapping(double.class, "f64");
	}

	private static final Map<Class, String> mapping;
	private static final Map<String, BiConsumer<PacketByteBuf, Object>> serializers;
	private static final Map<String, Function<PacketByteBuf, Object>> deserializers;
	private static final Map<Class, BiConsumer<PacketByteBuf, Object>> serializerCache;
	private static final Map<Class, Function<PacketByteBuf, Object>> deserializerCache;

	private static BiConsumer<PacketByteBuf, Object> createComplexSerializer(Class c) {
		try {
			List<Pair<MethodHandle, BiConsumer<PacketByteBuf, Object>>> serializationQueue = new ArrayList<>();

			if (c.getFields().length == 0) {
				return (b, o) -> {};
			}

			for (Field f : c.getFields()) {
				SendNetwork sn = f.getAnnotation(SendNetwork.class);
				if (sn != null) {
					//noinspection unchecked
					Pair<MethodHandle, BiConsumer<PacketByteBuf, Object>> pair = Pair.of(
							MethodHandles.publicLookup().unreflectGetter(f),
							!sn.type().isEmpty() ? serializers.get(sn.type()) : (BiConsumer<PacketByteBuf, Object>) getSerializer(f.getType())
					);

					if (pair.getLeft() == null) {
						throw new RuntimeException("Could not find getter for field " + f.getName() + " in " + c.getName() + "!");
					}

					if (pair.getRight() == null) {
						throw new RuntimeException("Could not find serializer for field " + f.getName() + " in " + c.getName() + "! (override: " + sn.type() + ")");
					}

					serializationQueue.add(pair);
				}
			}

			if (serializationQueue.isEmpty()) {
				throw new RuntimeException("Could not find any synchronizable fields for complex class " + c.getName() + "!");
			}

			return (b, o) -> serializationQueue.forEach((a) -> {
				try {
					a.getRight().accept(b, a.getLeft().invoke(o));
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Function<PacketByteBuf, Object> createComplexDeserializer(Class c) {
		try {
			if (c.getConstructor() == null) {
				throw new RuntimeException("Class " + c.getName() + " has no no-args constructor!");
			}

			List<Pair<MethodHandle, Function<PacketByteBuf, Object>>> serializationQueue = new ArrayList<>();

			int fieldCount = c.getFields().length;
			for (Field f : c.getFields()) {
				SendNetwork sn = f.getAnnotation(SendNetwork.class);
				if (sn != null) {
					//noinspection unchecked
					Pair<MethodHandle, Function<PacketByteBuf, Object>> pair = Pair.of(
							MethodHandles.publicLookup().unreflectSetter(f),
							!sn.type().isEmpty() ? deserializers.get(sn.type()) : (Function<PacketByteBuf, Object>) getDeserializer(f.getType())
					);

					if (pair.getLeft() == null) {
						throw new RuntimeException("Could not find getter for field " + f.getName() + " in " + c.getName() + "!");
					}

					if (pair.getRight() == null) {
						throw new RuntimeException("Could not find serializer for field " + f.getName() + " in " + c.getName() + "! (override: " + sn.type() + ")");
					}

					serializationQueue.add(pair);
				}
			}

			if (serializationQueue.isEmpty() && fieldCount > 0) {
				throw new RuntimeException("Could not find any synchronizable fields for complex class " + c.getName() + "!");
			}

			return (b) -> {
				try {
					Object o = c.newInstance();
					for (Pair<MethodHandle, Function<PacketByteBuf, Object>> a : serializationQueue) {
						a.getLeft().invoke(o, a.getRight().apply(b));
					}
					return o;
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			};
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> BiConsumer<PacketByteBuf, T> getSerializer(Class<T> c) {
		//noinspection unchecked
		return (BiConsumer<PacketByteBuf, T>) serializerCache.computeIfAbsent(c, (cl) -> {
			if (cl.isEnum()) {
				return (b, o) -> b.writeVarInt(((Enum) o).ordinal());
			}

			LinkedList<Class> cll = new LinkedList<>();
			cll.add(cl);
			while (!cll.isEmpty()) {
			    Class closest = cll.remove();
			    if (closest == null || closest == Object.class) {
			        continue;
                }

			    if (mapping.containsKey(closest)) {
			        return serializers.get(mapping.get(closest));
                }

			    if (closest.getInterfaces() != null) {
                    cll.addAll(Arrays.asList(closest.getInterfaces()));
                }

			    cll.add(closest.getSuperclass());
            }

            return createComplexSerializer(cl);
		});
	}

	public static <T> Function<PacketByteBuf, T> getDeserializer(Class<T> c) {
		//noinspection unchecked
		return (Function<PacketByteBuf, T>) deserializerCache.computeIfAbsent(c, (cl) -> {
			if (cl.isEnum()) {
				return (b) -> cl.getEnumConstants()[b.readVarInt()];
			}

            LinkedList<Class> cll = new LinkedList<>();
            cll.add(cl);
            while (!cll.isEmpty()) {
                Class closest = cll.remove();
                if (closest == null || closest == Object.class) {
                    continue;
                }

                if (mapping.containsKey(closest)) {
                    return deserializers.get(mapping.get(closest));
                }

                if (closest.getInterfaces() != null) {
                    cll.addAll(Arrays.asList(closest.getInterfaces()));
                }

                cll.add(closest.getSuperclass());
            }

            return createComplexDeserializer(cl);
		});
	}

	@SuppressWarnings("unchecked")
	public static <T> void register(String typeName, BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer) {
		serializers.put(typeName, (BiConsumer<PacketByteBuf, Object>) serializer);
		deserializers.put(typeName, (Function<PacketByteBuf, Object>) deserializer);
	}

	public static <T> void register(String typeName, Class<T> typeClass, BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer) {
		registerMapping(typeClass, typeName);
		register(typeName, serializer, deserializer);
	}

	public static <T> void register(Class<T> typeClass, BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer) {
		register(typeClass.getSimpleName(), typeClass, serializer, deserializer);
	}

	public static <T> void registerMapping(Class<T> typeClass, String typeName) {
		mapping.put(typeClass, typeName);
	}
}
