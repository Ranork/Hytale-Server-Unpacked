package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.NonTicking;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Set;
import javax.annotation.Nonnull;

public class NewSpawnStartTickingSystem extends TickingSystem<EntityStore> {
   @Nonnull
   private final ResourceType<EntityStore, NewSpawnStartTickingSystem.QueueResource> queueResourceType;
   @Nonnull
   private final ComponentType<EntityStore, NonTicking<EntityStore>> nonTickingComponentType;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.AFTER, StepCleanupSystem.class));

   public NewSpawnStartTickingSystem(@Nonnull ResourceType<EntityStore, NewSpawnStartTickingSystem.QueueResource> queueResourceType) {
      this.queueResourceType = queueResourceType;
      this.nonTickingComponentType = EntityStore.REGISTRY.getNonTickingComponentType();
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      ObjectList<Ref<EntityStore>> queue = store.getResource(this.queueResourceType).queue;
      if (!queue.isEmpty()) {
         ObjectListIterator var5 = queue.iterator();

         while (var5.hasNext()) {
            Ref<EntityStore> reference = (Ref<EntityStore>)var5.next();
            if (reference.isValid()) {
               store.removeComponent(reference, this.nonTickingComponentType);
            }
         }

         queue.clear();
      }
   }

   public static void queueNewSpawn(@Nonnull Ref<EntityStore> reference, @Nonnull Store<EntityStore> store) {
      store.ensureComponent(reference, EntityStore.REGISTRY.getNonTickingComponentType());
      store.getResource(NewSpawnStartTickingSystem.QueueResource.getResourceType()).queue.add(reference);
   }

   public static class QueueResource implements Resource<EntityStore> {
      @Nonnull
      private final ObjectList<Ref<EntityStore>> queue = new ObjectArrayList();

      @Nonnull
      public static ResourceType<EntityStore, NewSpawnStartTickingSystem.QueueResource> getResourceType() {
         return NPCPlugin.get().getNewSpawnStartTickingQueueResourceType();
      }

      @Nonnull
      @Override
      public Resource<EntityStore> clone() {
         NewSpawnStartTickingSystem.QueueResource queue = new NewSpawnStartTickingSystem.QueueResource();
         queue.queue.addAll(this.queue);
         return queue;
      }
   }
}
