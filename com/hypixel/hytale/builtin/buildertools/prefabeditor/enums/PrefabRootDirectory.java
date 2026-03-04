package com.hypixel.hytale.builtin.buildertools.prefabeditor.enums;

import com.hypixel.hytale.server.core.prefab.PrefabStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public enum PrefabRootDirectory {
   SERVER(() -> PrefabStore.get().getServerPrefabsPath(), "server.commands.editprefab.ui.rootDirectory.server", false),
   ASSET(() -> PrefabStore.get().getAssetPrefabsPath(), "server.commands.editprefab.ui.rootDirectory.asset", true),
   WORLDGEN(() -> PrefabStore.get().getWorldGenPrefabsPath(), "server.commands.editprefab.ui.rootDirectory.worldGen", false),
   ASSET_ROOT(() -> PrefabStore.get().getAssetRootPath(), "server.commands.editprefab.ui.rootDirectory.assetRoot", false);

   private final Supplier<Path> prefabPath;
   private final String localizationString;
   private final boolean supportsMultiPack;

   private PrefabRootDirectory(Supplier<Path> prefabPath, String localizationString, boolean supportsMultiPack) {
      this.prefabPath = prefabPath;
      this.localizationString = localizationString;
      this.supportsMultiPack = supportsMultiPack;
   }

   public Path getPrefabPath() {
      return this.prefabPath.get();
   }

   public String getLocalizationString() {
      return this.localizationString;
   }

   public boolean supportsMultiPack() {
      return this.supportsMultiPack;
   }

   @Nonnull
   public List<PrefabStore.AssetPackPrefabPath> getAllPrefabPaths() {
      if (this.supportsMultiPack) {
         return PrefabStore.get().getAllAssetPrefabPaths();
      } else {
         List<PrefabStore.AssetPackPrefabPath> result = new ObjectArrayList(1);
         result.add(new PrefabStore.AssetPackPrefabPath(null, this.getPrefabPath()));
         return result;
      }
   }
}
