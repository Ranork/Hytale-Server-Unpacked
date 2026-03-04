package com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class WorldMarkersResource implements Resource<ChunkStore>, UserMapMarkersStore {
   public static final BuilderCodec<WorldMarkersResource> CODEC = BuilderCodec.builder(WorldMarkersResource.class, WorldMarkersResource::new)
      .append(
         new KeyedCodec<>("UserMarkers", UserMapMarker.ARRAY_CODEC),
         (res, value) -> res.mapMarkersById = Arrays.stream(value).collect(Collectors.toConcurrentMap(UserMapMarker::getId, m -> (UserMapMarker)m)),
         res -> res.getUserMapMarkers().toArray(new UserMapMarker[0])
      )
      .documentation("The stored map markers submitted by this player.")
      .add()
      .build();
   private Map<String, UserMapMarker> mapMarkersById = new ConcurrentHashMap<>();

   public static ResourceType<ChunkStore, WorldMarkersResource> getResourceType() {
      return Universe.get().getWorldMarkersResourceType();
   }

   @NonNullDecl
   @Override
   public Collection<? extends UserMapMarker> getUserMapMarkers() {
      return this.mapMarkersById.values();
   }

   @NonNullDecl
   @Override
   public Collection<? extends UserMapMarker> getUserMapMarkers(UUID createdByUuid) {
      List<UserMapMarker> filtered = new ArrayList<>();

      for (UserMapMarker marker : this.mapMarkersById.values()) {
         if (createdByUuid.equals(marker.getCreatedByUuid())) {
            filtered.add(marker);
         }
      }

      return filtered;
   }

   @Override
   public void setUserMapMarkers(@NullableDecl Collection<? extends UserMapMarker> markers) {
      this.mapMarkersById = (Map<String, UserMapMarker>)(markers == null
         ? new ConcurrentHashMap<>()
         : markers.stream().collect(Collectors.toConcurrentMap(UserMapMarker::getId, x -> (UserMapMarker)x)));
   }

   @NullableDecl
   @Override
   public UserMapMarker getUserMapMarker(String markerId) {
      return this.mapMarkersById.get(markerId);
   }

   @NullableDecl
   @Override
   public Resource<ChunkStore> clone() {
      WorldMarkersResource clone = new WorldMarkersResource();
      clone.mapMarkersById = new ConcurrentHashMap<>(this.mapMarkersById);
      return clone;
   }
}
