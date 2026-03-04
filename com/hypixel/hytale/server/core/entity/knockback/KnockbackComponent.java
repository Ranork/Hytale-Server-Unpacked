package com.hypixel.hytale.server.core.entity.knockback;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KnockbackComponent implements Component<EntityStore> {
   @Nonnull
   private Vector3d velocity;
   private ChangeVelocityType velocityType = ChangeVelocityType.Add;
   @Nullable
   private VelocityConfig velocityConfig;
   @Nonnull
   private DoubleList modifiers = new DoubleArrayList();
   private float duration;
   private float timer;

   public static ComponentType<EntityStore, KnockbackComponent> getComponentType() {
      return EntityModule.get().getKnockbackComponentType();
   }

   @Nonnull
   public Vector3d getVelocity() {
      return this.velocity;
   }

   public void setVelocity(@Nonnull Vector3d velocity) {
      this.velocity = velocity;
   }

   public ChangeVelocityType getVelocityType() {
      return this.velocityType;
   }

   public void setVelocityType(ChangeVelocityType velocityType) {
      this.velocityType = velocityType;
   }

   @Nullable
   public VelocityConfig getVelocityConfig() {
      return this.velocityConfig;
   }

   public void setVelocityConfig(@Nullable VelocityConfig velocityConfig) {
      this.velocityConfig = velocityConfig;
   }

   public void addModifier(double modifier) {
      this.modifiers.add(modifier);
   }

   public void applyModifiers() {
      for (int i = 0; i < this.modifiers.size(); i++) {
         this.velocity.scale(this.modifiers.getDouble(i));
      }

      this.modifiers.clear();
   }

   public float getDuration() {
      return this.duration;
   }

   public void setDuration(float duration) {
      this.duration = duration;
   }

   public float getTimer() {
      return this.timer;
   }

   public void incrementTimer(float time) {
      this.timer += time;
   }

   public void setTimer(float time) {
      this.timer = time;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      KnockbackComponent component = new KnockbackComponent();
      component.velocity = this.velocity;
      component.velocityType = this.velocityType;
      component.velocityConfig = this.velocityConfig;
      component.duration = this.duration;
      component.timer = this.timer;
      return component;
   }
}
