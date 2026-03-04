package com.hypixel.hytale.server.core.universe.world.meta.state;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class BlockMapMarkersResource implements Resource<ChunkStore> {
   public static final BuilderCodec<BlockMapMarkersResource> CODEC = BuilderCodec.builder(BlockMapMarkersResource.class, BlockMapMarkersResource::new)
      .append(new KeyedCodec<>("Markers", new MapCodec<>(BlockMapMarkersResource.BlockMapMarkerData.CODEC, HashMap::new), true), (o, markersMap) -> {
         if (markersMap != null) {
            for (Entry<String, BlockMapMarkersResource.BlockMapMarkerData> entry : markersMap.entrySet()) {
               o.markers.put(Long.valueOf(entry.getKey()), entry.getValue());
            }
         }
      }, o -> {
         HashMap<String, BlockMapMarkersResource.BlockMapMarkerData> returnMap = new HashMap<>(o.markers.size());
         Iterator i$ = o.markers.entrySet().iterator();

         while (i$.hasNext()) {
            Entry<Long, BlockMapMarkersResource.BlockMapMarkerData> entry = (Entry<Long, BlockMapMarkersResource.BlockMapMarkerData>)i$.next();
            returnMap.put(String.valueOf(entry.getKey()), entry.getValue());
         }

         return returnMap;
      })
      .add()
      .build();
   private Long2ObjectMap<BlockMapMarkersResource.BlockMapMarkerData> markers = new Long2ObjectOpenHashMap();

   public BlockMapMarkersResource() {
   }

   public BlockMapMarkersResource(Long2ObjectMap<BlockMapMarkersResource.BlockMapMarkerData> markers) {
      this.markers = markers;
   }

   public static ResourceType<ChunkStore, BlockMapMarkersResource> getResourceType() {
      return BlockModule.get().getBlockMapMarkersResourceType();
   }

   @Nonnull
   public Long2ObjectMap<BlockMapMarkersResource.BlockMapMarkerData> getMarkers() {
      return this.markers;
   }

   public void addMarker(@Nonnull Vector3i position, @Nonnull String name, @Nonnull String icon) {
      long key = BlockUtil.pack(position);
      this.markers.put(key, new BlockMapMarkersResource.BlockMapMarkerData(position, name, icon, UUID.randomUUID().toString()));
   }

   public void removeMarker(@Nonnull Vector3i position) {
      long key = BlockUtil.pack(position);
      this.markers.remove(key);
   }

   @Override
   public Resource<ChunkStore> clone() {
      return new BlockMapMarkersResource(new Long2ObjectOpenHashMap(this.markers));
   }

   public static class BlockMapMarkerData {
      public static final BuilderCodec<BlockMapMarkersResource.BlockMapMarkerData> CODEC = BuilderCodec.builder(
            BlockMapMarkersResource.BlockMapMarkerData.class, BlockMapMarkersResource.BlockMapMarkerData::new
         )
         .append(new KeyedCodec<>("Position", Vector3i.CODEC), (o, v) -> o.position = v, o -> o.position)
         .add()
         .append(new KeyedCodec<>("Name", Codec.STRING), (o, v) -> o.name = v, o -> o.name)
         .add()
         .append(new KeyedCodec<>("Icon", Codec.STRING), (o, v) -> o.icon = v, o -> o.icon)
         .add()
         .append(new KeyedCodec<>("MarkerId", Codec.STRING), (o, v) -> o.markerId = v, o -> o.markerId)
         .add()
         .build();
      private Vector3i position;
      private String name;
      private String icon;
      private String markerId;

      public BlockMapMarkerData() {
      }

      public BlockMapMarkerData(Vector3i position, String name, String icon, String markerId) {
         this.position = position;
         this.name = name;
         this.icon = icon;
         this.markerId = markerId;
      }

      public Vector3i getPosition() {
         return this.position;
      }

      public String getName() {
         return this.name;
      }

      public String getIcon() {
         return this.icon;
      }

      public String getMarkerId() {
         return this.markerId;
      }
   }
}
