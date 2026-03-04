package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.function.consumer.TriIntConsumer;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import javax.annotation.Nonnull;

public class FillerBlockUtil {
   public static final float THRESHOLD = 0.0F;
   public static final int NO_FILLER = 0;
   private static final int BITS_PER_AXIS = 5;
   private static final int MASK = 31;
   private static final int INVERT = -32;

   public static void forEachFillerBlock(@Nonnull BlockBoundingBoxes.RotatedVariantBoxes blockBoundingBoxes, @Nonnull TriIntConsumer consumer) {
      forEachFillerBlock(0.0F, blockBoundingBoxes, consumer);
   }

   public static void forEachFillerBlock(float threshold, @Nonnull BlockBoundingBoxes.RotatedVariantBoxes blockBoundingBoxes, @Nonnull TriIntConsumer consumer) {
      if (!(threshold < 0.0F) && !(threshold >= 1.0F)) {
         Box boundingBox = blockBoundingBoxes.getBoundingBox();
         int minX = (int)boundingBox.min.x;
         int minY = (int)boundingBox.min.y;
         int minZ = (int)boundingBox.min.z;
         if (minX - boundingBox.min.x > threshold) {
            minX--;
         }

         if (minY - boundingBox.min.y > threshold) {
            minY--;
         }

         if (minZ - boundingBox.min.z > threshold) {
            minZ--;
         }

         int maxX = (int)boundingBox.max.x;
         int maxY = (int)boundingBox.max.y;
         int maxZ = (int)boundingBox.max.z;
         if (boundingBox.max.x - maxX > threshold) {
            maxX++;
         }

         if (boundingBox.max.y - maxY > threshold) {
            maxY++;
         }

         if (boundingBox.max.z - maxZ > threshold) {
            maxZ++;
         }

         int blockWidth = Math.max(maxX - minX, 1);
         int blockHeight = Math.max(maxY - minY, 1);
         int blockDepth = Math.max(maxZ - minZ, 1);

         for (int x = 0; x < blockWidth; x++) {
            for (int y = 0; y < blockHeight; y++) {
               for (int z = 0; z < blockDepth; z++) {
                  consumer.accept(minX + x, minY + y, minZ + z);
               }
            }
         }
      } else {
         throw new IllegalArgumentException("Threshold must be between 0 and 1");
      }
   }

   public static boolean testFillerBlocks(@Nonnull BlockBoundingBoxes.RotatedVariantBoxes blockBoundingBoxes, @Nonnull TriIntPredicate predicate) {
      return testFillerBlocks(0.0F, blockBoundingBoxes, predicate);
   }

   public static boolean testFillerBlocks(
      float threshold, @Nonnull BlockBoundingBoxes.RotatedVariantBoxes blockBoundingBoxes, @Nonnull TriIntPredicate predicate
   ) {
      if (!(threshold < 0.0F) && !(threshold >= 1.0F)) {
         Box boundingBox = blockBoundingBoxes.getBoundingBox();
         int minX = (int)boundingBox.min.x;
         int minY = (int)boundingBox.min.y;
         int minZ = (int)boundingBox.min.z;
         if (minX - boundingBox.min.x > threshold) {
            minX--;
         }

         if (minY - boundingBox.min.y > threshold) {
            minY--;
         }

         if (minZ - boundingBox.min.z > threshold) {
            minZ--;
         }

         int maxX = (int)boundingBox.max.x;
         int maxY = (int)boundingBox.max.y;
         int maxZ = (int)boundingBox.max.z;
         if (boundingBox.max.x - maxX > threshold) {
            maxX++;
         }

         if (boundingBox.max.y - maxY > threshold) {
            maxY++;
         }

         if (boundingBox.max.z - maxZ > threshold) {
            maxZ++;
         }

         int blockWidth = Math.max(maxX - minX, 1);
         int blockHeight = Math.max(maxY - minY, 1);
         int blockDepth = Math.max(maxZ - minZ, 1);

         for (int x = 0; x < blockWidth; x++) {
            for (int y = 0; y < blockHeight; y++) {
               for (int z = 0; z < blockDepth; z++) {
                  if (!predicate.test(minX + x, minY + y, minZ + z)) {
                     return false;
                  }
               }
            }
         }

         return true;
      } else {
         throw new IllegalArgumentException("Threshold must be between 0 and 1");
      }
   }

   public static <A, B> FillerBlockUtil.ValidationResult validateBlock(
      int x, int y, int z, int blockId, int rotation, int filler, A a, B b, @Nonnull FillerBlockUtil.FillerFetcher<A, B> fetcher
   ) {
      if (blockId == 0) {
         return FillerBlockUtil.ValidationResult.OK;
      } else {
         BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = BlockType.getAssetMap();
         BlockType blockType = blockTypeAssetMap.getAsset(blockId);
         if (blockType == null) {
            return FillerBlockUtil.ValidationResult.OK;
         } else {
            String id = blockType.getId();
            IndexedLookupTableAssetMap<String, BlockBoundingBoxes> hitboxAssetMap = BlockBoundingBoxes.getAssetMap();
            if (filler != 0) {
               int fillerX = unpackX(filler);
               int fillerY = unpackY(filler);
               int fillerZ = unpackZ(filler);
               int baseBlockId = fetcher.getBlock(a, b, x - fillerX, y - fillerY, z - fillerZ);
               BlockType baseBlock = blockTypeAssetMap.getAsset(baseBlockId);
               if (baseBlock == null) {
                  return FillerBlockUtil.ValidationResult.INVALID_BLOCK;
               } else {
                  String baseId = baseBlock.getId();
                  BlockBoundingBoxes hitbox = hitboxAssetMap.getAsset(baseBlock.getHitboxTypeIndex());
                  if (hitbox == null) {
                     return FillerBlockUtil.ValidationResult.OK;
                  } else {
                     int baseFiller = fetcher.getFiller(a, b, x - fillerX, y - fillerY, z - fillerZ);
                     int baseRotation = fetcher.getRotationIndex(a, b, x - fillerX, y - fillerY, z - fillerZ);
                     return baseFiller == 0
                           && baseRotation == rotation
                           && id.equals(baseId)
                           && hitbox.get(baseRotation).getBoundingBox().containsBlock(fillerX, fillerY, fillerZ)
                        ? FillerBlockUtil.ValidationResult.OK
                        : FillerBlockUtil.ValidationResult.INVALID_BLOCK;
                  }
               }
            } else {
               BlockBoundingBoxes hitbox = hitboxAssetMap.getAsset(blockType.getHitboxTypeIndex());
               if (hitbox != null && hitbox.protrudesUnitBox()) {
                  boolean result = testFillerBlocks(hitbox.get(rotation), (x1, y1, z1) -> {
                     if (x1 == 0 && y1 == 0 && z1 == 0) {
                        return true;
                     } else {
                        int worldX = x + x1;
                        int worldY = y + y1;
                        int worldZ = z + z1;
                        int fillerBlockId = fetcher.getBlock(a, b, worldX, worldY, worldZ);
                        BlockType fillerBlock = blockTypeAssetMap.getAsset(fillerBlockId);
                        int expectedFiller = pack(x1, y1, z1);
                        if (fetcher.getFiller(a, b, worldX, worldY, worldZ) != expectedFiller) {
                           return false;
                        } else if (fetcher.getRotationIndex(a, b, worldX, worldY, worldZ) != rotation) {
                           return false;
                        } else if (fillerBlock == null) {
                           return false;
                        } else {
                           String blockTypeKey = fillerBlock.getId();
                           return blockTypeKey.equals(id);
                        }
                     }
                  });
                  return result ? FillerBlockUtil.ValidationResult.OK : FillerBlockUtil.ValidationResult.INVALID_FILLER;
               } else {
                  return FillerBlockUtil.ValidationResult.OK;
               }
            }
         }
      }
   }

   public static int pack(int x, int y, int z) {
      return x & 31 | (z & 31) << 5 | (y & 31) << 10;
   }

   public static int unpackX(int val) {
      int result = val & 31;
      if ((result & 16) != 0) {
         result |= -32;
      }

      return result;
   }

   public static int unpackY(int val) {
      int result = val >> 10 & 31;
      if ((result & 16) != 0) {
         result |= -32;
      }

      return result;
   }

   public static int unpackZ(int val) {
      int result = val >> 5 & 31;
      if ((result & 16) != 0) {
         result |= -32;
      }

      return result;
   }

   public interface FillerFetcher<A, B> {
      int getBlock(A var1, B var2, int var3, int var4, int var5);

      int getFiller(A var1, B var2, int var3, int var4, int var5);

      int getRotationIndex(A var1, B var2, int var3, int var4, int var5);
   }

   public static enum ValidationResult {
      OK,
      INVALID_BLOCK,
      INVALID_FILLER;
   }
}
