package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SetOperation extends SequenceBrushOperation {
   public static final BuilderCodec<SetOperation> CODEC = BuilderCodec.builder(SetOperation.class, SetOperation::new)
      .documentation(
         "Runs a 'set' operation using the parameters of the brush configuration. Supports both blocks and fluids - if the pattern contains fluid items, it sets the fluid layer instead."
      )
      .build();

   public SetOperation() {
      super("Set", "Runs a 'set' operation using the parameters of the brush configuration", true);
   }

   @Override
   public boolean modifyBlocks(
      Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull BrushConfigEditStore edit,
      int x,
      int y,
      int z,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      edit.setMaterial(x, y, z, brushConfig.getNextMaterial());
      return true;
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
