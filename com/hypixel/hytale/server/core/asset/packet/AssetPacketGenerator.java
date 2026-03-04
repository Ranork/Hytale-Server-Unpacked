package com.hypixel.hytale.server.core.asset.packet;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AssetPacketGenerator<K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> {
   public abstract ToClientPacket generateInitPacket(M var1, Map<K, T> var2);

   public abstract ToClientPacket generateUpdatePacket(M var1, Map<K, T> var2, @Nonnull AssetUpdateQuery var3);

   @Nullable
   public abstract ToClientPacket generateRemovePacket(M var1, Set<K> var2, @Nonnull AssetUpdateQuery var3);
}
