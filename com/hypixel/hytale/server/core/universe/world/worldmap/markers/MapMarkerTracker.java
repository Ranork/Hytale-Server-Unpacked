package com.hypixel.hytale.server.core.universe.world.worldmap.markers;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMap;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MapMarkerTracker {
   private final WorldMapTracker worldMapTracker;
   private final Player player;
   private final Map<String, MapMarker> sentToClientById = new ConcurrentHashMap<>();
   public static final float SMALL_MOVEMENTS_UPDATE_INTERVAL = 10.0F;
   private float smallMovementsTimer;
   private Predicate<PlayerRef> playerMapFilter;
   @Nonnull
   private final Set<String> tempToRemove = new HashSet<>();
   @Nonnull
   private final Set<MapMarker> tempToAdd = new HashSet<>();
   @Nonnull
   private final Set<String> tempTestedMarkers = new HashSet<>();

   public MapMarkerTracker(WorldMapTracker worldMapTracker) {
      this.worldMapTracker = worldMapTracker;
      this.player = worldMapTracker.getPlayer();
   }

   public Player getPlayer() {
      return this.player;
   }

   public Map<String, MapMarker> getSentMarkers() {
      return this.sentToClientById;
   }

   public Predicate<PlayerRef> getPlayerMapFilter() {
      return this.playerMapFilter;
   }

   public void setPlayerMapFilter(Predicate<PlayerRef> playerMapFilter) {
      this.playerMapFilter = playerMapFilter;
   }

   private boolean isSendingSmallMovements() {
      return this.smallMovementsTimer <= 0.0F;
   }

   private void resetSmallMovementTimer() {
      this.smallMovementsTimer = 10.0F;
   }

   public void updatePointsOfInterest(float dt, @Nonnull World world, int chunkViewRadius, int playerChunkX, int playerChunkZ) {
      if (this.worldMapTracker.getTransformComponent() != null) {
         this.smallMovementsTimer -= dt;
         WorldMapManager worldMapManager = world.getWorldMapManager();
         Map<String, WorldMapManager.MarkerProvider> markerProviders = worldMapManager.getMarkerProviders();
         this.tempToAdd.clear();
         this.tempTestedMarkers.clear();
         MarkersCollectorImpl markersCollector = new MarkersCollectorImpl(this, chunkViewRadius, playerChunkX, playerChunkZ);

         for (WorldMapManager.MarkerProvider provider : markerProviders.values()) {
            provider.update(world, this.player, markersCollector);
         }

         if (this.isSendingSmallMovements()) {
            this.resetSmallMovementTimer();
         }

         this.tempToRemove.clear();
         this.tempToRemove.addAll(this.sentToClientById.keySet());
         if (!this.tempTestedMarkers.isEmpty()) {
            this.tempToRemove.removeAll(this.tempTestedMarkers);
         }

         for (String removedMarkerId : this.tempToRemove) {
            this.sentToClientById.remove(removedMarkerId);
         }

         if (!this.tempToAdd.isEmpty() || !this.tempToRemove.isEmpty()) {
            MapMarker[] addedMarkers = !this.tempToAdd.isEmpty() ? this.tempToAdd.toArray(MapMarker[]::new) : null;
            String[] removedMarkers = !this.tempToRemove.isEmpty() ? this.tempToRemove.toArray(String[]::new) : null;
            this.player.getPlayerConnection().writeNoCache(new UpdateWorldMap(null, addedMarkers, removedMarkers));
         }
      }
   }

   public void sendMapMarker(MapMarker marker) {
      this.tempTestedMarkers.add(marker.id);
      MapMarker oldMarker = this.sentToClientById.get(marker.id);
      if (this.doesMarkerNeedNetworkUpdate(oldMarker, marker)) {
         this.sentToClientById.put(marker.id, marker);
         this.tempToAdd.add(marker);
      }
   }

   private boolean doesMarkerNeedNetworkUpdate(@Nullable MapMarker oldMarker, MapMarker newMarker) {
      if (oldMarker == null) {
         return true;
      } else if (!Objects.equals(oldMarker.name, newMarker.name)) {
         return true;
      } else if (!Objects.equals(oldMarker.customName, newMarker.customName)) {
         return true;
      } else {
         double yawDistance = Math.abs(oldMarker.transform.orientation.yaw - newMarker.transform.orientation.yaw);
         if (!(yawDistance > 0.05) && (!this.isSendingSmallMovements() || !(yawDistance > 0.001))) {
            Position oldPosition = oldMarker.transform.position;
            Position newPosition = newMarker.transform.position;
            double distanceSq = Vector3d.distanceSquared(oldPosition.x, oldPosition.y, oldPosition.z, newPosition.x, newPosition.y, newPosition.z);
            return distanceSq > 25.0 || this.isSendingSmallMovements() && distanceSq > 0.01;
         } else {
            return true;
         }
      }
   }

   public void copyFrom(@Nonnull MapMarkerTracker other) {
      for (Entry<String, MapMarker> entry : other.sentToClientById.entrySet()) {
         this.sentToClientById.put(entry.getKey(), new MapMarker(entry.getValue()));
      }
   }
}
