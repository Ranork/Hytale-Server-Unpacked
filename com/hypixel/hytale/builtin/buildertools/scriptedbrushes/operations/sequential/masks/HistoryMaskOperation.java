package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class HistoryMaskOperation extends SequenceBrushOperation {
   public static final BuilderCodec<HistoryMaskOperation> CODEC = BuilderCodec.builder(HistoryMaskOperation.class, HistoryMaskOperation::new)
      .append(new KeyedCodec<>("HistoryMask", new EnumCodec<>(BrushConfig.HistoryMask.class)), (op, val) -> op.historyMaskArg = val, op -> op.historyMaskArg)
      .documentation("Changes the mask to block history, enable only history, or ignore history")
      .add()
      .documentation("Sets the history mask, allowing you to mask to previously edited or non-edited blocks")
      .build();
   @Nonnull
   public BrushConfig.HistoryMask historyMaskArg = BrushConfig.HistoryMask.None;

   public HistoryMaskOperation() {
      super("History Mask", "Sets the history mask, allowing you to mask to previously edited or non-edited blocks", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfig.setHistoryMask(this.historyMaskArg);
   }
}
