package li.cil.tis3d.gametest;

import li.cil.tis3d.api.serial.SerialInterface;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import li.cil.tis3d.gametest.Invocations.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class TestSerialInterfaceProvider implements SerialInterfaceProvider {
    private static final String TAG_VALUE = "value";

    private static final Map<BlockPos, Exchange> EXCHANGES = new ConcurrentHashMap<>();

    private static final class Exchange {
        private final Invocations invocations = new Invocations();
        private short offeredValue;
    }

    private static Exchange exchange(final BlockPos position) {
        return EXCHANGES.computeIfAbsent(position.immutable(), ignored -> new Exchange());
    }

    // --------------------------------------------------------------------- //

    public static void initialize(final BlockPos position, final int value) {
        final Exchange exchange = exchange(position);
        exchange.invocations.clear();
        exchange.offeredValue = (short) value;
    }

    public static Invocations invocations(final BlockPos position) {
        return exchange(position).invocations;
    }

    // --------------------------------------------------------------------- //

    @Override
    public boolean matches(final Level level, final BlockPos position, final Direction side) {
        return level.getBlockState(position).is(Blocks.NOTE_BLOCK);
    }

    @Override
    public Optional<SerialInterface> getInterface(final Level level, final BlockPos position, final Direction face) {
        return Optional.of(new TestSerialInterface(exchange(position)));
    }

    @Override
    public Optional<SerialProtocolDocumentationReference> getDocumentationReference() {
        return Optional.empty();
    }

    @Override
    public boolean stillValid(final Level level, final BlockPos position, final Direction side, final SerialInterface serialInterface) {
        return serialInterface instanceof TestSerialInterface;
    }

    // --------------------------------------------------------------------- //

    private static final class TestSerialInterface implements SerialInterface {
        private final Exchange exchange;

        TestSerialInterface(final Exchange exchange) {
            this.exchange = exchange;
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public void write(final short value) {
            exchange.invocations.record(0, Kind.WRITE, null, value);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public short peek() {
            return exchange.offeredValue;
        }

        @Override
        public void skip() {
            exchange.invocations.record(0, Kind.SKIP);
        }

        @Override
        public void reset() {
            exchange.invocations.record(0, Kind.RESET);
        }

        @Override
        public void save(final CompoundTag tag) {
            tag.putShort(TAG_VALUE, exchange.offeredValue);
        }

        @Override
        public void load(final CompoundTag tag) {
            exchange.offeredValue = tag.getShort(TAG_VALUE);
        }
    }
}
