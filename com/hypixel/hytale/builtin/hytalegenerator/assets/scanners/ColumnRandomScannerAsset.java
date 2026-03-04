package com.hypixel.hytale.builtin.hytalegenerator.assets.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.assets.ValidatorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.framework.DecimalConstantsFrameworkAsset;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.ColumnRandomScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class ColumnRandomScannerAsset extends ScannerAsset {
   @Nonnull
   public static final BuilderCodec<ColumnRandomScannerAsset> CODEC = BuilderCodec.builder(
         ColumnRandomScannerAsset.class, ColumnRandomScannerAsset::new, ScannerAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("MinY", Codec.INTEGER, true), (t, k) -> t.minY = k, k -> k.minY)
      .add()
      .append(new KeyedCodec<>("MaxY", Codec.INTEGER, true), (t, k) -> t.maxY = k, k -> k.maxY)
      .add()
      .<Integer>append(new KeyedCodec<>("ResultCap", Codec.INTEGER, true), (t, k) -> t.resultCap = k, k -> k.resultCap)
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .append(new KeyedCodec<>("Seed", Codec.STRING, false), (t, k) -> t.seed = k, k -> k.seed)
      .add()
      .<String>append(new KeyedCodec<>("Strategy", Codec.STRING, false), (t, k) -> t.strategyName = k, k -> k.strategyName)
      .addValidator(ValidatorUtil.validEnumValue(ColumnRandomScanner.Strategy.values()))
      .add()
      .append(new KeyedCodec<>("RelativeToPosition", Codec.BOOLEAN, false), (t, k) -> t.isRelativeToPosition = k, k -> k.isRelativeToPosition)
      .add()
      .append(new KeyedCodec<>("BaseHeightName", Codec.STRING, false), (t, k) -> t.baseHeightName = k, k -> k.baseHeightName)
      .add()
      .build();
   private int minY = 0;
   private int maxY = 1;
   private int resultCap = 1;
   private String seed = "A";
   private String strategyName = "DART_THROW";
   private boolean isRelativeToPosition = false;
   private String baseHeightName = "";

   @Nonnull
   @Override
   public Scanner build(@Nonnull ScannerAsset.Argument argument) {
      if (super.skip()) {
         return Scanner.noScanner();
      } else {
         SeedBox childSeed = argument.parentSeed.child(this.seed);
         ColumnRandomScanner.Strategy strategy = ColumnRandomScanner.Strategy.valueOf(this.strategyName);
         if (this.isRelativeToPosition) {
            return new ColumnRandomScanner(this.minY, this.maxY, this.resultCap, childSeed.createSupplier().get(), strategy, true, 0.0);
         } else {
            Double baseHeight = DecimalConstantsFrameworkAsset.Entries.get(this.baseHeightName, argument.referenceBundle);
            if (baseHeight == null) {
               baseHeight = 0.0;
            }

            return new ColumnRandomScanner(this.minY, this.maxY, this.resultCap, childSeed.createSupplier().get(), strategy, false, baseHeight);
         }
      }
   }
}
