package com.hypixel.hytale.server.npc.corecomponents.interaction;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.interaction.builders.BuilderSensorCanInteract;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class SensorCanInteract extends SensorBase {
   protected final float viewCone;
   protected final EnumSet<Attitude> attitudes;

   public SensorCanInteract(@Nonnull BuilderSensorCanInteract builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.viewCone = builder.getViewSectorRadians(support);
      this.attitudes = builder.getAttitudes(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         Ref<EntityStore> targetRef = role.getStateSupport().getInteractionIterationTarget();
         if (targetRef == null) {
            return false;
         } else {
            Archetype<EntityStore> targetArchetype = store.getArchetype(targetRef);
            if (targetArchetype.contains(DeathComponent.getComponentType())) {
               return false;
            } else {
               TransformComponent targetTransformComponent = store.getComponent(targetRef, TransformComponent.getComponentType());
               if (targetTransformComponent == null) {
                  return false;
               } else {
                  Vector3d targetPosition = targetTransformComponent.getPosition();
                  if (this.viewCone > 0.0F) {
                     TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

                     assert transformComponent != null;

                     HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

                     assert headRotationComponent != null;

                     Vector3d position = transformComponent.getPosition();
                     float headRotationYaw = headRotationComponent.getRotation().getYaw();
                     if (!NPCPhysicsMath.inViewSector(
                        position.getX(), position.getZ(), headRotationYaw, this.viewCone, targetPosition.getX(), targetPosition.getZ()
                     )) {
                        return false;
                     }
                  }

                  Attitude attitude = role.getWorldSupport().getAttitude(ref, targetRef, store);
                  return this.attitudes.contains(attitude);
               }
            }
         }
      }
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      role.getWorldSupport().requireAttitudeCache();
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
