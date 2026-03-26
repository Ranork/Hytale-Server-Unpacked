package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ClearRotationOperation extends SequenceBrushOperation {
   public static final BuilderCodec<ClearRotationOperation> CODEC = BuilderCodec.builder(ClearRotationOperation.class, ClearRotationOperation::new)
      .documentation("Reset the Brush-Config-provided rotation settings to default to undo the rotations")
      .build();

   public ClearRotationOperation() {
      super("Clear Rotation Settings", "Reset the Brush-Config-provided rotation settings to default to undo the rotations", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfig.resetTransform();
      brushConfig.setTransformOrigin(brushConfig.getOriginAfterOffset());
   }
}
