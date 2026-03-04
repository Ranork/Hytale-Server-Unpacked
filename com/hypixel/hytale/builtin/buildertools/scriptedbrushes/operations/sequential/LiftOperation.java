package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class LiftOperation extends SequenceBrushOperation {
   public static final BuilderCodec<LiftOperation> CODEC = BuilderCodec.builder(LiftOperation.class, LiftOperation::new)
      .documentation("Lift all blocks up by one (duplicating the block) that are touching air, preserving the material")
      .build();

   public LiftOperation() {
      super("Lift Blocks", "Lift all blocks up by one (duplicating the block) that are touching air, preserving the material", true);
   }

   @Override
   public boolean modifyBlocks(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull BrushConfigEditStore edit,
      int x,
      int y,
      int z,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      int currentBlock = edit.getBlock(x, y, z);
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRefComponent);
      if (currentBlock <= 0 && builderState.isAsideBlock(edit.getAccessor(), x, y, z)) {
         int blockId = brushConfig.getNextBlock();
         if (blockId == 0) {
            BuilderToolsPlugin.BuilderState.BlocksSampleData data = builderState.getBlocksSampleData(edit.getAccessor(), x, y, z, 1);
            blockId = data.mainBlockNotAir;
         }

         edit.setBlock(x, y, z, blockId);
      }

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
