package com.hypixel.hytale.server.worldgen.climate;

public class ClimateColor {
   public static final int UNSET = -1;
   public static final int ISLAND = 5588019;
   public static final int ISLAND_SHORE = 7820612;
   public static final int SHORE = 14535833;
   public static final int OCEAN = 33913;
   public static final int SHALLOW_OCEAN = 3377356;
   public final int land;
   public final int shore;
   public final int ocean;
   public final int shallowOcean;

   public ClimateColor(int land, int shore, int ocean, int shallowOcean) {
      this.land = land;
      this.shore = shore;
      this.ocean = ocean;
      this.shallowOcean = shallowOcean;
   }
}
