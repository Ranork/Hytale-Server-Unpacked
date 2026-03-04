package com.hypixel.hytale.server.npc.instructions;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.instructions.builders.BuilderInstructionRandomized;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InstructionRandomized extends Instruction {
   @Nonnull
   protected final IWeightedMap<InstructionRandomized.InstructionHolder> weightedInstructionMap;
   protected final boolean resetOnStateChange;
   protected final double minExecuteTime;
   protected final double maxExecuteTime;
   protected double timeout;
   @Nullable
   protected InstructionRandomized.InstructionHolder current;

   public InstructionRandomized(
      @Nonnull BuilderInstructionRandomized builder, Sensor sensor, @Nonnull Instruction[] instructionList, @Nonnull BuilderSupport support
   ) {
      super(builder, sensor, instructionList, support);
      WeightedMap.Builder<InstructionRandomized.InstructionHolder> mapBuilder = WeightedMap.builder(InstructionRandomized.InstructionHolder.EMPTY_ARRAY);

      for (Instruction instruction : instructionList) {
         mapBuilder.put(new InstructionRandomized.InstructionHolder(instruction), instruction.getWeight());
      }

      this.weightedInstructionMap = mapBuilder.build();
      this.resetOnStateChange = builder.getResetOnStateChange(support);
      double[] executeTimeRange = builder.getExecuteFor(support);
      this.minExecuteTime = executeTimeRange[0];
      this.maxExecuteTime = executeTimeRange[1];
   }

   @Override
   public void execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (this.instructionList.length != 0) {
         this.timeout -= dt;
         if (this.timeout <= 0.0 || this.current == null) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            this.current = this.weightedInstructionMap.get(random.nextDouble());
            this.timeout = RandomExtra.randomRange(this.minExecuteTime, this.maxExecuteTime);
         }

         Instruction instruction = this.current.instruction;
         if (instruction.matches(ref, role, dt, store)) {
            instruction.onMatched(role);
            instruction.execute(ref, role, dt, store);
            instruction.onCompleted(role);
         }
      }
   }

   @Override
   public void clearOnce() {
      super.clearOnce();
      if (this.resetOnStateChange) {
         this.current = null;
      }
   }

   @Override
   public void reset() {
      super.clearOnce();
      this.current = null;
   }

   protected static class InstructionHolder {
      protected static final InstructionRandomized.InstructionHolder[] EMPTY_ARRAY = new InstructionRandomized.InstructionHolder[0];
      private final Instruction instruction;

      protected InstructionHolder(Instruction instruction) {
         this.instruction = instruction;
      }
   }
}
