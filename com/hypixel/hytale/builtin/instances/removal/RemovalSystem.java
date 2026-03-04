package com.hypixel.hytale.builtin.instances.removal;

import com.hypixel.hytale.builtin.instances.config.InstanceWorldConfig;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.RunWhenPausedSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class RemovalSystem extends TickingSystem<ChunkStore> implements RunWhenPausedSystem<ChunkStore> {
   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<ChunkStore> store) {
      InstanceDataResource instanceDataResource = store.getResource(InstanceDataResource.getResourceType());
      if (!instanceDataResource.isRemoving() && shouldRemoveWorld(store)) {
         instanceDataResource.setRemoving(true);
         CompletableFutureUtil._catch(CompletableFuture.runAsync(() -> Universe.get().removeWorld(store.getExternalData().getWorld().getName())));
      }
   }

   public static boolean shouldRemoveWorld(@Nonnull Store<ChunkStore> store) {
      World world = store.getExternalData().getWorld();
      InstanceWorldConfig config = InstanceWorldConfig.get(world.getWorldConfig());
      if (config == null) {
         return false;
      } else {
         RemovalCondition[] removalConditions = config.getRemovalConditions();
         if (removalConditions.length == 0) {
            return false;
         } else {
            boolean shouldRemove = true;

            for (RemovalCondition cond : removalConditions) {
               shouldRemove &= cond.shouldRemoveWorld(store);
            }

            return shouldRemove;
         }
      }
   }
}
