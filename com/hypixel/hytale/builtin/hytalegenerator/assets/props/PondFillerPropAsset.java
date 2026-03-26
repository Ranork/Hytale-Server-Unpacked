package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockset.MaterialSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.bounds.IntegerBounds3dAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.ConstantMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.DirectScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.PondFillerProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class PondFillerPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<PondFillerPropAsset> CODEC = BuilderCodec.builder(
         PondFillerPropAsset.class, PondFillerPropAsset::new, PropAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("FillMaterial", MaterialProviderAsset.CODEC, true),
         (asset, v) -> asset.fluidMaterialProviderAsset = v,
         asset -> asset.fluidMaterialProviderAsset
      )
      .add()
      .append(new KeyedCodec<>("BarrierBlockSet", MaterialSetAsset.CODEC, true), (asset, v) -> asset.solidSetAsset = v, asset -> asset.solidSetAsset)
      .add()
      .append(new KeyedCodec<>("Bounds", IntegerBounds3dAsset.CODEC, true), (asset, v) -> asset.boundsAsset = v, asset -> asset.boundsAsset)
      .add()
      .append(new KeyedCodec<>("BoundingMin", Vector3i.CODEC, true), (asset, v) -> asset.boundingMin = v, asset -> asset.boundingMin)
      .add()
      .append(new KeyedCodec<>("BoundingMax", Vector3i.CODEC, true), (asset, v) -> asset.boundingMax = v, asset -> asset.boundingMax)
      .add()
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (asset, v) -> asset.patternAsset = v, asset -> asset.patternAsset)
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, true), (asset, v) -> asset.scannerAsset = v, asset -> asset.scannerAsset)
      .add()
      .build();
   @Nonnull
   private IntegerBounds3dAsset boundsAsset = new IntegerBounds3dAsset();
   @Nonnull
   private MaterialProviderAsset fluidMaterialProviderAsset = new ConstantMaterialProviderAsset();
   @Nonnull
   private MaterialSetAsset solidSetAsset = new MaterialSetAsset();
   private static final PatternAsset DEFAULT_PATTERN_ASSET = new ConstantPatternAsset();
   private static final ScannerAsset DEFAULT_SCANNER_ASSET = new DirectScannerAsset();
   private static final Vector3i DEFAULT_MIN_ASSET = new Vector3i(-10, -10, -10);
   private static final Vector3i DEFAULT_MAX_ASSET = new Vector3i(10, 10, 10);
   @Nonnull
   private Vector3i boundingMin = DEFAULT_MIN_ASSET;
   @Nonnull
   private Vector3i boundingMax = DEFAULT_MAX_ASSET;
   @Nonnull
   private PatternAsset patternAsset = DEFAULT_PATTERN_ASSET;
   @Nonnull
   private ScannerAsset scannerAsset = DEFAULT_SCANNER_ASSET;

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return EmptyProp.INSTANCE;
      } else if (this.patternAsset == DEFAULT_PATTERN_ASSET
         && this.scannerAsset == DEFAULT_SCANNER_ASSET
         && this.boundingMin == DEFAULT_MIN_ASSET
         && this.boundingMax == DEFAULT_MAX_ASSET) {
         return new PondFillerProp(
            this.boundsAsset.build(),
            this.fluidMaterialProviderAsset.build(MaterialProviderAsset.argumentFrom(argument)),
            this.solidSetAsset.build(argument.materialCache)
         );
      } else if (this.scannerAsset != null && this.patternAsset != null && this.fluidMaterialProviderAsset != null && this.solidSetAsset != null) {
         MaterialProvider<Material> materialProvider = this.fluidMaterialProviderAsset.build(MaterialProviderAsset.argumentFrom(argument));
         MaterialSet solidSet = this.solidSetAsset.build(argument.materialCache);
         Pattern pattern = this.patternAsset.build(PatternAsset.argumentFrom(argument));
         Scanner scanner = this.scannerAsset.build(ScannerAsset.argumentFrom(argument));
         return new com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.filler.PondFillerProp(
            this.boundingMin, this.boundingMax, solidSet, materialProvider, scanner, pattern
         );
      } else {
         return EmptyProp.INSTANCE;
      }
   }

   @Override
   public void cleanUp() {
      this.fluidMaterialProviderAsset.cleanUp();
      this.solidSetAsset.cleanUp();
      this.patternAsset.cleanUp();
      this.scannerAsset.cleanUp();
   }
}
