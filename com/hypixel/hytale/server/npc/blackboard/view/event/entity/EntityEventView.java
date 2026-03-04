package com.hypixel.hytale.server.npc.blackboard.view.event.entity;

import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.event.EntityEventNotification;
import com.hypixel.hytale.server.npc.blackboard.view.event.EventTypeRegistration;
import com.hypixel.hytale.server.npc.blackboard.view.event.EventView;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.ints.IntSet;
import javax.annotation.Nonnull;

public class EntityEventView extends EventView<EntityEventView, EntityEventType, EntityEventNotification> {
   public EntityEventView(@Nonnull World world) {
      super(EntityEventType.class, EntityEventType.VALUES, new EntityEventNotification(), world);
      this.eventRegistry.register(PlayerInteractEvent.class, world.getName(), this::onPlayerInteraction);

      for (EntityEventType eventType : EntityEventType.VALUES) {
         this.entityMapsByEventType
            .put(
               eventType,
               new EventTypeRegistration<>(
                  eventType, (set, roleIndex) -> TagSetPlugin.get(NPCGroup.class).tagInSet(set, roleIndex), NPCEntity::notifyEntityEvent
               )
            );
      }
   }

   public EntityEventView getUpdatedView(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World entityWorld = componentAccessor.getExternalData().getWorld();
      if (!entityWorld.equals(this.world)) {
         Blackboard blackboardResource = componentAccessor.getResource(Blackboard.getResourceType());
         return blackboardResource.getView(EntityEventView.class, ref, componentAccessor);
      } else {
         return this;
      }
   }

   @Override
   public void initialiseEntity(@Nonnull Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent) {
      for (int i = 0; i < EntityEventType.VALUES.length; i++) {
         EntityEventType type = EntityEventType.VALUES[i];
         IntSet eventSets = npcComponent.getBlackboardEntityEventSet(type);
         if (eventSets != null) {
            this.entityMapsByEventType.get(type).initialiseEntity(ref, eventSets);
         }
      }
   }

   protected void onEvent(
      int senderTypeId,
      double x,
      double y,
      double z,
      Ref<EntityStore> initiator,
      @Nonnull Ref<EntityStore> skip,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      EntityEventType type
   ) {
      FlockMembership membership = componentAccessor.getComponent(skip, FlockMembership.getComponentType());
      Ref<EntityStore> flockReference = membership != null ? membership.getFlockRef() : null;
      this.reusableEventNotification.setFlockReference(flockReference);
      super.onEvent(senderTypeId, x, y, z, initiator, skip, componentAccessor, type);
   }

   private void onPlayerInteraction(@Nonnull PlayerInteractEvent event) {
      Player playerComponent = event.getPlayer();
      Ref<EntityStore> playerRef = playerComponent.getReference();
      Store<EntityStore> store = playerRef.getStore();
      if (!event.isCancelled()) {
         if (playerComponent.getGameMode() == GameMode.Creative) {
            PlayerSettings playerSettingsComponent = store.getComponent(playerRef, PlayerSettings.getComponentType());
            if (playerSettingsComponent == null || !playerSettingsComponent.creativeSettings().allowNPCDetection()) {
               return;
            }
         }

         Entity entity = event.getTargetEntity();
         if (entity != null && event.getActionType() == InteractionType.Use && entity instanceof NPCEntity) {
            Ref<EntityStore> entityRef = event.getTargetRef();
            TransformComponent transformComponent = store.getComponent(entityRef, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d pos = transformComponent.getPosition();
            this.onEvent(((NPCEntity)entity).getRoleIndex(), pos.x, pos.y, pos.z, playerRef, entityRef, store, EntityEventType.INTERACTION);
         }
      }
   }

   public void processAttackedEvent(
      @Nonnull Ref<EntityStore> victim,
      @Nonnull Ref<EntityStore> attacker,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      EntityEventType eventType
   ) {
      int roleIndex;
      if (componentAccessor.getArchetype(victim).contains(Player.getComponentType())) {
         roleIndex = BuilderManager.getPlayerGroupID();
      } else {
         NPCEntity npc = componentAccessor.getComponent(victim, NPCEntity.getComponentType());
         if (npc == null) {
            return;
         }

         roleIndex = npc.getRoleIndex();
      }

      Store<EntityStore> store = victim.getStore();
      Vector3d pos = store.getComponent(victim, TransformComponent.getComponentType()).getPosition();
      this.onEvent(roleIndex, pos.x, pos.y, pos.z, attacker, attacker, componentAccessor, eventType);
   }
}
