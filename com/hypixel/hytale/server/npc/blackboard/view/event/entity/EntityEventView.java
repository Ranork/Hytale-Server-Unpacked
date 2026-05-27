package com.hypixel.hytale.server.npc.blackboard.view.event.entity;

import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionType;
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
import org.joml.Vector3d;

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
      for (EntityEventType type : EntityEventType.VALUES) {
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
      FlockMembership flockMembershipComponent = componentAccessor.getComponent(skip, FlockMembership.getComponentType());
      Ref<EntityStore> flockRef = flockMembershipComponent != null ? flockMembershipComponent.getFlockRef() : null;
      this.reusableEventNotification.setFlockReference(flockRef);
      super.onEvent(senderTypeId, x, y, z, initiator, skip, componentAccessor, type);
   }

   private void onPlayerInteraction(@Nonnull PlayerInteractEvent event) {
      if (!event.isCancelled()) {
         Player playerComponent = event.getPlayer();
         Ref<EntityStore> playerRef = event.getPlayerRef();
         Store<EntityStore> store = playerRef.getStore();
         if (playerComponent.getGameMode() == GameMode.Creative) {
            PlayerSettings playerSettingsComponent = store.getComponent(playerRef, PlayerSettings.getComponentType());
            if (playerSettingsComponent == null || !playerSettingsComponent.creativeSettings().allowNPCDetection()) {
               return;
            }
         }

         Ref<EntityStore> targetRef = event.getTargetRef();
         if (targetRef != null && targetRef.isValid()) {
            NPCEntity targetNpcComponent = store.getComponent(targetRef, NPCEntity.getComponentType());
            if (targetNpcComponent != null && event.getActionType() == InteractionType.Use) {
               TransformComponent targetTransformComponent = store.getComponent(targetRef, TransformComponent.getComponentType());

               assert targetTransformComponent != null;

               Vector3d targetPos = targetTransformComponent.getPosition();
               this.onEvent(targetNpcComponent.getRoleIndex(), targetPos.x, targetPos.y, targetPos.z, playerRef, targetRef, store, EntityEventType.INTERACTION);
            }
         }
      }
   }

   public void processAttackedEvent(
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull Ref<EntityStore> attackerRef,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull EntityEventType eventType
   ) {
      int roleIndex;
      if (componentAccessor.getArchetype(targetRef).contains(Player.getComponentType())) {
         roleIndex = BuilderManager.getPlayerGroupID();
      } else {
         NPCEntity targetNpcComponent = componentAccessor.getComponent(targetRef, NPCEntity.getComponentType());
         if (targetNpcComponent == null) {
            return;
         }

         roleIndex = targetNpcComponent.getRoleIndex();
      }

      Store<EntityStore> store = targetRef.getStore();
      TransformComponent transformComponent = store.getComponent(targetRef, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d pos = transformComponent.getPosition();
      this.onEvent(roleIndex, pos.x, pos.y, pos.z, attackerRef, attackerRef, componentAccessor, eventType);
   }
}
