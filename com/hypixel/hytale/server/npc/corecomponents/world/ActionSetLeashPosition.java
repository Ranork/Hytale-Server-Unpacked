package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionSetLeashPosition;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionSetLeashPosition extends ActionBase {
   protected final boolean toTarget;
   protected final boolean toCurrent;

   public ActionSetLeashPosition(@Nonnull BuilderActionSetLeashPosition builderActionSetLeashPosition) {
      super(builderActionSetLeashPosition);
      this.toCurrent = builderActionSetLeashPosition.isToCurrent();
      this.toTarget = builderActionSetLeashPosition.isToTarget();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      if (this.toCurrent) {
         setLeashPosition(ref, ref, store);
      } else if (this.toTarget && sensorInfo != null) {
         Ref<EntityStore> targetRef = sensorInfo.hasPosition() ? sensorInfo.getPositionProvider().getTarget() : null;
         if (targetRef != null) {
            setLeashPosition(ref, targetRef, store);
         }
      }

      return true;
   }

   protected static void setLeashPosition(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      NPCEntity selfNpcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert selfNpcComponent != null;

      TransformComponent entityTransformComponent = componentAccessor.getComponent(targetRef, TransformComponent.getComponentType());

      assert entityTransformComponent != null;

      Vector3f entityBodyRotation = entityTransformComponent.getRotation();
      selfNpcComponent.getLeashPoint().assign(entityTransformComponent.getPosition());
      selfNpcComponent.setLeashPitch(entityBodyRotation.getPitch());
      selfNpcComponent.setLeashHeading(entityBodyRotation.getYaw());
   }
}
