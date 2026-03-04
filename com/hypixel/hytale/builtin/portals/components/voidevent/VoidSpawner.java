package com.hypixel.hytale.builtin.portals.components.voidevent;

import com.hypixel.hytale.builtin.portals.PortalsPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public class VoidSpawner implements Component<EntityStore> {
   private List<UUID> spawnBeaconUuids = new ObjectArrayList();

   public static ComponentType<EntityStore, VoidSpawner> getComponentType() {
      return PortalsPlugin.getInstance().getVoidPortalComponentType();
   }

   public List<UUID> getSpawnBeaconUuids() {
      return this.spawnBeaconUuids;
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      VoidSpawner clone = new VoidSpawner();
      clone.spawnBeaconUuids = this.spawnBeaconUuids;
      return clone;
   }
}
