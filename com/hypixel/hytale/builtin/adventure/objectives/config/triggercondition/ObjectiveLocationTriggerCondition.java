package com.hypixel.hytale.builtin.adventure.objectives.config.triggercondition;

import com.hypixel.hytale.builtin.adventure.objectives.markers.objectivelocation.ObjectiveLocationMarker;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public abstract class ObjectiveLocationTriggerCondition {
   @Nonnull
   public static final CodecMapCodec<ObjectiveLocationTriggerCondition> CODEC = new CodecMapCodec<>("Type");

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveLocationTriggerCondition{}";
   }

   public abstract boolean isConditionMet(ComponentAccessor<EntityStore> var1, Ref<EntityStore> var2, ObjectiveLocationMarker var3);

   static {
      CODEC.register("HourRange", HourRangeTriggerCondition.class, HourRangeTriggerCondition.CODEC);
      CODEC.register("Weather", WeatherTriggerCondition.class, WeatherTriggerCondition.CODEC);
   }
}
