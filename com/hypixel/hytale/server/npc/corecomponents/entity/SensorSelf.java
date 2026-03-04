package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.corecomponents.SensorWithEntityFilters;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorSelf;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import javax.annotation.Nonnull;

public class SensorSelf extends SensorWithEntityFilters {
   protected final PositionProvider positionProvider = new PositionProvider();

   public SensorSelf(@Nonnull BuilderSensorSelf builder, @Nonnull BuilderSupport support) {
      super(builder, builder.getFilters(support, null, ComponentContext.SensorSelf));
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (super.matches(ref, role, dt, store) && this.matchesFilters(ref, ref, role, store)) {
         this.positionProvider.setTarget(store.getComponent(ref, TransformComponent.getComponentType()).getPosition());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
