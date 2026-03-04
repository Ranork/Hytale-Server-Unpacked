package com.hypixel.hytale.server.worldgen.loader.cave.shape;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.shape.PrefabCaveNodeShape;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabLoader;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.loader.prefab.BlockPlacementMaskJsonLoader;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabCaveNodeShapeGeneratorJsonLoader extends CaveNodeShapeGeneratorJsonLoader {
   public PrefabCaveNodeShapeGeneratorJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".PrefabCaveNodeShapeGenerator"), dataFolder, json);
   }

   @Nonnull
   public PrefabCaveNodeShape.PrefabCaveNodeShapeGenerator load() {
      return new PrefabCaveNodeShape.PrefabCaveNodeShapeGenerator(this.loadPrefabs(), this.loadMask());
   }

   @Nonnull
   protected List<WorldGenPrefabSupplier> loadPrefabs() {
      WorldGenPrefabLoader loader = this.seed.get().getLoader();
      List<WorldGenPrefabSupplier> prefabs = new ArrayList<>();
      JsonElement prefabElement = this.get("Prefab");
      if (prefabElement.isJsonArray()) {
         for (JsonElement prefabArrayElement : prefabElement.getAsJsonArray()) {
            String prefabString = prefabArrayElement.getAsString();
            Collections.addAll(prefabs, loader.get(prefabString));
         }
      } else {
         String prefabString = prefabElement.getAsString();
         Collections.addAll(prefabs, loader.get(prefabString));
      }

      if (prefabs.isEmpty()) {
         throw new IllegalArgumentException("Prefabs are empty! Key: Prefab");
      } else {
         return prefabs;
      }
   }

   @Nullable
   protected BlockMaskCondition loadMask() {
      BlockMaskCondition configuration = BlockMaskCondition.DEFAULT_TRUE;
      if (this.has("Mask")) {
         configuration = new BlockPlacementMaskJsonLoader(this.seed, this.dataFolder, this.getRaw("Mask")).load();
      }

      return configuration;
   }

   public interface Constants {
      String KEY_PREFAB = "Prefab";
      String KEY_MASK = "Mask";
   }
}
