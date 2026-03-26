package com.hypixel.hytale.builtin.buildertools.scriptedbrushes;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.saveandload.LoadOperationsFromAssetOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.BrushOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.GlobalBrushOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScriptedBrushAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ScriptedBrushAsset>> {
   public static final AssetBuilderCodec<String, ScriptedBrushAsset> CODEC = ((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(
            ScriptedBrushAsset.class,
            ScriptedBrushAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            asset -> asset.id,
            (asset, data) -> asset.data = data,
            asset -> asset.data
         )
         .append(new KeyedCodec<>("Operations", new ArrayCodec<>(BrushOperation.OPERATION_CODEC, BrushOperation[]::new)), (asset, operations) -> {
            asset.operations = new ObjectArrayList();
            if (operations != null) {
               Collections.addAll(asset.operations, operations);
            }
         }, asset -> asset.operations != null ? asset.operations.toArray(new BrushOperation[0]) : new BrushOperation[0])
         .documentation("The list of brush operations to execute sequentially")
         .add()
         .documentation("A scripted brush asset containing multiple brush operations that will be executed sequentially"))
      .build();
   private static DefaultAssetMap<String, ScriptedBrushAsset> ASSET_MAP;
   public static final String DEFAULT_EDITOR_TOOL_ID = "EditorTool_ScriptedBrushTemplate";
   @Nullable
   private static volatile Map<String, String> brushToItemCache;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected List<BrushOperation> operations = new ObjectArrayList();

   @Nonnull
   public static DefaultAssetMap<String, ScriptedBrushAsset> getAssetMap() {
      if (ASSET_MAP == null) {
         ASSET_MAP = (DefaultAssetMap<String, ScriptedBrushAsset>)AssetRegistry.getAssetStore(ScriptedBrushAsset.class).getAssetMap();
      }

      return ASSET_MAP;
   }

   @Nullable
   public static ScriptedBrushAsset get(@Nonnull String id) {
      return getAssetMap().getAsset(id);
   }

   @Nullable
   public static String getEditorToolItemId(@Nonnull String brushId) {
      Map<String, String> cache = brushToItemCache;
      if (cache == null) {
         cache = rebuildBrushToItemCache();
      }

      return cache.get(brushId);
   }

   @Nonnull
   static Map<String, String> rebuildBrushToItemCache() {
      Map<String, Item> items = Item.getAssetMap().getAssetMap();
      Object2ObjectOpenHashMap<String, String> map = new Object2ObjectOpenHashMap(items.size() / 4);

      for (Entry<String, Item> entry : items.entrySet()) {
         BuilderTool builderTool = entry.getValue().getBuilderTool();
         if (builderTool != null && builderTool.getBrushConfigurationCommand() != null && !builderTool.getBrushConfigurationCommand().isEmpty()) {
            map.put(builderTool.getBrushConfigurationCommand(), entry.getKey());
         }
      }

      brushToItemCache = map;
      return map;
   }

   public static void invalidateBrushToItemCache() {
      brushToItemCache = null;
   }

   @Nonnull
   public String getId() {
      return this.id;
   }

   @Nonnull
   public List<BrushOperation> getOperations() {
      return this.operations;
   }

   public void loadIntoExecutor(@Nonnull BrushConfigCommandExecutor executor) {
      executor.getSequentialOperations().clear();
      executor.getGlobalOperations().clear();

      for (BrushOperation operation : this.operations) {
         if (operation instanceof LoadOperationsFromAssetOperation loadOp) {
            ScriptedBrushAsset targetAsset = get(loadOp.getAssetId());
            if (targetAsset != null) {
               for (BrushOperation targetOp : targetAsset.getOperations()) {
                  if (targetOp instanceof GlobalBrushOperation) {
                     executor.getGlobalOperations().put(targetOp.getName().toLowerCase(), (GlobalBrushOperation)targetOp);
                  } else if (targetOp instanceof SequenceBrushOperation) {
                     executor.getSequentialOperations().add((SequenceBrushOperation)targetOp);
                  }
               }
            }
         } else if (operation instanceof GlobalBrushOperation) {
            executor.getGlobalOperations().put(operation.getName().toLowerCase(), (GlobalBrushOperation)operation);
         } else if (operation instanceof SequenceBrushOperation) {
            executor.getSequentialOperations().add((SequenceBrushOperation)operation);
         }
      }
   }
}
