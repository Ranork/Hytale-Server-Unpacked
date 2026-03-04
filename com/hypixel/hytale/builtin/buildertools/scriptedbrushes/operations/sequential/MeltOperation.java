package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class MeltOperation extends SequenceBrushOperation {
   public static final BuilderCodec<MeltOperation> CODEC = BuilderCodec.builder(MeltOperation.class, MeltOperation::new)
      .documentation("Remove the top layer of blocks in the brush editing area")
      .build();

   public MeltOperation() {
      super("Melt", "Remove the top layer of blocks in the brush editing area", true);
   }

   @Override
   public boolean modifyBlocks(
      @Nonnull Ref<EntityStore> ref,
      BrushConfig brushConfig,
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
      if (currentBlock > 0 && builderState.isAsideAir(edit.getAccessor(), x, y, z)) {
         edit.setMaterial(x, y, z, Material.EMPTY);
      } else if (currentBlock > 0 && edit.getFluid(x, y + 1, z) != 0) {
         edit.setMaterial(x, y, z, Material.EMPTY);
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
