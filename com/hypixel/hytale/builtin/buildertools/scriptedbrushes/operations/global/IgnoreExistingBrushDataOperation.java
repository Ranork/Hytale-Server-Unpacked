package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.global;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.GlobalBrushOperation;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class IgnoreExistingBrushDataOperation extends GlobalBrushOperation {
   public static final BuilderCodec<IgnoreExistingBrushDataOperation> CODEC = BuilderCodec.builder(
         IgnoreExistingBrushDataOperation.class, IgnoreExistingBrushDataOperation::new
      )
      .documentation("Ignores any existing brush settings specified on the tool")
      .build();

   public IgnoreExistingBrushDataOperation() {
      super("Ignore Existing Brush Settings", "Ignores any existing brush settings specified on the tool");
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfigCommandExecutor.setIgnoreExistingBrushData(true);
   }
}
