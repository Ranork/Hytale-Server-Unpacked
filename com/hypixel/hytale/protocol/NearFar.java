package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class NearFar {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public float near;
   public float far;

   public NearFar() {
   }

   public NearFar(float near, float far) {
      this.near = near;
      this.far = far;
   }

   public NearFar(@Nonnull NearFar other) {
      this.near = other.near;
      this.far = other.far;
   }

   @Nonnull
   public static NearFar deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("NearFar", 8, buf.readableBytes() - offset);
      } else {
         NearFar obj = new NearFar();
         obj.near = buf.getFloatLE(offset + 0);
         obj.far = buf.getFloatLE(offset + 4);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static float getNear(MemorySegment mem) {
      return getNear(mem, 0);
   }

   public static float getNear(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getFar(MemorySegment mem) {
      return getFar(mem, 0);
   }

   public static float getFar(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static NearFar toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static NearFar toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("NearFar", offset + 8, (int)mem.byteSize());
      } else {
         return new NearFar(mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)), mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.near);
      buf.writeFloatLE(this.far);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.near);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.far);
      return 8;
   }

   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public NearFar clone() {
      NearFar copy = new NearFar();
      copy.near = this.near;
      copy.far = this.far;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof NearFar other) ? false : this.near == other.near && this.far == other.far;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.near, this.far);
   }
}
