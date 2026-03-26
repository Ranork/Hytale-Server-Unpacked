package com.hypixel.hytale.builtin.hytalegenerator.assets.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.assets.delimiters.RangeIntAsset;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.EmptyScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.RandomScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.Axis;
import javax.annotation.Nonnull;

public class RandomScannerAsset extends ScannerAsset {
   @Nonnull
   public static final BuilderCodec<RandomScannerAsset> CODEC = BuilderCodec.builder(
         RandomScannerAsset.class, RandomScannerAsset::new, ScannerAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Axis", new EnumCodec<>(Axis.class), true), (asset, value) -> asset.axis = value, asset -> asset.axis)
      .add()
      .append(new KeyedCodec<>("Range", RangeIntAsset.CODEC, true), (asset, value) -> asset.rangeAsset = value, asset -> asset.rangeAsset)
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, false), (asset, value) -> asset.scannerAsset = value, asset -> asset.scannerAsset)
      .add()
      .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, value) -> asset.seed = value, asset -> asset.seed)
      .add()
      .build();
   @Nonnull
   private Axis axis = Axis.Y;
   @Nonnull
   private RangeIntAsset rangeAsset = new RangeIntAsset();
   @Nonnull
   private ScannerAsset scannerAsset = new DirectScannerAsset();
   @Nonnull
   private String seed = "";

   @Nonnull
   @Override
   public Scanner build(@Nonnull ScannerAsset.Argument argument) {
      if (super.skip()) {
         return EmptyScanner.INSTANCE;
      } else {
         Scanner childScanner = this.scannerAsset.build(argument);
         SeedBox seedBox = argument.parentSeed.child(this.seed);
         return new RandomScanner(this.axis, this.rangeAsset.build(), childScanner, seedBox.createSupplier().get());
      }
   }
}
