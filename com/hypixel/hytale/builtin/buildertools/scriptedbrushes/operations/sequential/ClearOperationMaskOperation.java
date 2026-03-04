package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ClearOperationMaskOperation extends SequenceBrushOperation {
   public static final BuilderCodec<ClearOperationMaskOperation> CODEC = BuilderCodec.builder(
         ClearOperationMaskOperation.class, ClearOperationMaskOperation::new
      )
      .documentation("Reset the Brush-Config-provided mask to nothing, keeping the brush tool's mask")
      .build();

   public ClearOperationMaskOperation() {
      super("Clear Operation Mask", "Reset the Brush-Config-provided mask to nothing, keeping the brush tool's mask", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfig.clearOperationMask();
   }
}
