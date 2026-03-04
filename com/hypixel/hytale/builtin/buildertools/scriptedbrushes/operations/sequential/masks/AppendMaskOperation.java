package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class AppendMaskOperation extends SequenceBrushOperation {
   public static final BuilderCodec<AppendMaskOperation> CODEC = BuilderCodec.builder(AppendMaskOperation.class, AppendMaskOperation::new)
      .append(new KeyedCodec<>("AppendMask", BlockMask.CODEC), (op, val) -> op.operationMaskArg = val, op -> op.operationMaskArg)
      .documentation("Combines the new mask with the current operation mask")
      .add()
      .documentation("Append new masks to the current operation mask")
      .build();
   @Nonnull
   public BlockMask operationMaskArg = BlockMask.EMPTY;

   public AppendMaskOperation() {
      super("Append Mask", "Append new masks to the current operation mask", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfig.appendOperationMask(this.operationMaskArg);
   }
}
