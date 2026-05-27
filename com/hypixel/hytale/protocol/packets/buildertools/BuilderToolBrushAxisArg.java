package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolBrushAxisArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   @Nonnull
   public BrushAxis defaultValue = BrushAxis.None;

   public BuilderToolBrushAxisArg() {
   }

   public BuilderToolBrushAxisArg(@Nonnull BrushAxis defaultValue) {
      this.defaultValue = defaultValue;
   }

   public BuilderToolBrushAxisArg(@Nonnull BuilderToolBrushAxisArg other) {
      this.defaultValue = other.defaultValue;
   }

   @Nonnull
   public static BuilderToolBrushAxisArg deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("BuilderToolBrushAxisArg", 1, buf.readableBytes() - offset);
      } else {
         BuilderToolBrushAxisArg obj = new BuilderToolBrushAxisArg();
         obj.defaultValue = BrushAxis.fromValue(buf.getByte(offset + 0));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static BrushAxis getDefault(MemorySegment mem) {
      return getDefault(mem, 0);
   }

   public static BrushAxis getDefault(MemorySegment mem, int offset) {
      return BrushAxis.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static BuilderToolBrushAxisArg toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolBrushAxisArg toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolBrushAxisArg", offset + 1, (int)mem.byteSize());
      } else {
         return new BuilderToolBrushAxisArg(BrushAxis.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.defaultValue.getValue());
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.defaultValue.getValue());
      return 1;
   }

   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 5 ? ValidationResult.error("Invalid BrushAxis value for Default") : ValidationResult.OK;
      }
   }

   public BuilderToolBrushAxisArg clone() {
      BuilderToolBrushAxisArg copy = new BuilderToolBrushAxisArg();
      copy.defaultValue = this.defaultValue;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BuilderToolBrushAxisArg other ? Objects.equals(this.defaultValue, other.defaultValue) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.defaultValue);
   }
}
