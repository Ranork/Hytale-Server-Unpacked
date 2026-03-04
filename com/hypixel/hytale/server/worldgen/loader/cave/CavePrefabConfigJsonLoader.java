package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.ConstantIntCondition;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateRndCondition;
import com.hypixel.hytale.procedurallib.condition.HeightCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateRndCondition;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.json.DoubleRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.HeightThresholdInterpreterJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoiseMaskConditionJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.supplier.ConstantDoubleCoordinateHashSupplier;
import com.hypixel.hytale.procedurallib.supplier.DoubleRangeCoordinateHashSupplier;
import com.hypixel.hytale.procedurallib.supplier.IDoubleCoordinateHashSupplier;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CavePrefabPlacement;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.biome.BiomeMaskJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.prefab.BlockPlacementMaskJsonLoader;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import java.nio.file.Path;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CavePrefabConfigJsonLoader extends JsonLoader<SeedStringResource, CavePrefabContainer.CavePrefabEntry.CavePrefabConfig> {
   private final ZoneFileContext zoneContext;

   public CavePrefabConfigJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, ZoneFileContext zoneContext) {
      super(seed.append(".CavePrefabConfig"), dataFolder, json);
      this.zoneContext = zoneContext;
   }

   @Nonnull
   public CavePrefabContainer.CavePrefabEntry.CavePrefabConfig load() {
      return new CavePrefabContainer.CavePrefabEntry.CavePrefabConfig(
         this.loadRotations(),
         this.loadPlacement(),
         this.loadBiomeMask(),
         this.loadBlockMask(),
         this.loadIterations(),
         this.loadDisplacementSupplier(),
         this.loadNoiseCondition(),
         this.loadHeightCondition()
      );
   }

   @Nonnull
   protected PrefabRotation[] loadRotations() {
      PrefabRotation[] prefabRotations = PrefabRotation.VALUES;
      if (this.has("Rotations")) {
         JsonElement element = this.get("Rotations");
         if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() <= 0) {
               throw new IllegalArgumentException("Array for rotations must have at least one entry or left away to allow random rotation");
            }

            prefabRotations = new PrefabRotation[array.size()];

            for (int i = 0; i < prefabRotations.length; i++) {
               String name = array.get(i).getAsString();

               try {
                  prefabRotations[i] = PrefabRotation.valueOf(name);
               } catch (Throwable var8) {
                  throw new Error(String.format(CavePrefabConfigJsonLoader.Constants.ERROR_ROTATIONS_UNKOWN, name));
               }
            }
         } else {
            if (!element.isJsonPrimitive()) {
               throw new Error(String.format("\"Rotations\" is not an array nor a string, other types are not supported! Given: %s", element));
            }

            prefabRotations = new PrefabRotation[1];
            String name = element.getAsString();

            try {
               prefabRotations[0] = PrefabRotation.valueOf(name);
            } catch (Throwable var7) {
               throw new Error(String.format(CavePrefabConfigJsonLoader.Constants.ERROR_ROTATIONS_UNKOWN, name));
            }
         }
      }

      return prefabRotations;
   }

   @Nonnull
   protected CavePrefabPlacement loadPlacement() {
      CavePrefabPlacement placement = CavePrefabPlacement.DEFAULT;
      if (this.has("Placement")) {
         placement = CavePrefabPlacement.valueOf(this.get("Placement").getAsString());
      }

      return placement;
   }

   @Nullable
   protected IIntCondition loadBiomeMask() {
      IIntCondition mask = ConstantIntCondition.DEFAULT_TRUE;
      if (this.has("BiomeMask")) {
         ZoneFileContext context = this.zoneContext.matchContext(this.json, "BiomeMask");
         mask = new BiomeMaskJsonLoader(this.seed, this.dataFolder, this.get("BiomeMask"), "Prefab", context).load();
      }

      return mask;
   }

   @Nullable
   protected BlockMaskCondition loadBlockMask() {
      BlockMaskCondition configuration = BlockMaskCondition.DEFAULT_TRUE;
      if (this.has("Mask")) {
         configuration = new BlockPlacementMaskJsonLoader(this.seed, this.dataFolder, this.getRaw("Mask")).load();
      }

      return configuration;
   }

   @Nullable
   protected IDoubleRange loadIterations() {
      return new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Iterations"), 5.0).load();
   }

   @Nonnull
   protected IDoubleCoordinateHashSupplier loadDisplacementSupplier() {
      IDoubleCoordinateHashSupplier array = ConstantDoubleCoordinateHashSupplier.ZERO;
      if (this.has("Displacement")) {
         array = new DoubleRangeCoordinateHashSupplier(new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Displacement"), 0.0).load());
      }

      return array;
   }

   @Nonnull
   protected ICoordinateCondition loadNoiseCondition() {
      return new NoiseMaskConditionJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
   }

   @Nonnull
   protected ICoordinateRndCondition loadHeightCondition() {
      ICoordinateRndCondition condition = DefaultCoordinateRndCondition.DEFAULT_TRUE;
      if (this.has("HeightThreshold")) {
         condition = new HeightCondition(new HeightThresholdInterpreterJsonLoader<>(this.seed, this.dataFolder, this.get("HeightThreshold"), 320).load());
      }

      return condition;
   }

   public interface Constants {
      String KEY_ROTATIONS = "Rotations";
      String KEY_PLACEMENT = "Placement";
      String KEY_BIOME_MASK = "BiomeMask";
      String KEY_BLOCK_MASK = "Mask";
      String KEY_ITERATIONS = "Iterations";
      String KEY_DISPLACEMENT = "Displacement";
      String KEY_NOISE_MASK = "NoiseMask";
      String KEY_HEIGHT_THRESHOLD = "HeightThreshold";
      String SEED_STRING_BIOME_MASK_TYPE = "Prefab";
      String ERROR_ROTATIONS_MUST_POSITIVE = "Array for rotations must have at least one entry or left away to allow random rotation";
      String ERROR_ROTATIONS_UNKOWN = "Could not find rotation \"%s\". Allowed: " + Arrays.toString((Object[])PrefabRotation.VALUES);
      String ERROR_ROTATIONS_UNKOWN_TYPE = "\"Rotations\" is not an array nor a string, other types are not supported! Given: %s";
   }
}
