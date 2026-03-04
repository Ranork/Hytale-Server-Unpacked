package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorLight;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.spawning.util.LightRangePredicate;
import javax.annotation.Nonnull;

public class SensorLight extends SensorBase {
   protected final int useTargetSlot;
   @Nonnull
   protected final LightRangePredicate lightRangePredicate;

   public SensorLight(@Nonnull BuilderSensorLight builderSensorLight, @Nonnull BuilderSupport builderSupport) {
      super(builderSensorLight);
      this.useTargetSlot = builderSensorLight.getUsedTargetSlot(builderSupport);
      this.lightRangePredicate = new LightRangePredicate();
      this.lightRangePredicate.setLightRange(builderSensorLight.getLightRange(builderSupport));
      this.lightRangePredicate.setSkyLightRange(builderSensorLight.getSkyLightRange(builderSupport));
      this.lightRangePredicate.setSunlightRange(builderSensorLight.getSunlightRange(builderSupport));
      this.lightRangePredicate.setRedLightRange(builderSensorLight.getRedLightRange(builderSupport));
      this.lightRangePredicate.setGreenLightRange(builderSensorLight.getGreenLightRange(builderSupport));
      this.lightRangePredicate.setBlueLightRange(builderSensorLight.getBlueLightRange(builderSupport));
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         Ref<EntityStore> entityReference;
         if (this.useTargetSlot >= 0) {
            entityReference = role.getMarkedEntitySupport().getMarkedEntityRef(this.useTargetSlot);
            if (entityReference == null) {
               return false;
            }
         } else {
            entityReference = ref;
         }

         TransformComponent transformComponent = store.getComponent(entityReference, TransformComponent.getComponentType());

         assert transformComponent != null;

         World world = store.getExternalData().getWorld();
         return this.lightRangePredicate.test(world, transformComponent.getPosition(), store);
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
