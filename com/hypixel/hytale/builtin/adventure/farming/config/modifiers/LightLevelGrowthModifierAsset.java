package com.hypixel.hytale.builtin.adventure.farming.config.modifiers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Range;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.GrowthModifierAsset;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkLightData;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class LightLevelGrowthModifierAsset extends GrowthModifierAsset {
   @Nonnull
   public static final BuilderCodec<LightLevelGrowthModifierAsset> CODEC = BuilderCodec.builder(
         LightLevelGrowthModifierAsset.class, LightLevelGrowthModifierAsset::new, ABSTRACT_CODEC
      )
      .addField(
         new KeyedCodec<>("ArtificialLight", LightLevelGrowthModifierAsset.ArtificialLight.CODEC),
         (lightLevel, artificialLight) -> lightLevel.artificialLight = artificialLight,
         lightLevel -> lightLevel.artificialLight
      )
      .addField(
         new KeyedCodec<>("Sunlight", ProtocolCodecs.RANGEF), (lightLevel, sunLight) -> lightLevel.sunlight = sunLight, lightLevel -> lightLevel.sunlight
      )
      .addField(
         new KeyedCodec<>("RequireBoth", Codec.BOOLEAN),
         (lightLevel, requireBoth) -> lightLevel.requireBoth = requireBoth,
         lightLevel -> lightLevel.requireBoth
      )
      .build();
   protected LightLevelGrowthModifierAsset.ArtificialLight artificialLight;
   protected Rangef sunlight;
   protected boolean requireBoth;

   public LightLevelGrowthModifierAsset.ArtificialLight getArtificialLight() {
      return this.artificialLight;
   }

   public Rangef getSunlight() {
      return this.sunlight;
   }

   public boolean isRequireBoth() {
      return this.requireBoth;
   }

   protected boolean checkArtificialLight(byte red, byte green, byte blue) {
      LightLevelGrowthModifierAsset.ArtificialLight artificialLight = this.artificialLight;
      Range redRange = artificialLight.getRed();
      Range greenRange = artificialLight.getGreen();
      Range blueRange = artificialLight.getBlue();
      return isInRange(redRange, red) && isInRange(greenRange, green) && isInRange(blueRange, blue);
   }

   protected boolean checkSunLight(@Nonnull WorldTimeResource worldTimeResource, byte sky) {
      Rangef range = this.sunlight;
      double sunlightFactor = worldTimeResource.getSunlightFactor();
      double daylight = sunlightFactor * sky;
      return range.min <= daylight && daylight <= range.max;
   }

   protected static boolean isInRange(@Nonnull Range range, int value) {
      return range.min <= value && value <= range.max;
   }

   @Override
   public double getCurrentGrowthMultiplier(
      @Nonnull CommandBuffer<ChunkStore> commandBuffer,
      @Nonnull Ref<ChunkStore> sectionRef,
      @Nonnull Ref<ChunkStore> blockRef,
      int x,
      int y,
      int z,
      boolean initialTick
   ) {
      BlockSection blockSectionComponent = commandBuffer.getComponent(sectionRef, BlockSection.getComponentType());

      assert blockSectionComponent != null;

      short lightRaw = blockSectionComponent.getGlobalLight().getLightRaw(x, y, z);
      byte redLight = ChunkLightData.getLightValue(lightRaw, 0);
      byte greenLight = ChunkLightData.getLightValue(lightRaw, 1);
      byte blueLight = ChunkLightData.getLightValue(lightRaw, 2);
      byte skyLight = ChunkLightData.getLightValue(lightRaw, 3);
      World world = commandBuffer.getExternalData().getWorld();
      EntityStore entityStore = world.getEntityStore();
      WorldTimeResource worldTimeResource = entityStore.getStore().getResource(WorldTimeResource.getResourceType());
      boolean active = false;
      boolean onlySunlight = false;
      if (this.requireBoth) {
         active = this.checkArtificialLight(redLight, greenLight, blueLight) && this.checkSunLight(worldTimeResource, skyLight);
      } else if (this.checkArtificialLight(redLight, greenLight, blueLight)) {
         active = true;
      } else if (this.checkSunLight(worldTimeResource, skyLight)) {
         active = true;
         onlySunlight = true;
      }

      if (active) {
         return onlySunlight && initialTick
            ? super.getCurrentGrowthMultiplier(commandBuffer, sectionRef, blockRef, x, y, z, initialTick) * 0.6F
            : super.getCurrentGrowthMultiplier(commandBuffer, sectionRef, blockRef, x, y, z, initialTick);
      } else {
         return 1.0;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "LightLevelGrowthModifierAsset{artificialLight="
         + this.artificialLight
         + ", sunLight="
         + this.sunlight
         + ", requireBoth="
         + this.requireBoth
         + "} "
         + super.toString();
   }

   public static class ArtificialLight {
      @Nonnull
      public static final BuilderCodec<LightLevelGrowthModifierAsset.ArtificialLight> CODEC = BuilderCodec.builder(
            LightLevelGrowthModifierAsset.ArtificialLight.class, LightLevelGrowthModifierAsset.ArtificialLight::new
         )
         .addField(new KeyedCodec<>("Red", ProtocolCodecs.RANGE), (light, red) -> light.red = red, light -> light.red)
         .addField(new KeyedCodec<>("Green", ProtocolCodecs.RANGE), (light, green) -> light.green = green, light -> light.green)
         .addField(new KeyedCodec<>("Blue", ProtocolCodecs.RANGE), (light, blue) -> light.blue = blue, light -> light.blue)
         .build();
      protected Range red;
      protected Range green;
      protected Range blue;

      public Range getRed() {
         return this.red;
      }

      public Range getGreen() {
         return this.green;
      }

      public Range getBlue() {
         return this.blue;
      }

      @Nonnull
      @Override
      public String toString() {
         return "ArtificialLightLevel{red=" + this.red + ", green=" + this.green + ", blue=" + this.blue + "}";
      }
   }
}
