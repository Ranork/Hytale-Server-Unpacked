package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockmask.BlockMaskAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.ConstantMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.OriginScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.props.DensityProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class DensityPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<DensityPropAsset> CODEC = BuilderCodec.builder(DensityPropAsset.class, DensityPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Range", Vector3i.CODEC, true), (asset, v) -> asset.range = v, asset -> asset.range)
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
      .append(new KeyedCodec<>("Density", DensityAsset.CODEC, true), (asset, v) -> asset.densityAsset = v, asset -> asset.densityAsset)
      .add()
      .append(
         new KeyedCodec<>("Material", MaterialProviderAsset.CODEC, true), (asset, v) -> asset.materialProviderAsset = v, asset -> asset.materialProviderAsset
      )
      .add()
      .build();
   private Vector3i range = new Vector3i();
   private BlockMaskAsset placementMaskAsset = new BlockMaskAsset();
   private PatternAsset patternAsset = new ConstantPatternAsset();
   private ScannerAsset scannerAsset = new OriginScannerAsset();
   private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();
   private DensityAsset densityAsset = new ConstantDensityAsset();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return Prop.noProp();
      } else if (this.placementMaskAsset == null) {
         return Prop.noProp();
      } else {
         BlockMask placementMask = this.placementMaskAsset.build(argument.materialCache);
         return (Prop)(this.scannerAsset != null && this.patternAsset != null && this.densityAsset != null && this.materialProviderAsset != null
            ? new DensityProp(
               this.range,
               this.densityAsset.build(DensityAsset.from(argument)),
               this.materialProviderAsset.build(MaterialProviderAsset.argumentFrom(argument)),
               this.scannerAsset.build(ScannerAsset.argumentFrom(argument)),
               this.patternAsset.build(PatternAsset.argumentFrom(argument)),
               placementMask,
               new Material(argument.materialCache.EMPTY_AIR, argument.materialCache.EMPTY_FLUID)
            )
            : Prop.noProp());
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
