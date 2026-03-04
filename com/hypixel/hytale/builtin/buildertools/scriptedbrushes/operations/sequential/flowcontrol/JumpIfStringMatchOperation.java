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

public class JumpIfStringMatchOperation extends SequenceBrushOperation {
   public static final BuilderCodec<JumpIfStringMatchOperation> CODEC = BuilderCodec.builder(JumpIfStringMatchOperation.class, JumpIfStringMatchOperation::new)
      .append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.indexVariableNameArg = val, op -> op.indexVariableNameArg)
      .documentation("The labeled index to jump to, previous or future")
      .add()
      .<String>append(new KeyedCodec<>("LeftSideOfStatement", Codec.STRING), (op, val) -> op.sideOneArg = val, op -> op.sideOneArg)
      .documentation("The left side of the statement for checking case-insensitive equals")
      .add()
      .<String>append(new KeyedCodec<>("RightSideOfStatement", Codec.STRING), (op, val) -> op.sideTwoArg = val, op -> op.sideTwoArg)
      .documentation("The right side of the statement for checking case-insensitive equals")
      .add()
      .documentation("Jump the execution of the stack to the stored point if a string matches, useful for macro commands.")
      .build();
   @Nonnull
   public String indexVariableNameArg = "Undefined";
   @Nonnull
   public String sideOneArg = "Undefined";
   @Nonnull
   public String sideTwoArg = "Undefined";

   public JumpIfStringMatchOperation() {
      super("Jump If String Matches", "Jump the execution of the stack to the stored point if a string matches, useful for macro commands.", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.sideOneArg.equalsIgnoreCase(this.sideTwoArg)) {
         brushConfigCommandExecutor.loadOperatingIndex(this.indexVariableNameArg);
      }
   }
}
