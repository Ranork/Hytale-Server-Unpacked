package com.hypixel.hytale.server.core.modules.physics.util;

import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import javax.annotation.Nonnull;

@Deprecated
public class ForceProviderEntity extends ForceProviderStandard {
   protected BoundingBox boundingBox;
   protected ForceProviderStandardState forceProviderStandardState;
   protected double density = 700.0;

   public ForceProviderEntity(BoundingBox boundingBox) {
      this.boundingBox = boundingBox;
   }

   public void setDensity(double density) {
      this.density = density;
   }

   public void setForceProviderStandardState(ForceProviderStandardState forceProviderStandardState) {
      this.forceProviderStandardState = forceProviderStandardState;
   }

   @Override
   public ForceProviderStandardState getForceProviderStandardState() {
      return this.forceProviderStandardState;
   }

   @Override
   public double getMass(double volume) {
      return volume * this.getDensity();
   }

   @Override
   public double getVolume() {
      return this.boundingBox.getBoundingBox().getVolume();
   }

   @Override
   public double getProjectedArea(@Nonnull PhysicsBodyState bodyState, double speed) {
      double area = PhysicsMath.computeProjectedArea(bodyState.velocity, this.boundingBox.getBoundingBox());
      return area == 0.0 ? 0.0 : area / speed;
   }

   @Override
   public double getDensity() {
      return this.density;
   }

   @Override
   public double getFrictionCoefficient() {
      return 0.0;
   }
}
