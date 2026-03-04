package com.hypixel.hytale.builtin.instances.removal;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class IdleTimeoutCondition implements RemovalCondition {
   @Nonnull
   public static final BuilderCodec<IdleTimeoutCondition> CODEC = BuilderCodec.builder(IdleTimeoutCondition.class, IdleTimeoutCondition::new)
      .documentation("A condition that triggers after the world has be idle (without players) for a set time.")
      .<Double>append(new KeyedCodec<>("TimeoutSeconds", Codec.DOUBLE), (o, i) -> o.timeoutSeconds = i, o -> o.timeoutSeconds)
      .documentation("How long (in seconds) the world has to be idle (without players) for before triggering.")
      .add()
      .build();
   private double timeoutSeconds = TimeUnit.MINUTES.toSeconds(5L);

   @Override
   public boolean shouldRemoveWorld(@Nonnull Store<ChunkStore> store) {
      InstanceDataResource instanceDataResource = store.getResource(InstanceDataResource.getResourceType());
      World world = store.getExternalData().getWorld();
      Store<EntityStore> entityStore = world.getEntityStore().getStore();
      TimeResource timeResource = entityStore.getResource(TimeResource.getResourceType());
      boolean hasPlayer = world.getPlayerCount() > 0;
      if (!hasPlayer) {
         if (instanceDataResource.getIdleTimeoutTimer() == null) {
            instanceDataResource.setIdleTimeoutTimer(timeResource.getNow().plusNanos((long)(this.timeoutSeconds * 1.0E9)));
         }

         return timeResource.getNow().isAfter(instanceDataResource.getIdleTimeoutTimer());
      } else {
         instanceDataResource.setIdleTimeoutTimer(null);
         return false;
      }
   }
}
