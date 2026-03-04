package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import java.awt.Color;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractAssetArgumentType<DataType extends JsonAssetWithMap<AssetKeyType, M>, M extends AssetMap<AssetKeyType, DataType>, AssetKeyType>
   extends SingleArgumentType<DataType> {
   @Nonnull
   private final Class<DataType> dataTypeClass;

   public AbstractAssetArgumentType(@Nonnull String name, @Nonnull Class<DataType> type, @Nonnull String argumentUsage) {
      super(name, argumentUsage);
      this.dataTypeClass = type;
   }

   @Nullable
   public DataType parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
      M assetMap = this.getAssetMap();
      AssetKeyType assetKey = this.getAssetKey(input);
      if (assetKey == null) {
         parseResult.fail(
            Message.translation("server.commands.notfound").param("type", this.dataTypeClass.getSimpleName()).param("id", input).color(Color.RED),
            Message.translation("server.general.failed.didYouMean")
               .param("choices", StringUtil.sortByFuzzyDistance(input, assetMap.getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT).toString())
         );
         return null;
      } else {
         DataType asset = assetMap.getAsset(assetKey);
         if (asset == null) {
            parseResult.fail(
               Message.translation("server.commands.notfound").param("type", this.dataTypeClass.getSimpleName()).param("id", input).color(Color.RED),
               Message.translation("server.general.failed.didYouMean")
                  .param("choices", StringUtil.sortByFuzzyDistance(input, assetMap.getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT).toString())
            );
            return null;
         } else {
            return asset;
         }
      }
   }

   @Nullable
   public abstract AssetKeyType getAssetKey(@Nonnull String var1);

   @Nonnull
   public M getAssetMap() {
      return (M)AssetRegistry.getAssetStore(this.dataTypeClass).getAssetMap();
   }
}
