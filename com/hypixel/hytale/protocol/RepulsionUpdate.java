package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class RepulsionUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int repulsionConfigIndex;

   public RepulsionUpdate() {
   }

   public RepulsionUpdate(int repulsionConfigIndex) {
      this.repulsionConfigIndex = repulsionConfigIndex;
   }

   public RepulsionUpdate(@Nonnull RepulsionUpdate other) {
      this.repulsionConfigIndex = other.repulsionConfigIndex;
   }

   @Nonnull
   public static RepulsionUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("RepulsionUpdate", 4, buf.readableBytes() - offset);
      } else {
         RepulsionUpdate obj = new RepulsionUpdate();
         obj.repulsionConfigIndex = buf.getIntLE(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static int getRepulsionConfigIndex(MemorySegment mem) {
      return getRepulsionConfigIndex(mem, 0);
   }

   public static int getRepulsionConfigIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static RepulsionUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RepulsionUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RepulsionUpdate", offset + 4, (int)mem.byteSize());
      } else {
         return new RepulsionUpdate(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeIntLE(this.repulsionConfigIndex);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.repulsionConfigIndex);
      return 4;
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public RepulsionUpdate clone() {
      RepulsionUpdate copy = new RepulsionUpdate();
      copy.repulsionConfigIndex = this.repulsionConfigIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof RepulsionUpdate other ? this.repulsionConfigIndex == other.repulsionConfigIndex : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.repulsionConfigIndex);
   }
}
