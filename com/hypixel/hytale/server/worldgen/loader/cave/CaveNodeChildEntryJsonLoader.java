package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.json.DoubleRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.FloatRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.procedurallib.supplier.IFloatRange;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveYawMode;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CaveNodeChildEntryJsonLoader extends JsonLoader<SeedStringResource, CaveNodeType.CaveNodeChildEntry> {
   protected final CaveNodeTypeStorage storage;

   public CaveNodeChildEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, CaveNodeTypeStorage storage) {
      super(seed.append(".CaveNodeChildEntry"), dataFolder, json);
      this.storage = storage;
   }

   @Nonnull
   public CaveNodeType.CaveNodeChildEntry load() {
      return new CaveNodeType.CaveNodeChildEntry(
         this.loadNodes(),
         this.loadAnchor(),
         this.loadOffset(),
         this.loadRotations(),
         this.loadChildrenLimit(),
         this.loadRepeat(),
         this.loadPitchModifier(),
         this.loadYawModifier(),
         this.loadChance(),
         this.loadYawMode()
      );
   }

   @Nonnull
   protected IWeightedMap<CaveNodeType> loadNodes() {
      WeightedMap.Builder<CaveNodeType> builder = WeightedMap.builder(CaveNodeType.EMPTY_ARRAY);
      JsonElement nodeElement = this.get("Node");
      if (nodeElement.isJsonArray()) {
         JsonArray nodeArray = nodeElement.getAsJsonArray();
         JsonArray weightsArray;
         if (this.has("Weights")) {
            JsonElement weightsElement = this.get("Weights");
            if (!weightsElement.isJsonArray()) {
               throw new IllegalArgumentException("'Weights' must be an array if set");
            }

            weightsArray = weightsElement.getAsJsonArray();
            if (weightsArray.size() != nodeArray.size()) {
               throw new IllegalArgumentException("Weight array size is different from node name array.");
            }
         } else {
            weightsArray = null;
         }

         for (int i = 0; i < nodeArray.size(); i++) {
            JsonElement nodeEntryElement = this.getOrLoad(nodeArray.get(i));
            CaveNodeType caveNodeType = this.loadCaveNodeType(i, nodeEntryElement);
            double weight = weightsArray != null ? weightsArray.get(i).getAsDouble() : 1.0;
            builder.put(caveNodeType, weight);
         }
      } else if (nodeElement.isJsonObject() || nodeElement.isJsonPrimitive()) {
         CaveNodeType caveNodeType = this.loadCaveNodeType(0, nodeElement);
         builder.put(caveNodeType, 1.0);
      }

      if (builder.size() <= 0) {
         throw new IllegalArgumentException("There are no valid nodes in this child entry!");
      } else {
         return builder.build();
      }
   }

   @Nonnull
   protected CaveNodeType loadCaveNodeType(int index, @Nonnull JsonElement element) {
      if (element.isJsonObject()) {
         String caveNodeTypeName = this.seed.get().getUniqueName("ChildCaveType#");
         return this.storage.loadCaveNodeType(caveNodeTypeName, element.getAsJsonObject());
      } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
         String caveNodeTypeName = element.getAsString();
         return this.storage.getOrLoadCaveNodeType(caveNodeTypeName);
      } else {
         throw error("Invalid cave node type entry: %d", index);
      }
   }

   @Nonnull
   protected Vector3d loadAnchor() {
      Vector3d anchor = Vector3d.ZERO;
      if (this.has("Anchor")) {
         anchor = this.loadVector(anchor.clone(), this.get("Anchor"));
      }

      return anchor;
   }

   @Nonnull
   protected Vector3d loadOffset() {
      Vector3d offset = Vector3d.ZERO;
      if (this.has("Offset")) {
         offset = this.loadVector(offset.clone(), this.get("Offset"));
      }

      return offset;
   }

   @Nonnull
   protected PrefabRotation[] loadRotations() {
      PrefabRotation[] rotations = new PrefabRotation[]{PrefabRotation.ROTATION_0};
      if (this.has("Rotation")) {
         JsonElement rotationElement = this.get("Rotation");
         if (rotationElement.isJsonPrimitive()) {
            rotations = new PrefabRotation[]{PrefabRotation.valueOfExtended(rotationElement.getAsString())};
         } else if (rotationElement.isJsonArray()) {
            JsonArray rotationArray = rotationElement.getAsJsonArray();
            rotations = new PrefabRotation[rotationArray.size()];

            for (int i = 0; i < rotations.length; i++) {
               rotations[i] = PrefabRotation.valueOfExtended(rotationArray.get(i).getAsString());
            }
         }
      }

      return rotations;
   }

   @Nullable
   protected IDoubleRange loadChildrenLimit() {
      return this.has("ChildrenLimit") ? new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("ChildrenLimit"), 0.0).load() : null;
   }

   @Nonnull
   protected IDoubleRange loadRepeat() {
      IDoubleRange range = DoubleRange.ONE;
      if (this.has("Repeat")) {
         range = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Repeat"), 0.0).load();
      }

      return range;
   }

   @Nonnull
   protected CaveNodeType.CaveNodeChildEntry.OrientationModifier loadYawModifier() {
      IFloatRange yawAdd = this.loadYawAdd();
      if (yawAdd != null) {
         return (current, random) -> current + yawAdd.getValue(random);
      } else {
         IFloatRange yawSet = this.loadYawSet();
         return yawSet != null ? (current, random) -> yawSet.getValue(random) : (current, random) -> current;
      }
   }

   @Nonnull
   protected CaveNodeType.CaveNodeChildEntry.OrientationModifier loadPitchModifier() {
      IFloatRange pitchAdd = this.loadPitchAdd();
      if (pitchAdd != null) {
         return (current, random) -> current + pitchAdd.getValue(random);
      } else {
         IFloatRange pitchSet = this.loadPitchSet();
         return pitchSet != null ? (current, random) -> pitchSet.getValue(random) : (current, random) -> current;
      }
   }

   @Nullable
   protected IFloatRange loadYawAdd() {
      IFloatRange yawAdd = null;
      if (this.has("YawAdd")) {
         yawAdd = new FloatRangeJsonLoader<>(this.seed, this.dataFolder, this.get("YawAdd"), 0.0F, deg -> deg * (float) (Math.PI / 180.0)).load();
      }

      return yawAdd;
   }

   @Nullable
   protected IFloatRange loadPitchAdd() {
      IFloatRange pitchAdd = null;
      if (this.has("PitchAdd")) {
         pitchAdd = new FloatRangeJsonLoader<>(this.seed, this.dataFolder, this.get("PitchAdd"), 0.0F, deg -> deg * (float) (Math.PI / 180.0)).load();
      }

      return pitchAdd;
   }

   @Nullable
   protected IFloatRange loadYawSet() {
      IFloatRange yawSet = null;
      if (this.has("YawSet")) {
         yawSet = new FloatRangeJsonLoader<>(this.seed, this.dataFolder, this.get("YawSet"), 0.0F, deg -> deg * (float) (Math.PI / 180.0)).load();
      }

      return yawSet;
   }

   @Nullable
   protected IFloatRange loadPitchSet() {
      IFloatRange pitchSet = null;
      if (this.has("PitchSet")) {
         pitchSet = new FloatRangeJsonLoader<>(this.seed, this.dataFolder, this.get("PitchSet"), 0.0F, deg -> deg * (float) (Math.PI / 180.0)).load();
      }

      return pitchSet;
   }

   protected double loadChance() {
      double chance = 1.0;
      if (this.has("Chance")) {
         chance = this.get("Chance").getAsDouble();
      }

      return chance;
   }

   @Nonnull
   protected CaveYawMode loadYawMode() {
      CaveYawMode combiner = CaveYawMode.NODE;
      if (this.has("YawMode")) {
         combiner = CaveYawMode.valueOf(this.get("YawMode").getAsString());
      }

      return combiner;
   }

   @Nonnull
   protected Vector3d loadVector(@Nonnull Vector3d vector, @Nonnull JsonElement jsonElement) {
      JsonArray array = jsonElement.getAsJsonArray();
      vector.x = array.get(0).getAsDouble();
      vector.y = array.get(1).getAsDouble();
      vector.z = array.get(2).getAsDouble();
      return vector;
   }

   public interface Constants {
      String KEY_NODE = "Node";
      String KEY_SEED = "Seed";
      String KEY_WEIGHTS = "Weights";
      String KEY_ANCHOR = "Anchor";
      String KEY_OFFSET = "Offset";
      String KEY_ROTATION = "Rotation";
      String KEY_CHILDREN_LIMIT = "ChildrenLimit";
      String KEY_REPEAT = "Repeat";
      String KEY_PITCH_ADD = "PitchAdd";
      String KEY_PITCH_SET = "PitchSet";
      String KEY_YAW_ADD = "YawAdd";
      String KEY_YAW_SET = "YawSet";
      String KEY_CHANCE = "Chance";
      String KEY_YAW_MODE = "YawMode";
      String ERROR_WEIGHTS_ARRAY = "'Weights' must be an array if set";
      String ERROR_ENTRY_WEIGHT_SIZE = "Weight array size is different from node name array.";
      String ERROR_NO_NODES = "There are no valid nodes in this child entry!";
   }
}
