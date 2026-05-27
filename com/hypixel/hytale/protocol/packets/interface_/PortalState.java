package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class PortalState {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public int remainingSeconds;
   public boolean breaching;

   public PortalState() {
   }

   public PortalState(int remainingSeconds, boolean breaching) {
      this.remainingSeconds = remainingSeconds;
      this.breaching = breaching;
   }

   public PortalState(@Nonnull PortalState other) {
      this.remainingSeconds = other.remainingSeconds;
      this.breaching = other.breaching;
   }

   @Nonnull
   public static PortalState deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("PortalState", 5, buf.readableBytes() - offset);
      } else {
         PortalState obj = new PortalState();
         obj.remainingSeconds = buf.getIntLE(offset + 0);
         obj.breaching = buf.getByte(offset + 4) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getRemainingSeconds(MemorySegment mem) {
      return getRemainingSeconds(mem, 0);
   }

   public static int getRemainingSeconds(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static boolean getBreaching(MemorySegment mem) {
      return getBreaching(mem, 0);
   }

   public static boolean getBreaching(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4));
   }

   public static PortalState toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PortalState toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PortalState", offset + 5, (int)mem.byteSize());
      } else {
         return new PortalState(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.remainingSeconds);
      buf.writeByte(this.breaching ? 1 : 0);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.remainingSeconds);
      mem.set(PacketIO.PROTO_BOOL, offset + 4, this.breaching);
      return 5;
   }

   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 5 ? ValidationResult.error("Buffer too small: expected at least 5 bytes") : ValidationResult.OK;
   }

   public PortalState clone() {
      PortalState copy = new PortalState();
      copy.remainingSeconds = this.remainingSeconds;
      copy.breaching = this.breaching;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PortalState other) ? false : this.remainingSeconds == other.remainingSeconds && this.breaching == other.breaching;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.remainingSeconds, this.breaching);
   }
}
