package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.server.core.asset.AssetModule;
import java.nio.file.Path;

public class AssetUtil {
   @Deprecated(forRemoval = true)
   public static Path getHytaleAssetsPath() {
      return AssetModule.get().getBaseAssetPack().getRoot();
   }
}
