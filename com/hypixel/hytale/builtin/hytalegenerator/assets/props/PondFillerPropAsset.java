package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockset.MaterialSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.ConstantMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.OriginScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.filler.PondFillerProp;
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
      .append(new KeyedCodec<>("BoundingMin", Vector3i.CODEC, true), (asset, v) -> asset.boundingMin = v, asset -> asset.boundingMin)
      .add()
      .append(new KeyedCodec<>("BoundingMax", Vector3i.CODEC, true), (asset, v) -> asset.boundingMax = v, asset -> asset.boundingMax)
      .add()
      .append(
         new KeyedCodec<>("FillMaterial", MaterialProviderAsset.CODEC, true),
         (asset, v) -> asset.fluidMaterialProviderAsset = v,
         asset -> asset.fluidMaterialProviderAsset
      )
      .add()
      .append(new KeyedCodec<>("BarrierBlockSet", MaterialSetAsset.CODEC, true), (asset, v) -> asset.solidSetAsset = v, asset -> asset.solidSetAsset)
      .add()
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (asset, v) -> asset.patternAsset = v, asset -> asset.patternAsset)
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, true), (asset, v) -> asset.scannerAsset = v, asset -> asset.scannerAsset)
      .add()
      .build();
   private Vector3i boundingMin = new Vector3i(-10, -10, -10);
   private Vector3i boundingMax = new Vector3i(10, 10, 10);
   private MaterialProviderAsset fluidMaterialProviderAsset = new ConstantMaterialProviderAsset();
   private MaterialSetAsset solidSetAsset = new MaterialSetAsset();
   private PatternAsset patternAsset = new ConstantPatternAsset();
   private ScannerAsset scannerAsset = new OriginScannerAsset();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return Prop.noProp();
      } else if (this.scannerAsset != null && this.patternAsset != null && this.fluidMaterialProviderAsset != null && this.solidSetAsset != null) {
         MaterialProvider<Material> materialProvider = this.fluidMaterialProviderAsset.build(MaterialProviderAsset.argumentFrom(argument));
         MaterialSet solidSet = this.solidSetAsset.build(argument.materialCache);
         Pattern pattern = this.patternAsset.build(PatternAsset.argumentFrom(argument));
         Scanner scanner = this.scannerAsset.build(ScannerAsset.argumentFrom(argument));
         return new PondFillerProp(this.boundingMin, this.boundingMax, solidSet, materialProvider, scanner, pattern);
      } else {
         return Prop.noProp();
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
