package com.hypixel.hytale.server.spawning.util;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.assets.spawns.LightType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LightRangePredicate {
   private byte lightValueMin;
   private byte lightValueMax;
   private byte skyLightValueMin;
   private byte skyLightValueMax;
   private byte sunlightValueMin;
   private byte sunlightValueMax;
   private byte redLightValueMin;
   private byte redLightValueMax;
   private byte greenLightValueMin;
   private byte greenLightValueMax;
   private byte blueLightValueMin;
   private byte blueLightValueMax;
   private boolean testLightValue;
   private boolean testSkyLightValue;
   private boolean testSunlightValue;
   private boolean testRedLightValue;
   private boolean testGreenLightValue;
   private boolean testBlueLightValue;

   public static int lightToPrecentage(byte light) {
      return MathUtil.fastRound(light * 100.0F / 15.0F);
   }

   public void setLightRange(@Nonnull LightType type, double[] lightRange) {
      switch (type) {
         case Light:
            this.setLightRange(lightRange);
            break;
         case SkyLight:
            this.setSkyLightRange(lightRange);
            break;
         case Sunlight:
            this.setSunlightRange(lightRange);
            break;
         case RedLight:
            this.setRedLightRange(lightRange);
            break;
         case GreenLight:
            this.setGreenLightRange(lightRange);
            break;
         case BlueLight:
            this.setBlueLightRange(lightRange);
      }
   }

   public void setLightRange(@Nullable double[] lightRange) {
      this.testLightValue = lightRange != null;
      if (this.testLightValue) {
         this.lightValueMin = this.lightPercentageToAbsolute(lightRange[0]);
         this.lightValueMax = this.lightPercentageToAbsolute(lightRange[1]);
         this.testLightValue = this.isPartialRange(this.lightValueMin, this.lightValueMax);
      }
   }

   public void setSkyLightRange(@Nullable double[] lightRange) {
      this.testSkyLightValue = lightRange != null;
      if (this.testSkyLightValue) {
         this.skyLightValueMin = this.lightPercentageToAbsolute(lightRange[0]);
         this.skyLightValueMax = this.lightPercentageToAbsolute(lightRange[1]);
         this.testSkyLightValue = this.isPartialRange(this.skyLightValueMin, this.skyLightValueMax);
      }
   }

   public void setSunlightRange(@Nullable double[] lightRange) {
      this.testSunlightValue = lightRange != null;
      if (this.testSunlightValue) {
         this.sunlightValueMin = this.lightPercentageToAbsolute(lightRange[0]);
         this.sunlightValueMax = this.lightPercentageToAbsolute(lightRange[1]);
         this.testSunlightValue = this.isPartialRange(this.sunlightValueMin, this.sunlightValueMax);
      }
   }

   public void setRedLightRange(@Nullable double[] lightRange) {
      this.testRedLightValue = lightRange != null;
      if (this.testRedLightValue) {
         this.redLightValueMin = this.lightPercentageToAbsolute(lightRange[0]);
         this.redLightValueMax = this.lightPercentageToAbsolute(lightRange[1]);
         this.testRedLightValue = this.isPartialRange(this.redLightValueMin, this.redLightValueMax);
      }
   }

   public void setGreenLightRange(@Nullable double[] lightRange) {
      this.testGreenLightValue = lightRange != null;
      if (this.testGreenLightValue) {
         this.greenLightValueMin = this.lightPercentageToAbsolute(lightRange[0]);
         this.greenLightValueMax = this.lightPercentageToAbsolute(lightRange[1]);
         this.testGreenLightValue = this.isPartialRange(this.greenLightValueMin, this.greenLightValueMax);
      }
   }

   public void setBlueLightRange(@Nullable double[] lightRange) {
      this.testBlueLightValue = lightRange != null;
      if (this.testBlueLightValue) {
         this.blueLightValueMin = this.lightPercentageToAbsolute(lightRange[0]);
         this.blueLightValueMax = this.lightPercentageToAbsolute(lightRange[1]);
         this.testBlueLightValue = this.isPartialRange(this.blueLightValueMin, this.blueLightValueMax);
      }
   }

   public boolean isTestLightValue() {
      return this.testLightValue;
   }

   public boolean isTestSkyLightValue() {
      return this.testSkyLightValue;
   }

   public boolean isTestSunlightValue() {
      return this.testSunlightValue;
   }

   public boolean isTestRedLightValue() {
      return this.testRedLightValue;
   }

   public boolean isTestGreenLightValue() {
      return this.testGreenLightValue;
   }

   public boolean isTestBlueLightValue() {
      return this.testBlueLightValue;
   }

   public boolean test(@Nonnull World world, @Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      int x = MathUtil.floor(position.getX());
      int y = MathUtil.floor(position.getY());
      int z = MathUtil.floor(position.getZ());
      WorldTimeResource worldTimeResource = componentAccessor.getResource(WorldTimeResource.getResourceType());
      WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
      return chunk != null && this.test(chunk.getBlockChunk(), x, y, z, worldTimeResource.getSunlightFactor());
   }

   public boolean test(@Nullable BlockChunk blockChunk, int x, int y, int z, double sunlightFactor) {
      if (blockChunk == null) {
         return false;
      } else {
         if (this.testLightValue) {
            byte maxLight = calculateLightValue(blockChunk, x, y, z, sunlightFactor);
            if (!this.testLight(maxLight)) {
               return false;
            }
         }

         if (this.testSkyLightValue) {
            byte lightValue = blockChunk.getSkyLight(x, y, z);
            if (!this.testSkyLight(lightValue)) {
               return false;
            }
         }

         if (this.testSunlightValue) {
            byte lightValue = (byte)(blockChunk.getSkyLight(x, y, z) * sunlightFactor);
            if (!this.testSunlight(lightValue)) {
               return false;
            }
         }

         if (this.testRedLightValue) {
            byte lightValue = blockChunk.getRedBlockLight(x, y, z);
            if (!this.testRedLight(lightValue)) {
               return false;
            }
         }

         if (this.testGreenLightValue) {
            byte lightValue = blockChunk.getGreenBlockLight(x, y, z);
            if (!this.testGreenLight(lightValue)) {
               return false;
            }
         }

         if (this.testBlueLightValue) {
            byte lightValue = blockChunk.getBlueBlockLight(x, y, z);
            if (!this.testBlueLight(lightValue)) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean testLight(byte lightValue) {
      return this.test(lightValue, this.lightValueMin, this.lightValueMax);
   }

   public boolean testSkyLight(byte lightValue) {
      return this.test(lightValue, this.skyLightValueMin, this.skyLightValueMax);
   }

   public boolean testSunlight(byte lightValue) {
      return this.test(lightValue, this.sunlightValueMin, this.sunlightValueMax);
   }

   public boolean testRedLight(byte lightValue) {
      return this.test(lightValue, this.redLightValueMin, this.redLightValueMax);
   }

   public boolean testGreenLight(byte lightValue) {
      return this.test(lightValue, this.greenLightValueMin, this.greenLightValueMax);
   }

   public boolean testBlueLight(byte lightValue) {
      return this.test(lightValue, this.blueLightValueMin, this.blueLightValueMax);
   }

   public static byte calculateLightValue(@Nonnull BlockChunk blockChunk, int x, int y, int z, double sunlightFactor) {
      int lightValue = blockChunk.getBlockLightIntensity(x, y, z);
      byte skyLightValue = (byte)(blockChunk.getSkyLight(x, y, z) * sunlightFactor);
      return (byte)Math.max(skyLightValue, lightValue);
   }

   private boolean test(byte lightValue, byte min, byte max) {
      return lightValue >= min && lightValue <= max;
   }

   private byte lightPercentageToAbsolute(double light) {
      return (byte)MathUtil.fastRound(light * 0.15);
   }

   private boolean isPartialRange(byte min, byte max) {
      return min > 0 || max < 15;
   }
}
