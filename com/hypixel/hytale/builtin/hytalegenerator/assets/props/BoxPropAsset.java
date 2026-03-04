package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.assets.material.MaterialAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.OriginScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.props.BoxProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class BoxPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<BoxPropAsset> CODEC = BuilderCodec.builder(BoxPropAsset.class, BoxPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Range", Vector3i.CODEC, true), (asset, v) -> asset.range = v, asset -> asset.range)
      .add()
      .append(new KeyedCodec<>("Material", MaterialAsset.CODEC, true), (asset, value) -> asset.materialAsset = value, asset -> asset.materialAsset)
      .add()
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (asset, v) -> asset.patternAsset = v, asset -> asset.patternAsset)
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, true), (asset, v) -> asset.scannerAsset = v, asset -> asset.scannerAsset)
      .add()
      .build();
   private Vector3i range = new Vector3i();
   private MaterialAsset materialAsset = new MaterialAsset();
   private PatternAsset patternAsset = new ConstantPatternAsset();
   private ScannerAsset scannerAsset = new OriginScannerAsset();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return Prop.noProp();
      } else {
         Material material = this.materialAsset.build(argument.materialCache);
         return (Prop)(this.scannerAsset != null && this.patternAsset != null
            ? new BoxProp(
               this.range, material, this.scannerAsset.build(ScannerAsset.argumentFrom(argument)), this.patternAsset.build(PatternAsset.argumentFrom(argument))
            )
            : Prop.noProp());
      }
   }

   @Override
   public void cleanUp() {
      this.patternAsset.cleanUp();
      this.scannerAsset.cleanUp();
   }
}
