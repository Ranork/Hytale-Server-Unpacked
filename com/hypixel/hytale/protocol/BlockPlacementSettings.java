package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
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

   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 17 ? ValidationResult.error("Buffer too small: expected at least 17 bytes") : ValidationResult.OK;
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
