package com.hypixel.hytale.server.worldgen.prefab;

import com.hypixel.hytale.server.core.prefab.PrefabStore;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public enum PrefabStoreRoot {
   ASSETS,
   WORLD_GEN;

   public static final PrefabStoreRoot DEFAULT = WORLD_GEN;

   @Nonnull
   public static Path resolvePrefabStore(@Nonnull PrefabStoreRoot store, @Nonnull Path dataFolder) {
      return switch (store) {
         case ASSETS -> PrefabStore.get().getAssetPrefabsPath();
         case WORLD_GEN -> dataFolder.resolve("Prefabs");
      };
   }
}
