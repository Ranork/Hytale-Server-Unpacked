package com.hypixel.hytale.builtin.adventure.npcobjectives.resources;

import com.hypixel.hytale.builtin.adventure.npcobjectives.NPCObjectivesPlugin;
import com.hypixel.hytale.builtin.adventure.npcobjectives.transaction.KillTaskTransaction;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class KillTrackerResource implements Resource<EntityStore> {
   @Nonnull
   private final List<KillTaskTransaction> killTasks = new ObjectArrayList();

   public static ResourceType<EntityStore, KillTrackerResource> getResourceType() {
      return NPCObjectivesPlugin.get().getKillTrackerResourceType();
   }

   public void watch(@Nonnull KillTaskTransaction task) {
      this.killTasks.add(task);
   }

   public void unwatch(@Nonnull KillTaskTransaction task) {
      this.killTasks.remove(task);
   }

   @Nonnull
   public List<KillTaskTransaction> getKillTasks() {
      return this.killTasks;
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      return new KillTrackerResource();
   }
}
