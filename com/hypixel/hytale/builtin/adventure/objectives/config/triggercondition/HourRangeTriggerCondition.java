package com.hypixel.hytale.builtin.adventure.objectives.config.triggercondition;

import com.hypixel.hytale.builtin.adventure.objectives.markers.objectivelocation.ObjectiveLocationMarker;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class HourRangeTriggerCondition extends ObjectiveLocationTriggerCondition {
   @Nonnull
   public static final BuilderCodec<HourRangeTriggerCondition> CODEC = BuilderCodec.builder(HourRangeTriggerCondition.class, HourRangeTriggerCondition::new)
      .append(
         new KeyedCodec<>("MinHour", Codec.INTEGER),
         (hourRangeTriggerCondition, integer) -> hourRangeTriggerCondition.minHour = integer,
         hourRangeTriggerCondition -> hourRangeTriggerCondition.minHour
      )
      .add()
      .append(
         new KeyedCodec<>("MaxHour", Codec.INTEGER),
         (hourRangeTriggerCondition, integer) -> hourRangeTriggerCondition.maxHour = integer,
         hourRangeTriggerCondition -> hourRangeTriggerCondition.maxHour
      )
      .add()
      .build();
   @Nonnull
   protected static final ResourceType<EntityStore, WorldTimeResource> WORLD_TIME_RESOURCE_RESOURCE_TYPE = WorldTimeResource.getResourceType();
   protected int minHour;
   protected int maxHour;

   @Override
   public boolean isConditionMet(
      @Nonnull ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref, ObjectiveLocationMarker objectiveLocationMarker
   ) {
      int currentHour = componentAccessor.getResource(WORLD_TIME_RESOURCE_RESOURCE_TYPE).getCurrentHour();
      return this.minHour > this.maxHour
         ? currentHour >= this.minHour || currentHour < this.maxHour
         : currentHour >= this.minHour && currentHour < this.maxHour;
   }

   @Nonnull
   @Override
   public String toString() {
      return "HourRangeTriggerCondition{minHour=" + this.minHour + ", maxHour=" + this.maxHour + "} " + super.toString();
   }
}
