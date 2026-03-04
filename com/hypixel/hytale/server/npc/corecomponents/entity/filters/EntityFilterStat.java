package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.function.ToFloatFunction;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterStat;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class EntityFilterStat extends EntityFilterBase {
   public static final int COST = 200;
   protected static final ComponentType<EntityStore, EntityStatMap> ENTITY_STAT_MAP_COMPONENT_TYPE = EntityStatMap.getComponentType();
   protected final int stat;
   protected final EntityFilterStat.EntityStatTarget statTarget;
   protected final int relativeTo;
   protected final EntityFilterStat.EntityStatTarget relativeToTarget;
   protected final double minValue;
   protected final double maxValue;

   public EntityFilterStat(@Nonnull BuilderEntityFilterStat builder, @Nonnull BuilderSupport support) {
      this.stat = builder.getStat(support);
      this.statTarget = builder.getStatTarget(support);
      this.relativeTo = builder.getRelativeTo(support);
      this.relativeToTarget = builder.getRelativeToTarget(support);
      double[] valueRange = builder.getValueRange(support);
      this.minValue = valueRange[0];
      this.maxValue = valueRange[1];
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      EntityStatMap entityStatMap = store.getComponent(targetRef, ENTITY_STAT_MAP_COMPONENT_TYPE);
      EntityStatValue entityStatValue = entityStatMap.get(this.stat);
      EntityStatValue relativeEntityStatValue = entityStatMap.get(this.relativeTo);
      float statValue = this.statTarget.get(entityStatValue);
      float relativeStatValue = this.relativeToTarget.get(relativeEntityStatValue);
      double ratio = statValue / relativeStatValue;
      return ratio >= this.minValue && ratio <= this.maxValue;
   }

   @Override
   public int cost() {
      return 200;
   }

   public static enum EntityStatTarget implements Supplier<String> {
      Value("Current value", EntityStatValue::get),
      Min("Min value", EntityStatValue::getMin),
      Max("Max value", EntityStatValue::getMax);

      private final String description;
      private final ToFloatFunction<EntityStatValue> getter;

      private EntityStatTarget(String description, ToFloatFunction<EntityStatValue> getter) {
         this.description = description;
         this.getter = getter;
      }

      public String get() {
         return this.description;
      }

      public float get(EntityStatValue entityStatValue) {
         return this.getter.applyAsFloat(entityStatValue);
      }
   }
}
