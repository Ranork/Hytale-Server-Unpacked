package com.hypixel.hytale.builtin.hytalegenerator;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.Assignments;
import javax.annotation.Nonnull;

public class PropField {
   @Nonnull
   private final Assignments assignments;
   @Nonnull
   private final PositionProvider positionProvider;
   private final int runtime;

   public PropField(int runtime, @Nonnull Assignments assignments, @Nonnull PositionProvider positionProvider) {
      this.runtime = runtime;
      this.assignments = assignments;
      this.positionProvider = positionProvider;
   }

   @Nonnull
   public PositionProvider getPositionProvider() {
      return this.positionProvider;
   }

   @Nonnull
   public Assignments getPropDistribution() {
      return this.assignments;
   }

   public int getRuntime() {
      return this.runtime;
   }
}
