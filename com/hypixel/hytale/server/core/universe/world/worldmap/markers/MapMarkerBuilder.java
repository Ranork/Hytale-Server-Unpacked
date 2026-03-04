package com.hypixel.hytale.server.core.universe.world.worldmap.markers;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.worldmap.ContextMenuItem;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarkerComponent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.util.PositionUtil;
import java.util.ArrayList;
import java.util.List;

public class MapMarkerBuilder {
   private final String id;
   private final String image;
   private final Transform transform;
   private Message name;
   private String customName;
   private List<ContextMenuItem> contextMenuItems;
   private List<MapMarkerComponent> mapMarkerComponents;

   public MapMarkerBuilder(String id, String image, Transform transform) {
      this.id = id;
      this.image = image;
      this.transform = transform;
   }

   public MapMarkerBuilder withName(Message name) {
      this.name = name;
      return this;
   }

   public MapMarkerBuilder withCustomName(String customName) {
      this.customName = customName;
      return this;
   }

   public MapMarkerBuilder withContextMenuItem(ContextMenuItem contextMenuItem) {
      if (this.contextMenuItems == null) {
         this.contextMenuItems = new ArrayList<>();
      }

      this.contextMenuItems.add(contextMenuItem);
      return this;
   }

   public MapMarkerBuilder withComponent(MapMarkerComponent component) {
      if (this.mapMarkerComponents == null) {
         this.mapMarkerComponents = new ArrayList<>();
      }

      this.mapMarkerComponents.add(component);
      return this;
   }

   public MapMarker build() {
      return new MapMarker(
         this.id,
         this.name == null ? null : this.name.getFormattedMessage(),
         this.customName,
         this.image,
         PositionUtil.toTransformPacket(this.transform),
         this.contextMenuItems == null ? null : this.contextMenuItems.toArray(ContextMenuItem[]::new),
         this.mapMarkerComponents == null ? null : this.mapMarkerComponents.toArray(MapMarkerComponent[]::new)
      );
   }
}
