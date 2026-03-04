package com.hypixel.hytale.builtin.fluid;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public record FluidState(int fluidLevel, byte verticalFill) {
   public static int SOURCE_LEVEL = 0;
   public static final int FULL_LEVEL = 8;
   @Nonnull
   public static final FluidState[] FLUID_STATES = generateFluidStates(8);

   public FluidState(int fluidLevel, int verticalFill) {
      this(fluidLevel, (byte)verticalFill);
   }

   @Nonnull
   public static FluidState[] generateFluidStates(int maxLevel) {
      List<FluidState> fluidStateList = new ObjectArrayList();
      fluidStateList.add(new FluidState(SOURCE_LEVEL, maxLevel));

      for (int i = 1; i <= maxLevel; i++) {
         fluidStateList.add(new FluidState(i, i));
      }

      return fluidStateList.toArray(FluidState[]::new);
   }

   @Nonnull
   @Override
   public String toString() {
      return "FluidState{fluidLevel=" + this.fluidLevel + ", verticalFill=" + this.verticalFill + "}";
   }
}
