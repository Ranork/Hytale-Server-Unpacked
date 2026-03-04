package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.HeadMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderHeadMotionWatch;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HeadMotionWatch extends HeadMotionBase {
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected static final ComponentType<EntityStore, ModelComponent> MODEL_COMPONENT_TYPE = ModelComponent.getComponentType();
   protected final double relativeTurnSpeed;

   public HeadMotionWatch(@Nonnull BuilderHeadMotionWatch builderHeadMotionWatch, @Nonnull BuilderSupport support) {
      super(builderHeadMotionWatch);
      this.relativeTurnSpeed = builderHeadMotionWatch.getRelativeTurnSpeed(support);
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nullable InfoProvider sensorInfo,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (sensorInfo != null && sensorInfo.hasPosition()) {
         IPositionProvider positionProvider = sensorInfo.getPositionProvider();
         Ref<EntityStore> targetRef = positionProvider.getTarget();
         double x = positionProvider.getX();
         double y = positionProvider.getY();
         double z = positionProvider.getZ();
         if (targetRef != null) {
            ModelComponent targetModelComponent = componentAccessor.getComponent(targetRef, MODEL_COMPONENT_TYPE);
            y += targetModelComponent != null ? targetModelComponent.getModel().getEyeHeight() : 0.0;
         }

         TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         ModelComponent modelComponent = componentAccessor.getComponent(ref, MODEL_COMPONENT_TYPE);

         assert modelComponent != null;

         Model model = modelComponent.getModel();
         Vector3d position = transformComponent.getPosition();
         x -= position.getX();
         y -= position.getY() + model.getEyeHeight();
         z -= position.getZ();
         float yaw = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(x, z));
         float pitch = PhysicsMath.pitchFromDirection(x, y, z);
         desiredSteering.clearTranslation();
         desiredSteering.setYaw(yaw);
         desiredSteering.setPitch(pitch);
         desiredSteering.setRelativeTurnSpeed(this.relativeTurnSpeed);
         return true;
      } else {
         desiredSteering.clear();
         return true;
      }
   }
}
