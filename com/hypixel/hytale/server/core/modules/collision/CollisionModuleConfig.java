package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nullable;

public class CollisionModuleConfig {
   public static final BuilderCodec<CollisionModuleConfig> CODEC = BuilderCodec.builder(CollisionModuleConfig.class, CollisionModuleConfig::new)
      .addField(new KeyedCodec<>("ExtentMax", Codec.DOUBLE), (config, d) -> config.extentMax = d, config -> config.extentMax)
      .addField(new KeyedCodec<>("DumpInvalidBlocks", Codec.BOOLEAN), (config, b) -> config.dumpInvalidBlocks = b, config -> config.dumpInvalidBlocks)
      .addField(new KeyedCodec<>("MinimumThickness", Codec.DOUBLE), (config, d) -> config.minimumThickness = d, config -> config.minimumThickness)
      .build();
   public static final double MOVEMENT_THRESHOLD = 1.0E-5;
   public static final double MOVEMENT_THRESHOLD_SQUARED = 1.0000000000000002E-10;
   public static final double EXTENT = 1.0E-5;
   private double extentMax = 0.0;
   private boolean dumpInvalidBlocks = false;
   @Nullable
   private Double minimumThickness = null;

   public double getExtentMax() {
      return this.extentMax;
   }

   public void setExtentMax(double extentMax) {
      this.extentMax = extentMax;
   }

   public boolean isDumpInvalidBlocks() {
      return this.dumpInvalidBlocks;
   }

   public void setDumpInvalidBlocks(boolean dumpInvalidBlocks) {
      this.dumpInvalidBlocks = dumpInvalidBlocks;
   }

   public double getMinimumThickness() {
      return this.minimumThickness;
   }

   public void setMinimumThickness(double minimumThickness) {
      this.minimumThickness = minimumThickness;
   }

   public boolean hasMinimumThickness() {
      return this.minimumThickness != null;
   }
}
