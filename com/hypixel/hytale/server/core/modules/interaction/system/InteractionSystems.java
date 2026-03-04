package com.hypixel.hytale.server.core.modules.interaction.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.InteractionsUpdate;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsSystems;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.InteractionSimulationHandler;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionSystems {
   public static class CleanUpSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, InteractionManager> interactionComponentType = InteractionModule.get().getInteractionManagerComponent();

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         InteractionManager interactionManager = store.getComponent(ref, this.interactionComponentType);

         assert interactionManager != null;

         interactionManager.clear();
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.interactionComponentType;
      }
   }

   public static class EntityTrackerRemove extends RefChangeSystem<EntityStore, Interactions> {
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;

      public EntityTrackerRemove(ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType) {
         this.visibleComponentType = visibleComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.visibleComponentType;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, Interactions> componentType() {
         return Interactions.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull Interactions component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         Interactions oldComponent,
         @Nonnull Interactions newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull Interactions component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityTrackerSystems.Visible visible = commandBuffer.getComponent(ref, this.visibleComponentType);
         if (visible != null) {
            for (EntityTrackerSystems.EntityViewer viewer : visible.visibleTo.values()) {
               viewer.queueRemove(ref, ComponentUpdateType.Interactions);
            }
         }
      }
   }

   public static class PlayerAddManagerSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private final Query<EntityStore> query = Query.and(
         Player.getComponentType(), PlayerRef.getComponentType(), Query.not(InteractionModule.get().getInteractionManagerComponent())
      );

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         Player playerComponent = holder.getComponent(Player.getComponentType());

         assert playerComponent != null;

         PlayerRef playerRefComponent = holder.getComponent(PlayerRef.getComponentType());

         assert playerRefComponent != null;

         holder.addComponent(
            InteractionModule.get().getInteractionManagerComponent(),
            new InteractionManager(playerComponent, playerRefComponent, new InteractionSimulationHandler())
         );
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class TickInteractionManagerSystem extends EntityTickingSystem<EntityStore> implements EntityStatsSystems.StatModifyingSystem {
      @Nonnull
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      @Nonnull
      private final ComponentType<EntityStore, InteractionManager> interactionManagerComponent = InteractionModule.get().getInteractionManagerComponent();

      @Override
      public Query<EntityStore> getQuery() {
         return this.interactionManagerComponent;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

         try {
            InteractionManager interactionManager = archetypeChunk.getComponent(index, this.interactionManagerComponent);

            assert interactionManager != null;

            PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
            interactionManager.tick(ref, commandBuffer, dt);
            ObjectList<SyncInteractionChain> syncPackets = interactionManager.getSyncPackets();
            if (playerRef != null && !syncPackets.isEmpty()) {
               playerRef.getPacketHandler().writeNoCache(new SyncInteractionChains((SyncInteractionChain[])syncPackets.toArray(SyncInteractionChain[]::new)));
               syncPackets.clear();
            }
         } catch (Throwable var10) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var10)).log("Exception while ticking entity interactions! Removing!");
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         }
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return DamageModule.get().getGatherDamageGroup();
      }
   }

   public static class TrackerTickSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType = EntityTrackerSystems.Visible.getComponentType();
      @Nonnull
      private final Query<EntityStore> query = Query.and(this.visibleComponentType, Interactions.getComponentType());

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityTrackerSystems.QUEUE_UPDATE_GROUP;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityTrackerSystems.Visible visibleComponentType = archetypeChunk.getComponent(index, this.visibleComponentType);

         assert visibleComponentType != null;

         Interactions interactionsComponent = archetypeChunk.getComponent(index, Interactions.getComponentType());

         assert interactionsComponent != null;

         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         if (interactionsComponent.consumeNetworkOutdated()) {
            queueUpdatesFor(ref, visibleComponentType.visibleTo, interactionsComponent);
         } else if (!visibleComponentType.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(ref, visibleComponentType.newlyVisibleTo, interactionsComponent);
         }
      }

      private static void queueUpdatesFor(
         @Nonnull Ref<EntityStore> ref, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo, @Nonnull Interactions component
      ) {
         Object2IntOpenHashMap<InteractionType> interactions = new Object2IntOpenHashMap();

         for (Entry<InteractionType, String> entry : component.getInteractions().entrySet()) {
            interactions.put(entry.getKey(), RootInteraction.getRootInteractionIdOrUnknown(entry.getValue()));
         }

         InteractionsUpdate componentUpdate = new InteractionsUpdate(interactions, component.getInteractionHint());

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, componentUpdate);
         }
      }
   }
}
