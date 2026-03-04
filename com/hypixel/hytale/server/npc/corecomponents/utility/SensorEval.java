package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorEval;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class SensorEval extends SensorBase {
   protected final String expression;
   @Nonnull
   protected final CompileContext compileContext;
   protected ExecutionContext.Instruction[] instructions;
   protected boolean isValid;

   public SensorEval(@Nonnull BuilderSensorEval builderSensorEval, @Nonnull BuilderSupport support) {
      super(builderSensorEval);
      this.expression = builderSensorEval.getExpression();
      this.compileContext = new CompileContext();
      this.isValid = true;

      try {
         ObjectArrayList<ExecutionContext.Instruction> instructions = new ObjectArrayList();
         StdScope scope = support.getSensorScope();
         ValueType valueType = this.compile(this.expression, scope, instructions);
         if (valueType != ValueType.BOOLEAN) {
            this.isValid = false;
            throw new IllegalStateException("Expression '" + this.expression + "' must return boolean value but is:" + valueType);
         } else {
            this.instructions = (ExecutionContext.Instruction[])instructions.toArray(ExecutionContext.Instruction[]::new);
         }
      } catch (RuntimeException var6) {
         this.isValid = false;
         throw new RuntimeException("Error evaluating '" + this.expression + "'", var6);
      }
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      return super.matches(ref, role, dt, store) && this.isValid && this.evalBoolean(role.getEntitySupport().getSensorScope(), this.instructions);
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }

   protected ValueType compile(@Nonnull String expression, StdScope sensorScope, List<ExecutionContext.Instruction> instructions) {
      return this.compileContext.compile(expression, sensorScope, true, instructions);
   }

   protected boolean evalBoolean(StdScope sensorScope, @Nonnull ExecutionContext.Instruction[] instructions) {
      ExecutionContext executionContext = this.compileContext.getExecutionContext();
      if (executionContext.execute(instructions, sensorScope) != ValueType.BOOLEAN) {
         throw new IllegalStateException("Expression must return boolean value");
      } else {
         return executionContext.popBoolean();
      }
   }
}
