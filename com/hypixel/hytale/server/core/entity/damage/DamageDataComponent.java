package com.hypixel.hytale.server.core.entity.damage;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.WieldingInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageDataComponent implements Component<EntityStore> {
   @Nonnull
   private Instant lastCombatAction = Instant.MIN;
   @Nonnull
   private Instant lastDamageTime = Instant.MIN;
   @Nullable
   private WieldingInteraction currentWielding;
   @Nullable
   private Instant lastChargeTime;

   @Nonnull
   public static ComponentType<EntityStore, DamageDataComponent> getComponentType() {
      return EntityModule.get().getDamageDataComponentType();
   }

   @Nonnull
   public Instant getLastCombatAction() {
      return this.lastCombatAction;
   }

   public void setLastCombatAction(@Nonnull Instant lastCombatAction) {
      this.lastCombatAction = lastCombatAction;
   }

   @Nonnull
   public Instant getLastDamageTime() {
      return this.lastDamageTime;
   }

   public void setLastDamageTime(@Nonnull Instant lastDamageTime) {
      this.lastDamageTime = lastDamageTime;
   }

   @Nullable
   public Instant getLastChargeTime() {
      return this.lastChargeTime;
   }

   public void setLastChargeTime(@Nonnull Instant lastChargeTime) {
      this.lastChargeTime = lastChargeTime;
   }

   @Nullable
   public WieldingInteraction getCurrentWielding() {
      return this.currentWielding;
   }

   public void setCurrentWielding(@Nullable WieldingInteraction currentWielding) {
      this.currentWielding = currentWielding;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      DamageDataComponent damageDataComponent = new DamageDataComponent();
      damageDataComponent.lastCombatAction = this.lastCombatAction;
      damageDataComponent.lastDamageTime = this.lastDamageTime;
      damageDataComponent.currentWielding = this.currentWielding;
      damageDataComponent.lastChargeTime = this.lastChargeTime;
      return damageDataComponent;
   }
}
