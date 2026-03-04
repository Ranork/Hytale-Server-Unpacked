package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract class SequenceBrushOperation extends BrushOperation {
   private final boolean doesOperateOnBlocks;

   public SequenceBrushOperation(String name, String description, boolean doesOperateOnBlocks) {
      super(name, description);
      this.doesOperateOnBlocks = doesOperateOnBlocks;
   }

   public boolean modifyBlocks(
      Ref<EntityStore> ref,
      BrushConfig brushConfig,
      BrushConfigCommandExecutor brushConfigCommandExecutor,
      BrushConfigEditStore edit,
      int x,
      int y,
      int z,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      return false;
   }

   public void beginIterationIndex(int iterationIndex) {
   }

   public int getNumModifyBlockIterations() {
      return 1;
   }

   public boolean doesOperateOnBlocks() {
      return this.doesOperateOnBlocks;
   }
}
