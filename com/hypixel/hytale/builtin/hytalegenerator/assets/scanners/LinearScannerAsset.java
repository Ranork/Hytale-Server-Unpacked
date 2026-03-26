package com.hypixel.hytale.builtin.hytalegenerator.assets.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.assets.delimiters.RangeIntAsset;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.EmptyScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.LinearScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.Axis;
import javax.annotation.Nonnull;

public class LinearScannerAsset extends ScannerAsset {
   @Nonnull
   public static final BuilderCodec<LinearScannerAsset> CODEC = BuilderCodec.builder(
         LinearScannerAsset.class, LinearScannerAsset::new, ScannerAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Axis", new EnumCodec<>(Axis.class), true), (asset, value) -> asset.axis = value, asset -> asset.axis)
      .add()
      .append(new KeyedCodec<>("Range", RangeIntAsset.CODEC, true), (asset, value) -> asset.rangeAsset = value, asset -> asset.rangeAsset)
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, false), (asset, value) -> asset.scannerAsset = value, asset -> asset.scannerAsset)
      .add()
      .append(new KeyedCodec<>("AscendingOrder", Codec.BOOLEAN, false), (asset, value) -> asset.isAscendingOrder = value, asset -> asset.isAscendingOrder)
      .add()
      .build();
   @Nonnull
   private Axis axis = Axis.Y;
   @Nonnull
   private RangeIntAsset rangeAsset = new RangeIntAsset();
   @Nonnull
   private ScannerAsset scannerAsset = new DirectScannerAsset();
   private boolean isAscendingOrder = false;

   @Nonnull
   @Override
   public Scanner build(@Nonnull ScannerAsset.Argument argument) {
      if (super.skip()) {
         return EmptyScanner.INSTANCE;
      } else {
         Scanner childScanner = this.scannerAsset.build(argument);
         return new LinearScanner(this.axis, this.rangeAsset.build(), childScanner, this.isAscendingOrder);
      }
   }
}
