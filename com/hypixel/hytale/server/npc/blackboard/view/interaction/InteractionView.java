package com.hypixel.hytale.server.npc.blackboard.view.interaction;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.PrioritisedProviderView;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.UUID;
import javax.annotation.Nonnull;

public class InteractionView extends PrioritisedProviderView<ReservationProvider, InteractionView> {
   private final World world;

   public InteractionView(World world) {
      this.world = world;
      this.registerProvider(Integer.MAX_VALUE, (npcRef, playerRef, componentAccessor) -> {
         NPCEntity npcComponent = componentAccessor.getComponent(npcRef, NPCEntity.getComponentType());

         assert npcComponent != null;

         if (!npcComponent.isReserved()) {
            return ReservationStatus.NOT_RESERVED;
         } else {
            UUIDComponent playerUUIDComponent = componentAccessor.getComponent(playerRef, UUIDComponent.getComponentType());

            assert playerUUIDComponent != null;

            UUID playerUUID = playerUUIDComponent.getUuid();
            return npcComponent.isReservedBy(playerUUID) ? ReservationStatus.RESERVED_THIS : ReservationStatus.RESERVED_OTHER;
         }
      });
   }

   @Override
   public boolean isOutdated(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      return false;
   }

   public InteractionView getUpdatedView(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World entityWorld = componentAccessor.getExternalData().getWorld();
      if (!entityWorld.equals(this.world)) {
         Blackboard blackboardResource = componentAccessor.getResource(Blackboard.getResourceType());
         return blackboardResource.getView(InteractionView.class, ref, componentAccessor);
      } else {
         return this;
      }
   }

   @Override
   public void initialiseEntity(@Nonnull Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent) {
   }

   @Override
   public void cleanup() {
   }

   @Override
   public void onWorldRemoved() {
   }

   @Nonnull
   public ReservationStatus getReservationStatus(
      @Nonnull Ref<EntityStore> npcRef, @Nonnull Ref<EntityStore> playerRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      for (int i = 0; i < this.providers.size(); i++) {
         ReservationStatus status = this.providers.get(i).getProvider().getReservationStatus(npcRef, playerRef, componentAccessor);
         if (status != ReservationStatus.NOT_RESERVED) {
            return status;
         }
      }

      return ReservationStatus.NOT_RESERVED;
   }
}
