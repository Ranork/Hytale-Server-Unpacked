package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IntersectionHighlight {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public float highlightThreshold;
   @Nullable
   public Color highlightColor;

   public IntersectionHighlight() {
   }

   public IntersectionHighlight(float highlightThreshold, @Nullable Color highlightColor) {
      this.highlightThreshold = highlightThreshold;
      this.highlightColor = highlightColor;
   }

   public IntersectionHighlight(@Nonnull IntersectionHighlight other) {
      this.highlightThreshold = other.highlightThreshold;
      this.highlightColor = other.highlightColor;
   }

   @Nonnull
   public static IntersectionHighlight deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("IntersectionHighlight", 8, buf.readableBytes() - offset);
      } else {
         IntersectionHighlight obj = new IntersectionHighlight();
         byte nullBits = buf.getByte(offset);
         obj.highlightThreshold = buf.getFloatLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.highlightColor = Color.deserialize(buf, offset + 5);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static float getHighlightThreshold(MemorySegment mem) {
      return getHighlightThreshold(mem, 0);
   }

   public static float getHighlightThreshold(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   @Nullable
   public static Color getHighlightColor(MemorySegment mem) {
      return getHighlightColor(mem, 0);
   }

   @Nullable
   public static Color getHighlightColor(MemorySegment mem, int offset) {
      return hasHighlightColor(mem, offset) ? Color.toObject(mem, offset + 5) : null;
   }

   public static boolean hasHighlightColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static IntersectionHighlight toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static IntersectionHighlight toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("IntersectionHighlight", offset + 8, (int)mem.byteSize());
      } else {
         return new IntersectionHighlight(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)), hasHighlightColor(mem, offset) ? Color.toObject(mem, offset + 5) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.highlightColor != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.highlightThreshold);
      if (this.highlightColor != null) {
         this.highlightColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.highlightColor != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.highlightThreshold);
      if (this.highlightColor != null) {
         this.highlightColor.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 3L).fill((byte)0);
      }

      return 8;
   }

   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 8) {
         return ValidationResult.error("Buffer too small: expected at least 8 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public IntersectionHighlight clone() {
      IntersectionHighlight copy = new IntersectionHighlight();
      copy.highlightThreshold = this.highlightThreshold;
      copy.highlightColor = this.highlightColor != null ? this.highlightColor.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof IntersectionHighlight other)
            ? false
            : this.highlightThreshold == other.highlightThreshold && Objects.equals(this.highlightColor, other.highlightColor);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.highlightThreshold, this.highlightColor);
   }
}
