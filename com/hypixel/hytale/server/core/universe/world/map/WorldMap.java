package com.hypixel.hytale.server.core.universe.world.map;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.worldmap.MapChunk;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMap;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.util.PositionUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Map;
import javax.annotation.Nonnull;

public class WorldMap implements NetworkSerializable<UpdateWorldMap> {
   private final Map<String, MapMarker> pointsOfInterest = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Long2ObjectMap<MapImage> chunks;
   private UpdateWorldMap packet;

   public WorldMap(int chunks) {
      this.chunks = new Long2ObjectOpenHashMap(chunks);
   }

   @Nonnull
   public Map<String, MapMarker> getPointsOfInterest() {
      return this.pointsOfInterest;
   }

   @Nonnull
   public Long2ObjectMap<MapImage> getChunks() {
      return this.chunks;
   }

   public void addPointOfInterest(String id, String name, String markerType, @Nonnull Vector3i pos) {
      this.addPointOfInterest(id, name, markerType, new Transform(pos));
   }

   public void addPointOfInterest(String id, String name, String markerType, @Nonnull Vector3d pos) {
      this.addPointOfInterest(id, name, markerType, new Transform(pos));
   }

   public void addPointOfInterest(String id, String name, String markerType, @Nonnull Transform transform) {
      MapMarker old = this.pointsOfInterest.putIfAbsent(id, new MapMarker(id, null, name, markerType, PositionUtil.toTransformPacket(transform), null, null));
      if (old != null) {
         throw new IllegalArgumentException("Id " + id + " already exists!");
      }
   }

   @Nonnull
   public UpdateWorldMap toPacket() {
      if (this.packet != null) {
         return this.packet;
      } else {
         MapChunk[] mapChunks = new MapChunk[this.chunks.size()];
         int i = 0;
         ObjectIterator var3 = this.chunks.long2ObjectEntrySet().iterator();

         while (var3.hasNext()) {
            Entry<MapImage> entry = (Entry<MapImage>)var3.next();
            long index = entry.getLongKey();
            int chunkX = ChunkUtil.xOfChunkIndex(index);
            int chunkZ = ChunkUtil.zOfChunkIndex(index);
            mapChunks[i++] = new MapChunk(chunkX, chunkZ, (MapImage)entry.getValue());
         }

         return this.packet = new UpdateWorldMap(mapChunks, this.pointsOfInterest.values().toArray(MapMarker[]::new), null);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "WorldMap{pointsOfInterest=" + this.pointsOfInterest + ", chunks=" + this.chunks + "}";
   }
}
