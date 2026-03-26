package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockmask.BlockMaskAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.bounds.IntegerBounds3dAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.ConstantMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.DirectScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.props.DensityProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class DensityPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<DensityPropAsset> CODEC = BuilderCodec.builder(DensityPropAsset.class, DensityPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Density", DensityAsset.CODEC, true), (asset, v) -> asset.densityAsset = v, asset -> asset.densityAsset)
      .add()
      .append(
         new KeyedCodec<>("Material", MaterialProviderAsset.CODEC, true), (asset, v) -> asset.materialProviderAsset = v, asset -> asset.materialProviderAsset
      )
      .add()
      .append(new KeyedCodec<>("Bounds", IntegerBounds3dAsset.CODEC, true), (asset, v) -> asset.boundsAsset = v, asset -> asset.boundsAsset)
      .add()
      .<Vector3i>append(new KeyedCodec<>("Range", Vector3i.CODEC, true), (asset, v) -> asset.range = v, asset -> asset.range)
      .addValidator((LegacyValidator<? super Vector3i>)((v, r) -> {
         if (v.x < 0 || v.y < 0 || v.z < 0) {
            r.fail("Range has a value smaller than 0");
         }
      }))
      .add()
      .append(new KeyedCodec<>("PlacementMask", BlockMaskAsset.CODEC, true), (asset, v) -> asset.placementMaskAsset = v, asset -> asset.placementMaskAsset)
      .add()
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (asset, v) -> asset.patternAsset = v, asset -> asset.patternAsset)
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, true), (asset, v) -> asset.scannerAsset = v, asset -> asset.scannerAsset)
      .add()
      .build();
   @Nonnull
   private DensityAsset densityAsset = new ConstantDensityAsset();
   @Nonnull
   private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();
   @Nonnull
   private IntegerBounds3dAsset boundsAsset = new IntegerBounds3dAsset();
   private static final PatternAsset DEFAULT_PATTERN_ASSET = new ConstantPatternAsset();
   private static final ScannerAsset DEFAULT_SCANNER_ASSET = new DirectScannerAsset();
   private static final BlockMaskAsset DEFAULT_MASK_ASSET = new BlockMaskAsset();
   private static final Vector3i DEFAULT_RANGE_ASSET = new Vector3i();
   @Nonnull
   private Vector3i range = DEFAULT_RANGE_ASSET;
   @Nonnull
   private BlockMaskAsset placementMaskAsset = DEFAULT_MASK_ASSET;
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
         && this.range == DEFAULT_RANGE_ASSET
         && this.placementMaskAsset == DEFAULT_MASK_ASSET) {
         return new DensityProp(
            this.densityAsset.build(DensityAsset.from(argument)),
            this.materialProviderAsset.build(MaterialProviderAsset.argumentFrom(argument)),
            this.boundsAsset.build()
         );
      } else if (this.placementMaskAsset == null) {
         return EmptyProp.INSTANCE;
      } else {
         BlockMask placementMask = this.placementMaskAsset.build(argument.materialCache);
         return (Prop)(this.scannerAsset != null && this.patternAsset != null && this.densityAsset != null && this.materialProviderAsset != null
            ? new com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.DensityProp(
               this.range,
               this.densityAsset.build(DensityAsset.from(argument)),
               this.materialProviderAsset.build(MaterialProviderAsset.argumentFrom(argument)),
               this.scannerAsset.build(ScannerAsset.argumentFrom(argument)),
               this.patternAsset.build(PatternAsset.argumentFrom(argument)),
               placementMask,
               new Material(argument.materialCache.EMPTY_AIR, argument.materialCache.EMPTY_FLUID)
            )
            : EmptyProp.INSTANCE);
      }
   }

   @Override
   public void cleanUp() {
      this.placementMaskAsset.cleanUp();
      this.patternAsset.cleanUp();
      this.scannerAsset.cleanUp();
      this.materialProviderAsset.cleanUp();
      this.densityAsset.cleanUp();
   }
}
