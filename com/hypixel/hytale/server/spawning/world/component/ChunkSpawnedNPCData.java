package com.hypixel.hytale.server.spawning.world.component;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Object2DoubleMapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry;
import java.util.Iterator;
import javax.annotation.Nonnull;

public class ChunkSpawnedNPCData implements Component<ChunkStore> {
   public static final BuilderCodec<ChunkSpawnedNPCData> CODEC = BuilderCodec.builder(ChunkSpawnedNPCData.class, ChunkSpawnedNPCData::new)
      .append(new KeyedCodec<>("EnvironmentSpawnCounts", new Object2DoubleMapCodec<>(Codec.STRING, Object2DoubleOpenHashMap::new, false)), (chunk, o) -> {
         Int2DoubleMap map = chunk.environmentSpawnCounts;
         map.clear();
         Iterator i$ = o.object2DoubleEntrySet().iterator();

         while (i$.hasNext()) {
            Entry<String> entry = (Entry<String>)i$.next();
            String key = (String)entry.getKey();
            int index = Environment.getIndexOrUnknown(key, "Failed to find environment '%s' while deserializing spawned NPC data", key);
            map.put(index, entry.getDoubleValue());
         }
      }, chunk -> {
         Object2DoubleOpenHashMap<String> map = new Object2DoubleOpenHashMap();
         IndexedLookupTableAssetMap<String, Environment> assetMap = Environment.getAssetMap();
         Iterator i$ = chunk.environmentSpawnCounts.int2DoubleEntrySet().iterator();

         while (i$.hasNext()) {
            it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry entry = (it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry)i$.next();
            Environment environment = assetMap.getAsset(entry.getIntKey());
            String key = environment != null ? environment.getId() : Environment.UNKNOWN.getId();
            map.put(key, entry.getDoubleValue());
         }

         return map;
      })
      .add()
      .build();
   private final Int2DoubleMap environmentSpawnCounts = new Int2DoubleOpenHashMap();

   public static ComponentType<ChunkStore, ChunkSpawnedNPCData> getComponentType() {
      return SpawningPlugin.get().getChunkSpawnedNPCDataComponentType();
   }

   public double getEnvironmentSpawnCount(int environment) {
      return this.environmentSpawnCounts.get(environment);
   }

   public void setEnvironmentSpawnCount(int environment, double count) {
      this.environmentSpawnCounts.put(environment, count);
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      ChunkSpawnedNPCData data = new ChunkSpawnedNPCData();
      data.environmentSpawnCounts.putAll(this.environmentSpawnCounts);
      return data;
   }
}
