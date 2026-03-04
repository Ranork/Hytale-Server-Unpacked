package com.hypixel.hytale.builtin.path;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.path.WorldPath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;

public class WorldPathBuilder implements Component<EntityStore> {
   private WorldPath path;

   public static ComponentType<EntityStore, WorldPathBuilder> getComponentType() {
      return PathPlugin.get().getWorldPathBuilderComponentType();
   }

   public WorldPath getPath() {
      return this.path;
   }

   public void setPath(WorldPath path) {
      this.path = path;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      WorldPathBuilder builder = new WorldPathBuilder();
      builder.path = new WorldPath(this.path.getName(), List.copyOf(this.path.getWaypoints()));
      return builder;
   }
}
