package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ReplaceOperation extends SequenceBrushOperation {
   public static final BuilderCodec<ReplaceOperation> CODEC = BuilderCodec.builder(ReplaceOperation.class, ReplaceOperation::new)
      .append(new KeyedCodec<>("FromBlockType", Codec.STRING), (op, val) -> op.blockTypeKeyToReplace = val, op -> op.blockTypeKeyToReplace)
      .documentation("The block type to get replaced")
      .add()
      .<BlockPattern>append(new KeyedCodec<>("ToBlockPattern", BlockPattern.CODEC), (op, val) -> op.replacementBlocks = val, op -> op.replacementBlocks)
      .documentation("The pattern of blocks set to")
      .add()
      .documentation("Replace one kind of block with another pattern of blocks within the current brush editing area")
      .build();
   @Nonnull
   public String blockTypeKeyToReplace = "Rock_Stone";
   @Nonnull
   public BlockPattern replacementBlocks = BlockPattern.parse("Rock_Stone");

   public ReplaceOperation() {
      super("Replace Blocks", "Replace one kind of block with another pattern of blocks within the current brush editing area", true);
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
      int block = edit.getBlock(x, y, z);
      if (block == BlockType.getAssetMap().getIndex(this.blockTypeKeyToReplace)) {
         edit.setMaterial(x, y, z, Material.fromPattern(this.replacementBlocks, brushConfig.getRandom()));
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
