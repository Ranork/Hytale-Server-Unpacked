package com.hypixel.hytale.builtin.hytalegenerator.assets.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.scanners.EmptyScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.QueueScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class QueueScannerAsset extends ScannerAsset {
   @Nonnull
   public static final BuilderCodec<QueueScannerAsset> CODEC = BuilderCodec.builder(
         QueueScannerAsset.class, QueueScannerAsset::new, ScannerAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Scanners", new ArrayCodec<>(ScannerAsset.CODEC, ScannerAsset[]::new), true),
         (asset, value) -> asset.scannerAssets = value,
         asset -> asset.scannerAssets
      )
      .add()
      .build();
   @Nonnull
   private ScannerAsset[] scannerAssets = new ScannerAsset[0];

   @Nonnull
   @Override
   public Scanner build(@Nonnull ScannerAsset.Argument argument) {
      if (super.skip()) {
         return EmptyScanner.INSTANCE;
      } else {
         List<Scanner> scanners = new ArrayList<>(this.scannerAssets.length);

         for (ScannerAsset scannerAsset : this.scannerAssets) {
            scanners.add(scannerAsset.build(argument));
         }

         return new QueueScanner(scanners);
      }
   }
}
