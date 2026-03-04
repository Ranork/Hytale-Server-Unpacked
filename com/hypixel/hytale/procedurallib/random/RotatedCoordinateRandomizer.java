package com.hypixel.hytale.procedurallib.random;

import javax.annotation.Nonnull;

public class RotatedCoordinateRandomizer implements ICoordinateRandomizer {
   protected final ICoordinateRandomizer randomizer;
   protected final CoordinateRotator rotation;

   public RotatedCoordinateRandomizer(ICoordinateRandomizer randomizer, CoordinateRotator rotation) {
      this.randomizer = randomizer;
      this.rotation = rotation;
   }

   @Override
   public double randomDoubleX(int seed, double x, double y) {
      double px = this.rotation.rotateX(x, y);
      double py = this.rotation.rotateY(x, y);
      return this.randomizer.randomDoubleX(seed, px, py);
   }

   @Override
   public double randomDoubleY(int seed, double x, double y) {
      double px = this.rotation.rotateX(x, y);
      double py = this.rotation.rotateY(x, y);
      return this.randomizer.randomDoubleY(seed, px, py);
   }

   @Override
   public double randomDoubleX(int seed, double x, double y, double z) {
      double px = this.rotation.rotateX(x, y, z);
      double py = this.rotation.rotateY(x, y, z);
      double pz = this.rotation.rotateZ(x, y, z);
      return this.randomizer.randomDoubleX(seed, px, py, pz);
   }

   @Override
   public double randomDoubleY(int seed, double x, double y, double z) {
      double px = this.rotation.rotateX(x, y, z);
      double py = this.rotation.rotateY(x, y, z);
      double pz = this.rotation.rotateZ(x, y, z);
      return this.randomizer.randomDoubleY(seed, px, py, pz);
   }

   @Override
   public double randomDoubleZ(int seed, double x, double y, double z) {
      double px = this.rotation.rotateX(x, y, z);
      double py = this.rotation.rotateY(x, y, z);
      double pz = this.rotation.rotateZ(x, y, z);
      return this.randomizer.randomDoubleZ(seed, px, py, pz);
   }

   @Nonnull
   @Override
   public String toString() {
      return "RotatedCoordinateRandomizer{randomizer=" + this.randomizer + ", rotation=" + this.rotation + "}";
   }
}
