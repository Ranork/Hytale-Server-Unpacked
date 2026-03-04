package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class StringHolderBase extends ValueHolder {
   protected List<BiConsumer<ExecutionContext, String>> relationValidators;

   protected StringHolderBase() {
      super(ValueType.STRING);
   }

   public void addRelationValidator(BiConsumer<ExecutionContext, String> validator) {
      if (this.relationValidators == null) {
         this.relationValidators = new ObjectArrayList();
      }

      this.relationValidators.add(validator);
   }

   protected void validateRelations(ExecutionContext executionContext, String value) {
      if (this.relationValidators != null) {
         for (BiConsumer<ExecutionContext, String> executionContextConsumer : this.relationValidators) {
            executionContextConsumer.accept(executionContext, value);
         }
      }
   }
}
