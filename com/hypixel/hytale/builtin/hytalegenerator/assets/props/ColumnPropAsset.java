package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockmask.BlockMaskAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.material.MaterialAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality.DirectionalityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality.StaticDirectionalityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.OriginScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.props.ColumnProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class ColumnPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<ColumnPropAsset> CODEC = BuilderCodec.builder(ColumnPropAsset.class, ColumnPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(
         new KeyedCodec<>("ColumnBlocks", new ArrayCodec<>(ColumnPropAsset.ColumnBlock.CODEC, ColumnPropAsset.ColumnBlock[]::new), true),
         (asset, v) -> asset.columnBlocks = v,
         asset -> asset.columnBlocks
      )
      .add()
      .append(new KeyedCodec<>("BlockMask", BlockMaskAsset.CODEC, false), (asset, v) -> asset.blockMaskAsset = v, asset -> asset.blockMaskAsset)
      .add()
      .append(
         new KeyedCodec<>("Directionality", DirectionalityAsset.CODEC, true), (asset, v) -> asset.directionalityAsset = v, asset -> asset.directionalityAsset
      )
      .add()
      .append(new KeyedCodec<>("Scanner", ScannerAsset.CODEC, true), (asset, v) -> asset.scannerAsset = v, asset -> asset.scannerAsset)
      .add()
      .build();
   private ColumnPropAsset.ColumnBlock[] columnBlocks = new ColumnPropAsset.ColumnBlock[0];
   private BlockMaskAsset blockMaskAsset = new BlockMaskAsset();
   private DirectionalityAsset directionalityAsset = new StaticDirectionalityAsset();
   private ScannerAsset scannerAsset = new OriginScannerAsset();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return Prop.noProp();
      } else if (this.directionalityAsset == null) {
         return Prop.noProp();
      } else {
         ArrayList<Integer> blockPositions = new ArrayList<>();
         ArrayList<Material> blockTypes = new ArrayList<>();

         for (int i = 0; i < this.columnBlocks.length; i++) {
            blockPositions.add(this.columnBlocks[i].y);
            blockTypes.add(this.columnBlocks[i].materialAsset.build(argument.materialCache));
         }

         BlockMask blockMask;
         if (this.blockMaskAsset != null) {
            blockMask = this.blockMaskAsset.build(argument.materialCache);
         } else {
            blockMask = new BlockMask();
         }

         Directionality directionality = this.directionalityAsset.build(DirectionalityAsset.argumentFrom(argument));
         Scanner scanner = this.scannerAsset.build(ScannerAsset.argumentFrom(argument));
         return new ColumnProp(blockPositions, blockTypes, blockMask, scanner, directionality, argument.materialCache);
      }
   }

   @Override
   public void cleanUp() {
      this.blockMaskAsset.cleanUp();
      this.directionalityAsset.cleanUp();
      this.scannerAsset.cleanUp();
   }

   public static class ColumnBlock implements JsonAssetWithMap<String, DefaultAssetMap<String, ColumnPropAsset.ColumnBlock>> {
      @Nonnull
      public static final AssetBuilderCodec<String, ColumnPropAsset.ColumnBlock> CODEC = AssetBuilderCodec.builder(
            ColumnPropAsset.ColumnBlock.class,
            ColumnPropAsset.ColumnBlock::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Y", Codec.INTEGER, true), (t, y) -> t.y = y, t -> t.y)
         .add()
         .append(new KeyedCodec<>("Material", MaterialAsset.CODEC, true), (asset, value) -> asset.materialAsset = value, asset -> asset.materialAsset)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private int y = 1;
      private MaterialAsset materialAsset = new MaterialAsset("Empty", "Empty", false);

      public String getId() {
         return this.id;
      }
   }
}
