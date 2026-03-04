package com.hypixel.hytale.server.worldgen.cave;

import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;

public enum CavePrefabPlacement {
   CEILING((seed, x, z, caveNode) -> caveNode.getCeilingPosition(seed, x, z)),
   FLOOR((seed, x, z, caveNode) -> caveNode.getFloorPosition(seed, x, z)),
   DEFAULT((seed, x, z, caveNode) -> (int)caveNode.getBounds().fractionY(0.5));

   public static final int NO_HEIGHT = -1;
   private final CavePrefabPlacement.PrefabPlacementFunction function;

   private CavePrefabPlacement(CavePrefabPlacement.PrefabPlacementFunction function) {
      this.function = function;
   }

   public CavePrefabPlacement.PrefabPlacementFunction getFunction() {
      return this.function;
   }

   @FunctionalInterface
   public interface PrefabPlacementFunction {
      int generate(int var1, double var2, double var4, CaveNode var6);
   }
}
