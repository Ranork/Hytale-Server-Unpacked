package com.hypixel.hytale.builtin.adventure.objectives.config.worldlocationproviders;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.iterator.SpiralIterator;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CheckTagWorldHeightRadiusProvider extends WorldLocationProvider {
   @Nonnull
   public static final BuilderCodec<CheckTagWorldHeightRadiusProvider> CODEC = BuilderCodec.builder(
         CheckTagWorldHeightRadiusProvider.class, CheckTagWorldHeightRadiusProvider::new, BASE_CODEC
      )
      .append(
         new KeyedCodec<>("BlockTags", Codec.STRING_ARRAY),
         (lookBlocksBelowCondition, strings) -> lookBlocksBelowCondition.blockTags = strings,
         lookBlocksBelowCondition -> lookBlocksBelowCondition.blockTags
      )
      .addValidator(Validators.nonEmptyArray())
      .addValidator(Validators.uniqueInArray())
      .add()
      .<Integer>append(
         new KeyedCodec<>("Radius", Codec.INTEGER),
         (checkTagWorldHeightRadiusCondition, integer) -> checkTagWorldHeightRadiusCondition.radius = integer,
         checkTagWorldHeightRadiusCondition -> checkTagWorldHeightRadiusCondition.radius
      )
      .addValidator(Validators.greaterThan(0))
      .add()
      .afterDecode(checkTagWorldHeightRadiusCondition -> {
         if (checkTagWorldHeightRadiusCondition.blockTags != null) {
            checkTagWorldHeightRadiusCondition.blockTagsIndexes = new int[checkTagWorldHeightRadiusCondition.blockTags.length];

            for (int i = 0; i < checkTagWorldHeightRadiusCondition.blockTags.length; i++) {
               String blockTag = checkTagWorldHeightRadiusCondition.blockTags[i];
               checkTagWorldHeightRadiusCondition.blockTagsIndexes[i] = AssetRegistry.getOrCreateTagIndex(blockTag);
            }
         }
      })
      .build();
   protected String[] blockTags;
   protected int radius = 5;
   private int[] blockTagsIndexes;

   public CheckTagWorldHeightRadiusProvider(@Nonnull String[] blockTags, int radius) {
      this.blockTags = blockTags;
      this.radius = radius;
      this.blockTagsIndexes = new int[blockTags.length];

      for (int i = 0; i < blockTags.length; i++) {
         String blockTag = blockTags[i];
         this.blockTagsIndexes[i] = AssetRegistry.getOrCreateTagIndex(blockTag);
      }
   }

   protected CheckTagWorldHeightRadiusProvider() {
   }

   @Nullable
   @Override
   public Vector3i runCondition(@Nonnull World world, @Nonnull Vector3i position) {
      SpiralIterator iterator = new SpiralIterator(position.x, position.z, this.radius);

      while (iterator.hasNext()) {
         long pos = iterator.next();
         int blockX = MathUtil.unpackLeft(pos);
         int blockZ = MathUtil.unpackRight(pos);
         long chunkIndex = ChunkUtil.indexChunkFromBlock(blockX, blockZ);
         WorldChunk worldChunkComponent = world.getNonTickingChunk(chunkIndex);
         if (worldChunkComponent != null) {
            int blockY = worldChunkComponent.getHeight(blockX, blockZ);
            int blockId = worldChunkComponent.getBlock(blockX, blockY, blockZ);

            for (int i = 0; i < this.blockTagsIndexes.length; i++) {
               if (BlockType.getAssetMap().getIndexesForTag(this.blockTagsIndexes[i]).contains(blockId)) {
                  return new Vector3i(blockX, blockY + 1, blockZ);
               }
            }
         }
      }

      return null;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CheckTagWorldHeightRadiusProvider that = (CheckTagWorldHeightRadiusProvider)o;
         return this.radius != that.radius ? false : Arrays.equals((Object[])this.blockTags, (Object[])that.blockTags);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Arrays.hashCode((Object[])this.blockTags);
      return 31 * result + this.radius;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CheckTagWorldHeightRadiusProvider{blockTags=" + Arrays.toString((Object[])this.blockTags) + ", radius=" + this.radius + "} " + super.toString();
   }
}
