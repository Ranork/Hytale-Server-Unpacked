package com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.basic;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.Registry;
import com.hypixel.hytale.builtin.hytalegenerator.assets.biomes.BiomeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.framework.FrameworkAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.WorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.biome.Biome;
import com.hypixel.hytale.builtin.hytalegenerator.cartas.SimpleNoiseCarta;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.rangemaps.DoubleRange;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.worldstructure.WorldStructure;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class BasicWorldStructureAsset extends WorldStructureAsset {
   @Nonnull
   public static final BuilderCodec<BasicWorldStructureAsset> CODEC = BuilderCodec.builder(
         BasicWorldStructureAsset.class, BasicWorldStructureAsset::new, WorldStructureAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Biomes", new ArrayCodec<>(BiomeRangeAsset.CODEC, BiomeRangeAsset[]::new), true),
         (asset, value) -> asset.biomeRangeAssets = value,
         asset -> asset.biomeRangeAssets
      )
      .add()
      .append(new KeyedCodec<>("Density", DensityAsset.CODEC, true), (asset, value) -> asset.densityAsset = value, asset -> asset.densityAsset)
      .add()
      .<String>append(
         new KeyedCodec<>("DefaultBiome", new ContainedAssetCodec<>(BiomeAsset.class, BiomeAsset.CODEC), true),
         (asset, value) -> asset.defaultBiomeId = value,
         asset -> asset.defaultBiomeId
      )
      .addValidatorLate(() -> BiomeAsset.VALIDATOR_CACHE.getValidator().late())
      .add()
      .<Integer>append(
         new KeyedCodec<>("DefaultTransitionDistance", Codec.INTEGER, true),
         (asset, value) -> asset.biomeTransitionDistance = value,
         asset -> asset.biomeTransitionDistance
      )
      .addValidator(Validators.greaterThan(0))
      .add()
      .<Integer>append(
         new KeyedCodec<>("MaxBiomeEdgeDistance", Codec.INTEGER, true),
         (asset, value) -> asset.maxBiomeEdgeDistance = value,
         asset -> asset.maxBiomeEdgeDistance
      )
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .append(
         new KeyedCodec<>("Framework", new ArrayCodec<>(FrameworkAsset.CODEC, FrameworkAsset[]::new), false),
         (asset, value) -> asset.frameworkAssets = value,
         asset -> asset.frameworkAssets
      )
      .add()
      .append(
         new KeyedCodec<>("SpawnPositions", PositionProviderAsset.CODEC, false),
         (asset, value) -> asset.spawnPositionsAsset = value,
         asset -> asset.spawnPositionsAsset
      )
      .add()
      .build();
   private BiomeRangeAsset[] biomeRangeAssets = new BiomeRangeAsset[0];
   private int biomeTransitionDistance = 32;
   private int maxBiomeEdgeDistance = 0;
   private DensityAsset densityAsset = new ConstantDensityAsset();
   private String defaultBiomeId = "";
   private FrameworkAsset[] frameworkAssets = new FrameworkAsset[0];
   private PositionProviderAsset spawnPositionsAsset = new ListPositionProviderAsset();

   @Nullable
   @Override
   public WorldStructure build(@Nonnull WorldStructureAsset.Argument argument) {
      ReferenceBundle referenceBundle = new ReferenceBundle();

      for (FrameworkAsset frameworkAsset : this.frameworkAssets) {
         frameworkAsset.build(argument, referenceBundle);
      }

      HashMap<BiomeAsset, Biome> biomeAssetToBiomeType = new HashMap<>();
      BiomeAsset defaultBiomeAsset = (BiomeAsset)((DefaultAssetMap)BiomeAsset.getAssetStore().getAssetMap()).getAsset(this.defaultBiomeId);
      if (defaultBiomeAsset == null) {
         LoggerUtil.getLogger().warning("Couldn't find Biome asset with id: " + this.defaultBiomeId);
         return null;
      } else {
         Biome defaultBiome = defaultBiomeAsset.build(argument.materialCache, argument.parentSeed, referenceBundle, argument.workerId);
         biomeAssetToBiomeType.put(defaultBiomeAsset, defaultBiome);
         Density noise = this.densityAsset.build(DensityAsset.from(argument, referenceBundle));
         Registry<Biome> biomeRegistry = new Registry<>();
         int defaultBiomeId = biomeRegistry.getIdOrRegister(defaultBiome);
         SimpleNoiseCarta<Integer> carta = new SimpleNoiseCarta<>(noise, defaultBiomeId);

         for (BiomeRangeAsset asset : this.biomeRangeAssets) {
            DoubleRange range = asset.getRange();
            BiomeAsset biomeAsset = asset.getBiomeAsset();
            if (biomeAsset == null) {
               LoggerUtil.getLogger().warning("Couldn't find biome asset with name " + asset.getBiomeAssetId());
            } else {
               Biome biome;
               if (biomeAssetToBiomeType.containsKey(biomeAsset)) {
                  biome = biomeAssetToBiomeType.get(biomeAsset);
               } else {
                  biome = biomeAsset.build(argument.materialCache, argument.parentSeed, referenceBundle, argument.workerId);
                  biomeAssetToBiomeType.put(biomeAsset, biome);
               }

               carta.put(range, biomeRegistry.getIdOrRegister(biome));
            }
         }

         int biomeTransitionDistance = Math.max(1, this.biomeTransitionDistance);
         PositionProvider spawnPositions = this.spawnPositionsAsset
            .build(new PositionProviderAsset.Argument(argument.parentSeed, referenceBundle, argument.workerId));
         return new WorldStructure(carta, biomeRegistry, biomeTransitionDistance, this.maxBiomeEdgeDistance, spawnPositions);
      }
   }

   @NonNullDecl
   @Override
   public PositionProviderAsset getSpawnPositionsAsset() {
      return this.spawnPositionsAsset;
   }

   @Override
   public void cleanUp() {
      this.densityAsset.cleanUp();

      for (FrameworkAsset frameworkAsset : this.frameworkAssets) {
         frameworkAsset.cleanUp();
      }

      BiomeAsset defaultBiomeAsset = (BiomeAsset)((DefaultAssetMap)BiomeAsset.getAssetStore().getAssetMap()).getAsset(this.defaultBiomeId);
      if (defaultBiomeAsset != null) {
         defaultBiomeAsset.cleanUp();
      }

      for (BiomeRangeAsset asset : this.biomeRangeAssets) {
         BiomeAsset biomeAsset = asset.getBiomeAsset();
         if (biomeAsset != null) {
            biomeAsset.cleanUp();
         }
      }
   }
}
