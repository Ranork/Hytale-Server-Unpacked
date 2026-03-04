package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.global;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.GlobalBrushOperation;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DisableHoldInteractionOperation extends GlobalBrushOperation {
   public static final BuilderCodec<DisableHoldInteractionOperation> CODEC = BuilderCodec.builder(
         DisableHoldInteractionOperation.class, DisableHoldInteractionOperation::new
      )
      .documentation("Disables the ability of the brush to activate multiple times on holding a button")
      .build();

   public DisableHoldInteractionOperation() {
      super("Disable Activate-On-Hold", "Disables the ability of the brush to activate multiple times on holding a button");
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (brushConfig.isHoldDownInteraction()) {
         brushConfigCommandExecutor.exitExecution(ref, componentAccessor);
      }
   }
}
