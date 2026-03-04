package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorLeash;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import javax.annotation.Nonnull;

public class SensorLeash extends SensorBase {
   protected final double range;
   protected final double rangeSq;
   protected final PositionProvider positionProvider = new PositionProvider();

   public SensorLeash(@Nonnull BuilderSensorLeash builderSensorLeash, @Nonnull BuilderSupport builderSupport) {
      super(builderSensorLeash);
      this.range = builderSensorLeash.getRange(builderSupport);
      this.rangeSq = this.range * this.range;
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());
         Vector3d leashPoint = npcComponent.getLeashPoint();
         if (transformComponent.getPosition().distanceSquaredTo(leashPoint) > this.rangeSq) {
            this.positionProvider.setTarget(leashPoint);
            return true;
         } else {
            this.positionProvider.clear();
            return false;
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
