package com.hypixel.hytale.server.npc.util.expression;

import java.util.concurrent.ThreadLocalRandom;

public class StdLib extends StdScope {
   private static final StdLib instance = new StdLib();

   private StdLib() {
      super(null);
      this.addConst("true", true);
      this.addConst("false", false);
      this.addConst("PI", (float) Math.PI);
      this.addInvariant(
         "max",
         (context, numArgs) -> context.popPush(Math.max(context.getNumber(0), context.getNumber(1)), 2),
         ValueType.NUMBER,
         ValueType.NUMBER,
         ValueType.NUMBER
      );
      this.addInvariant(
         "min",
         (context, numArgs) -> context.popPush(Math.min(context.getNumber(0), context.getNumber(1)), 2),
         ValueType.NUMBER,
         ValueType.NUMBER,
         ValueType.NUMBER
      );
      this.addInvariant("isEmpty", (context, numArgs) -> {
         String string = context.getString(0);
         context.popPush(string == null || string.isEmpty(), 1);
      }, ValueType.BOOLEAN, ValueType.STRING);
      this.addInvariant(
         "isEmptyStringArray", (context, numArgs) -> context.popPush(context.getStringArray(0).length == 0, 1), ValueType.BOOLEAN, ValueType.STRING_ARRAY
      );
      this.addInvariant(
         "isEmptyNumberArray", (context, numArgs) -> context.popPush(context.getNumberArray(0).length == 0, 1), ValueType.BOOLEAN, ValueType.NUMBER_ARRAY
      );
      this.addVariant("random", (context, numArgs) -> context.push(ThreadLocalRandom.current().nextDouble()), ValueType.NUMBER);
      this.addVariant(
         "randomInRange",
         (context, numArgs) -> context.popPush(ThreadLocalRandom.current().nextDouble(context.getNumber(1), context.getNumber(0)), 2),
         ValueType.NUMBER,
         ValueType.NUMBER,
         ValueType.NUMBER
      );
      this.addInvariant("makeRange", (context, numArgs) -> {
         double value = context.getNumber(0);
         context.popPush(new double[]{value, value}, 1);
      }, ValueType.NUMBER_ARRAY, ValueType.NUMBER);
   }

   public static StdScope getInstance() {
      return instance;
   }
}
