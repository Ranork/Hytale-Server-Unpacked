package com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.TriIntConsumer;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.protocol.SelectedHitEntity;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

@Deprecated
public class ClientSourcedSelector implements Selector {
   private final Selector parent;
   private final InteractionContext context;

   public ClientSourcedSelector(Selector parent, InteractionContext context) {
      this.parent = parent;
      this.context = context;
   }

   @Override
   public void tick(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref, float time, float runTime) {
      this.parent.tick(commandBuffer, ref, time, runTime);
   }

   @Override
   public void selectTargetEntities(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BiConsumer<Ref<EntityStore>, Vector4d> consumer,
      Predicate<Ref<EntityStore>> filter
   ) {
      SelectedHitEntity[] hits = this.context.getClientState().hitEntities;
      if (hits != null) {
         EntityStore store = commandBuffer.getStore().getExternalData();

         for (SelectedHitEntity info : hits) {
            Ref<EntityStore> targetRef = store.getRefFromNetworkId(info.networkId);
            if (targetRef != null) {
               consumer.accept(targetRef, new Vector4d(info.hitLocation.x, info.hitLocation.y, info.hitLocation.z, 0.0));
            }
         }
      }
   }

   @Override
   public void selectTargetBlocks(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref, @Nonnull TriIntConsumer consumer) {
      this.parent.selectTargetBlocks(commandBuffer, ref, consumer);
   }
}
