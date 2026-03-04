package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class CraftingConfig {
   @Nonnull
   public static final BuilderCodec<CraftingConfig> CODEC = BuilderCodec.builder(CraftingConfig.class, CraftingConfig::new)
      .appendInherited(
         new KeyedCodec<>("BenchMaterialChestHorizontalSearchRadius", Codec.INTEGER),
         (gameplayConfig, o) -> gameplayConfig.benchMaterialHorizontalChestSearchRadius = o,
         gameplayConfig -> gameplayConfig.benchMaterialHorizontalChestSearchRadius,
         (gameplayConfig, parent) -> gameplayConfig.benchMaterialHorizontalChestSearchRadius = parent.benchMaterialHorizontalChestSearchRadius
      )
      .addValidator(Validators.range(0, 14))
      .documentation("The horizontal radius of search around a bench to use materials from the chests")
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("BenchMaterialChestVerticalSearchRadius", Codec.INTEGER),
         (gameplayConfig, o) -> gameplayConfig.benchMaterialVerticalChestSearchRadius = o,
         gameplayConfig -> gameplayConfig.benchMaterialVerticalChestSearchRadius,
         (gameplayConfig, parent) -> gameplayConfig.benchMaterialVerticalChestSearchRadius = parent.benchMaterialVerticalChestSearchRadius
      )
      .addValidator(Validators.range(0, 14))
      .documentation("The vertical radius of search around a bench to use materials from the chests")
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("BenchMaterialChestLimit", Codec.INTEGER),
         (gameplayConfig, o) -> gameplayConfig.benchMaterialChestLimit = o,
         gameplayConfig -> gameplayConfig.benchMaterialChestLimit,
         (gameplayConfig, parent) -> gameplayConfig.benchMaterialChestLimit = parent.benchMaterialChestLimit
      )
      .addValidator(Validators.range(0, 200))
      .documentation("The maximum number of chests a crafting bench will draw materials from")
      .add()
      .build();
   protected int benchMaterialHorizontalChestSearchRadius = 14;
   protected int benchMaterialVerticalChestSearchRadius = 6;
   protected int benchMaterialChestLimit = 100;

   public int getBenchMaterialHorizontalChestSearchRadius() {
      return this.benchMaterialHorizontalChestSearchRadius;
   }

   public int getBenchMaterialVerticalChestSearchRadius() {
      return this.benchMaterialVerticalChestSearchRadius;
   }

   public int getBenchMaterialChestLimit() {
      return this.benchMaterialChestLimit;
   }
}
