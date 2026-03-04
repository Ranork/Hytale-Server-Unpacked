package com.hypixel.hytale.builtin.mounts;

import com.hypixel.hytale.builtin.mounts.minecart.MinecartComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.BlockMount;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.protocol.MountedUpdate;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.mountpoints.BlockMountPoint;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerInput;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSystems;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.TeleportSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MountSystems {
   private static void handleMountedRemoval(
      @Nonnull Ref<EntityStore> ref, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull MountedComponent component
   ) {
      Ref<EntityStore> mountedToEntity = component.getMountedToEntity();
      if (mountedToEntity != null && mountedToEntity.isValid()) {
         MountedByComponent mountedBy = commandBuffer.getComponent(mountedToEntity, MountedByComponent.getComponentType());
         if (mountedBy != null) {
            mountedBy.removePassenger(ref);
         }
      }

      Ref<ChunkStore> mountedToBlock = component.getMountedToBlock();
      if (mountedToBlock != null && mountedToBlock.isValid()) {
         Store<ChunkStore> chunkStore = mountedToBlock.getStore();
         BlockMountComponent seatComponent = chunkStore.getComponent(mountedToBlock, BlockMountComponent.getComponentType());
         if (seatComponent != null) {
            seatComponent.removeSeatedEntity(ref);
            if (seatComponent.isDead()) {
               chunkStore.removeComponent(mountedToBlock, BlockMountComponent.getComponentType());
            }
         }
      }
   }

   public static class EnsureMinecartComponents extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, MinecartComponent> minecartComponentType;
      @Nonnull
      private final ComponentType<EntityStore, Interactable> interactableComponentType;
      @Nonnull
      private final ComponentType<EntityStore, NetworkId> networkIdComponentType;
      @Nonnull
      private final ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableComponentType;

      public EnsureMinecartComponents(
         @Nonnull ComponentType<EntityStore, MinecartComponent> minecartComponentType,
         @Nonnull ComponentType<EntityStore, Interactable> interactableComponentType,
         @Nonnull ComponentType<EntityStore, NetworkId> networkIdComponentType,
         @Nonnull ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableComponentType
      ) {
         this.minecartComponentType = minecartComponentType;
         this.interactableComponentType = interactableComponentType;
         this.networkIdComponentType = networkIdComponentType;
         this.prefabCopyableComponentType = prefabCopyableComponentType;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.interactableComponentType);
         holder.putComponent(this.networkIdComponentType, new NetworkId(store.getExternalData().takeNextNetworkId()));
         holder.ensureComponent(this.prefabCopyableComponentType);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.minecartComponentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return RootDependency.firstSet();
      }
   }

   public static class HandleMountInput extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;
      @Nonnull
      private final ComponentType<EntityStore, PlayerInput> playerInputComponentType;
      @Nonnull
      private final ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> deps = Set.of(new SystemDependency<>(Order.BEFORE, PlayerSystems.ProcessPlayerInput.class));

      public HandleMountInput(
         @Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType,
         @Nonnull ComponentType<EntityStore, PlayerInput> playerInputComponentType,
         @Nonnull ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType,
         @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType
      ) {
         this.mountedComponentType = mountedComponentType;
         this.playerInputComponentType = playerInputComponentType;
         this.movementStatesComponentType = movementStatesComponentType;
         this.transformComponentType = transformComponentType;
         this.query = Query.and(mountedComponentType, playerInputComponentType);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         MountedComponent mountedComponent = archetypeChunk.getComponent(index, this.mountedComponentType);

         assert mountedComponent != null;

         PlayerInput playerInputComponent = archetypeChunk.getComponent(index, this.playerInputComponentType);

         assert playerInputComponent != null;

         MountController controller = mountedComponent.getControllerType();
         Ref<EntityStore> targetRef = controller == MountController.BlockMount ? archetypeChunk.getReferenceTo(index) : mountedComponent.getMountedToEntity();
         List<PlayerInput.InputUpdate> queue = playerInputComponent.getMovementUpdateQueue();

         for (int i = 0; i < queue.size(); i++) {
            PlayerInput.InputUpdate inputUpdate = queue.get(i);
            if (controller == MountController.BlockMount
               && (inputUpdate instanceof PlayerInput.RelativeMovement || inputUpdate instanceof PlayerInput.AbsoluteMovement)) {
               if (mountedComponent.getMountedDurationMs() < 600L) {
                  continue;
               }

               Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
               commandBuffer.tryRemoveComponent(ref, this.mountedComponentType);
            }

            if (inputUpdate instanceof PlayerInput.SetRiderMovementStates s) {
               MovementStates states = s.movementStates();
               MovementStatesComponent movementStatesComponent = archetypeChunk.getComponent(index, this.movementStatesComponentType);
               if (movementStatesComponent != null) {
                  movementStatesComponent.setMovementStates(states);
               }
            } else if (!(inputUpdate instanceof PlayerInput.WishMovement)) {
               if (inputUpdate instanceof PlayerInput.RelativeMovement relative) {
                  relative.apply(commandBuffer, archetypeChunk, index);
                  TransformComponent transform = commandBuffer.getComponent(targetRef, this.transformComponentType);
                  if (transform != null) {
                     transform.getPosition().add(relative.getX(), relative.getY(), relative.getZ());
                  }
               } else if (inputUpdate instanceof PlayerInput.AbsoluteMovement absolute) {
                  absolute.apply(commandBuffer, archetypeChunk, index);
                  TransformComponent transform = commandBuffer.getComponent(targetRef, this.transformComponentType);
                  if (transform != null) {
                     transform.getPosition().assign(absolute.getX(), absolute.getY(), absolute.getZ());
                  }
               } else if (inputUpdate instanceof PlayerInput.SetMovementStates sx) {
                  MovementStates states = sx.movementStates();
                  MovementStatesComponent movementStatesComponent = commandBuffer.getComponent(targetRef, this.movementStatesComponentType);
                  if (movementStatesComponent != null) {
                     movementStatesComponent.setMovementStates(states);
                  }
               } else if (inputUpdate instanceof PlayerInput.SetBody body) {
                  body.apply(commandBuffer, archetypeChunk, index);
                  TransformComponent transform = commandBuffer.getComponent(targetRef, this.transformComponentType);
                  if (transform != null) {
                     transform.getRotation().assign(body.direction().pitch, body.direction().yaw, body.direction().roll);
                  }
               } else if (inputUpdate instanceof PlayerInput.SetHead head) {
                  head.apply(commandBuffer, archetypeChunk, index);
               }
            }
         }

         queue.clear();
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.deps;
      }
   }

   public static class MountedEntityDeath extends RefChangeSystem<EntityStore, DeathComponent> {
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;
      @Nonnull
      private final ComponentType<EntityStore, DeathComponent> deathComponentType;

      public MountedEntityDeath(
         @Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType, @Nonnull ComponentType<EntityStore, DeathComponent> deathComponentType
      ) {
         this.mountedComponentType = mountedComponentType;
         this.deathComponentType = deathComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.mountedComponentType;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, DeathComponent> componentType() {
         return this.deathComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.removeComponent(ref, this.mountedComponentType);
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         @Nullable DeathComponent oldComponent,
         @Nonnull DeathComponent newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class OnMinecartHit extends DamageEventSystem {
      private static final Duration HIT_RESET_TIME = Duration.ofSeconds(10L);
      private static final int NUMBER_OF_HITS = 3;
      @Nonnull
      private final ComponentType<EntityStore, MinecartComponent> minecartComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;
      @Nonnull
      private final ResourceType<EntityStore, TimeResource> timeResourceType;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies = Set.of(
         new SystemGroupDependency<>(Order.AFTER, DamageModule.get().getGatherDamageGroup()),
         new SystemGroupDependency<>(Order.AFTER, DamageModule.get().getFilterDamageGroup()),
         new SystemGroupDependency<>(Order.BEFORE, DamageModule.get().getInspectDamageGroup())
      );

      public OnMinecartHit(
         @Nonnull ComponentType<EntityStore, MinecartComponent> minecartComponentType,
         @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
         @Nonnull ComponentType<EntityStore, Player> playerComponentType,
         @Nonnull ResourceType<EntityStore, TimeResource> timeResourceType
      ) {
         this.minecartComponentType = minecartComponentType;
         this.transformComponentType = transformComponentType;
         this.playerComponentType = playerComponentType;
         this.timeResourceType = timeResourceType;
         this.query = Archetype.of(minecartComponentType, transformComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Damage damage
      ) {
         MinecartComponent minecartComponent = archetypeChunk.getComponent(index, this.minecartComponentType);

         assert minecartComponent != null;

         Instant currentTime = commandBuffer.getResource(this.timeResourceType).getNow();
         if (minecartComponent.getLastHit() != null && currentTime.isAfter(minecartComponent.getLastHit().plus(HIT_RESET_TIME))) {
            minecartComponent.setLastHit(null);
            minecartComponent.setNumberOfHits(0);
         }

         if (!(damage.getAmount() <= 0.0F)) {
            minecartComponent.setNumberOfHits(minecartComponent.getNumberOfHits() + 1);
            minecartComponent.setLastHit(currentTime);
            if (minecartComponent.getNumberOfHits() == 3) {
               commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
               boolean shouldDropItem = true;
               if (damage.getSource() instanceof Damage.EntitySource source) {
                  Player playerComponent = source.getRef().isValid() ? commandBuffer.getComponent(source.getRef(), this.playerComponentType) : null;
                  if (playerComponent != null) {
                     shouldDropItem = playerComponent.getGameMode() != GameMode.Creative;
                  }
               }

               if (shouldDropItem && minecartComponent.getSourceItem() != null) {
                  TransformComponent transform = archetypeChunk.getComponent(index, this.transformComponentType);

                  assert transform != null;

                  Holder<EntityStore> drop = ItemComponent.generateItemDrop(
                     commandBuffer, new ItemStack(minecartComponent.getSourceItem()), transform.getPosition(), transform.getRotation(), 0.0F, 1.0F, 0.0F
                  );
                  if (drop != null) {
                     commandBuffer.addEntity(drop, AddReason.SPAWN);
                  }
               }
            }
         }
      }
   }

   public static class PlayerMount extends RefChangeSystem<EntityStore, MountedComponent> {
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;
      @Nonnull
      private final ComponentType<EntityStore, PlayerInput> playerInputComponentType;
      @Nonnull
      private final ComponentType<EntityStore, NetworkId> networkIdComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public PlayerMount(
         @Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType,
         @Nonnull ComponentType<EntityStore, PlayerInput> playerInputComponentType,
         @Nonnull ComponentType<EntityStore, NetworkId> networkIdComponentType
      ) {
         this.mountedComponentType = mountedComponentType;
         this.playerInputComponentType = playerInputComponentType;
         this.networkIdComponentType = networkIdComponentType;
         this.query = playerInputComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, MountedComponent> componentType() {
         return this.mountedComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull MountedComponent component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         MountedComponent mountedComponent = commandBuffer.getComponent(ref, this.mountedComponentType);

         assert mountedComponent != null;

         PlayerInput playerInputComponent = commandBuffer.getComponent(ref, this.playerInputComponentType);

         assert playerInputComponent != null;

         Ref<EntityStore> mountRef = mountedComponent.getMountedToEntity();
         if (mountRef != null && mountRef.isValid()) {
            NetworkId mountNetworkIdComponent = commandBuffer.getComponent(mountRef, this.networkIdComponentType);
            if (mountNetworkIdComponent != null) {
               int mountNetworkId = mountNetworkIdComponent.getId();
               playerInputComponent.setMountId(mountNetworkId);
               playerInputComponent.getMovementUpdateQueue().clear();
            }
         }
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         @Nullable MountedComponent oldComponent,
         @Nonnull MountedComponent newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull MountedComponent component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         PlayerInput playerInputComponent = commandBuffer.getComponent(ref, this.playerInputComponentType);

         assert playerInputComponent != null;

         playerInputComponent.setMountId(0);
      }
   }

   public static class RemoveBlockSeat extends RefSystem<ChunkStore> {
      @Nonnull
      private final ComponentType<ChunkStore, BlockMountComponent> blockMountComponentType;
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;

      public RemoveBlockSeat(
         @Nonnull ComponentType<ChunkStore, BlockMountComponent> blockMountComponentType,
         @Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType
      ) {
         this.blockMountComponentType = blockMountComponentType;
         this.mountedComponentType = mountedComponentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         BlockMountComponent blockSeatComponent = commandBuffer.getComponent(ref, this.blockMountComponentType);

         assert blockSeatComponent != null;

         ObjectArrayList<? extends Ref<EntityStore>> dismounting = new ObjectArrayList(blockSeatComponent.getSeatedEntities());
         World world = ref.getStore().getExternalData().getWorld();
         ObjectListIterator var8 = dismounting.iterator();

         while (var8.hasNext()) {
            Ref<EntityStore> seated = (Ref<EntityStore>)var8.next();
            blockSeatComponent.removeSeatedEntity(seated);
            world.execute(() -> {
               if (seated.isValid()) {
                  seated.getStore().tryRemoveComponent(seated, this.mountedComponentType);
               }
            });
         }
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.blockMountComponentType;
      }
   }

   public static class RemoveMounted extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;

      public RemoveMounted(@Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType) {
         this.mountedComponentType = mountedComponentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         MountedComponent mounted = commandBuffer.getComponent(ref, this.mountedComponentType);
         MountSystems.handleMountedRemoval(ref, commandBuffer, mounted);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.mountedComponentType;
      }
   }

   public static class RemoveMountedBy extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, MountedByComponent> mountedByComponentType;
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;

      public RemoveMountedBy(
         @Nonnull ComponentType<EntityStore, MountedByComponent> mountedByComponentType,
         @Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType
      ) {
         this.mountedByComponentType = mountedByComponentType;
         this.mountedComponentType = mountedComponentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         MountedByComponent mountedByComponent = commandBuffer.getComponent(ref, this.mountedByComponentType);

         assert mountedByComponent != null;

         for (Ref<EntityStore> p : mountedByComponent.getPassengers()) {
            if (p.isValid()) {
               MountedComponent mountedComponent = commandBuffer.getComponent(p, this.mountedComponentType);
               if (mountedComponent != null) {
                  Ref<EntityStore> targetRef = mountedComponent.getMountedToEntity();
                  if (targetRef != null && (!targetRef.isValid() || targetRef.equals(ref))) {
                     commandBuffer.removeComponent(p, this.mountedComponentType);
                  }
               }
            }
         }
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.mountedByComponentType;
      }
   }

   public static class RemoveMountedHolder extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;

      public RemoveMountedHolder(@Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType) {
         this.mountedComponentType = mountedComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.mountedComponentType;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         holder.removeComponent(this.mountedComponentType);
      }
   }

   public static class TeleportMountedEntity extends RefChangeSystem<EntityStore, Teleport> {
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;
      @Nonnull
      private final ComponentType<EntityStore, Teleport> teleportComponentType;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies = Set.of(
         new SystemDependency<>(Order.BEFORE, TeleportSystems.MoveSystem.class, OrderPriority.CLOSEST),
         new SystemDependency<>(Order.BEFORE, TeleportSystems.PlayerMoveSystem.class, OrderPriority.CLOSEST)
      );

      public TeleportMountedEntity(
         @Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType, @Nonnull ComponentType<EntityStore, Teleport> teleportComponentType
      ) {
         this.mountedComponentType = mountedComponentType;
         this.teleportComponentType = teleportComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.mountedComponentType;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, Teleport> componentType() {
         return this.teleportComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull Teleport component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.removeComponent(ref, this.mountedComponentType);
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         @Nullable Teleport oldComponent,
         @Nonnull Teleport newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull Teleport component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }
   }

   public static class TrackedMounted extends RefChangeSystem<EntityStore, MountedComponent> {
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;
      @Nonnull
      private final ComponentType<EntityStore, MountedByComponent> mountedByComponentType;

      public TrackedMounted(
         @Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType,
         @Nonnull ComponentType<EntityStore, MountedByComponent> mountedByComponentType
      ) {
         this.mountedComponentType = mountedComponentType;
         this.mountedByComponentType = mountedByComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.mountedComponentType;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, MountedComponent> componentType() {
         return this.mountedComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull MountedComponent component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Ref<EntityStore> target = component.getMountedToEntity();
         if (target != null && target.isValid()) {
            MountedByComponent mountedBy = commandBuffer.ensureAndGetComponent(target, this.mountedByComponentType);
            mountedBy.addPassenger(ref);
         }
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         MountedComponent oldComponent,
         @Nonnull MountedComponent newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull MountedComponent component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         MountSystems.handleMountedRemoval(ref, commandBuffer, component);
      }
   }

   public static class TrackerRemove extends RefChangeSystem<EntityStore, MountedComponent> {
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;

      public TrackerRemove(
         @Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType,
         @Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType
      ) {
         this.mountedComponentType = mountedComponentType;
         this.visibleComponentType = visibleComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.visibleComponentType;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, MountedComponent> componentType() {
         return this.mountedComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull MountedComponent component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         MountedComponent oldComponent,
         @Nonnull MountedComponent newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull MountedComponent component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         if (component.getControllerType() == MountController.BlockMount) {
            AnimationUtils.stopAnimation(ref, AnimationSlot.Movement, true, commandBuffer);
         }

         EntityTrackerSystems.Visible visibleComponent = store.getComponent(ref, this.visibleComponentType);

         assert visibleComponent != null;

         for (EntityTrackerSystems.EntityViewer viewer : visibleComponent.visibleTo.values()) {
            viewer.queueRemove(ref, ComponentUpdateType.Mounted);
         }
      }
   }

   public static class TrackerUpdate extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final ComponentType<EntityStore, MountedComponent> mountedComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public TrackerUpdate(
         @Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType,
         @Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType
      ) {
         this.visibleComponentType = visibleComponentType;
         this.mountedComponentType = mountedComponentType;
         this.query = Query.and(visibleComponentType, mountedComponentType);
      }

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
         EntityTrackerSystems.Visible visibleComponent = archetypeChunk.getComponent(index, this.visibleComponentType);

         assert visibleComponent != null;

         MountedComponent mountedComponent = archetypeChunk.getComponent(index, this.mountedComponentType);

         assert mountedComponent != null;

         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         if (mountedComponent.consumeNetworkOutdated()) {
            queueUpdatesFor(ref, visibleComponent.visibleTo, mountedComponent);
         } else if (!visibleComponent.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(ref, visibleComponent.newlyVisibleTo, mountedComponent);
         }
      }

      private static void queueUpdatesFor(
         @Nonnull Ref<EntityStore> ref, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo, @Nonnull MountedComponent component
      ) {
         Ref<EntityStore> mountedToEntity = component.getMountedToEntity();
         Ref<ChunkStore> mountedToBlock = component.getMountedToBlock();
         Vector3f offset = component.getAttachmentOffset();
         com.hypixel.hytale.protocol.Vector3f netOffset = new com.hypixel.hytale.protocol.Vector3f(offset.x, offset.y, offset.z);
         MountedUpdate mountedUpdate;
         if (mountedToEntity != null) {
            NetworkId mountedToNetworkIdComponent = ref.getStore().getComponent(mountedToEntity, NetworkId.getComponentType());
            if (mountedToNetworkIdComponent == null) {
               return;
            }

            int mountedToNetworkId = mountedToNetworkIdComponent.getId();
            mountedUpdate = new MountedUpdate(mountedToNetworkId, netOffset, component.getControllerType(), null);
         } else {
            if (mountedToBlock == null) {
               throw new UnsupportedOperationException("Couldn't create MountedUpdate packet for MountedComponent");
            }

            BlockMountComponent blockMountComponent = mountedToBlock.getStore().getComponent(mountedToBlock, BlockMountComponent.getComponentType());
            if (blockMountComponent == null) {
               return;
            }

            BlockMountPoint occupiedSeat = blockMountComponent.getSeatBlockBySeatedEntity(ref);
            if (occupiedSeat == null) {
               return;
            }

            BlockType blockType = blockMountComponent.getExpectedBlockType();
            Vector3f position = occupiedSeat.computeWorldSpacePosition(blockMountComponent.getBlockPos());
            Vector3f rotationEuler = occupiedSeat.computeRotationEuler(blockMountComponent.getExpectedRotation());
            BlockMount blockMount = new BlockMount(
               blockMountComponent.getType(),
               new com.hypixel.hytale.protocol.Vector3f(position.x, position.y, position.z),
               new com.hypixel.hytale.protocol.Vector3f(rotationEuler.x, rotationEuler.y, rotationEuler.z),
               BlockType.getAssetMap().getIndex(blockType.getId())
            );
            mountedUpdate = new MountedUpdate(0, netOffset, component.getControllerType(), blockMount);
         }

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, mountedUpdate);
         }
      }
   }
}
