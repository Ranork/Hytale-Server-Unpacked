package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionSetStat;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionSetStat extends ActionBase {
   protected static final ComponentType<EntityStore, EntityStatMap> STAT_MAP_COMPONENT_TYPE = EntityStatMap.getComponentType();
   protected final int stat;
   protected final float value;
   protected final boolean add;

   public ActionSetStat(@Nonnull BuilderActionSetStat builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.stat = builder.getStat(support);
      this.value = builder.getValue(support);
      this.add = builder.isAdd(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return !super.canExecute(ref, role, sensorInfo, dt, store) ? false : store.getComponent(ref, STAT_MAP_COMPONENT_TYPE).get(this.stat) != null;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      EntityStatMap entityStatMapComponent = store.getComponent(ref, STAT_MAP_COMPONENT_TYPE);

      assert entityStatMapComponent != null;

      if (this.add) {
         entityStatMapComponent.addStatValue(this.stat, this.value);
      } else {
         entityStatMapComponent.setStatValue(this.stat, this.value);
      }

      return true;
   }
}
