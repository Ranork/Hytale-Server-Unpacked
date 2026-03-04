package com.hypixel.hytale.builtin.adventure.worldlocationcondition;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.range.IntRange;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.tagpattern.config.TagPattern;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.worldlocationcondition.WorldLocationCondition;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NeighbourBlockTagsLocationCondition extends WorldLocationCondition {
   @Nonnull
   public static final BuilderCodec<NeighbourBlockTagsLocationCondition> CODEC = BuilderCodec.builder(
         NeighbourBlockTagsLocationCondition.class, NeighbourBlockTagsLocationCondition::new, WorldLocationCondition.BASE_CODEC
      )
      .append(
         new KeyedCodec<>("TagPattern", Codec.STRING),
         (neighbourBlockTagsLocationCondition, s) -> neighbourBlockTagsLocationCondition.tagPatternId = s,
         neighbourBlockTagsLocationCondition -> neighbourBlockTagsLocationCondition.tagPatternId
      )
      .documentation("A TagPattern can be used if the block at the chosen location needs to fulfill specific conditions.")
      .addValidator(Validators.nonNull())
      .add()
      .<NeighbourBlockTagsLocationCondition.NeighbourDirection>append(
         new KeyedCodec<>("NeighbourBlock", new EnumCodec<>(NeighbourBlockTagsLocationCondition.NeighbourDirection.class)),
         (neighbourBlockTagsLocationCondition, neighbourDirection) -> neighbourBlockTagsLocationCondition.neighbourDirection = neighbourDirection,
         neighbourBlockTagsLocationCondition -> neighbourBlockTagsLocationCondition.neighbourDirection
      )
      .documentation("Defines which block has to be checked related to original location. Possible values: Above, Below, Sideways.")
      .addValidator(Validators.nonNull())
      .add()
      .<IntRange>append(
         new KeyedCodec<>("Support", IntRange.CODEC),
         (neighbourBlockTagsLocationCondition, blockSupport) -> neighbourBlockTagsLocationCondition.support = blockSupport,
         neighbourBlockTagsLocationCondition -> neighbourBlockTagsLocationCondition.support
      )
      .documentation("Additional field used if NeighbourBlock is set to Sideways.")
      .add()
      .build();
   protected String tagPatternId;
   protected NeighbourBlockTagsLocationCondition.NeighbourDirection neighbourDirection;
   protected IntRange support = new IntRange(1, 4);

   @Override
   public boolean test(@Nonnull World world, int worldX, int worldY, int worldZ) {
      if (worldY <= 0) {
         return false;
      } else {
         long chunkIndex = ChunkUtil.indexChunkFromBlock(worldX, worldZ);
         WorldChunk worldChunk = world.getNonTickingChunk(chunkIndex);
         if (worldChunk == null) {
            return false;
         } else if (this.neighbourDirection == NeighbourBlockTagsLocationCondition.NeighbourDirection.SIDEWAYS) {
            int count = 0;
            ChunkAccessor chunkAccessor = worldChunk.getChunkAccessor();
            if (this.checkBlockHasTag(worldX - 1, worldY, worldZ, chunkAccessor.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(worldX - 1, worldZ)))) {
               count++;
            }

            if (this.checkBlockHasTag(worldX + 1, worldY, worldZ, chunkAccessor.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(worldX + 1, worldZ)))) {
               count++;
            }

            if (this.checkBlockHasTag(worldX, worldY, worldZ - 1, chunkAccessor.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(worldX, worldZ - 1)))) {
               count++;
            }

            if (this.checkBlockHasTag(worldX, worldY, worldZ + 1, chunkAccessor.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(worldX, worldZ + 1)))) {
               count++;
            }

            return this.support.includes(count);
         } else {
            int yPos = worldY;
            switch (this.neighbourDirection) {
               case ABOVE:
                  yPos = worldY + 1;
                  break;
               case BELOW:
                  yPos = worldY - 1;
            }

            return this.checkBlockHasTag(worldX, yPos, worldZ, worldChunk);
         }
      }
   }

   private boolean checkBlockHasTag(int x, int y, int z, @Nullable BlockAccessor worldChunk) {
      if (worldChunk == null) {
         return false;
      } else {
         int blockIndex = worldChunk.getBlock(x, y, z);
         TagPattern tagPattern = TagPattern.getAssetMap().getAsset(this.tagPatternId);
         if (tagPattern != null) {
            BlockType blockType = BlockType.getAssetMap().getAsset(blockIndex);
            if (blockType == null) {
               return false;
            } else {
               AssetExtraInfo.Data data = blockType.getData();
               return data == null ? false : tagPattern.test(data.getTags());
            }
         } else {
            HytaleLogger.getLogger().at(Level.WARNING).log("No TagPattern asset found for id: " + this.tagPatternId);
            return false;
         }
      }
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         NeighbourBlockTagsLocationCondition that = (NeighbourBlockTagsLocationCondition)o;
         if (!this.tagPatternId.equals(that.tagPatternId)) {
            return false;
         } else if (this.neighbourDirection != that.neighbourDirection) {
            return false;
         } else {
            return this.support != null ? this.support.equals(that.support) : that.support == null;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.tagPatternId.hashCode();
      result = 31 * result + this.neighbourDirection.hashCode();
      return 31 * result + (this.support != null ? this.support.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "NeighbourBlockTagsLocationCondition{tagPatternId='"
         + this.tagPatternId
         + "', neighbourDirection="
         + this.neighbourDirection
         + ", support="
         + this.support
         + "} "
         + super.toString();
   }

   protected static enum NeighbourDirection {
      ABOVE,
      BELOW,
      SIDEWAYS;
   }
}
