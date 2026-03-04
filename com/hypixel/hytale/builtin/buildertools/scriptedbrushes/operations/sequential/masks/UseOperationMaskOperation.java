package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks;

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

public class UseOperationMaskOperation extends SequenceBrushOperation {
   public static final BuilderCodec<UseOperationMaskOperation> CODEC = BuilderCodec.builder(UseOperationMaskOperation.class, UseOperationMaskOperation::new)
      .append(new KeyedCodec<>("UseOperationMask", Codec.BOOLEAN), (op, val) -> op.useOperationMask = val, op -> op.useOperationMask)
      .documentation("Enables or disables the operation mask")
      .add()
      .documentation("Enable or disable the use of the operation mask")
      .build();
   @Nonnull
   public Boolean useOperationMask = true;

   public UseOperationMaskOperation() {
      super("Use Operation Mask", "Enable or disable the use of the operation mask", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfig.setUseOperationMask(this.useOperationMask);
   }
}
