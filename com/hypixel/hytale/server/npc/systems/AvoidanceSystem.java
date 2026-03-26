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
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import com.hypixel.hytale.server.npc.util.VisHelper;
import java.util.Set;
import javax.annotation.Nonnull;

public class AvoidanceSystem extends SteppableTickingSystem {
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
      if (role != null) {
         if (role.isAvoidingEntities() || role.isApplySeparation()) {
            Ref<EntityStore> target = role.getMarkedEntitySupport().getTargetReferenceToIgnoreForAvoidance();
            if (target != null && target.isValid()) {
               role.getIgnoredEntitiesForAvoidance().add(target);
            }
         }

         DebugSupport debugSupport = role.getDebugSupport();
         boolean debugVisSteeringPre = debugSupport.isDebugFlagSet(RoleDebugFlags.VisSteeringPre);
         Vector3d preBlendSteering = debugVisSteeringPre ? role.getBodySteering().getTranslation().clone() : null;
         role.clearSteeringChanged();
         if (!role.getActiveMotionController().isObstructed()) {
            TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            World world = commandBuffer.getExternalData().getWorld();
            Vector3f rotation = transformComponent.getRotation();
            boolean debugVisAvoidance = debugSupport.isDebugFlagSet(RoleDebugFlags.VisAvoidance);
            boolean debugVisSeparation = debugSupport.isDebugFlagSet(RoleDebugFlags.VisSeparation);
            boolean debugVisOrientation = debugSupport.isDebugFlagSet(RoleDebugFlags.VisOrientation) && (debugVisAvoidance || debugVisSeparation);
            if (role.isAvoidingEntities()) {
               role.blendAvoidance(npcRef, position, rotation, role.getBodySteering(), commandBuffer);
               if (debugVisAvoidance) {
                  VisHelper.renderDebugVector(position, role.getLastAvoidanceSteering(), VisHelper.DEBUG_COLOR_AVOIDANCE, world);
               }
            }

            if (role.isApplySeparation()) {
               role.blendSeparation(npcRef, position, rotation, role.getBodySteering(), this.transformComponentType, commandBuffer);
               if (debugVisSeparation) {
                  VisHelper.renderDebugVector(position, role.getLastAvoidanceSteering(), VisHelper.DEBUG_COLOR_SEPARATION, world);
               }
            }

            if (debugVisOrientation && role.getBodySteering().hasDirectionHint()) {
               Vector3d hint = new Vector3d();
               PhysicsMath.vectorFromAngles(role.getBodySteering().getDirectionHint().getYaw(), role.getBodySteering().getDirectionHint().getPitch(), hint);
               hint.scale(0.25);
               VisHelper.renderDebugVector(position, hint, DebugUtils.COLOR_CYAN, world);
            }

            if (debugVisSteeringPre) {
               VisHelper.renderDebugVectorTo(position, preBlendSteering, VisHelper.DEBUG_COLOR_STEERING_PRE, world);
            }

            if (debugSupport.isDebugFlagSet(RoleDebugFlags.VisSteeringPost)) {
               VisHelper.renderDebugVector(position, role.getBodySteering().getTranslation(), VisHelper.DEBUG_COLOR_STEERING_POST, world);
            }
         }
      }
   }
}
