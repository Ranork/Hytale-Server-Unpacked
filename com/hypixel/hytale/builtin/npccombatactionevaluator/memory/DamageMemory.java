package com.hypixel.hytale.builtin.npccombatactionevaluator.memory;

import com.hypixel.hytale.builtin.npccombatactionevaluator.NPCCombatActionEvaluatorPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DamageMemory implements Component<EntityStore> {
   private float recentDamage;
   private float totalCombatDamage;

   public static ComponentType<EntityStore, DamageMemory> getComponentType() {
      return NPCCombatActionEvaluatorPlugin.get().getDamageMemoryComponentType();
   }

   public float getRecentDamage() {
      return this.recentDamage;
   }

   public float getTotalCombatDamage() {
      return this.totalCombatDamage;
   }

   public void addDamage(float damage) {
      this.totalCombatDamage += damage;
      this.recentDamage += damage;
   }

   public void clearRecentDamage() {
      this.recentDamage = 0.0F;
   }

   public void clearTotalDamage() {
      this.totalCombatDamage = 0.0F;
      this.clearRecentDamage();
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      DamageMemory damageMemory = new DamageMemory();
      damageMemory.recentDamage = this.recentDamage;
      damageMemory.totalCombatDamage = this.totalCombatDamage;
      return damageMemory;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DamageMemory{recentDamage=" + this.recentDamage + ", totalCombatDamage=" + this.totalCombatDamage + "}" + super.toString();
   }
}
