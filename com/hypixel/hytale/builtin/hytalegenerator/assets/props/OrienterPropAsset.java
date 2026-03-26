package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.assets.material.OrthogonalRotationAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.DirectScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.OrienterProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class OrienterPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<OrienterPropAsset> CODEC = BuilderCodec.builder(OrienterPropAsset.class, OrienterPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (asset, value) -> asset.propAsset = value, asset -> asset.propAsset)
      .add()
      .append(
         new KeyedCodec<>("Rotations", new ArrayCodec<>(OrthogonalRotationAsset.CODEC, OrthogonalRotationAsset[]::new), true),
         (asset, value) -> asset.rotationAssets = value,
         asset -> asset.rotationAssets
      )
      .add()
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (asset, value) -> asset.patternAsset = value, asset -> asset.patternAsset)
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, true), (asset, value) -> asset.scannerAsset = value, asset -> asset.scannerAsset)
      .add()
      .append(
         new KeyedCodec<>("SelectionMode", new EnumCodec<>(OrienterProp.SelectionMode.class), true),
         (asset, value) -> asset.selectionMode = value,
         asset -> asset.selectionMode
      )
      .add()
      .append(new KeyedCodec<>("Seed", Codec.STRING, false), (asset, value) -> asset.seed = value, asset -> asset.seed)
      .add()
      .build();
   @Nonnull
   private PropAsset propAsset = new EmptyPropAsset();
   @Nonnull
   private PatternAsset patternAsset = new ConstantPatternAsset();
   @Nonnull
   private ScannerAsset scannerAsset = new DirectScannerAsset();
   @Nonnull
   private OrthogonalRotationAsset[] rotationAssets = new OrthogonalRotationAsset[0];
   @Nonnull
   private OrienterProp.SelectionMode selectionMode = OrienterProp.SelectionMode.FIRST_VALID;
   @Nonnull
   private String seed = "";

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return EmptyProp.INSTANCE;
      } else {
         SeedBox seedBox = argument.parentSeed.child(this.seed);
         Prop prop = this.propAsset.build(argument);
         Pattern pattern = this.patternAsset.build(PatternAsset.argumentFrom(argument));
         Scanner scanner = this.scannerAsset.build(ScannerAsset.argumentFrom(argument));
         List<RotationTuple> rotations = new ArrayList<>(this.rotationAssets.length);

         for (int i = 0; i < this.rotationAssets.length; i++) {
            RotationTuple rotation = this.rotationAssets[i].build();
            rotations.add(rotation);
         }

         return new OrienterProp(rotations, prop, pattern, scanner, argument.materialCache, this.selectionMode, seedBox.createSupplier().get());
      }
   }

   @Override
   public void cleanUp() {
      this.propAsset.cleanUp();
   }
}
