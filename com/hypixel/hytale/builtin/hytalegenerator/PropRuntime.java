package com.hypixel.hytale.builtin.hytalegenerator;

import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import javax.annotation.Nonnull;

public class PropRuntime {
   @Nonnull
   private final PropDistribution propDistribution;
   private final int runtime;

   public PropRuntime(int runtime, @Nonnull PropDistribution propDistribution) {
      this.runtime = runtime;
      this.propDistribution = propDistribution;
   }

   @Nonnull
   public PropDistribution getPropDistribution() {
      return this.propDistribution;
   }

   public int getRuntimeIndex() {
      return this.runtime;
   }
}
