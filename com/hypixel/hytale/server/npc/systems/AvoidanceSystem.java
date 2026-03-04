package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import java.util.Set;
import javax.annotation.Nonnull;

public class AvoidanceSystem extends SteppableTickingSystem {
   public static final Vector3f DEBUG_COLOR_STEERING_POST = new Vector3f(0.0F, 1.0F, 0.0F);
   public static final Vector3f DEBUG_COLOR_STEERING_PRE = new Vector3f(1.0F, 0.0F, 0.0F);
   public static final Vector3f DEBUG_COLOR_AVOIDANCE = new Vector3f(1.0F, 1.0F, 1.0F);
   public static final Vector3f DEBUG_COLOR_SEPARATION = new Vector3f(0.0F, 0.0F, 1.0F);
   public static final double DEBUG_MIN_VECTOR_DRAW_LENGTH_SQUARED = 0.01;
   public static final double DEBUG_VECTORS_SCALE = 4.0;
   public static final float DEBUG_VECTORS_TIME = 0.05F;
   @Nonnull
   private final ComponentType<EntityStore, NPCEntity> componentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType;
   @Nonnull
   private final Query<EntityStore> query;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.AFTER, RoleSystems.BehaviourTickSystem.class));

   public AvoidanceSystem(@Nonnull ComponentType<EntityStore, NPCEntity> componentType) {
      this.componentType = componentType;
      this.transformComponentType = TransformComponent.getComponentType();
      this.query = Query.and(componentType, this.transformComponentType);
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
      Ref<EntityStore> npcRef = archetypeChunk.getReferenceTo(index);
      NPCEntity npcComponent = archetypeChunk.getComponent(index, this.componentType);

      assert npcComponent != null;

      Role role = npcComponent.getRole();
      if (role.isAvoidingEntities() || role.isApplySeparation()) {
         Ref<EntityStore> target = role.getMarkedEntitySupport().getTargetReferenceToIgnoreForAvoidance();
         if (target != null && target.isValid()) {
            role.getIgnoredEntitiesForAvoidance().add(target);
         }
      }

      if (!role.getActiveMotionController().isObstructed()) {
         DebugSupport debugSupport = role.getDebugSupport();
         boolean debugVisAvoidance = debugSupport.isDebugFlagSet(RoleDebugFlags.VisAvoidance);
         boolean debugVisSeparation = debugSupport.isDebugFlagSet(RoleDebugFlags.VisSeparation);
         Vector3d preBlendSteering = !debugVisSeparation && !debugVisAvoidance ? null : role.getBodySteering().getTranslation().clone();
         boolean renderSteering = false;
         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         World world = commandBuffer.getExternalData().getWorld();
         if (role.isAvoidingEntities()) {
            role.blendAvoidance(npcRef, position, role.getBodySteering(), commandBuffer);
            if (debugVisAvoidance) {
               renderSteering = true;
               renderDebugSteeringVector(position, role.getLastAvoidanceSteering(), DEBUG_COLOR_AVOIDANCE, world);
            }
         }

         if (role.isApplySeparation()) {
            role.blendSeparation(archetypeChunk.getReferenceTo(index), position, role.getBodySteering(), this.transformComponentType, commandBuffer);
            if (debugVisSeparation) {
               renderSteering = true;
               renderDebugSteeringVector(position, role.getLastSeparationSteering(), DEBUG_COLOR_SEPARATION, world);
            }
         }

         if (renderSteering) {
            renderDebugSteeringVectorInverse(position, preBlendSteering, DEBUG_COLOR_STEERING_PRE, world);
            renderDebugSteeringVector(position, role.getBodySteering().getTranslation(), DEBUG_COLOR_STEERING_POST, world);
         }
      }
   }

   private static void renderDebugSteeringVector(@Nonnull Vector3d position, @Nonnull Vector3d direction, @Nonnull Vector3f color, @Nonnull World world) {
      if (!(direction.squaredLength() < 0.01)) {
         Vector3d scaledDir = direction.clone().scale(4.0);
         DebugUtils.addArrow(world, position, scaledDir, color, 0.05F, false);
      }
   }

   private static void renderDebugSteeringVectorInverse(@Nonnull Vector3d position, @Nonnull Vector3d direction, @Nonnull Vector3f color, @Nonnull World world) {
      if (!(direction.squaredLength() < 0.01)) {
         Vector3d scaledDir = direction.clone().scale(4.0);
         Vector3d start = position.clone().subtract(scaledDir);
         DebugUtils.addArrow(world, start, scaledDir, color, 0.05F, false);
      }
   }
}
