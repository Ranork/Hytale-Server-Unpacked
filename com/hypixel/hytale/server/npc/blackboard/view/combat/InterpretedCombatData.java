package com.hypixel.hytale.server.npc.blackboard.view.combat;

import javax.annotation.Nonnull;

public class InterpretedCombatData {
   private String attack;
   private boolean charging;
   private float currentElapsedTime;
   private boolean performingMeleeAttack;
   private boolean performingRangedAttack;
   private boolean performingBlock;

   public String getAttack() {
      return this.attack;
   }

   public void setAttack(String attack) {
      this.attack = attack;
   }

   public boolean isCharging() {
      return this.charging;
   }

   public void setCharging(boolean charging) {
      this.charging = charging;
   }

   public float getCurrentElapsedTime() {
      return this.currentElapsedTime;
   }

   public void setCurrentElapsedTime(float currentElapsedTime) {
      this.currentElapsedTime = currentElapsedTime;
   }

   public boolean isPerformingMeleeAttack() {
      return this.performingMeleeAttack;
   }

   public void setPerformingMeleeAttack(boolean performingMeleeAttack) {
      this.performingMeleeAttack = performingMeleeAttack;
   }

   public boolean isPerformingRangedAttack() {
      return this.performingRangedAttack;
   }

   public void setPerformingRangedAttack(boolean performingRangedAttack) {
      this.performingRangedAttack = performingRangedAttack;
   }

   public boolean isPerformingBlock() {
      return this.performingBlock;
   }

   public void setPerformingBlock(boolean performingBlock) {
      this.performingBlock = performingBlock;
   }

   @Nonnull
   public InterpretedCombatData clone() {
      InterpretedCombatData data = new InterpretedCombatData();
      data.attack = this.attack;
      data.charging = this.charging;
      data.currentElapsedTime = this.currentElapsedTime;
      data.performingMeleeAttack = this.performingMeleeAttack;
      data.performingRangedAttack = this.performingRangedAttack;
      data.performingBlock = this.performingBlock;
      return data;
   }
}
