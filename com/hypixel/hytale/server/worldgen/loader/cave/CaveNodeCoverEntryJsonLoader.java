package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.procedurallib.condition.ConstantBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateRndCondition;
import com.hypixel.hytale.procedurallib.condition.HeightCondition;
import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateRndCondition;
import com.hypixel.hytale.procedurallib.json.HeightThresholdInterpreterJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoiseMaskConditionJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.loader.util.ResolvedBlockArrayJsonLoader;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.ResolvedBlockArray;
import com.hypixel.hytale.server.worldgen.util.condition.FilteredBlockFluidCondition;
import com.hypixel.hytale.server.worldgen.util.condition.HashSetBlockFluidCondition;
import com.hypixel.hytale.server.worldgen.util.condition.RandomCoordinateCondition;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class CaveNodeCoverEntryJsonLoader extends JsonLoader<SeedStringResource, CaveNodeType.CaveNodeCoverEntry> {
   private static final IBlockFluidCondition DEFAULT_PARENT_MASK = new FilteredBlockFluidCondition(0, ConstantBlockFluidCondition.DEFAULT_TRUE);

   public CaveNodeCoverEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".CaveNodeCoverEntry"), dataFolder, json);
   }

   @Nonnull
   public CaveNodeType.CaveNodeCoverEntry load() {
      return new CaveNodeType.CaveNodeCoverEntry(
         this.loadEntries(),
         this.loadHeightCondition(),
         this.loadMapCondition(),
         this.loadDensityCondition(),
         this.loadParentCondition(),
         this.loadAnchorType()
      );
   }

   @Nonnull
   protected IWeightedMap<CaveNodeType.CaveNodeCoverEntry.Entry> loadEntries() {
      if (!this.has("Type")) {
         throw new IllegalArgumentException("Could not find type array for cave cover container! Keyword: Type");
      } else {
         ResolvedBlockArray types = new ResolvedBlockArrayJsonLoader(this.seed, this.dataFolder, this.get("Type")).load();
         JsonArray weights = this.has("Weight") ? this.get("Weight").getAsJsonArray() : null;
         if (weights != null && weights.size() != types.size()) {
            throw new IllegalArgumentException("Weight array size does not equal size of types array");
         } else {
            WeightedMap.Builder<CaveNodeType.CaveNodeCoverEntry.Entry> builder = WeightedMap.builder(CaveNodeType.CaveNodeCoverEntry.Entry.EMPTY_ARRAY);

            for (int i = 0; i < types.size(); i++) {
               BlockFluidEntry blockEntry = types.getEntries()[i];
               int offset = this.loadOffset();
               double weight = weights == null ? 1.0 : weights.get(i).getAsDouble();
               CaveNodeType.CaveNodeCoverEntry.Entry entry = new CaveNodeType.CaveNodeCoverEntry.Entry(blockEntry, offset);
               builder.put(entry, weight);
            }

            if (builder.size() <= 0) {
               throw new IllegalArgumentException("There are no blocks in this cover container!");
            } else {
               return builder.build();
            }
         }
      }
   }

   @Nonnull
   protected ICoordinateRndCondition loadHeightCondition() {
      ICoordinateRndCondition condition = DefaultCoordinateRndCondition.DEFAULT_TRUE;
      if (this.has("HeightThreshold")) {
         condition = new HeightCondition(new HeightThresholdInterpreterJsonLoader<>(this.seed, this.dataFolder, this.get("HeightThreshold"), 320).load());
      }

      return condition;
   }

   @Nonnull
   protected ICoordinateCondition loadMapCondition() {
      return new NoiseMaskConditionJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
   }

   @Nonnull
   protected ICoordinateCondition loadDensityCondition() {
      ICoordinateCondition densityCondition = DefaultCoordinateCondition.DEFAULT_TRUE;
      if (this.has("Density")) {
         densityCondition = new RandomCoordinateCondition(this.get("Density").getAsDouble());
      }

      return densityCondition;
   }

   @Nonnull
   protected IBlockFluidCondition loadParentCondition() {
      IBlockFluidCondition parentMask = DEFAULT_PARENT_MASK;
      if (this.has("Parent")) {
         ResolvedBlockArray blockArray = new ResolvedBlockArrayJsonLoader(this.seed, this.dataFolder, this.get("Parent")).load();
         LongSet blockSet = blockArray.getEntrySet();
         parentMask = new HashSetBlockFluidCondition(blockSet);
      }

      return parentMask;
   }

   protected int loadOffset() {
      int offset = 0;
      if (this.has("Offset")) {
         offset = this.get("Offset").getAsInt();
      }

      return offset;
   }

   @Nonnull
   protected CaveNodeType.CaveNodeCoverType loadAnchorType() {
      CaveNodeType.CaveNodeCoverType anchorType = CaveNodeType.CaveNodeCoverType.FLOOR;
      if (this.has("AnchorType")) {
         anchorType = CaveNodeType.CaveNodeCoverType.valueOf(this.get("AnchorType").getAsString());
      }

      return anchorType;
   }

   public interface Constants {
      String KEY_TYPE = "Type";
      String KEY_WEIGHT = "Weight";
      String KEY_HEIGHT_THRESHOLD = "HeightThreshold";
      String KEY_NOISE_MASK = "NoiseMask";
      String KEY_DENSITY = "Density";
      String KEY_OFFSET = "Offset";
      String KEY_PARENT = "Parent";
      String KEY_ANCHOR_TYPE = "AnchorType";
      String ERROR_NO_TYPE = "Could not find type array for cave cover container! Keyword: Type";
      String ERROR_NO_ENTRIES = "There are no blocks in this cover container!";
      String ERROR_WEIGHTS_ARRAY_SIZE = "Weight array size does not equal size of types array";
   }
}
