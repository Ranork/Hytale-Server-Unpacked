package com.hypixel.hytale.builtin.hytalegenerator.assets.noisegenerators;

import com.hypixel.hytale.builtin.hytalegenerator.fields.FastNoiseLite;
import com.hypixel.hytale.builtin.hytalegenerator.fields.noise.CellNoiseField;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

public class CellNoiseAsset extends NoiseAsset {
   @Nonnull
   private static Set<String> validCellTypes = new HashSet<>();
   @Nonnull
   public static final BuilderCodec<CellNoiseAsset> CODEC;
   private double warpScale = 1.0;
   private double warpAmount = 1.0;
   private double scale = 1.0;
   private double jitter = 0.5;
   private int octaves = 1;
   private String seedKey = "A";
   @Nonnull
   private FastNoiseLite.CellularReturnType cellType = FastNoiseLite.CellularReturnType.CellValue;

   @Nonnull
   public CellNoiseField build(@Nonnull SeedBox parentSeed) {
      SeedBox childSeed = parentSeed.child(this.seedKey);
      return new CellNoiseField(
         childSeed.createSupplier().get(),
         this.scale,
         this.scale,
         this.scale,
         this.jitter,
         this.octaves,
         this.cellType,
         FastNoiseLite.DomainWarpType.OpenSimplex2,
         this.warpAmount,
         this.warpScale
      );
   }

   static {
      for (FastNoiseLite.CellularReturnType e : FastNoiseLite.CellularReturnType.values()) {
         validCellTypes.add(e.toString());
      }

      CODEC = BuilderCodec.builder(CellNoiseAsset.class, CellNoiseAsset::new, NoiseAsset.ABSTRACT_CODEC)
         .append(new KeyedCodec<>("WarpAmount", Codec.DOUBLE, true), (asset, warpAmount) -> asset.warpAmount = warpAmount, asset -> asset.warpAmount)
         .addValidator(Validators.greaterThan(0.0))
         .add()
         .<Double>append(new KeyedCodec<>("WarpScale", Codec.DOUBLE, true), (asset, warpScale) -> asset.warpScale = warpScale, asset -> asset.warpScale)
         .addValidator(Validators.greaterThan(0.0))
         .add()
         .<Double>append(new KeyedCodec<>("Scale", Codec.DOUBLE, true), (asset, scale) -> asset.scale = scale, asset -> asset.scale)
         .addValidator(Validators.greaterThan(0.0))
         .add()
         .<Integer>append(new KeyedCodec<>("Octaves", Codec.INTEGER, true), (asset, octaves) -> asset.octaves = octaves, asset -> asset.octaves)
         .addValidator(Validators.greaterThan(0))
         .add()
         .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, seed) -> asset.seedKey = seed, asset -> asset.seedKey)
         .add()
         .<String>append(
            new KeyedCodec<>("CellType", Codec.STRING, true),
            (asset, cellType) -> asset.cellType = FastNoiseLite.CellularReturnType.valueOf(cellType),
            asset -> asset.cellType.name()
         )
         .addValidator((LegacyValidator<? super String>)((v, r) -> {
            try {
               FastNoiseLite.CellularReturnType.valueOf(v);
            } catch (IllegalArgumentException var6) {
               String msg = "Invalid CellType: " + v + ". Valid choices: ";

               for (String t : validCellTypes) {
                  msg = msg + " ";
                  msg = msg + t;
               }

               r.fail(msg);
            }
         }))
         .add()
         .build();
   }
}
