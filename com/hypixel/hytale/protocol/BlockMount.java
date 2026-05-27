package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.joml.Vector3fc;

public class BlockMount {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 29;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 29;
   public static final int MAX_SIZE = 29;
   @Nonnull
   public BlockMountType type = BlockMountType.Seat;
   @Nonnull
   public Vector3fc position = PacketIO.ZERO_VECTOR3;
   @Nonnull
   public Vector3fc orientation = PacketIO.ZERO_VECTOR3;
   public int blockTypeId;

   public BlockMount() {
   }

   public BlockMount(@Nonnull BlockMountType type, @Nonnull Vector3fc position, @Nonnull Vector3fc orientation, int blockTypeId) {
      this.type = type;
      this.position = position;
      this.orientation = orientation;
      this.blockTypeId = blockTypeId;
   }

   public BlockMount(@Nonnull BlockMount other) {
      this.type = other.type;
      this.position = other.position;
      this.orientation = other.orientation;
      this.blockTypeId = other.blockTypeId;
   }

   @Nonnull
   public static BlockMount deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 29) {
         throw ProtocolException.bufferTooSmall("BlockMount", 29, buf.readableBytes() - offset);
      } else {
         BlockMount obj = new BlockMount();
         obj.type = BlockMountType.fromValue(buf.getByte(offset + 0));
         obj.position = PacketIO.readVector3f(buf, offset + 1);
         obj.orientation = PacketIO.readVector3f(buf, offset + 13);
         obj.blockTypeId = buf.getIntLE(offset + 25);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 29;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 29L;
   }

   public static BlockMountType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static BlockMountType getType(MemorySegment mem, int offset) {
      return BlockMountType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static Vector3fc getPosition(MemorySegment mem) {
      return getPosition(mem, 0);
   }

   public static Vector3fc getPosition(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 1);
   }

   public static Vector3fc getOrientation(MemorySegment mem) {
      return getOrientation(mem, 0);
   }

   public static Vector3fc getOrientation(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 13);
   }

   public static int getBlockTypeId(MemorySegment mem) {
      return getBlockTypeId(mem, 0);
   }

   public static int getBlockTypeId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 25));
   }

   public static BlockMount toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockMount toObject(MemorySegment mem, int offset) {
      if (offset + 29 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockMount", offset + 29, (int)mem.byteSize());
      } else {
         return new BlockMount(
            BlockMountType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))),
            PacketIO.readVector3f(mem, offset + 1),
            PacketIO.readVector3f(mem, offset + 13),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 25))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.type.getValue());
      PacketIO.writeVector3f(buf, this.position);
      PacketIO.writeVector3f(buf, this.orientation);
      buf.writeIntLE(this.blockTypeId);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.type.getValue());
      PacketIO.writeVector3f(mem, offset + 1, this.position);
      PacketIO.writeVector3f(mem, offset + 13, this.orientation);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 25), this.blockTypeId);
      return 29;
   }

   public int computeSize() {
      return 29;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 29) {
         return ValidationResult.error("Buffer too small: expected at least 29 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 2 ? ValidationResult.error("Invalid BlockMountType value for Type") : ValidationResult.OK;
      }
   }

   public BlockMount clone() {
      BlockMount copy = new BlockMount();
      copy.type = this.type;
      copy.position = this.position;
      copy.orientation = this.orientation;
      copy.blockTypeId = this.blockTypeId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockMount other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.position, other.position)
               && Objects.equals(this.orientation, other.orientation)
               && this.blockTypeId == other.blockTypeId;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.position, this.orientation, this.blockTypeId);
   }
}
