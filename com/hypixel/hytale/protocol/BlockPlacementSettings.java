package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BlockPlacementSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   public boolean allowRotationKey;
   public boolean placeInEmptyBlocks;
   @Nonnull
   public BlockPreviewVisibility previewVisibility = BlockPreviewVisibility.AlwaysVisible;
   @Nonnull
   public BlockPlacementRotationMode rotationMode = BlockPlacementRotationMode.FacingPlayer;
   public int wallPlacementOverrideBlockId;
   public int floorPlacementOverrideBlockId;
   public int ceilingPlacementOverrideBlockId;
   public boolean allowBreakReplace;

   public BlockPlacementSettings() {
   }

   public BlockPlacementSettings(
      boolean allowRotationKey,
      boolean placeInEmptyBlocks,
      @Nonnull BlockPreviewVisibility previewVisibility,
      @Nonnull BlockPlacementRotationMode rotationMode,
      int wallPlacementOverrideBlockId,
      int floorPlacementOverrideBlockId,
      int ceilingPlacementOverrideBlockId,
      boolean allowBreakReplace
   ) {
      this.allowRotationKey = allowRotationKey;
      this.placeInEmptyBlocks = placeInEmptyBlocks;
      this.previewVisibility = previewVisibility;
      this.rotationMode = rotationMode;
      this.wallPlacementOverrideBlockId = wallPlacementOverrideBlockId;
      this.floorPlacementOverrideBlockId = floorPlacementOverrideBlockId;
      this.ceilingPlacementOverrideBlockId = ceilingPlacementOverrideBlockId;
      this.allowBreakReplace = allowBreakReplace;
   }

   public BlockPlacementSettings(@Nonnull BlockPlacementSettings other) {
      this.allowRotationKey = other.allowRotationKey;
      this.placeInEmptyBlocks = other.placeInEmptyBlocks;
      this.previewVisibility = other.previewVisibility;
      this.rotationMode = other.rotationMode;
      this.wallPlacementOverrideBlockId = other.wallPlacementOverrideBlockId;
      this.floorPlacementOverrideBlockId = other.floorPlacementOverrideBlockId;
      this.ceilingPlacementOverrideBlockId = other.ceilingPlacementOverrideBlockId;
      this.allowBreakReplace = other.allowBreakReplace;
   }

   @Nonnull
   public static BlockPlacementSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("BlockPlacementSettings", 17, buf.readableBytes() - offset);
      } else {
         BlockPlacementSettings obj = new BlockPlacementSettings();
         obj.allowRotationKey = buf.getByte(offset + 0) != 0;
         obj.placeInEmptyBlocks = buf.getByte(offset + 1) != 0;
         obj.previewVisibility = BlockPreviewVisibility.fromValue(buf.getByte(offset + 2));
         obj.rotationMode = BlockPlacementRotationMode.fromValue(buf.getByte(offset + 3));
         obj.wallPlacementOverrideBlockId = buf.getIntLE(offset + 4);
         obj.floorPlacementOverrideBlockId = buf.getIntLE(offset + 8);
         obj.ceilingPlacementOverrideBlockId = buf.getIntLE(offset + 12);
         obj.allowBreakReplace = buf.getByte(offset + 16) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   public static boolean getAllowRotationKey(MemorySegment mem) {
      return getAllowRotationKey(mem, 0);
   }

   public static boolean getAllowRotationKey(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static boolean getPlaceInEmptyBlocks(MemorySegment mem) {
      return getPlaceInEmptyBlocks(mem, 0);
   }

   public static boolean getPlaceInEmptyBlocks(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static BlockPreviewVisibility getPreviewVisibility(MemorySegment mem) {
      return getPreviewVisibility(mem, 0);
   }

   public static BlockPreviewVisibility getPreviewVisibility(MemorySegment mem, int offset) {
      return BlockPreviewVisibility.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)));
   }

   public static BlockPlacementRotationMode getRotationMode(MemorySegment mem) {
      return getRotationMode(mem, 0);
   }

   public static BlockPlacementRotationMode getRotationMode(MemorySegment mem, int offset) {
      return BlockPlacementRotationMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3)));
   }

   public static int getWallPlacementOverrideBlockId(MemorySegment mem) {
      return getWallPlacementOverrideBlockId(mem, 0);
   }

   public static int getWallPlacementOverrideBlockId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getFloorPlacementOverrideBlockId(MemorySegment mem) {
      return getFloorPlacementOverrideBlockId(mem, 0);
   }

   public static int getFloorPlacementOverrideBlockId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static int getCeilingPlacementOverrideBlockId(MemorySegment mem) {
      return getCeilingPlacementOverrideBlockId(mem, 0);
   }

   public static int getCeilingPlacementOverrideBlockId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 12));
   }

   public static boolean getAllowBreakReplace(MemorySegment mem) {
      return getAllowBreakReplace(mem, 0);
   }

   public static boolean getAllowBreakReplace(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 16));
   }

   public static BlockPlacementSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockPlacementSettings toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockPlacementSettings", offset + 17, (int)mem.byteSize());
      } else {
         return new BlockPlacementSettings(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            BlockPreviewVisibility.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2))),
            BlockPlacementRotationMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 16))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.allowRotationKey ? 1 : 0);
      buf.writeByte(this.placeInEmptyBlocks ? 1 : 0);
      buf.writeByte(this.previewVisibility.getValue());
      buf.writeByte(this.rotationMode.getValue());
      buf.writeIntLE(this.wallPlacementOverrideBlockId);
      buf.writeIntLE(this.floorPlacementOverrideBlockId);
      buf.writeIntLE(this.ceilingPlacementOverrideBlockId);
      buf.writeByte(this.allowBreakReplace ? 1 : 0);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.allowRotationKey);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.placeInEmptyBlocks);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.previewVisibility.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 3), (byte)this.rotationMode.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.wallPlacementOverrideBlockId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.floorPlacementOverrideBlockId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.ceilingPlacementOverrideBlockId);
      mem.set(PacketIO.PROTO_BOOL, offset + 16, this.allowBreakReplace);
      return 17;
   }

   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         int v = buffer.getByte(offset + 2) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid BlockPreviewVisibility value for PreviewVisibility");
         } else {
            v = buffer.getByte(offset + 3) & 255;
            return v >= 4 ? ValidationResult.error("Invalid BlockPlacementRotationMode value for RotationMode") : ValidationResult.OK;
         }
      }
   }

   public BlockPlacementSettings clone() {
      BlockPlacementSettings copy = new BlockPlacementSettings();
      copy.allowRotationKey = this.allowRotationKey;
      copy.placeInEmptyBlocks = this.placeInEmptyBlocks;
      copy.previewVisibility = this.previewVisibility;
      copy.rotationMode = this.rotationMode;
      copy.wallPlacementOverrideBlockId = this.wallPlacementOverrideBlockId;
      copy.floorPlacementOverrideBlockId = this.floorPlacementOverrideBlockId;
      copy.ceilingPlacementOverrideBlockId = this.ceilingPlacementOverrideBlockId;
      copy.allowBreakReplace = this.allowBreakReplace;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockPlacementSettings other)
            ? false
            : this.allowRotationKey == other.allowRotationKey
               && this.placeInEmptyBlocks == other.placeInEmptyBlocks
               && Objects.equals(this.previewVisibility, other.previewVisibility)
               && Objects.equals(this.rotationMode, other.rotationMode)
               && this.wallPlacementOverrideBlockId == other.wallPlacementOverrideBlockId
               && this.floorPlacementOverrideBlockId == other.floorPlacementOverrideBlockId
               && this.ceilingPlacementOverrideBlockId == other.ceilingPlacementOverrideBlockId
               && this.allowBreakReplace == other.allowBreakReplace;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.allowRotationKey,
         this.placeInEmptyBlocks,
         this.previewVisibility,
         this.rotationMode,
         this.wallPlacementOverrideBlockId,
         this.floorPlacementOverrideBlockId,
         this.ceilingPlacementOverrideBlockId,
         this.allowBreakReplace
      );
   }
}
