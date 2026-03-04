package com.hypixel.hytale.server.flock;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.event.events.ecs.ChangeGameModeEvent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlockSystems {
   public static class EntityRemoved extends RefSystem<EntityStore> {
      private final ComponentType<EntityStore, UUIDComponent> flockIdComponentType = UUIDComponent.getComponentType();
      private final ComponentType<EntityStore, EntityGroup> entityGroupComponentType = EntityGroup.getComponentType();
      private final ComponentType<EntityStore, Flock> flockComponentType;
      private final Archetype<EntityStore> archetype;

      public EntityRemoved(ComponentType<EntityStore, Flock> flockComponentType) {
         this.flockComponentType = flockComponentType;
         this.archetype = Archetype.of(this.flockIdComponentType, this.entityGroupComponentType, flockComponentType);
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.archetype;
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
         UUID flockId = store.getComponent(ref, this.flockIdComponentType).getUuid();
         EntityGroup entityGroup = store.getComponent(ref, this.entityGroupComponentType);
         Flock flock = store.getComponent(ref, this.flockComponentType);
         switch (reason) {
            case REMOVE:
               entityGroup.setDissolved(true);

               for (Ref<EntityStore> memberRef : entityGroup.getMemberList()) {
                  commandBuffer.removeComponent(memberRef, FlockMembership.getComponentType());
                  TransformComponent transformComponent = commandBuffer.getComponent(memberRef, TransformComponent.getComponentType());

                  assert transformComponent != null;

                  transformComponent.markChunkDirty(commandBuffer);
               }

               flock.setRemovedStatus(Flock.FlockRemovedStatus.DISSOLVED);
               entityGroup.clear();
               if (flock.isTrace()) {
                  FlockPlugin.get().getLogger().at(Level.INFO).log("Flock %s: Dissolving", flockId);
               }
               break;
            case UNLOAD:
               flock.setRemovedStatus(Flock.FlockRemovedStatus.UNLOADED);
               entityGroup.clear();
               if (flock.isTrace()) {
                  FlockPlugin.get().getLogger().at(Level.INFO).log("Flock %s: Flock unloaded, size=%s", flockId, entityGroup.size());
               }
         }
      }
   }

   public static class FlockDebugSystem extends EntityTickingSystem<EntityStore> {
      private static final float DEBUG_SHAPE_TIME = 0.1F;
      private static final float FLOCK_VIS_RING_OUTER_RADIUS_OFFSET = 0.3F;
      private static final float FLOCK_VIS_RING_THICKNESS = 0.08F;
      private static final float FLOCK_VIS_LINE_THICKNESS = 0.04F;
      private static final int FLOCK_VIS_RING_SEGMENTS = 8;
      private static final float FLOCK_VIS_LEADER_EXTRA_RING_OFFSET = 0.15F;
      private final ComponentType<EntityStore, Flock> flockComponentType;
      private final ComponentType<EntityStore, EntityGroup> entityGroupComponentType;
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentType;
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      private final ComponentType<EntityStore, BoundingBox> boundingBoxComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public FlockDebugSystem(@Nonnull ComponentType<EntityStore, Flock> flockComponentType) {
         this.flockComponentType = flockComponentType;
         this.entityGroupComponentType = EntityGroup.getComponentType();
         this.uuidComponentType = UUIDComponent.getComponentType();
         this.transformComponentType = TransformComponent.getComponentType();
         this.boundingBoxComponentType = BoundingBox.getComponentType();
         this.query = Query.and(flockComponentType, this.entityGroupComponentType, this.uuidComponentType);
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
         Flock flock = archetypeChunk.getComponent(index, this.flockComponentType);

         assert flock != null;

         if (flock.hasVisFlockMember()) {
            EntityGroup entityGroup = archetypeChunk.getComponent(index, this.entityGroupComponentType);

            assert entityGroup != null;

            if (!entityGroup.isDissolved() && entityGroup.size() >= 2) {
               UUIDComponent uuidComponent = archetypeChunk.getComponent(index, this.uuidComponentType);

               assert uuidComponent != null;

               World world = store.getExternalData().getWorld();
               int colorIndex = Math.abs(uuidComponent.getUuid().hashCode()) % DebugUtils.INDEXED_COLORS.length;
               Vector3f color = DebugUtils.INDEXED_COLORS[colorIndex];
               Ref<EntityStore> leaderRef = entityGroup.getLeaderRef();
               if (leaderRef != null && leaderRef.isValid()) {
                  TransformComponent leaderTransform = store.getComponent(leaderRef, this.transformComponentType);
                  BoundingBox leaderBoundingBox = store.getComponent(leaderRef, this.boundingBoxComponentType);
                  if (leaderTransform != null && leaderBoundingBox != null) {
                     Vector3d leaderPos = leaderTransform.getPosition();
                     Box leaderBox = leaderBoundingBox.getBoundingBox();
                     double leaderMidY = leaderPos.y + leaderBox.max.y / 2.0;
                     double leaderWidth = Math.max(leaderBox.max.x - leaderBox.min.x, leaderBox.max.z - leaderBox.min.z);
                     double leaderRingOuterRadius = leaderWidth / 2.0 + 0.3F;
                     double leaderRingInnerRadius = leaderRingOuterRadius - 0.08F;
                     double leaderOuterRingRadius = leaderRingOuterRadius + 0.15F;
                     double leaderOuterRingInnerRadius = leaderOuterRingRadius - 0.08F;
                     DebugUtils.addDisc(world, leaderPos.x, leaderMidY, leaderPos.z, leaderRingOuterRadius, leaderRingInnerRadius, color, 0.8F, 8, 0.1F, false);
                     DebugUtils.addDisc(
                        world, leaderPos.x, leaderMidY, leaderPos.z, leaderOuterRingRadius, leaderOuterRingInnerRadius, color, 0.8F, 8, 0.1F, false
                     );

                     for (Ref<EntityStore> memberRef : entityGroup.getMemberList()) {
                        if (memberRef.isValid() && !memberRef.equals(leaderRef)) {
                           TransformComponent memberTransform = store.getComponent(memberRef, this.transformComponentType);
                           BoundingBox memberBoundingBox = store.getComponent(memberRef, this.boundingBoxComponentType);
                           if (memberTransform != null && memberBoundingBox != null) {
                              Vector3d memberPos = memberTransform.getPosition();
                              Box memberBox = memberBoundingBox.getBoundingBox();
                              double memberMidY = memberPos.y + memberBox.max.y / 2.0;
                              double memberWidth = Math.max(memberBox.max.x - memberBox.min.x, memberBox.max.z - memberBox.min.z);
                              double memberRingOuterRadius = memberWidth / 2.0 + 0.3F;
                              double memberRingInnerRadius = memberRingOuterRadius - 0.08F;
                              DebugUtils.addDisc(
                                 world, memberPos.x, memberMidY, memberPos.z, memberRingOuterRadius, memberRingInnerRadius, color, 0.8F, 8, 0.1F, false
                              );
                              renderConnectingLine(
                                 world,
                                 memberPos.x,
                                 memberMidY,
                                 memberPos.z,
                                 memberRingOuterRadius,
                                 leaderPos.x,
                                 leaderMidY,
                                 leaderPos.z,
                                 leaderOuterRingRadius,
                                 color
                              );
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      private static void renderConnectingLine(
         @Nonnull World world,
         double memberX,
         double memberY,
         double memberZ,
         double memberRadius,
         double leaderX,
         double leaderY,
         double leaderZ,
         double leaderRadius,
         @Nonnull Vector3f color
      ) {
         double dirX = leaderX - memberX;
         double dirZ = leaderZ - memberZ;
         double horizontalDist = Math.sqrt(dirX * dirX + dirZ * dirZ);
         if (horizontalDist < 0.001) {
            DebugUtils.addLine(world, memberX, memberY, memberZ, leaderX, leaderY, leaderZ, color, 0.04F, 0.1F, false);
         } else {
            double hDirX = dirX / horizontalDist;
            double hDirZ = dirZ / horizontalDist;
            double startX = memberX + hDirX * memberRadius;
            double startZ = memberZ + hDirZ * memberRadius;
            double endX = leaderX - hDirX * leaderRadius;
            double endZ = leaderZ - hDirZ * leaderRadius;
            DebugUtils.addLine(world, startX, memberY, startZ, endX, leaderY, endZ, color, 0.04F, 0.1F, false);
         }
      }
   }

   public static class PlayerChangeGameModeEventSystem extends EntityEventSystem<EntityStore, ChangeGameModeEvent> {
      public PlayerChangeGameModeEventSystem() {
         super(ChangeGameModeEvent.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull ChangeGameModeEvent event
      ) {
         if (event.getGameMode() != GameMode.Adventure) {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            commandBuffer.tryRemoveComponent(ref, FlockMembership.getComponentType());
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }
   }

   public static class Ticking extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, Flock> flockComponentType;

      public Ticking(ComponentType<EntityStore, Flock> flockComponentType) {
         this.flockComponentType = flockComponentType;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.flockComponentType;
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
         Flock flock = archetypeChunk.getComponent(index, this.flockComponentType);
         flock.swapDamageDataBuffers();
      }
   }
}
