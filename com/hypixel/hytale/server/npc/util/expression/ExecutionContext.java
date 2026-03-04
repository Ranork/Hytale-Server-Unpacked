package com.hypixel.hytale.server.npc.util.expression;

import com.hypixel.hytale.common.util.ArrayUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExecutionContext {
   public static final int STACK_GROW_INCREMENT = 8;
   protected Scope scope;
   protected ExecutionContext.Operand[] operandStack;
   protected int stackTop;
   protected ValueType lastPushedType;
   protected String combatConfig;
   protected Map<String, String> interactionVars;
   public static final ExecutionContext.Instruction UNARY_PLUS = context -> {};
   public static final ExecutionContext.Instruction UNARY_MINUS = context -> context.push(-context.popNumber());
   public static final ExecutionContext.Instruction LOGICAL_NOT = context -> context.push(!context.popBoolean());
   public static final ExecutionContext.Instruction BITWISE_NOT = context -> context.push(~context.popInt());
   public static final ExecutionContext.Instruction EXPONENTIATION = context -> context.popPush(Math.pow(context.getNumber(1), context.getNumber(0)), 2);
   public static final ExecutionContext.Instruction REMAINDER = context -> context.popPush(context.getNumber(1) % context.getNumber(0), 2);
   public static final ExecutionContext.Instruction DIVIDE = context -> context.popPush(context.getNumber(1) / context.getNumber(0), 2);
   public static final ExecutionContext.Instruction MULTIPLY = context -> context.popPush(context.getNumber(1) * context.getNumber(0), 2);
   public static final ExecutionContext.Instruction MINUS = context -> context.popPush(context.getNumber(1) - context.getNumber(0), 2);
   public static final ExecutionContext.Instruction PLUS = context -> context.popPush(context.getNumber(1) + context.getNumber(0), 2);
   public static final ExecutionContext.Instruction GREATER_EQUAL = context -> context.popPush(context.getNumber(1) >= context.getNumber(0), 2);
   public static final ExecutionContext.Instruction GREATER = context -> context.popPush(context.getNumber(1) > context.getNumber(0), 2);
   public static final ExecutionContext.Instruction LESS_EQUAL = context -> context.popPush(context.getNumber(1) <= context.getNumber(0), 2);
   public static final ExecutionContext.Instruction LESS = context -> context.popPush(context.getNumber(1) < context.getNumber(0), 2);
   public static final ExecutionContext.Instruction NOT_EQUAL = context -> context.popPush(context.getNumber(1) != context.getNumber(0), 2);
   public static final ExecutionContext.Instruction EQUAL = context -> context.popPush(context.getNumber(1) == context.getNumber(0), 2);
   public static final ExecutionContext.Instruction NOT_EQUAL_BOOL = context -> context.popPush(context.getBoolean(1) != context.getBoolean(0), 2);
   public static final ExecutionContext.Instruction EQUAL_BOOL = context -> context.popPush(context.getBoolean(1) == context.getBoolean(0), 2);
   public static final ExecutionContext.Instruction BITWISE_AND = context -> context.popPush(context.getInt(1) & context.getInt(0), 2);
   public static final ExecutionContext.Instruction BITWISE_XOR = context -> context.popPush(context.getInt(1) ^ context.getInt(0), 2);
   public static final ExecutionContext.Instruction BITWISE_OR = context -> context.popPush(context.getInt(1) | context.getInt(0), 2);
   public static final ExecutionContext.Instruction LOGICAL_AND = context -> context.popPush(context.getBoolean(1) && context.getBoolean(0), 2);
   public static final ExecutionContext.Instruction LOGICAL_OR = context -> context.popPush(context.getBoolean(1) || context.getBoolean(0), 2);

   public ExecutionContext(Scope scope) {
      this.scope = scope;
      this.operandStack = new ExecutionContext.Operand[8];

      for (int i = 0; i < this.operandStack.length; i++) {
         this.operandStack[i] = new ExecutionContext.Operand();
      }
   }

   public ExecutionContext() {
      this(null);
   }

   public ValueType execute(@Nonnull List<ExecutionContext.Instruction> instructions, Scope scope) {
      this.setScope(scope);
      return this.execute(instructions);
   }

   public ValueType execute(@Nonnull List<ExecutionContext.Instruction> instructions) {
      Objects.requireNonNull(this.scope, "Scope not initialised executing instructions");
      Objects.requireNonNull(instructions, "Instruction sequence is null executing instructions");
      this.stackTop = -1;
      this.lastPushedType = ValueType.VOID;
      instructions.forEach(instruction -> instruction.execute(this));
      return this.getType();
   }

   public ValueType execute(@Nonnull ExecutionContext.Instruction[] instructions, Scope scope) {
      this.setScope(scope);
      return this.execute(instructions);
   }

   public ValueType execute(@Nonnull ExecutionContext.Instruction[] instructions) {
      Objects.requireNonNull(this.scope, "Scope not initialised executing instructions");
      Objects.requireNonNull(instructions, "Instruction sequence is null executing instructions");

      try {
         this.stackTop = -1;
         this.lastPushedType = ValueType.VOID;

         for (ExecutionContext.Instruction instruction : instructions) {
            instruction.execute(this);
         }

         return this.getType();
      } catch (Throwable var6) {
         throw new IllegalStateException("Failed to execute instruction sequence: ", var6);
      }
   }

   public ValueType getType() {
      return this.lastPushedType;
   }

   public ExecutionContext.Operand top() {
      return this.get(0);
   }

   public Scope setScope(Scope scope) {
      Scope oldScope = this.getScope();
      this.scope = scope;
      return oldScope;
   }

   public Scope getScope() {
      return this.scope;
   }

   public String getCombatConfig() {
      return this.combatConfig;
   }

   public void setCombatConfig(String combatConfig) {
      this.combatConfig = combatConfig;
   }

   public Map<String, String> getInteractionVars() {
      return this.interactionVars;
   }

   public void setInteractionVars(Map<String, String> interactionVars) {
      this.interactionVars = interactionVars;
   }

   protected ExecutionContext.Operand push() {
      this.stackTop++;
      if (this.operandStack.length <= this.stackTop) {
         int i = this.operandStack.length;
         this.operandStack = Arrays.copyOf(this.operandStack, i + 8);

         while (i < this.operandStack.length) {
            this.operandStack[i++] = new ExecutionContext.Operand();
         }
      }

      return this.operandStack[this.stackTop];
   }

   public void push(String value) {
      this.lastPushedType = this.push().set(value);
   }

   public void push(double value) {
      this.lastPushedType = this.push().set(value);
   }

   public void push(int value) {
      this.lastPushedType = this.push().set(value);
   }

   public void push(boolean value) {
      this.lastPushedType = this.push().set(value);
   }

   public void push(String[] value) {
      this.lastPushedType = this.push().set(value);
   }

   public void push(double[] value) {
      this.lastPushedType = this.push().set(value);
   }

   public void push(boolean[] value) {
      this.lastPushedType = this.push().set(value);
   }

   public void pushEmptyArray() {
      this.lastPushedType = this.push().setEmptyArray();
   }

   protected ExecutionContext.Operand popPush(int popCount) {
      this.stackTop -= popCount - 1;
      return this.operandStack[this.stackTop];
   }

   public void popPush(String value, int popCount) {
      this.lastPushedType = this.popPush(popCount).set(value);
   }

   public void popPush(double value, int popCount) {
      this.lastPushedType = this.popPush(popCount).set(value);
   }

   public void popPush(int value, int popCount) {
      this.lastPushedType = this.popPush(popCount).set(value);
   }

   public void popPush(boolean value, int popCount) {
      this.lastPushedType = this.popPush(popCount).set(value);
   }

   public void popPush(String[] value, int popCount) {
      this.lastPushedType = this.popPush(popCount).set(value);
   }

   public void popPush(double[] value, int popCount) {
      this.lastPushedType = this.popPush(popCount).set(value);
   }

   public void popPush(boolean[] value, int popCount) {
      this.lastPushedType = this.popPush(popCount).set(value);
   }

   public void popPushEmptyArray(int popCount) {
      this.lastPushedType = this.popPush(popCount).setEmptyArray();
   }

   protected ExecutionContext.Operand pop() {
      this.lastPushedType = ValueType.VOID;
      return this.operandStack[this.stackTop--];
   }

   public double popNumber() {
      return this.pop().number;
   }

   public int popInt() {
      return (int)this.pop().number;
   }

   public String popString() {
      return this.pop().string;
   }

   public boolean popBoolean() {
      return this.pop().bool;
   }

   public double[] popNumberArray() {
      return this.top().type != ValueType.EMPTY_ARRAY ? this.pop().numberArray : ArrayUtil.EMPTY_DOUBLE_ARRAY;
   }

   @Nullable
   public String[] popStringArray() {
      return this.top().type != ValueType.EMPTY_ARRAY ? this.pop().stringArray : ArrayUtil.EMPTY_STRING_ARRAY;
   }

   public boolean[] popBooleanArray() {
      return this.top().type != ValueType.EMPTY_ARRAY ? this.pop().boolArray : ArrayUtil.EMPTY_BOOLEAN_ARRAY;
   }

   public String popAsString() {
      ExecutionContext.Operand op = this.pop();

      return switch (op.type) {
         case VOID -> "null";
         case STRING -> op.string;
         case NUMBER -> Double.toString(op.number);
         case BOOLEAN -> Boolean.toString(op.bool);
         case NUMBER_ARRAY -> Arrays.toString(op.numberArray);
         case STRING_ARRAY -> Arrays.toString((Object[])op.stringArray);
         case BOOLEAN_ARRAY -> Arrays.toString(op.boolArray);
         case EMPTY_ARRAY -> "[]";
      };
   }

   protected ExecutionContext.Operand get(int index) {
      return this.operandStack[this.stackTop - index];
   }

   public double getNumber(int index) {
      return this.get(index).number;
   }

   public int getInt(int index) {
      return (int)this.get(index).number;
   }

   public String getString(int index) {
      return this.get(index).string;
   }

   public boolean getBoolean(int index) {
      return this.get(index).bool;
   }

   public double[] getNumberArray(int index) {
      return this.get(index).numberArray;
   }

   @Nullable
   public String[] getStringArray(int index) {
      return this.get(index).stringArray;
   }

   public boolean[] getBooleanArray(int index) {
      return this.get(index).boolArray;
   }

   @Nonnull
   public static ExecutionContext.Instruction genPUSH(String value) {
      return context -> context.push(value);
   }

   @Nonnull
   public static ExecutionContext.Instruction genPUSH(double value) {
      return context -> context.push(value);
   }

   @Nonnull
   public static ExecutionContext.Instruction genPUSH(boolean value) {
      return context -> context.push(value);
   }

   @Nonnull
   public static ExecutionContext.Instruction genPUSH(String[] value) {
      return context -> context.push(value);
   }

   @Nonnull
   public static ExecutionContext.Instruction genPUSH(double[] value) {
      return context -> context.push(value);
   }

   @Nonnull
   public static ExecutionContext.Instruction genPUSH(boolean[] value) {
      return context -> context.push(value);
   }

   @Nonnull
   public static ExecutionContext.Instruction genPUSHEmptyArray() {
      return ExecutionContext::pushEmptyArray;
   }

   @Nonnull
   public static ExecutionContext.Instruction genREAD(String ident, @Nonnull ValueType type, @Nullable Scope scope) {
      if (scope == null) {
         return switch (type) {
            case STRING -> context -> context.push(context.scope.getString(ident));
            case NUMBER -> context -> context.push(context.scope.getNumber(ident));
            case BOOLEAN -> context -> context.push(context.scope.getBoolean(ident));
            case NUMBER_ARRAY -> context -> context.push(context.scope.getNumberArray(ident));
            case STRING_ARRAY -> context -> context.push(context.scope.getStringArray(ident));
            case BOOLEAN_ARRAY -> context -> context.push(context.scope.getBooleanArray(ident));
            default -> throw new RuntimeException("ExecutionContext: Invalid read type");
         };
      } else {
         return switch (type) {
            case STRING -> {
               Supplier<String> supplier = scope.getStringSupplier(ident);
               yield context -> context.push(supplier.get());
            }
            case NUMBER -> {
               DoubleSupplier supplier = scope.getNumberSupplier(ident);
               yield context -> context.push(supplier.getAsDouble());
            }
            case BOOLEAN -> {
               BooleanSupplier supplier = scope.getBooleanSupplier(ident);
               yield context -> context.push(supplier.getAsBoolean());
            }
            case NUMBER_ARRAY -> {
               Supplier<double[]> supplier = scope.getNumberArraySupplier(ident);
               yield context -> context.push(supplier.get());
            }
            case STRING_ARRAY -> {
               Supplier<String[]> supplier = scope.getStringArraySupplier(ident);
               yield context -> context.push(supplier.get());
            }
            case BOOLEAN_ARRAY -> {
               Supplier<boolean[]> supplier = scope.getBooleanArraySupplier(ident);
               yield context -> context.push(supplier.get());
            }
            default -> throw new RuntimeException("ExecutionContext: Invalid read type");
         };
      }
   }

   @Nonnull
   public static ExecutionContext.Instruction genCALL(String ident, int numArgs, @Nullable Scope scope) {
      if (scope == null) {
         return context -> context.scope.getFunction(ident).call(context, numArgs);
      } else {
         Scope.Function function = scope.getFunction(ident);
         return context -> function.call(context, numArgs);
      }
   }

   @Nonnull
   public static ExecutionContext.Instruction genNumberPACK(int size) {
      return context -> {
         double[] array = new double[size];

         for (int i = 0; i < size; i++) {
            array[i] = context.getNumber(size - i);
         }

         context.popPush(array, size);
      };
   }

   @Nonnull
   public static ExecutionContext.Instruction genStringPACK(int size) {
      return context -> {
         String[] array = new String[size];

         for (int i = 0; i < size; i++) {
            array[i] = context.getString(size - i);
         }

         context.popPush(array, size);
      };
   }

   @Nonnull
   public static ExecutionContext.Instruction genBooleanPACK(int size) {
      return context -> {
         boolean[] array = new boolean[size];

         for (int i = 0; i < size; i++) {
            array[i] = context.getBoolean(size - i);
         }

         context.popPush(array, size);
      };
   }

   @Nonnull
   public static ExecutionContext.Instruction genPACK(@Nonnull ValueType arrayType, int size) {
      return switch (arrayType) {
         case NUMBER_ARRAY -> genNumberPACK(size);
         case STRING_ARRAY -> genStringPACK(size);
         case BOOLEAN_ARRAY -> genBooleanPACK(size);
         default -> throw new IllegalStateException("Cannot create PACK instruction for type " + arrayType);
      };
   }

   @Nonnull
   @Override
   public String toString() {
      return "ExecutionContext{scope="
         + this.scope
         + ", operandStack="
         + Arrays.toString((Object[])this.operandStack)
         + ", stackTop="
         + this.stackTop
         + ", lastPushedType="
         + this.lastPushedType
         + "}";
   }

   @FunctionalInterface
   public interface Instruction {
      void execute(ExecutionContext var1);
   }

   public static class Operand {
      public ValueType type;
      public String string;
      public double number;
      public boolean bool;
      @Nullable
      public double[] numberArray;
      @Nullable
      public String[] stringArray;
      @Nullable
      public boolean[] boolArray;

      public ValueType set(String value) {
         this.reInit(ValueType.STRING);
         this.string = value;
         return this.type;
      }

      public ValueType set(double value) {
         this.reInit(ValueType.NUMBER);
         this.number = value;
         return this.type;
      }

      public ValueType set(boolean value) {
         this.reInit(ValueType.BOOLEAN);
         this.bool = value;
         return this.type;
      }

      public ValueType set(String[] value) {
         this.reInit(ValueType.STRING_ARRAY);
         this.stringArray = value;
         return this.type;
      }

      public ValueType set(double[] value) {
         this.reInit(ValueType.NUMBER_ARRAY);
         this.numberArray = value;
         return this.type;
      }

      public ValueType set(boolean[] value) {
         this.reInit(ValueType.BOOLEAN_ARRAY);
         this.boolArray = value;
         return this.type;
      }

      public ValueType setEmptyArray() {
         this.reInit(ValueType.EMPTY_ARRAY);
         return this.type;
      }

      private void reInit(ValueType type) {
         this.type = type;
         this.numberArray = null;
         this.stringArray = null;
         this.boolArray = null;
      }

      @Nonnull
      @Override
      public String toString() {
         return "Operand{type="
            + this.type
            + ", string='"
            + this.string
            + "', number="
            + this.number
            + ", bool="
            + this.bool
            + ", numberArray="
            + Arrays.toString(this.numberArray)
            + ", stringArray="
            + Arrays.toString((Object[])this.stringArray)
            + ", boolArray="
            + Arrays.toString(this.boolArray)
            + "}";
      }
   }
}
