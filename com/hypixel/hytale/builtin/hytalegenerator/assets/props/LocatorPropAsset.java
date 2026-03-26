package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.DirectScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.LocatorProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class LocatorPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<LocatorPropAsset> CODEC = BuilderCodec.builder(LocatorPropAsset.class, LocatorPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (asset, value) -> asset.propAsset = value, asset -> asset.propAsset)
      .add()
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (asset, value) -> asset.patternAsset = value, asset -> asset.patternAsset)
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, true), (asset, value) -> asset.scannerAsset = value, asset -> asset.scannerAsset)
      .add()
      .<Integer>append(new KeyedCodec<>("PlacementCap", Codec.INTEGER, true), (asset, value) -> asset.placementCap = value, asset -> asset.placementCap)
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .build();
   @Nonnull
   private PropAsset propAsset = new EmptyPropAsset();
   @Nonnull
   private PatternAsset patternAsset = new ConstantPatternAsset();
   @Nonnull
   private ScannerAsset scannerAsset = new DirectScannerAsset();
   private int placementCap = 1;

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return EmptyProp.INSTANCE;
      } else {
         Prop prop = this.propAsset.build(argument);
         Pattern pattern = this.patternAsset.build(PatternAsset.argumentFrom(argument));
         Scanner scanner = this.scannerAsset.build(ScannerAsset.argumentFrom(argument));
         return new LocatorProp(prop, pattern, scanner, this.placementCap);
      }
   }

   @Override
   public void cleanUp() {
      this.propAsset.cleanUp();
      this.patternAsset.cleanUp();
      this.scannerAsset.cleanUp();
   }
}
