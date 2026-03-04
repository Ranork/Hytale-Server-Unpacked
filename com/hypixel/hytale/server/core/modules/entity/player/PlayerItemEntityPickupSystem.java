package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialStructure;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PickupItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerSpatialSystem;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Set;
import javax.annotation.Nonnull;

public class PlayerItemEntityPickupSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, ItemComponent> itemComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Player> playerComponentType;
   @Nonnull
   private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent;
   @Nonnull
   private final ComponentType<EntityStore, InteractionManager> interactionManagerType;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;
   @Nonnull
   private final Query<EntityStore> query;

   public PlayerItemEntityPickupSystem(
      @Nonnull ComponentType<EntityStore, ItemComponent> itemComponentType,
      @Nonnull ComponentType<EntityStore, Player> playerComponentType,
      @Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent
   ) {
      this.itemComponentType = itemComponentType;
      this.playerComponentType = playerComponentType;
      this.interactionManagerType = InteractionModule.get().getInteractionManagerComponent();
      this.playerSpatialComponent = playerSpatialComponent;
      this.dependencies = Set.of(new SystemDependency<>(Order.AFTER, PlayerSpatialSystem.class, OrderPriority.CLOSEST));
      this.query = Query.and(
         itemComponentType,
         TransformComponent.getComponentType(),
         Query.not(Interactable.getComponentType()),
         Query.not(PickupItemComponent.getComponentType()),
         Query.not(PreventPickup.getComponentType())
      );
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return false;
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      Ref<EntityStore> itemRef = archetypeChunk.getReferenceTo(index);
      ItemComponent itemComponent = archetypeChunk.getComponent(index, this.itemComponentType);

      assert itemComponent != null;

      if (itemComponent.pollPickupDelay(dt)) {
         if (itemComponent.pollPickupThrottle(dt)) {
            TimeResource timeResource = commandBuffer.getResource(TimeResource.getResourceType());
            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(this.playerSpatialComponent);
            SpatialStructure<Ref<EntityStore>> spatialStructure = playerSpatialResource.getSpatialStructure();
            TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d itemEntityPosition = transformComponent.getPosition();
            DespawnComponent despawnComponent = archetypeChunk.getComponent(index, DespawnComponent.getComponentType());
            float pickupRadius = itemComponent.getPickupRadius(commandBuffer);
            ItemStack itemStack = itemComponent.getItemStack();
            Item item = itemStack.getItem();
            String interactions = item.getInteractions().get(InteractionType.Pickup);
            if (interactions != null) {
               Ref<EntityStore> targetRef = spatialStructure.closest(itemEntityPosition);
               if (targetRef != null) {
                  TransformComponent targetTransformComponent = store.getComponent(targetRef, TransformComponent.getComponentType());

                  assert targetTransformComponent != null;

                  InteractionManager targetInteractionManagerComponent = store.getComponent(targetRef, this.interactionManagerType);

                  assert targetInteractionManagerComponent != null;

                  Vector3d targetPosition = targetTransformComponent.getPosition();
                  double distance = targetPosition.distanceTo(itemEntityPosition);
                  if (!(distance > pickupRadius)) {
                     Ref<EntityStore> reference = archetypeChunk.getReferenceTo(index);
                     commandBuffer.run(
                        _store -> {
                           InteractionContext context = InteractionContext.forInteraction(
                              targetInteractionManagerComponent, targetRef, InteractionType.Pickup, commandBuffer
                           );
                           InteractionChain chain = targetInteractionManagerComponent.initChain(
                              InteractionType.Pickup, context, RootInteraction.getRootInteractionOrUnknown(interactions), false
                           );
                           context.getMetaStore().putMetaObject(Interaction.TARGET_ENTITY, reference);
                           targetInteractionManagerComponent.executeChain(reference, commandBuffer, chain);
                           _store.removeEntity(reference, RemoveReason.REMOVE);
                        }
                     );
                  }
               }
            } else {
               ObjectList<Ref<EntityStore>> targetPlayerRefs = SpatialResource.getThreadLocalReferenceList();
               spatialStructure.ordered(itemEntityPosition, pickupRadius, targetPlayerRefs);
               ObjectListIterator targetTransformComponent = targetPlayerRefs.iterator();

               while (targetTransformComponent.hasNext()) {
                  Ref<EntityStore> targetPlayerRef = (Ref<EntityStore>)targetTransformComponent.next();
                  if (!store.getArchetype(targetPlayerRef).contains(DeathComponent.getComponentType())) {
                     Player playerComponent = store.getComponent(targetPlayerRef, this.playerComponentType);

                     assert playerComponent != null;

                     ItemStackTransaction transaction = playerComponent.giveItem(itemStack, targetPlayerRef, commandBuffer);
                     ItemStack remainder = transaction.getRemainder();
                     if (ItemStack.isEmpty(remainder)) {
                        itemComponent.setRemovedByPlayerPickup(true);
                        commandBuffer.removeEntity(itemRef, RemoveReason.REMOVE);
                        playerComponent.notifyPickupItem(targetPlayerRef, itemStack, itemEntityPosition, commandBuffer);
                        Holder<EntityStore> pickupItemHolder = ItemComponent.generatePickedUpItem(itemRef, commandBuffer, targetPlayerRef, itemEntityPosition);
                        if (pickupItemHolder != null) {
                           commandBuffer.addEntity(pickupItemHolder, AddReason.SPAWN);
                        }
                        break;
                     }

                     if (!remainder.equals(itemStack)) {
                        int quantity = itemStack.getQuantity() - remainder.getQuantity();
                        itemStack = remainder;
                        itemComponent.setItemStack(remainder);
                        float newLifetime = itemComponent.computeLifetimeSeconds(commandBuffer);
                        DespawnComponent.trySetDespawn(commandBuffer, timeResource, itemRef, despawnComponent, newLifetime);
                        Holder<EntityStore> pickupItemHolder = ItemComponent.generatePickedUpItem(itemRef, commandBuffer, targetPlayerRef, itemEntityPosition);
                        if (pickupItemHolder != null) {
                           commandBuffer.addEntity(pickupItemHolder, AddReason.SPAWN);
                        }

                        if (quantity > 0) {
                           playerComponent.notifyPickupItem(targetPlayerRef, remainder.withQuantity(quantity), itemEntityPosition, commandBuffer);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
