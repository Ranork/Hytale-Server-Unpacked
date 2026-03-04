package com.hypixel.hytale.server.core.universe.datastore;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;

public interface DataStoreProvider {
   BuilderCodecMapCodec<DataStoreProvider> CODEC = new BuilderCodecMapCodec<>("Type");

   <T> DataStore<T> create(BuilderCodec<T> var1);
}
