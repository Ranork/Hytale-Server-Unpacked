package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class EntityMatcher {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 2;
   @Nonnull
   public EntityMatcherType type = EntityMatcherType.Server;
   public boolean invert;

   public EntityMatcher() {
   }

   public EntityMatcher(@Nonnull EntityMatcherType type, boolean invert) {
      this.type = type;
      this.invert = invert;
   }

   public EntityMatcher(@Nonnull EntityMatcher other) {
      this.type = other.type;
      this.invert = other.invert;
   }

   @Nonnull
   public static EntityMatcher deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("EntityMatcher", 2, buf.readableBytes() - offset);
      } else {
         EntityMatcher obj = new EntityMatcher();
         obj.type = EntityMatcherType.fromValue(buf.getByte(offset + 0));
         obj.invert = buf.getByte(offset + 1) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static EntityMatcherType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static EntityMatcherType getType(MemorySegment mem, int offset) {
      return EntityMatcherType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static boolean getInvert(MemorySegment mem) {
      return getInvert(mem, 0);
   }

   public static boolean getInvert(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static EntityMatcher toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EntityMatcher toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EntityMatcher", offset + 2, (int)mem.byteSize());
      } else {
         return new EntityMatcher(
            EntityMatcherType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))), mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.type.getValue());
      buf.writeByte(this.invert ? 1 : 0);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.invert);
      return 2;
   }

   public int computeSize() {
      return 2;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 3 ? ValidationResult.error("Invalid EntityMatcherType value for Type") : ValidationResult.OK;
      }
   }

   public EntityMatcher clone() {
      EntityMatcher copy = new EntityMatcher();
      copy.type = this.type;
      copy.invert = this.invert;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityMatcher other) ? false : Objects.equals(this.type, other.type) && this.invert == other.invert;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.invert);
   }
}
