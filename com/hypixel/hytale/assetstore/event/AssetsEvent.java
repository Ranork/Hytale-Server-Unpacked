package com.hypixel.hytale.assetstore.event;

import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.event.IEvent;

public abstract class AssetsEvent<K, T extends JsonAsset<K>> implements IEvent<Class<T>> {
}
