package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import javax.annotation.Nonnull;

public class AssetArgumentType<DataType extends JsonAssetWithMap<String, M>, M extends AssetMap<String, DataType>>
   extends AbstractAssetArgumentType<DataType, M, String> {
   public AssetArgumentType(String name, Class<DataType> type, @Nonnull String argumentUsage) {
      super(name, type, argumentUsage);
   }

   public String getAssetKey(@Nonnull String input) {
      return input;
   }
}
