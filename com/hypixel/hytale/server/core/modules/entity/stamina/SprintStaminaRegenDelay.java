package com.hypixel.hytale.server.core.modules.entity.stamina;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class SprintStaminaRegenDelay implements Resource<EntityStore> {
   private static final AtomicInteger ASSET_VALIDATION_STATE = new AtomicInteger(0);
   protected int statIndex = 0;
   protected float statValue;
   protected int validationState = ASSET_VALIDATION_STATE.get() - 1;

   public static ResourceType<EntityStore, SprintStaminaRegenDelay> getResourceType() {
      return StaminaModule.get().getSprintRegenDelayResourceType();
   }

   public SprintStaminaRegenDelay() {
   }

   public SprintStaminaRegenDelay(@Nonnull SprintStaminaRegenDelay other) {
      this.statIndex = other.statIndex;
      this.statValue = other.statValue;
      this.validationState = other.validationState;
   }

   public int getIndex() {
      return this.statIndex;
   }

   public float getValue() {
      return this.statValue;
   }

   public boolean validate() {
      return this.validationState == ASSET_VALIDATION_STATE.get();
   }

   public boolean hasDelay() {
      return this.statIndex != 0 && this.statValue < 0.0F;
   }

   public void markEmpty() {
      this.update(0, 0.0F);
   }

   public void update(int statIndex, float statValue) {
      this.statIndex = statIndex;
      this.statValue = statValue;
      this.validationState = ASSET_VALIDATION_STATE.get();
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      return new SprintStaminaRegenDelay(this);
   }

   @Nonnull
   @Override
   public String toString() {
      return "SprintStaminaRegenDelay{statIndex=" + this.statIndex + ", statValue=" + this.statValue + ", validationState=" + this.validationState + "}";
   }

   public static void invalidateResources() {
      ASSET_VALIDATION_STATE.incrementAndGet();
   }
}
