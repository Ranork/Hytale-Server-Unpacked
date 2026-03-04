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

public class SaveIndexOperation extends SequenceBrushOperation {
   public static final BuilderCodec<SaveIndexOperation> CODEC = BuilderCodec.builder(SaveIndexOperation.class, SaveIndexOperation::new)
      .append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.variableNameArg = val, op -> op.variableNameArg)
      .documentation("The name to store the current execution index at")
      .add()
      .documentation("Mark this spot in the stack in order to loop or jump to it")
      .build();
   @Nonnull
   public String variableNameArg = "Undefined";

   public SaveIndexOperation() {
      super("Store Current Operation Index", "Mark this spot in the stack in order to loop or jump to it", false);
   }

   @Override
   public void preExecutionModifyBrushConfig(@Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor, int operationIndex) {
      brushConfigCommandExecutor.storeOperatingIndex(this.variableNameArg, operationIndex);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
   }
}
