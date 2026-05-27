package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.BlockPlacementRotationMode;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class BlockPlacementSettings implements NetworkSerializable<com.hypixel.hytale.protocol.BlockPlacementSettings> {
   public static final BuilderCodec<BlockPlacementSettings> CODEC = BuilderCodec.builder(BlockPlacementSettings.class, BlockPlacementSettings::new)
      .append(
         new KeyedCodec<>("AllowRotationKey", Codec.BOOLEAN),
         (placementSettings, o) -> placementSettings.allowRotationKey = o,
         placementSettings -> placementSettings.allowRotationKey
      )
      .add()
      .<Boolean>append(
         new KeyedCodec<>("PlaceInEmptyBlocks", Codec.BOOLEAN),
         (placementSettings, o) -> placementSettings.placeInEmptyBlocks = o,
         placementSettings -> placementSettings.placeInEmptyBlocks
      )
      .documentation("If this block is allowed to be placed inside other blocks with an Empty Material (destroying them).")
      .add()
      .<BlockPlacementSettings.RotationMode>append(
         new KeyedCodec<>("RotationMode", BlockPlacementSettings.RotationMode.CODEC),
         (placementSettings, o) -> placementSettings.rotationMode = o,
         placementSettings -> placementSettings.rotationMode
      )
      .documentation("The mode determining the rotation of this block when placed.")
      .add()
      .<BlockPlacementSettings.BlockPreviewVisibility>append(
         new KeyedCodec<>("BlockPreviewVisibility", BlockPlacementSettings.BlockPreviewVisibility.CODEC),
         (placementSettings, o) -> placementSettings.previewVisibility = o,
         placementSettings -> placementSettings.previewVisibility
      )
      .documentation("An override for the block preview visibility")
      .add()
      .append(
         new KeyedCodec<>("WallPlacementOverrideBlockId", Codec.STRING),
         (placementSettings, o) -> placementSettings.wallPlacementOverrideBlockId = o,
         placementSettings -> placementSettings.wallPlacementOverrideBlockId
      )
      .add()
      .append(
         new KeyedCodec<>("FloorPlacementOverrideBlockId", Codec.STRING),
         (placementSettings, o) -> placementSettings.floorPlacementOverrideBlockId = o,
         placementSettings -> placementSettings.floorPlacementOverrideBlockId
      )
      .add()
      .append(
         new KeyedCodec<>("CeilingPlacementOverrideBlockId", Codec.STRING),
         (placementSettings, o) -> placementSettings.ceilingPlacementOverrideBlockId = o,
         placementSettings -> placementSettings.ceilingPlacementOverrideBlockId
      )
      .add()
      .append(new KeyedCodec<>("AllowBreakReplace", Codec.BOOLEAN), (o, v) -> o.allowBreakReplace = v, o -> o.allowBreakReplace)
      .add()
      .build();
   protected String wallPlacementOverrideBlockId;
   protected String floorPlacementOverrideBlockId;
   protected String ceilingPlacementOverrideBlockId;
   private boolean allowRotationKey = true;
   private boolean placeInEmptyBlocks;
   private BlockPlacementSettings.BlockPreviewVisibility previewVisibility = BlockPlacementSettings.BlockPreviewVisibility.DEFAULT;
   private BlockPlacementSettings.RotationMode rotationMode = BlockPlacementSettings.RotationMode.DEFAULT;
   protected boolean allowBreakReplace;

   protected BlockPlacementSettings() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BlockPlacementSettings toPacket() {
      com.hypixel.hytale.protocol.BlockPlacementSettings packet = new com.hypixel.hytale.protocol.BlockPlacementSettings();
      packet.allowRotationKey = this.allowRotationKey;
      packet.placeInEmptyBlocks = this.placeInEmptyBlocks;
      packet.allowBreakReplace = this.allowBreakReplace;

      packet.previewVisibility = switch (this.previewVisibility) {
         case null -> com.hypixel.hytale.protocol.BlockPreviewVisibility.Default;
         case DEFAULT -> com.hypixel.hytale.protocol.BlockPreviewVisibility.Default;
         case ALWAYS_HIDDEN -> com.hypixel.hytale.protocol.BlockPreviewVisibility.AlwaysHidden;
         case ALWAYS_VISIBLE -> com.hypixel.hytale.protocol.BlockPreviewVisibility.AlwaysVisible;
      };

      packet.rotationMode = switch (this.rotationMode) {
         case null -> BlockPlacementRotationMode.Default;
         case DEFAULT -> BlockPlacementRotationMode.Default;
         case FACING_PLAYER -> BlockPlacementRotationMode.FacingPlayer;
         case STAIR_FACING_PLAYER -> BlockPlacementRotationMode.StairFacingPlayer;
         case BLOCK_NORMAL -> BlockPlacementRotationMode.BlockNormal;
      };
      packet.wallPlacementOverrideBlockId = this.wallPlacementOverrideBlockId == null
         ? -1
         : BlockType.getAssetMap().getIndex(this.wallPlacementOverrideBlockId);
      if (packet.wallPlacementOverrideBlockId == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Unknown key! " + this.wallPlacementOverrideBlockId);
      } else {
         packet.floorPlacementOverrideBlockId = this.floorPlacementOverrideBlockId == null
            ? -1
            : BlockType.getAssetMap().getIndex(this.floorPlacementOverrideBlockId);
         if (packet.floorPlacementOverrideBlockId == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + this.floorPlacementOverrideBlockId);
         } else {
            packet.ceilingPlacementOverrideBlockId = this.ceilingPlacementOverrideBlockId == null
               ? -1
               : BlockType.getAssetMap().getIndex(this.ceilingPlacementOverrideBlockId);
            if (packet.ceilingPlacementOverrideBlockId == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + this.ceilingPlacementOverrideBlockId);
            } else {
               return packet;
            }
         }
      }
   }

   public String getWallPlacementOverrideBlockId() {
      return this.wallPlacementOverrideBlockId;
   }

   public String getFloorPlacementOverrideBlockId() {
      return this.floorPlacementOverrideBlockId;
   }

   public String getCeilingPlacementOverrideBlockId() {
      return this.ceilingPlacementOverrideBlockId;
   }

   public static enum BlockPreviewVisibility {
      ALWAYS_VISIBLE,
      ALWAYS_HIDDEN,
      DEFAULT;

      public static final EnumCodec<BlockPlacementSettings.BlockPreviewVisibility> CODEC = new EnumCodec<>(BlockPlacementSettings.BlockPreviewVisibility.class);
   }

   public static enum RotationMode {
      FACING_PLAYER,
      BLOCK_NORMAL,
      STAIR_FACING_PLAYER,
      DEFAULT;

      public static final EnumCodec<BlockPlacementSettings.RotationMode> CODEC = new EnumCodec<>(BlockPlacementSettings.RotationMode.class);
   }
}
