package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class JumpIfBlockTypeOperation extends SequenceBrushOperation {
   public static final BuilderCodec<JumpIfBlockTypeOperation> CODEC = BuilderCodec.builder(JumpIfBlockTypeOperation.class, JumpIfBlockTypeOperation::new)
      .append(new KeyedCodec<>("Mask", BlockMask.CODEC), (op, val) -> op.blockMaskArg = val, op -> op.blockMaskArg)
      .documentation("The block mask for the comparison.")
      .add()
      .<String>append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.indexVariableNameArg = val, op -> op.indexVariableNameArg)
      .documentation("The labeled index to jump to, previous or future")
      .add()
      .documentation("Jump the execution of the stack based on a block type comparison")
      .build();
   @Nonnull
   public BlockMask blockMaskArg = BlockMask.EMPTY;
   @Nonnull
   public String indexVariableNameArg = "Undefined";

   public JumpIfBlockTypeOperation() {
      super("Jump If Block Type Comparison", "Jump the execution of the stack based on a block type comparison", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Vector3i currentBrushOrigin = brushConfig.getOrigin();
      if (currentBrushOrigin == null) {
         brushConfig.setErrorFlag("Could not find the origin for the operation.");
      } else {
         int targetBlockId = brushConfigCommandExecutor.getEdit().getBlock(currentBrushOrigin.x, currentBrushOrigin.y, currentBrushOrigin.z);
         int targetFluidId = brushConfigCommandExecutor.getEdit().getFluid(currentBrushOrigin.x, currentBrushOrigin.y, currentBrushOrigin.z);
         if (!this.blockMaskArg
            .isExcluded(
               brushConfigCommandExecutor.getEdit().getAccessor(),
               currentBrushOrigin.x,
               currentBrushOrigin.y,
               currentBrushOrigin.z,
               null,
               null,
               targetBlockId,
               targetFluidId
            )) {
            brushConfigCommandExecutor.loadOperatingIndex(this.indexVariableNameArg);
         }
      }
   }
}
