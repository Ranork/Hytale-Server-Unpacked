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

public class UseBrushMaskOperation extends SequenceBrushOperation {
   public static final BuilderCodec<UseBrushMaskOperation> CODEC = BuilderCodec.builder(UseBrushMaskOperation.class, UseBrushMaskOperation::new)
      .append(new KeyedCodec<>("UseBrushMask", Codec.BOOLEAN), (op, val) -> op.useBrushMask = val, op -> op.useBrushMask)
      .documentation("Enables or disables the brush's mask")
      .add()
      .documentation("Enable the brush tool's mask (the mask placed on the tool)")
      .build();
   @Nonnull
   public Boolean useBrushMask = true;

   public UseBrushMaskOperation() {
      super("Use Brush Mask", "Enable the brush tool's mask (the mask placed on the tool)", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfig.setUseBrushMask(this.useBrushMask);
   }
}
