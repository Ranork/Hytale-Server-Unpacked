package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.HeightThresholdCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.IHeightThresholdInterpreter;
import com.hypixel.hytale.procedurallib.json.DoubleRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.HeightThresholdInterpreterJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShapeEnum;
import com.hypixel.hytale.server.worldgen.loader.cave.shape.CylinderCaveNodeShapeGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.cave.shape.DistortedCaveNodeShapeGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.cave.shape.EllipsoidCaveNodeShapeGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.cave.shape.EmptyLineCaveNodeShapeGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.cave.shape.PipeCaveNodeShapeGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.cave.shape.PrefabCaveNodeShapeGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.util.ResolvedBlockArrayJsonLoader;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.ResolvedBlockArray;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CaveNodeTypeJsonLoader extends JsonLoader<SeedStringResource, CaveNodeType> {
   protected final String name;
   protected final CaveNodeTypeStorage storage;
   protected final ZoneFileContext zoneContext;

   public CaveNodeTypeJsonLoader(
      @Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, String name, CaveNodeTypeStorage storage, ZoneFileContext zoneContext
   ) {
      super(seed.append(".CaveNodeType-" + name), dataFolder, json);
      this.name = name;
      this.storage = storage;
      this.zoneContext = zoneContext;
   }

   @Nonnull
   public CaveNodeType load() {
      CaveNodeType caveNodeType = new CaveNodeType(
         this.name,
         this.loadPrefabs(),
         this.loadFillings(),
         this.loadShapeGenerator(),
         this.loadHeightCondition(),
         this.loadChildCountBounds(),
         this.loadCovers(),
         this.loadPriority(),
         this.loadEnvironment()
      );
      this.storage.add(this.name, caveNodeType);
      caveNodeType.setChildren(this.loadChildren());
      return caveNodeType;
   }

   @Nonnull
   protected CaveNodeType.CaveNodeChildEntry[] loadChildren() {
      if (this.has("Children")) {
         JsonElement childrenElement = this.get("Children");
         if (childrenElement.isJsonArray()) {
            JsonArray childrenArray = childrenElement.getAsJsonArray();
            CaveNodeType.CaveNodeChildEntry[] children = new CaveNodeType.CaveNodeChildEntry[childrenArray.size()];

            for (int i = 0; i < childrenArray.size(); i++) {
               children[i] = new CaveNodeChildEntryJsonLoader(
                     this.seed.append(String.format(".Child-%s", i)), this.dataFolder, this.getOrLoad(childrenArray.get(i)), this.storage
                  )
                  .load();
            }

            return children;
         }

         if (childrenElement.isJsonObject()) {
            return new CaveNodeType.CaveNodeChildEntry[]{
               new CaveNodeChildEntryJsonLoader(this.seed.append(String.format(".Child-%s", 0)), this.dataFolder, this.getOrLoad(childrenElement), this.storage)
                  .load()
            };
         }
      }

      return CaveNodeType.CaveNodeChildEntry.EMPTY_ARRAY;
   }

   @Nullable
   protected CavePrefabContainer loadPrefabs() {
      CavePrefabContainer container = null;
      if (this.has("Prefabs")) {
         ZoneFileContext context = this.zoneContext.matchContext(this.json, "Prefabs");
         container = new CavePrefabContainerJsonLoader(this.seed, this.dataFolder, this.get("Prefabs"), context).load();
      }

      return container;
   }

   @Nonnull
   protected IWeightedMap<BlockFluidEntry> loadFillings() {
      WeightedMap.Builder<BlockFluidEntry> builder = WeightedMap.builder(BlockFluidEntry.EMPTY_ARRAY);
      JsonElement fillingElement = this.get("Filling");
      if (fillingElement == null || fillingElement.isJsonNull()) {
         builder.put(new BlockFluidEntry(0, 0, 0), 1.0);
      } else if (fillingElement.isJsonObject()) {
         JsonObject fillingObject = fillingElement.getAsJsonObject();
         JsonArray blockArray = fillingObject.getAsJsonArray("Types");
         JsonArray weightArray = fillingObject.getAsJsonArray("Weight");
         ResolvedBlockArray blocks = new ResolvedBlockArrayJsonLoader(this.seed, this.dataFolder, blockArray).load();

         for (int i = 0; i < blockArray.size(); i++) {
            builder.put(blocks.getEntries()[i], weightArray.get(i).getAsDouble());
         }
      } else if (fillingElement.isJsonArray()) {
         JsonArray blockArray = fillingElement.getAsJsonArray();
         ResolvedBlockArray blocks = new ResolvedBlockArrayJsonLoader(this.seed, this.dataFolder, blockArray).load();

         for (int i = 0; i < blockArray.size(); i++) {
            builder.put(blocks.getEntries()[i], 1.0);
         }
      } else {
         BlockPattern.BlockEntry key = BlockPattern.BlockEntry.decode(fillingElement.getAsString());
         int index = BlockType.getAssetMap().getIndex(key.blockTypeKey());
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         builder.put(new BlockFluidEntry(index, key.rotation(), 0), 1.0);
      }

      return builder.build();
   }

   @Nonnull
   protected CaveNodeShapeEnum.CaveNodeShapeGenerator loadShapeGenerator() {
      if (this.has("Type")) {
         JsonElement typeElement = this.get("Type");
         String typeString = typeElement.getAsString();

         try {
            return (CaveNodeShapeEnum.CaveNodeShapeGenerator)(switch (CaveNodeShapeEnum.valueOf(typeString.toUpperCase())) {
               case PIPE -> new PipeCaveNodeShapeGeneratorJsonLoader(this.seed, this.dataFolder, this.json).load();
               case CYLINDER -> new CylinderCaveNodeShapeGeneratorJsonLoader(this.seed, this.dataFolder, this.json).load();
               case PREFAB -> new PrefabCaveNodeShapeGeneratorJsonLoader(this.seed, this.dataFolder, this.json).load();
               case ELLIPSOID -> new EllipsoidCaveNodeShapeGeneratorJsonLoader(this.seed, this.dataFolder, this.json).load();
               case EMPTY_LINE -> new EmptyLineCaveNodeShapeGeneratorJsonLoader(this.seed, this.dataFolder, this.json).load();
               case DISTORTED -> new DistortedCaveNodeShapeGeneratorJsonLoader(this.seed, this.dataFolder, this.json).load();
            });
         } catch (Throwable var4) {
            throw new Error(String.format("Could not find Shape by the name %s in Json: %s", typeString, typeElement), var4);
         }
      } else {
         return new PipeCaveNodeShapeGeneratorJsonLoader(this.seed, this.dataFolder, this.json).load();
      }
   }

   @Nonnull
   protected ICoordinateCondition loadHeightCondition() {
      ICoordinateCondition heightCondition = DefaultCoordinateCondition.DEFAULT_TRUE;
      if (this.has("HeightThreshold")) {
         IHeightThresholdInterpreter interpreter = new HeightThresholdInterpreterJsonLoader<>(this.seed, this.dataFolder, this.get("HeightThreshold"), 320)
            .load();
         heightCondition = new HeightThresholdCoordinateCondition(interpreter);
      }

      return heightCondition;
   }

   @Nullable
   protected IDoubleRange loadChildCountBounds() {
      IDoubleRange bounds = null;
      if (this.has("ChildCountBounds")) {
         bounds = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("ChildCountBounds")).load();
      }

      return bounds;
   }

   @Nonnull
   protected CaveNodeType.CaveNodeCoverEntry[] loadCovers() {
      CaveNodeType.CaveNodeCoverEntry[] entries = CaveNodeType.CaveNodeCoverEntry.EMPTY_ARRAY;
      if (this.has("Cover")) {
         JsonElement coverElement = this.get("Cover");
         if (coverElement.isJsonArray()) {
            JsonArray coverArray = coverElement.getAsJsonArray();
            entries = new CaveNodeType.CaveNodeCoverEntry[coverArray.size()];

            for (int i = 0; i < entries.length; i++) {
               entries[i] = new CaveNodeCoverEntryJsonLoader(this.seed.append(String.format("-cover#%s", i)), this.dataFolder, coverArray.get(i)).load();
            }
         } else {
            entries = new CaveNodeType.CaveNodeCoverEntry[]{new CaveNodeCoverEntryJsonLoader(this.seed, this.dataFolder, coverElement).load()};
         }
      }

      return entries;
   }

   protected int loadPriority() {
      int priority = 0;
      if (this.has("Priority")) {
         priority = this.get("Priority").getAsInt();
      }

      return priority;
   }

   protected int loadEnvironment() {
      int environment = Integer.MIN_VALUE;
      if (this.has("Environment")) {
         String environmentId = this.get("Environment").getAsString();
         environment = Environment.getAssetMap().getIndex(environmentId);
         if (environment == Integer.MIN_VALUE) {
            throw new Error(String.format("Error while looking up environment \"%s\"!", environmentId));
         }
      }

      return environment;
   }

   public interface Constants {
      String KEY_CHILDREN = "Children";
      String KEY_PREFABS = "Prefabs";
      String KEY_FILLING = "Filling";
      String KEY_FILLING_TYPES = "Types";
      String KEY_FILLING_WEIGHT = "Weight";
      String KEY_TYPE = "Type";
      String KEY_HEIGHT_THRESHOLDS = "HeightThreshold";
      String KEY_CHILD_COUNT_BOUNDS = "ChildCountBounds";
      String KEY_COVER = "Cover";
      String KEY_PRIORITY = "Priority";
      String KEY_ENVIRONMENT = "Environment";
      String SEED_COVER_SUFFIX = "-cover#%s";
      String SEED_CHILD_ENTRY_SUFFIX = ".Child-%s";
      String ERROR_UNKNOWN_SHAPE_NAME = "Could not find Shape by the name %s in Json: %s";
      String ERROR_UNKOWN_CONSTRUCTOR_NODE_SHAPE = "Could not find Constructor for %s CaveNodeShape";
      String ERROR_LOADING_ENVIRONMENT = "Error while looking up environment \"%s\"!";
   }
}
