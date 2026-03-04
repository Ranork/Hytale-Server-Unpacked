package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackSystems;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.system.TransformSystems;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class SteeringSystem extends SteppableTickingSystem {
   @Nonnull
   private final ComponentType<EntityStore, NPCEntity> npcEntityComponent;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;
   @Nonnull
   private final Query<EntityStore> query;

   public SteeringSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcEntityComponent) {
      this.npcEntityComponent = npcEntityComponent;
      this.dependencies = Set.of(
         new SystemDependency<>(Order.AFTER, AvoidanceSystem.class),
         new SystemDependency<>(Order.AFTER, KnockbackSystems.ApplyKnockback.class),
         new SystemDependency<>(Order.BEFORE, TransformSystems.EntityTrackerUpdate.class)
      );
      this.query = Query.and(npcEntityComponent, TransformComponent.getComponentType());
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return false;
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
      NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcEntityComponent);

      assert npcComponent != null;

      TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

      assert transformComponent != null;

      Role role = npcComponent.getRole();
      if (role != null) {
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

         try {
            if (role.getDebugSupport().isDebugMotionSteering()) {
               Vector3d position = transformComponent.getPosition();
               double x = position.getX();
               double z = position.getZ();
               float yaw = transformComponent.getRotation().getYaw();
               role.getActiveMotionController().steer(ref, role, role.getBodySteering(), role.getHeadSteering(), dt, commandBuffer);
               x = position.getX() - x;
               z = position.getZ() - z;
               double l = Math.sqrt(x * x + z * z);
               double v = l / dt;
               double vx = x / dt;
               double vz = z / dt;
               double vh = l > 0.0 ? PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(x, z)) : 0.0;
               NPCPlugin.get()
                  .getLogger()
                  .at(Level.FINER)
                  .log(
                     "=   Role    = t =%.4f v =%.4f vx=%.4f vz=%.4f h =%.4f nh=%.4f vh=%.4f",
                     dt,
                     v,
                     vx,
                     vz,
                     (180.0F / (float)Math.PI) * yaw,
                     (180.0F / (float)Math.PI) * yaw,
                     180.0F / (float)Math.PI * vh
                  );
            } else {
               role.getActiveMotionController().steer(ref, role, role.getBodySteering(), role.getHeadSteering(), dt, commandBuffer);
            }
         } catch (IllegalStateException | IllegalArgumentException var26) {
            ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.SEVERE).withCause(var26)).log();
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         }
      }
   }
}
