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

public class SaveBrushConfigOperation extends SequenceBrushOperation {
   public static final BuilderCodec<SaveBrushConfigOperation> CODEC = BuilderCodec.builder(SaveBrushConfigOperation.class, SaveBrushConfigOperation::new)
      .append(new KeyedCodec<>("StoredName", Codec.STRING), (op, val) -> op.variableNameArg = val, op -> op.variableNameArg)
      .documentation("The name to store the snapshot of this brush config under")
      .add()
      .documentation("Save a snapshot of the current brush config in order to restore it later")
      .build();
   @Nonnull
   public String variableNameArg = "Undefined";

   public SaveBrushConfigOperation() {
      super("Save Brush Config Snapshot", "Save a snapshot of the current brush config in order to restore it later", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfigCommandExecutor.storeBrushConfigSnapshot(this.variableNameArg);
   }
}
