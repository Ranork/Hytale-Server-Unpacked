package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class TintComponent extends MapMarkerComponent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 3;
   @Nonnull
   public Color color = new Color();

   public TintComponent() {
   }

   public TintComponent(@Nonnull Color color) {
      this.color = color;
   }

   public TintComponent(@Nonnull TintComponent other) {
      this.color = other.color;
   }

   @Nonnull
   public static TintComponent deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 3) {
         throw ProtocolException.bufferTooSmall("TintComponent", 3, buf.readableBytes() - offset);
      } else {
         TintComponent obj = new TintComponent();
         obj.color = Color.deserialize(buf, offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 3;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 3L;
   }

   public static Color getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   public static Color getColor(MemorySegment mem, int offset) {
      return Color.toObject(mem, offset + 0);
   }

   public static TintComponent toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TintComponent toObject(MemorySegment mem, int offset) {
      if (offset + 3 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TintComponent", offset + 3, (int)mem.byteSize());
      } else {
         return new TintComponent(Color.toObject(mem, offset + 0));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      this.color.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      this.color.serialize(mem, offset + 0);
      return 3;
   }

   @Override
   public int computeSize() {
      return 3;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 3 ? ValidationResult.error("Buffer too small: expected at least 3 bytes") : ValidationResult.OK;
   }

   public TintComponent clone() {
      TintComponent copy = new TintComponent();
      copy.color = this.color.clone();
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof TintComponent other ? Objects.equals(this.color, other.color) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.color);
   }
}
