package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorReadPosition;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import javax.annotation.Nonnull;

public class SensorReadPosition extends SensorBase {
   protected final int slot;
   protected final boolean useMarkedTarget;
   protected final double minRange;
   protected final double range;
   protected final PositionProvider positionProvider = new PositionProvider();

   public SensorReadPosition(@Nonnull BuilderSensorReadPosition builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.slot = builder.getSlot(support);
      this.useMarkedTarget = builder.isUseMarkedTarget(support);
      this.minRange = builder.getMinRange(support);
      this.range = builder.getRange(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         this.positionProvider.clear();
         return false;
      } else {
         Vector3d position;
         if (this.useMarkedTarget) {
            Ref<EntityStore> entityRef = role.getMarkedEntitySupport().getMarkedEntityRef(this.slot);
            if (entityRef == null) {
               this.positionProvider.clear();
               return false;
            }

            TransformComponent entityTransformComponent = store.getComponent(entityRef, TransformComponent.getComponentType());

            assert entityTransformComponent != null;

            position = entityTransformComponent.getPosition();
         } else {
            position = role.getMarkedEntitySupport().getStoredPosition(this.slot);
         }

         if (position.equals(Vector3d.MIN)) {
            this.positionProvider.clear();
            return false;
         } else {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            double dist2 = transformComponent.getPosition().distanceSquaredTo(position);
            if (!(dist2 > this.range * this.range) && !(dist2 < this.minRange * this.minRange)) {
               this.positionProvider.setTarget(position);
               return true;
            } else {
               this.positionProvider.clear();
               return false;
            }
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
