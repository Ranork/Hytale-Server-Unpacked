package com.hypixel.hytale.builtin.hytalegenerator.assets.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.scanners.OriginScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class OriginScannerAsset extends ScannerAsset {
   @Nonnull
   public static final BuilderCodec<OriginScannerAsset> CODEC = BuilderCodec.builder(
         OriginScannerAsset.class, OriginScannerAsset::new, ScannerAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public Scanner build(@Nonnull ScannerAsset.Argument argument) {
      return (Scanner)(super.skip() ? Scanner.noScanner() : OriginScanner.getInstance());
   }
}
