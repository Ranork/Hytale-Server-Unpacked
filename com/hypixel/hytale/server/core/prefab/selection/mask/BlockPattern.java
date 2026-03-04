package com.hypixel.hytale.server.core.prefab.selection.mask;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.function.FunctionCodec;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BlockTypeListAsset;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPattern {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final Codec<BlockPattern> CODEC = new FunctionCodec<>(Codec.STRING, BlockPattern::parse, BlockPattern::toString);
   public static final BlockPattern EMPTY = new BlockPattern(parseBlockPattern("Empty"));
   public static final BlockPattern[] EMPTY_ARRAY = new BlockPattern[0];
   private static final Pattern FILLER_TEMP_REMOVER_PATTERN = Pattern.compile("(Filler=-?\\d+),(-?\\d+),(-?\\d+)");
   private static final String BLOCK_SEPARATOR = ",";
   private static final String ALT_BLOCK_SEPARATOR = ";";
   private static final String CHANCE_SUFFIX = "%";
   private static final double DEFAULT_CHANCE = 100.0;
   private final IWeightedMap<String> weightedMap;
   private final transient String toString0;
   private IWeightedMap<Integer> resolvedWeightedMap;
   private IWeightedMap<BlockPattern.BlockEntry> resolvedWeightedMapBtk;

   public BlockPattern(IWeightedMap<String> weightedMap) {
      this.weightedMap = weightedMap;
      this.toString0 = this.toString0();
   }

   public Integer[] getResolvedKeys() {
      this.resolve();
      return this.resolvedWeightedMap.internalKeys();
   }

   public void resolve() {
      if (this.resolvedWeightedMap == null) {
         WeightedMap.Builder<Integer> mapBuilder = WeightedMap.builder(ArrayUtil.EMPTY_INTEGER_ARRAY);
         WeightedMap.Builder<BlockPattern.BlockEntry> mapBuilderKey = WeightedMap.builder(new BlockPattern.BlockEntry[0]);
         this.weightedMap.forEachEntry((blockName, weight) -> {
            int blockId = parseBlock(blockName);
            BlockPattern.BlockEntry key = tryParseBlockTypeKey(blockName);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            if (blockType != null && blockType.getBlockListAssetId() != null) {
               BlockTypeListAsset blockTypeListAsset = BlockTypeListAsset.getAssetMap().getAsset(blockType.getBlockListAssetId());
               if (blockTypeListAsset != null && blockTypeListAsset.getBlockPattern() != null) {
                  for (String resolvedKey : blockTypeListAsset.getBlockTypeKeys()) {
                     int resolvedId = BlockType.getAssetMap().getIndex(resolvedKey);
                     if (resolvedId == Integer.MIN_VALUE) {
                        LOGGER.at(Level.WARNING).log("BlockTypeList '%s' contains invalid block '%s' - skipping", blockType.getBlockListAssetId(), resolvedKey);
                     } else {
                        mapBuilder.put(resolvedId, weight / blockTypeListAsset.getBlockTypeKeys().size());
                     }
                  }

                  return;
               }
            }

            mapBuilder.put(blockId, weight);
            if (key != null) {
               mapBuilderKey.put(key, weight);
            }
         });
         this.resolvedWeightedMap = mapBuilder.build();
         this.resolvedWeightedMapBtk = mapBuilderKey.build();
      }
   }

   public boolean isEmpty() {
      return this.weightedMap.size() == 0;
   }

   public int nextBlock(Random random) {
      this.resolve();
      return this.resolvedWeightedMap.get(random);
   }

   @Nullable
   public BlockPattern.BlockEntry nextBlockTypeKey(Random random) {
      this.resolve();
      return this.resolvedWeightedMapBtk.get(random);
   }

   @Deprecated
   public int firstBlock() {
      this.resolve();
      return this.resolvedWeightedMap.size() > 0 ? this.resolvedWeightedMap.internalKeys()[0] : 0;
   }

   @Override
   public String toString() {
      return this.toString0;
   }

   private String toString0() {
      if (this.weightedMap.size() == 1) {
         return this.weightedMap.internalKeys()[0];
      } else {
         List<String> blocks = new ObjectArrayList();
         this.weightedMap.forEachEntry((k, v) -> blocks.add(v + "%" + k));
         return String.join(",", blocks);
      }
   }

   public static BlockPattern parse(@Nonnull String str) {
      if (!str.isEmpty() && !str.equals("Empty")) {
         if (str.toLowerCase().contains("filler=")) {
            str = FILLER_TEMP_REMOVER_PATTERN.matcher(str).replaceAll("$1;$2;$3");
         }

         str = str.replace(";", ",");
         return new BlockPattern(parseBlockPattern(str.split(",")));
      } else {
         return EMPTY;
      }
   }

   @Nonnull
   private static IWeightedMap<String> parseBlockPattern(@Nonnull String... blocksArgs) {
      WeightedMap.Builder<String> builder = WeightedMap.builder(ArrayUtil.EMPTY_STRING_ARRAY);

      for (String blockArg : blocksArgs) {
         if (!blockArg.isEmpty()) {
            double chance = 100.0;
            String[] blockArr = blockArg.split("%");
            if (blockArr.length > 1) {
               try {
                  chance = Double.parseDouble(blockArr[0]);
               } catch (NumberFormatException var10) {
                  throw new IllegalArgumentException("Invalid Chance Value: " + blockArr[0], var10);
               }

               blockArg = blockArr[1];
            }

            builder.put(blockArg, chance);
         }
      }

      return builder.build();
   }

   public static int parseBlock(@Nonnull String blockText) {
      int blockId;
      try {
         blockId = Integer.parseInt(blockText);
         if (BlockType.getAssetMap().getAsset(blockId) == null) {
            throw new IllegalArgumentException("Block with id '" + blockText + "' doesn't exist!");
         }
      } catch (NumberFormatException var4) {
         blockText = blockText.replace(";", ",");
         int oldData = blockText.indexOf(124);
         if (oldData != -1) {
            blockText = blockText.substring(0, oldData);
         }

         blockId = BlockType.getAssetMap().getIndex(blockText);
         if (blockId == Integer.MIN_VALUE) {
            LOGGER.at(Level.WARNING).log("Invalid block name '%s' - using empty block", blockText);
            return 0;
         }
      }

      return blockId;
   }

   @Nullable
   public static BlockPattern.BlockEntry tryParseBlockTypeKey(String blockText) {
      try {
         blockText = blockText.replace(";", ",");
         return BlockPattern.BlockEntry.decode(blockText);
      } catch (Exception var2) {
         return null;
      }
   }

   public record BlockEntry(String blockTypeKey, int rotation, int filler) {
      @Deprecated(forRemoval = true)
      public static Codec<BlockPattern.BlockEntry> CODEC = new FunctionCodec<>(Codec.STRING, BlockPattern.BlockEntry::decode, BlockPattern.BlockEntry::encode);

      @Deprecated(forRemoval = true)
      private String encode() {
         if (this.filler == 0 && this.rotation == 0) {
            return this.blockTypeKey;
         } else {
            StringBuilder out = new StringBuilder(this.blockTypeKey);
            RotationTuple rot = RotationTuple.get(this.rotation);
            if (rot.yaw() != Rotation.None) {
               out.append("|Yaw=").append(rot.yaw().getDegrees());
            }

            if (rot.pitch() != Rotation.None) {
               out.append("|Pitch=").append(rot.pitch().getDegrees());
            }

            if (rot.roll() != Rotation.None) {
               out.append("|Roll=").append(rot.roll().getDegrees());
            }

            if (this.filler != 0) {
               int fillerX = FillerBlockUtil.unpackX(this.filler);
               int fillerY = FillerBlockUtil.unpackY(this.filler);
               int fillerZ = FillerBlockUtil.unpackZ(this.filler);
               out.append("|Filler=").append(fillerX).append(",").append(fillerY).append(",").append(fillerZ);
            }

            return out.toString();
         }
      }

      @Deprecated(forRemoval = true)
      public static BlockPattern.BlockEntry decode(String key) {
         int filler = 0;
         if (key.contains("|Filler=")) {
            int start = key.indexOf("|Filler=") + "|Filler=".length();
            int firstComma = key.indexOf(44, start);
            if (firstComma == -1) {
               throw new IllegalArgumentException("Invalid filler metadata! Missing comma");
            }

            int secondComma = key.indexOf(44, firstComma + 1);
            if (secondComma == -1) {
               throw new IllegalArgumentException("Invalid filler metadata! Missing second comma");
            }

            int end = key.indexOf(124, start);
            if (end == -1) {
               end = key.length();
            }

            int fillerX = Integer.parseInt(key, start, firstComma, 10);
            int fillerY = Integer.parseInt(key, firstComma + 1, secondComma, 10);
            int fillerZ = Integer.parseInt(key, secondComma + 1, end, 10);
            filler = FillerBlockUtil.pack(fillerX, fillerY, fillerZ);
         }

         Rotation rotationYaw = Rotation.None;
         Rotation rotationPitch = Rotation.None;
         Rotation rotationRoll = Rotation.None;
         if (key.contains("|Yaw=")) {
            int startx = key.indexOf("|Yaw=") + "|Yaw=".length();
            int end = key.indexOf(124, startx);
            if (end == -1) {
               end = key.length();
            }

            rotationYaw = Rotation.ofDegrees(Integer.parseInt(key, startx, end, 10));
         }

         if (key.contains("|Pitch=")) {
            int startx = key.indexOf("|Pitch=") + "|Pitch=".length();
            int end = key.indexOf(124, startx);
            if (end == -1) {
               end = key.length();
            }

            rotationPitch = Rotation.ofDegrees(Integer.parseInt(key, startx, end, 10));
         }

         if (key.contains("|Roll=")) {
            int startx = key.indexOf("|Roll=") + "|Roll=".length();
            int end = key.indexOf(124, startx);
            if (end == -1) {
               end = key.length();
            }

            rotationRoll = Rotation.ofDegrees(Integer.parseInt(key, startx, end, 10));
         }

         int end = key.indexOf(124);
         if (end == -1) {
            end = key.length();
         }

         String name = key.substring(0, end);
         return new BlockPattern.BlockEntry(name, RotationTuple.of(rotationYaw, rotationPitch, rotationRoll).index(), filler);
      }
   }
}
