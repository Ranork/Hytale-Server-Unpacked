package com.hypixel.hytale.builtin.hytalegenerator.assets.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.scanners.DirectScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.EmptyScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class DirectScannerAsset extends ScannerAsset {
   @Nonnull
   public static final BuilderCodec<DirectScannerAsset> CODEC = BuilderCodec.builder(
         DirectScannerAsset.class, DirectScannerAsset::new, ScannerAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public Scanner build(@Nonnull ScannerAsset.Argument argument) {
      return (Scanner)(super.skip() ? EmptyScanner.INSTANCE : new DirectScanner());
   }
}
