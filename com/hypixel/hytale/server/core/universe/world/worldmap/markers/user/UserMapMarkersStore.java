package com.hypixel.hytale.server.core.universe.world.worldmap.markers.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface UserMapMarkersStore {
   @Nonnull
   Collection<? extends UserMapMarker> getUserMapMarkers();

   @Nonnull
   Collection<? extends UserMapMarker> getUserMapMarkers(UUID var1);

   void setUserMapMarkers(@Nullable Collection<? extends UserMapMarker> var1);

   default void addUserMapMarker(UserMapMarker marker) {
      List<UserMapMarker> markers = new ArrayList<>(this.getUserMapMarkers());
      markers.add(marker);
      this.setUserMapMarkers(markers);
   }

   default void removeUserMapMarker(String markerId) {
      List<UserMapMarker> markers = new ArrayList<>(this.getUserMapMarkers());
      markers.removeIf(marker -> markerId.equals(marker.getId()));
      this.setUserMapMarkers(markers);
   }

   @Nullable
   UserMapMarker getUserMapMarker(String var1);
}
