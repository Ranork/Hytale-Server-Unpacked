package com.hypixel.hytale.server.core.modules.physics.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PhysicsValues implements Component<EntityStore> {
   @Nonnull
   public static final Double ZERO = 0.0;
   @Nonnull
   public static final BuilderCodec<PhysicsValues> CODEC = BuilderCodec.builder(PhysicsValues.class, PhysicsValues::new)
      .append(new KeyedCodec<>("Mass", Codec.DOUBLE), (instance, value) -> instance.mass = value, instance -> instance.mass)
      .addValidator(Validators.greaterThan(ZERO))
      .add()
      .<Double>append(
         new KeyedCodec<>("DragCoefficient", Codec.DOUBLE), (instance, value) -> instance.dragCoefficient = value, instance -> instance.dragCoefficient
      )
      .addValidator(Validators.greaterThanOrEqual(ZERO))
      .add()
      .append(new KeyedCodec<>("InvertedGravity", Codec.BOOLEAN), (instance, value) -> instance.invertedGravity = value, instance -> instance.invertedGravity)
      .add()
      .build();
   private static final double DEFAULT_MASS = 1.0;
   private static final double DEFAULT_DRAG_COEFFICIENT = 0.5;
   private static final boolean DEFAULT_INVERTED_GRAVITY = false;
   protected double mass;
   protected double dragCoefficient;
   protected boolean invertedGravity;

   @Nonnull
   public static ComponentType<EntityStore, PhysicsValues> getComponentType() {
      return EntityModule.get().getPhysicsValuesComponentType();
   }

   public PhysicsValues() {
      this(1.0, 0.5, false);
   }

   public PhysicsValues(@Nonnull PhysicsValues other) {
      this(other.mass, other.dragCoefficient, other.isInvertedGravity());
   }

   public PhysicsValues(double mass, double dragCoefficient, boolean invertedGravity) {
      this.mass = mass;
      this.dragCoefficient = dragCoefficient;
      this.invertedGravity = invertedGravity;
   }

   public void replaceValues(@Nonnull PhysicsValues other) {
      this.mass = other.mass;
      this.dragCoefficient = other.dragCoefficient;
      this.invertedGravity = other.invertedGravity;
   }

   public void resetToDefault() {
      this.mass = 1.0;
      this.dragCoefficient = 0.5;
      this.invertedGravity = false;
   }

   public void scale(float scale) {
      this.mass *= scale;
      this.dragCoefficient *= scale;
   }

   public double getMass() {
      return this.mass;
   }

   public double getDragCoefficient() {
      return this.dragCoefficient;
   }

   public boolean isInvertedGravity() {
      return this.invertedGravity;
   }

   @Nonnull
   public static PhysicsValues getDefault() {
      return new PhysicsValues();
   }

   @Nonnull
   @Override
   public String toString() {
      return "PhysicsValues{mass=" + this.mass + ", dragCoefficient=" + this.dragCoefficient + ", invertedGravity=" + this.invertedGravity + "}";
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new PhysicsValues(this);
   }
}
