package com.hypixel.hytale.server.core.universe.world.worldmap.markers;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public interface MarkersCollector {
   void add(MapMarker var1);

   void addIgnoreViewDistance(MapMarker var1);

   @Deprecated
   @Nullable
   Predicate<PlayerRef> getPlayerMapFilter();

   default boolean isInViewDistance(Transform transform) {
      return this.isInViewDistance(transform.getPosition());
   }

   default boolean isInViewDistance(Vector3d position) {
      return this.isInViewDistance(position.x, position.z);
   }

   boolean isInViewDistance(double var1, double var3);
}
