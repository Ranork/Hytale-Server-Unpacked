package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.NewSpawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entity.system.ModelSystems;
import com.hypixel.hytale.server.core.modules.entity.system.TransformSystems;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.components.StepComponent;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugDisplay;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import com.hypixel.hytale.server.npc.role.support.EntitySupport;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class RoleSystems {
   private static final ThreadLocal<List<Ref<EntityStore>>> ENTITY_LIST = ThreadLocal.withInitial(ArrayList::new);

   public static class BehaviourTickSystem extends TickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, StepComponent> stepComponentType;
      @Nonnull
      private final ComponentType<EntityStore, Frozen> frozenComponentType;
      @Nonnull
      private final ComponentType<EntityStore, NewSpawnComponent> newSpawnComponentType;

      public BehaviourTickSystem(
         @Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType, @Nonnull ComponentType<EntityStore, StepComponent> stepComponentType
      ) {
         this.npcComponentType = npcComponentType;
         this.stepComponentType = stepComponentType;
         this.frozenComponentType = Frozen.getComponentType();
         this.newSpawnComponentType = NewSpawnComponent.getComponentType();
      }

      @Override
      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         List<Ref<EntityStore>> entities = RoleSystems.ENTITY_LIST.get();
         store.forEachChunk(this.npcComponentType, (archetypeChunk, commandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
               entities.add(archetypeChunk.getReferenceTo(index));
            }
         });
         World world = store.getExternalData().getWorld();
         boolean isAllNpcFrozen = world.getWorldConfig().isAllNPCFrozen();

         for (Ref<EntityStore> entityReference : entities) {
            if (entityReference.isValid() && store.getComponent(entityReference, this.newSpawnComponentType) == null) {
               float tickLength;
               if (store.getComponent(entityReference, this.frozenComponentType) == null && !isAllNpcFrozen) {
                  tickLength = dt;
               } else {
                  StepComponent stepComponent = store.getComponent(entityReference, this.stepComponentType);
                  if (stepComponent == null) {
                     continue;
                  }

                  tickLength = stepComponent.getTickLength();
               }

               NPCEntity npcComponent = store.getComponent(entityReference, this.npcComponentType);

               assert npcComponent != null;

               try {
                  Role role = npcComponent.getRole();
                  boolean benchmarking = NPCPlugin.get().isBenchmarkingRole();
                  if (benchmarking) {
                     long start = System.nanoTime();
                     role.tick(entityReference, tickLength, store);
                     NPCPlugin.get().collectRoleTick(role.getRoleIndex(), System.nanoTime() - start);
                  } else {
                     role.tick(entityReference, tickLength, store);
                  }
               } catch (IllegalArgumentException | IllegalStateException | NullPointerException var15) {
                  ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.SEVERE).withCause(var15)).log("Failed to tick NPC: %s", npcComponent.getRoleName());
                  store.removeEntity(entityReference, RemoveReason.REMOVE);
               }
            }
         }

         entities.clear();
      }
   }

   public static class PostBehaviourSupportTickSystem extends SteppableTickingSystem {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies = Set.of(
         new SystemDependency<>(Order.AFTER, SteeringSystem.class), new SystemDependency<>(Order.BEFORE, TransformSystems.EntityTrackerUpdate.class)
      );

      public PostBehaviourSupportTickSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType) {
         this.npcComponentType = npcComponentType;
         this.transformComponentType = TransformComponent.getComponentType();
         this.query = Query.and(npcComponentType, this.transformComponentType);
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public void steppedTick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcComponentType);

         assert npcComponent != null;

         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         Role role = npcComponent.getRole();
         MotionController activeMotionController = role.getActiveMotionController();
         activeMotionController.clearOverrides();
         activeMotionController.constrainRotations(role, archetypeChunk.getComponent(index, this.transformComponentType));
         role.getCombatSupport().tick(dt);
         role.getWorldSupport().tick(dt);
         EntitySupport entitySupport = role.getEntitySupport();
         entitySupport.tick(dt);
         entitySupport.handleNominatedDisplayName(ref, commandBuffer);
         role.getStateSupport().update(commandBuffer);
         npcComponent.clearDamageData();
         role.getMarkedEntitySupport().setTargetSlotToIgnoreForAvoidance(Integer.MIN_VALUE);
         role.setReachedTerminalAction(false);
         role.getPositionCache().clear(dt);
      }
   }

   public static class PreBehaviourSupportTickSystem extends SteppableTickingSystem {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;
      @Nonnull
      private final ComponentType<EntityStore, DeathComponent> deathComponentType;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public PreBehaviourSupportTickSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType) {
         this.npcComponentType = npcComponentType;
         this.playerComponentType = Player.getComponentType();
         this.deathComponentType = DeathComponent.getComponentType();
         this.dependencies = Set.of(new SystemDependency<>(Order.BEFORE, RoleSystems.BehaviourTickSystem.class));
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.npcComponentType;
      }

      @Override
      public void steppedTick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         MarkedEntitySupport markedEntitySupport = role.getMarkedEntitySupport();
         Ref<EntityStore>[] entityTargets = markedEntitySupport.getEntityTargets();

         for (int i = 0; i < entityTargets.length; i++) {
            Ref<EntityStore> targetReference = entityTargets[i];
            if (targetReference != null) {
               if (!targetReference.isValid()) {
                  entityTargets[i] = null;
               } else {
                  Player playerComponent = commandBuffer.getComponent(targetReference, this.playerComponentType);
                  if (playerComponent != null && playerComponent.getGameMode() != GameMode.Adventure) {
                     if (playerComponent.getGameMode() != GameMode.Creative) {
                        entityTargets[i] = null;
                        continue;
                     }

                     PlayerSettings playerSettingsComponent = commandBuffer.getComponent(targetReference, PlayerSettings.getComponentType());
                     if (playerSettingsComponent == null || !playerSettingsComponent.creativeSettings().allowNPCDetection()) {
                        entityTargets[i] = null;
                        continue;
                     }
                  }

                  DeathComponent deathComponent = commandBuffer.getComponent(targetReference, this.deathComponentType);
                  if (deathComponent != null) {
                     entityTargets[i] = null;
                  }
               }
            }
         }

         role.clearOnceIfNeeded();
         role.getBodySteering().clear();
         role.getHeadSteering().clear();
         role.getIgnoredEntitiesForAvoidance().clear();
         npcComponent.invalidateCachedHorizontalSpeedMultiplier();
      }
   }

   public static class RoleActivateSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType;
      @Nonnull
      private final ComponentType<EntityStore, BoundingBox> boundingBoxComponentType;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public RoleActivateSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType) {
         this.npcComponentType = npcComponentType;
         this.modelComponentType = ModelComponent.getComponentType();
         this.boundingBoxComponentType = BoundingBox.getComponentType();
         this.query = Query.and(npcComponentType, this.modelComponentType, this.boundingBoxComponentType);
         this.dependencies = Set.of(
            new SystemDependency<>(Order.AFTER, BalancingInitialisationSystem.class), new SystemDependency<>(Order.AFTER, ModelSystems.ModelSpawned.class)
         );
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

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         NPCEntity npcComponent = holder.getComponent(this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         role.getStateSupport().activate();
         role.getDebugSupport().notifyDebugFlagsListeners(role.getDebugSupport().getDebugFlags());
         ModelComponent modelComponent = holder.getComponent(this.modelComponentType);

         assert modelComponent != null;

         BoundingBox boundingBoxComponent = holder.getComponent(this.boundingBoxComponentType);

         assert boundingBoxComponent != null;

         role.updateMotionControllers(null, modelComponent.getModel(), boundingBoxComponent.getBoundingBox(), null);
         role.clearOnce();
         role.getActiveMotionController().activate();
         holder.ensureComponent(InteractionModule.get().getChainingDataComponent());
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         NPCEntity npcComponent = holder.getComponent(this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         role.getActiveMotionController().deactivate();
         role.getWorldSupport().resetAllBlockSensors();
      }
   }

   public static class RoleDebugSystem extends SteppableTickingSystem {
      private static final float DEBUG_SHAPE_TIME = 0.1F;
      private static final float SENSOR_VIS_OPACITY = 0.4F;
      private static final double FULL_CIRCLE_EPSILON = 0.01;
      private static final float LEASH_SPHERE_RADIUS = 0.3F;
      private static final float LEASH_RING_OUTER_RADIUS = 0.5F;
      private static final float LEASH_RING_INNER_RADIUS = 0.4F;
      private static final float NPC_RING_THICKNESS = 0.1F;
      private static final float NPC_RING_OFFSET = 0.1F;
      private static final float LEASH_LINE_THICKNESS = 0.05F;
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public RoleDebugSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType, @Nonnull Set<Dependency<EntityStore>> dependencies) {
         this.npcComponentType = npcComponentType;
         this.dependencies = dependencies;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return Query.and(this.npcComponentType, TransformComponent.getComponentType(), BoundingBox.getComponentType());
      }

      @Override
      public void steppedTick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         if (role != null) {
            DebugSupport debugSupport = role.getDebugSupport();
            RoleDebugDisplay debugDisplay = debugSupport.getDebugDisplay();
            if (debugDisplay != null) {
               debugDisplay.display(role, index, archetypeChunk, commandBuffer);
            }

            if (debugSupport.isDebugFlagSet(RoleDebugFlags.VisMarkedTargets)) {
               renderMarkedTargetArrows(role, index, archetypeChunk, commandBuffer);
            }

            boolean hasSensorVis = debugSupport.hasSensorVisData();
            boolean hasLeashVis = debugSupport.isDebugFlagSet(RoleDebugFlags.VisLeashPosition);
            if (hasSensorVis || hasLeashVis) {
               Ref<EntityStore> npcRef = archetypeChunk.getReferenceTo(index);
               TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

               assert transformComponent != null;

               BoundingBox boundingBoxComponent = archetypeChunk.getComponent(index, BoundingBox.getComponentType());

               assert boundingBoxComponent != null;

               World world = commandBuffer.getExternalData().getWorld();
               if (hasSensorVis) {
                  renderSensorVisualization(debugSupport, npcRef, transformComponent, boundingBoxComponent, world, commandBuffer);
               }

               if (hasLeashVis) {
                  renderLeashPositionVisualization(npcComponent, npcRef, transformComponent, boundingBoxComponent, world);
               }
            }
         }
      }

      private static void renderMarkedTargetArrows(
         @Nonnull Role role, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Ref<EntityStore> npcRef = archetypeChunk.getReferenceTo(index);
         Transform npcLook = TargetUtil.getLook(npcRef, commandBuffer);
         Vector3d npcEyePosition = npcLook.getPosition();
         World world = commandBuffer.getExternalData().getWorld();
         MarkedEntitySupport markedEntitySupport = role.getMarkedEntitySupport();
         Ref<EntityStore>[] entityTargets = markedEntitySupport.getEntityTargets();

         for (int slotIndex = 0; slotIndex < entityTargets.length; slotIndex++) {
            Ref<EntityStore> targetRef = entityTargets[slotIndex];
            if (targetRef != null && targetRef.isValid()) {
               Transform targetLook = TargetUtil.getLook(targetRef, commandBuffer);
               Vector3d targetEyePosition = targetLook.getPosition();
               Vector3d direction = new Vector3d(
                  targetEyePosition.x - npcEyePosition.x, targetEyePosition.y - npcEyePosition.y, targetEyePosition.z - npcEyePosition.z
               );
               Vector3f color = DebugUtils.INDEXED_COLORS[slotIndex % DebugUtils.INDEXED_COLORS.length];
               DebugUtils.addArrow(world, npcEyePosition, direction, color, 0.1F, false);
            }
         }
      }

      private static void renderSensorVisualization(
         @Nonnull DebugSupport debugSupport,
         @Nonnull Ref<EntityStore> npcRef,
         @Nonnull TransformComponent transformComponent,
         @Nonnull BoundingBox boundingBoxComponent,
         @Nonnull World world,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         List<DebugSupport.SensorVisData> sensorDataList = debugSupport.getSensorVisData();
         if (sensorDataList != null) {
            Vector3d npcPosition = transformComponent.getPosition();
            double npcMidHeight = boundingBoxComponent.getBoundingBox().max.y / 2.0;
            HeadRotation headRotation = commandBuffer.getComponent(npcRef, HeadRotation.getComponentType());
            double heading = headRotation != null ? headRotation.getRotation().getYaw() : transformComponent.getRotation().getYaw();
            sensorDataList.sort((a, b) -> Double.compare(b.range(), a.range()));
            double discStackOffset = 0.1;

            for (int i = 0; i < sensorDataList.size(); i++) {
               DebugSupport.SensorVisData sensorData = sensorDataList.get(i);
               Vector3f color = DebugUtils.INDEXED_COLORS[sensorData.colorIndex() % DebugUtils.INDEXED_COLORS.length];
               double height = npcPosition.y + npcMidHeight + i * 0.1;
               if (sensorData.viewAngle() > 0.0 && sensorData.viewAngle() < 6.273185482025147) {
                  double sectorHeading = -heading + Math.PI;
                  DebugUtils.addSector(
                     world,
                     npcPosition.x,
                     height,
                     npcPosition.z,
                     sectorHeading,
                     sensorData.range(),
                     sensorData.viewAngle(),
                     sensorData.minRange(),
                     color,
                     0.4F,
                     0.1F,
                     false
                  );
               } else {
                  DebugUtils.addDisc(world, npcPosition.x, height, npcPosition.z, sensorData.range(), sensorData.minRange(), color, 0.4F, 0.1F, false);
               }
            }

            Map<Ref<EntityStore>, List<DebugSupport.EntityVisData>> entityDataMap = debugSupport.getEntityVisData();
            if (entityDataMap != null) {
               double markerOffset = 0.3;
               double sphereStackOffset = 0.3;
               double defaultEntityHeight = 2.0;

               for (Entry<Ref<EntityStore>, List<DebugSupport.EntityVisData>> entry : entityDataMap.entrySet()) {
                  Ref<EntityStore> entityRef = entry.getKey();
                  List<DebugSupport.EntityVisData> checks = entry.getValue();
                  if (!checks.isEmpty() && entityRef.isValid()) {
                     TransformComponent entityTransform = commandBuffer.getComponent(entityRef, TransformComponent.getComponentType());
                     if (entityTransform != null) {
                        Vector3d entityPosition = entityTransform.getPosition();
                        BoundingBox entityBoundingBox = commandBuffer.getComponent(entityRef, BoundingBox.getComponentType());
                        double entityHeight = entityBoundingBox != null ? entityBoundingBox.getBoundingBox().max.y : 2.0;
                        double markerBaseHeight = entityHeight + 0.3;
                        boolean anyMatched = false;

                        for (DebugSupport.EntityVisData check : checks) {
                           if (check.matched()) {
                              anyMatched = true;
                              break;
                           }
                        }

                        int sphereCount = 0;

                        for (DebugSupport.EntityVisData checkx : checks) {
                           if (checkx.matched()) {
                              Vector3f sensorColor = DebugUtils.INDEXED_COLORS[checkx.sensorColorIndex() % DebugUtils.INDEXED_COLORS.length];
                              double sphereHeight = markerBaseHeight + sphereCount * 0.3;
                              DebugUtils.addSphere(world, entityPosition.x, entityPosition.y + sphereHeight, entityPosition.z, sensorColor, 0.2, 0.1F);
                              sphereCount++;
                           }
                        }

                        if (!anyMatched) {
                           DebugUtils.addCube(world, entityPosition.x, entityPosition.y + markerBaseHeight, entityPosition.z, DebugUtils.COLOR_GRAY, 0.2, 0.1F);
                        }

                        DebugUtils.addLine(
                           world,
                           npcPosition.x,
                           npcPosition.y + npcMidHeight,
                           npcPosition.z,
                           entityPosition.x,
                           entityPosition.y + markerBaseHeight,
                           entityPosition.z,
                           DebugUtils.COLOR_GRAY,
                           0.03,
                           0.1F,
                           false
                        );
                     }
                  }
               }
            }

            debugSupport.clearSensorVisData();
         }
      }

      private static void renderLeashPositionVisualization(
         @Nonnull NPCEntity npcComponent,
         @Nonnull Ref<EntityStore> npcRef,
         @Nonnull TransformComponent transformComponent,
         @Nonnull BoundingBox boundingBoxComponent,
         @Nonnull World world
      ) {
         if (npcComponent.requiresLeashPosition()) {
            Box boundingBox = boundingBoxComponent.getBoundingBox();
            double npcWidth = boundingBox.max.x - boundingBox.min.x;
            double npcDepth = boundingBox.max.z - boundingBox.min.z;
            double npcRingOuterRadius = Math.max(npcWidth, npcDepth) / 2.0 + 0.1F;
            double npcRingInnerRadius = npcRingOuterRadius - 0.1F;
            int colorIndex = Math.abs(npcRef.getIndex()) % DebugUtils.INDEXED_COLORS.length;
            Vector3f color = DebugUtils.INDEXED_COLORS[colorIndex];
            Vector3d leashPoint = npcComponent.getLeashPoint();
            DebugUtils.addSphere(world, leashPoint, color, 0.3F, 0.1F);
            Vector3d npcPosition = transformComponent.getPosition();
            double npcMidHeight = boundingBox.max.y / 2.0;
            double npcMidY = npcPosition.y + npcMidHeight;
            double dirX = npcPosition.x - leashPoint.x;
            double dirZ = npcPosition.z - leashPoint.z;
            double horizontalDist = Math.sqrt(dirX * dirX + dirZ * dirZ);
            if (horizontalDist > 0.001) {
               double verticalDist = npcMidY - leashPoint.y;
               double pitchAngle = Math.atan2(verticalDist, horizontalDist);
               double yawAngle = Math.atan2(dirZ, dirX);
               addChainRing(world, leashPoint.x, leashPoint.y, leashPoint.z, 0.5, 0.4F, yawAngle, -pitchAngle, color);
               addChainRing(world, npcPosition.x, npcMidY, npcPosition.z, npcRingOuterRadius, npcRingInnerRadius, yawAngle + Math.PI, pitchAngle, color);
               double hDirX = dirX / horizontalDist;
               double hDirZ = dirZ / horizontalDist;
               double cosPitch = Math.cos(pitchAngle);
               double sinPitch = Math.sin(pitchAngle);
               double leashEdgeX = leashPoint.x + hDirX * 0.5 * cosPitch;
               double leashEdgeY = leashPoint.y + sinPitch * 0.5;
               double leashEdgeZ = leashPoint.z + hDirZ * 0.5 * cosPitch;
               double npcEdgeX = npcPosition.x - hDirX * npcRingOuterRadius * cosPitch;
               double npcEdgeY = npcMidY - sinPitch * npcRingOuterRadius;
               double npcEdgeZ = npcPosition.z - hDirZ * npcRingOuterRadius * cosPitch;
               DebugUtils.addLine(world, leashEdgeX, leashEdgeY, leashEdgeZ, npcEdgeX, npcEdgeY, npcEdgeZ, color, 0.05F, 0.1F, false);
            } else {
               DebugUtils.addDisc(world, leashPoint.x, leashPoint.y, leashPoint.z, 0.5, 0.4F, color, 0.8F, 0.1F, false);
               DebugUtils.addDisc(world, npcPosition.x, npcMidY, npcPosition.z, npcRingOuterRadius, npcRingInnerRadius, color, 0.8F, 0.1F, false);
               DebugUtils.addLine(world, leashPoint.x, leashPoint.y, leashPoint.z, npcPosition.x, npcMidY, npcPosition.z, color, 0.05F, 0.1F, false);
            }
         }
      }

      private static void addChainRing(
         @Nonnull World world,
         double x,
         double y,
         double z,
         double outerRadius,
         double innerRadius,
         double yawAngle,
         double pitchAngle,
         @Nonnull Vector3f color
      ) {
         Matrix4d matrix = new Matrix4d();
         matrix.identity();
         matrix.translate(x, y, z);
         Matrix4d tmp = new Matrix4d();
         matrix.rotateAxis(yawAngle, 0.0, 1.0, 0.0, tmp);
         matrix.rotateAxis(pitchAngle, 0.0, 0.0, 1.0, tmp);
         DebugUtils.addDisc(world, matrix, outerRadius, innerRadius, color, 0.8F, 0.1F, false);
      }
   }
}
