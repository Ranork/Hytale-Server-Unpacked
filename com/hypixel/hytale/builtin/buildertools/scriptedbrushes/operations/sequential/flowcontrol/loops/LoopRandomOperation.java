package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.loops;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.codec.PairCodec;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.Pair;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

public class LoopRandomOperation extends SequenceBrushOperation {
   public static final int MAX_REPETITIONS = 100;
   public static final int IDLE_STATE = -1;
   public static final BuilderCodec<LoopRandomOperation> CODEC = BuilderCodec.builder(LoopRandomOperation.class, LoopRandomOperation::new)
      .append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.indexNameArg = val, op -> op.indexNameArg)
      .documentation("The name of the previously stored index to begin the loop at. Note: This can only be an index previous to the current.")
      .add()
      .<PairCodec.IntegerPair>append(
         new KeyedCodec<>("RangeOfAdditionalRepetitions", PairCodec.IntegerPair.CODEC),
         (op, val) -> op.repetitionsArg = val.toPair(),
         op -> PairCodec.IntegerPair.fromPair(op.repetitionsArg)
      )
      .documentation(
         "The minimum and maximum of a range, randomly choosing the amount of additional times to repeat the loop after the initial, normal execution"
      )
      .add()
      .documentation("Loop the execution of instructions a random amount of times")
      .build();
   @Nonnull
   public String indexNameArg = "Undefined";
   @Nonnull
   public Pair<Integer, Integer> repetitionsArg = Pair.of(1, 1);
   private int repetitionsRemaining = -1;

   public LoopRandomOperation() {
      super("Loop Operations Random Amount", "Loop the execution of instructions a random amount of times", false);
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
         int repetitions = this.randomlyChooseRepetitionsAmount();
         if (repetitions > 100) {
            brushConfig.setErrorFlag("Cannot have more than 100 repetitions");
            return;
         }

         this.repetitionsRemaining = repetitions;
      }

      if (this.repetitionsRemaining == 0) {
         this.repetitionsRemaining = -1;
      } else {
         this.repetitionsRemaining--;
         brushConfigCommandExecutor.loadOperatingIndex(this.indexNameArg, false);
      }
   }

   private int randomlyChooseRepetitionsAmount() {
      return ((Integer)this.repetitionsArg.left()).equals(this.repetitionsArg.right())
         ? (Integer)this.repetitionsArg.left()
         : ThreadLocalRandom.current().nextInt((Integer)this.repetitionsArg.left(), (Integer)this.repetitionsArg.right() + 1);
   }
}
