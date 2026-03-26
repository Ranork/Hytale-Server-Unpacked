package com.hypixel.hytale.builtin.hytalegenerator.assets.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.assets.bounds.IntegerBounds3dAsset;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.EmptyScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.RadialScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class RadialScannerAsset extends ScannerAsset {
   @Nonnull
   public static final BuilderCodec<RadialScannerAsset> CODEC = BuilderCodec.builder(
         RadialScannerAsset.class, RadialScannerAsset::new, ScannerAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Bounds", IntegerBounds3dAsset.CODEC, true), (asset, value) -> asset.boundsAsset = value, asset -> asset.boundsAsset)
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, false), (asset, value) -> asset.scannerAsset = value, asset -> asset.scannerAsset)
      .add()
      .build();
   @Nonnull
   private IntegerBounds3dAsset boundsAsset = new IntegerBounds3dAsset();
   @Nonnull
   private ScannerAsset scannerAsset = new DirectScannerAsset();

   @Nonnull
   @Override
   public Scanner build(@Nonnull ScannerAsset.Argument argument) {
      if (super.skip()) {
         return EmptyScanner.INSTANCE;
      } else {
         Scanner childScanner = this.scannerAsset.build(argument);
         return new RadialScanner(this.boundsAsset.build(), childScanner);
      }
   }
}
