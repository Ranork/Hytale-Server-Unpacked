package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.loops;

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

public class LoopOperation extends SequenceBrushOperation {
   public static final int MAX_REPETITIONS = 100;
   public static final int IDLE_STATE = -1;
   public static final BuilderCodec<LoopOperation> CODEC = BuilderCodec.builder(LoopOperation.class, LoopOperation::new)
      .append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.indexNameArg = val, op -> op.indexNameArg)
      .documentation("The name of the previously stored index to begin the loop at. Note: This can only be an index previous to the current.")
      .add()
      .<Integer>append(new KeyedCodec<>("AdditionalRepetitions", Codec.INTEGER), (op, val) -> op.repetitionsArg = val, op -> op.repetitionsArg)
      .documentation("The amount of additional times to repeat the loop after the initial, normal execution")
      .add()
      .documentation("Loop the execution of instructions a set amount of times")
      .build();
   @Nonnull
   public String indexNameArg = "Undefined";
   @Nonnull
   public Integer repetitionsArg = 0;
   private int repetitionsRemaining = -1;

   public LoopOperation() {
      super("Loop Operations", "Loop the execution of instructions a set amount of times", false);
   }

   @Override
   public void resetInternalState() {
      this.repetitionsRemaining = -1;
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.repetitionsRemaining == -1) {
         if (this.repetitionsArg > 100 || this.repetitionsArg < 0) {
            brushConfig.setErrorFlag("Cannot have more than 100 repetitions, or negative repetitions");
            return;
         }

         this.repetitionsRemaining = this.repetitionsArg;
      }

      if (this.repetitionsRemaining == 0) {
         this.repetitionsRemaining = -1;
      } else {
         this.repetitionsRemaining--;
         brushConfigCommandExecutor.loadOperatingIndex(this.indexNameArg, false);
      }
   }
}
