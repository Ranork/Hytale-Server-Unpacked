package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionTakeOff;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerFly;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionTakeOff extends BodyMotionBase {
   protected final double jumpSpeed;

   public BodyMotionTakeOff(@Nonnull BuilderBodyMotionTakeOff builderBodyMotionTakeOff) {
      super(builderBodyMotionTakeOff);
      this.jumpSpeed = builderBodyMotionTakeOff.getJumpSpeed();
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
      if (!role.getActiveMotionController().matchesType(MotionControllerFly.class)) {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         role.setActiveMotionController(ref, npcComponent, "Fly", componentAccessor);
         Vector3d position = transformComponent.getPosition();
         position.setY(transformComponent.getPosition().getY() + 0.1);
         ((MotionControllerFly)role.getActiveMotionController()).takeOff(ref, this.jumpSpeed, componentAccessor);
      }

      return false;
   }
}
