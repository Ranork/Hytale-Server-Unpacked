package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class JumpToIndexOperation extends SequenceBrushOperation {
   public static final BuilderCodec<JumpToIndexOperation> CODEC = BuilderCodec.builder(JumpToIndexOperation.class, JumpToIndexOperation::new)
      .append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.variableNameArg = val, op -> op.variableNameArg)
      .documentation("The labeled index to jump to, previous or future")
      .add()
      .documentation("Jump the stack execution to the point in the stack of the given saved index name")
      .build();
   @Nonnull
   public String variableNameArg = "Undefined";

   public JumpToIndexOperation() {
      super("Jump to Index", "Jump the stack execution to the point in the stack of the given saved index name", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfigCommandExecutor.loadOperatingIndex(this.variableNameArg);
   }
}
