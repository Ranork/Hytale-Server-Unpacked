package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockmask.BlockMaskAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality.DirectionalityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality.StaticDirectionalityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.OriginScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.prefab.MoldingDirection;
import com.hypixel.hytale.builtin.hytalegenerator.props.prefab.PrefabMoldingConfiguration;
import com.hypixel.hytale.builtin.hytalegenerator.props.prefab.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<PrefabPropAsset> CODEC = BuilderCodec.builder(PrefabPropAsset.class, PrefabPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(
         new KeyedCodec<>("WeightedPrefabPaths", new ArrayCodec<>(PrefabPropAsset.WeightedPathAsset.CODEC, PrefabPropAsset.WeightedPathAsset[]::new), true),
         (asset, v) -> asset.weightedPrefabPathAssets = v,
         asset -> asset.weightedPrefabPathAssets
      )
      .add()
      .append(new KeyedCodec<>("LegacyPath", Codec.BOOLEAN, false), (asset, v) -> asset.legacyPath = v, asset -> asset.legacyPath)
      .add()
      .append(
         new KeyedCodec<>("Directionality", DirectionalityAsset.CODEC, true), (asset, v) -> asset.directionalityAsset = v, asset -> asset.directionalityAsset
      )
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, true), (asset, v) -> asset.scannerAsset = v, asset -> asset.scannerAsset)
      .add()
      .append(new KeyedCodec<>("BlockMask", BlockMaskAsset.CODEC, false), (asset, v) -> asset.blockMaskAsset = v, asset -> asset.blockMaskAsset)
      .add()
      .append(new KeyedCodec<>("MoldingDirection", MoldingDirection.CODEC, false), (t, k) -> t.moldingDirectionName = k, k -> k.moldingDirectionName)
      .add()
      .append(new KeyedCodec<>("MoldingPattern", PatternAsset.CODEC, false), (asset, v) -> asset.moldingPatternAsset = v, asset -> asset.moldingPatternAsset)
      .add()
      .append(new KeyedCodec<>("MoldingScanner", ScannerAsset.CODEC, false), (asset, v) -> asset.moldingScannerAsset = v, asset -> asset.moldingScannerAsset)
      .add()
      .append(new KeyedCodec<>("MoldingChildren", Codec.BOOLEAN, false), (asset, v) -> asset.moldChildren = v, asset -> asset.moldChildren)
      .add()
      .append(new KeyedCodec<>("LoadEntities", Codec.BOOLEAN, false), (asset, v) -> asset.loadEntities = v, asset -> asset.loadEntities)
      .add()
      .build();
   private PrefabPropAsset.WeightedPathAsset[] weightedPrefabPathAssets = new PrefabPropAsset.WeightedPathAsset[0];
   private boolean legacyPath = false;
   private boolean loadEntities = true;
   private DirectionalityAsset directionalityAsset = new StaticDirectionalityAsset();
   private ScannerAsset scannerAsset = new OriginScannerAsset();
   private BlockMaskAsset blockMaskAsset = new BlockMaskAsset();
   private MoldingDirection moldingDirectionName = MoldingDirection.NONE;
   private ScannerAsset moldingScannerAsset = new OriginScannerAsset();
   private PatternAsset moldingPatternAsset = new ConstantPatternAsset();
   private boolean moldChildren = false;

   @Override
   public void cleanUp() {
      this.directionalityAsset.cleanUp();
      this.scannerAsset.cleanUp();
      this.blockMaskAsset.cleanUp();
      this.moldingScannerAsset.cleanUp();
      this.moldingPatternAsset.cleanUp();
   }

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (!super.skip() && this.weightedPrefabPathAssets.length != 0) {
         WeightedMap<List<PrefabBuffer>> prefabWeightedMap = new WeightedMap<>();

         for (PrefabPropAsset.WeightedPathAsset pathAsset : this.weightedPrefabPathAssets) {
            List<PrefabBuffer> pathPrefabs = this.loadPrefabBuffersFrom(pathAsset.path);
            if (pathPrefabs != null) {
               prefabWeightedMap.add(pathPrefabs, pathAsset.weight);
            }
         }

         if (prefabWeightedMap.size() == 0) {
            return Prop.noProp();
         } else {
            MaterialCache voxelCache = argument.materialCache;
            BlockMask blockMask;
            if (this.blockMaskAsset == null) {
               blockMask = new BlockMask();
            } else {
               blockMask = this.blockMaskAsset.build(voxelCache);
            }

            Scanner scanner = this.scannerAsset.build(ScannerAsset.argumentFrom(argument));
            Directionality directionality = this.directionalityAsset.build(DirectionalityAsset.argumentFrom(argument));
            MoldingDirection moldingDirection = this.moldingDirectionName;
            PrefabMoldingConfiguration moldingConfiguration = null;
            if (moldingDirection != MoldingDirection.DOWN && moldingDirection != MoldingDirection.UP) {
               moldingConfiguration = PrefabMoldingConfiguration.none();
            } else {
               Scanner moldingScanner = this.moldingScannerAsset == null
                  ? Scanner.noScanner()
                  : this.moldingScannerAsset.build(ScannerAsset.argumentFrom(argument));
               Pattern moldingPattern = this.moldingPatternAsset == null
                  ? Pattern.noPattern()
                  : this.moldingPatternAsset.build(PatternAsset.argumentFrom(argument));
               moldingConfiguration = new PrefabMoldingConfiguration(moldingScanner, moldingPattern, moldingDirection, this.moldChildren);
            }

            return new PrefabProp(
               prefabWeightedMap,
               scanner,
               directionality,
               voxelCache,
               blockMask,
               moldingConfiguration,
               this::loadPrefabBuffersFrom,
               argument.parentSeed,
               this.loadEntities
            );
         }
      } else {
         return Prop.noProp();
      }
   }

   @Nullable
   private List<PrefabBuffer> loadPrefabBuffersFrom(@Nonnull String path) {
      List<PrefabBuffer> pathPrefabs = new ArrayList<>();

      for (AssetPack pack : AssetModule.get().getAssetPacks()) {
         Path prefabsDir = pack.getRoot().resolve("Server");
         if (this.legacyPath) {
            prefabsDir = prefabsDir.resolve("World").resolve("Default").resolve("Prefabs");
         } else {
            prefabsDir = prefabsDir.resolve("Prefabs");
         }

         Path fullPath = PathUtil.resolvePathWithinDir(prefabsDir, path);
         if (fullPath == null) {
            LoggerUtil.getLogger().severe("Invalid prefab path: " + path);
            return null;
         }

         try {
            PrefabLoader.loadAllPrefabBuffersUnder(fullPath, pathPrefabs);
         } catch (Exception var9) {
            String msg = "Couldn't load prefab with path: " + path;
            msg = msg + "\n";
            msg = msg + ExceptionUtil.toStringWithStack(var9);
            LoggerUtil.getLogger().severe(msg);
            return null;
         }
      }

      if (pathPrefabs.isEmpty()) {
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log("This prefab path contains no prefabs: " + path);
         return null;
      } else {
         return pathPrefabs;
      }
   }

   public static class WeightedPathAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, PrefabPropAsset.WeightedPathAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, PrefabPropAsset.WeightedPathAsset> CODEC = AssetBuilderCodec.builder(
            PrefabPropAsset.WeightedPathAsset.class,
            PrefabPropAsset.WeightedPathAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, y) -> t.weight = y, t -> t.weight)
         .addValidator(Validators.greaterThanOrEqual(0.0))
         .add()
         .append(new KeyedCodec<>("Path", Codec.STRING, true), (t, out) -> t.path = out, t -> t.path)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private double weight = 1.0;
      private String path;

      public String getId() {
         return this.id;
      }
   }
}
