package com.hypixel.hytale.assetstore.map;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.JsonAsset;

public interface JsonAssetWithMap<K, M extends AssetMap<K, ?>> extends JsonAsset<K> {
}
