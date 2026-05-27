package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class DetailBox {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 37;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 37;
   public static final int MAX_SIZE = 37;
   @Nonnull
   public Vector3fc offset = PacketIO.ZERO_VECTOR3;
   @Nullable
   public Hitbox box;

   public DetailBox() {
   }

   public DetailBox(@Nonnull Vector3fc offset, @Nullable Hitbox box) {
      this.offset = offset;
      this.box = box;
   }

   public DetailBox(@Nonnull DetailBox other) {
      this.offset = other.offset;
      this.box = other.box;
   }

   @Nonnull
   public static DetailBox deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 37) {
         throw ProtocolException.bufferTooSmall("DetailBox", 37, buf.readableBytes() - offset);
      } else {
         DetailBox obj = new DetailBox();
         byte nullBits = buf.getByte(offset);
         obj.offset = PacketIO.readVector3f(buf, offset + 1);
         if ((nullBits & 1) != 0) {
            obj.box = Hitbox.deserialize(buf, offset + 13);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 37;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 37L;
   }

   public static Vector3fc getOffset(MemorySegment mem) {
      return getOffset(mem, 0);
   }

   public static Vector3fc getOffset(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 1);
   }

   @Nullable
   public static Hitbox getBox(MemorySegment mem) {
      return getBox(mem, 0);
   }

   @Nullable
   public static Hitbox getBox(MemorySegment mem, int offset) {
      return hasBox(mem, offset) ? Hitbox.toObject(mem, offset + 13) : null;
   }

   public static boolean hasBox(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static DetailBox toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static DetailBox toObject(MemorySegment mem, int offset) {
      if (offset + 37 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("DetailBox", offset + 37, (int)mem.byteSize());
      } else {
         return new DetailBox(PacketIO.readVector3f(mem, offset + 1), hasBox(mem, offset) ? Hitbox.toObject(mem, offset + 13) : null);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.box != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      PacketIO.writeVector3f(buf, this.offset);
      if (this.box != null) {
         this.box.serialize(buf);
      } else {
         buf.writeZero(24);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.box != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      PacketIO.writeVector3f(mem, offset + 1, this.offset);
      if (this.box != null) {
         this.box.serialize(mem, offset + 13);
      } else {
         mem.asSlice(offset + 13, 24L).fill((byte)0);
      }

      return 37;
   }

   public int computeSize() {
      return 37;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 37) {
         return ValidationResult.error("Buffer too small: expected at least 37 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public DetailBox clone() {
      DetailBox copy = new DetailBox();
      copy.offset = this.offset;
      copy.box = this.box != null ? this.box.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof DetailBox other) ? false : Objects.equals(this.offset, other.offset) && Objects.equals(this.box, other.box);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.offset, this.box);
   }
}
