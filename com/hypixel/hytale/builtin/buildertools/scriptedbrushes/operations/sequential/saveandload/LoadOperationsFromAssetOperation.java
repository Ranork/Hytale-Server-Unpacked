package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.saveandload;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class LoadOperationsFromAssetOperation extends SequenceBrushOperation {
   public static final BuilderCodec<LoadOperationsFromAssetOperation> CODEC = BuilderCodec.builder(
         LoadOperationsFromAssetOperation.class, LoadOperationsFromAssetOperation::new
      )
      .append(new KeyedCodec<>("AssetId", Codec.STRING), (op, val) -> op.assetId = val, op -> op.assetId)
      .documentation("The ID of the ScriptedBrushAsset to load operations from")
      .add()
      .documentation("Load and inline operations from another ScriptedBrushAsset")
      .build();
   @Nonnull
   private String assetId = "";

   public LoadOperationsFromAssetOperation() {
      super("Load Operations From Asset", "Load and inline operations from another ScriptedBrushAsset", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
   }

   @Nonnull
   public String getAssetId() {
      return this.assetId;
   }

   public void setAssetId(@Nonnull String assetId) {
      this.assetId = assetId;
   }
}
