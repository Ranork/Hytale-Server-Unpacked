package com.hypixel.hytale.server.npc.util.expression;

import com.hypixel.hytale.common.util.ArrayUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StdScope implements Scope {
   protected static final StdScope.SymbolStringArray VAR_EMPTY_STRING_ARRAY = new StdScope.SymbolStringArray(false, () -> ArrayUtil.EMPTY_STRING_ARRAY);
   protected static final StdScope.SymbolNumberArray VAR_EMPTY_NUMBER_ARRAY = new StdScope.SymbolNumberArray(false, () -> ArrayUtil.EMPTY_DOUBLE_ARRAY);
   protected static final StdScope.SymbolBooleanArray VAR_EMPTY_BOOLEAN_ARRAY = new StdScope.SymbolBooleanArray(false, () -> ArrayUtil.EMPTY_BOOLEAN_ARRAY);
   protected static final StdScope.SymbolStringArray VAR_NULL_STRING_ARRAY = new StdScope.SymbolStringArray(false, () -> null);
   protected static final StdScope.SymbolNumberArray VAR_NULL_NUMBER_ARRAY = new StdScope.SymbolNumberArray(false, () -> null);
   protected static final StdScope.SymbolBooleanArray VAR_NULL_BOOLEAN_ARRAY = new StdScope.SymbolBooleanArray(false, () -> null);
   protected static final StdScope.SymbolString VAR_NULL_STRING = new StdScope.SymbolString(false, () -> null);
   protected static final StdScope.SymbolString VAR_EMPTY_STRING = new StdScope.SymbolString(false, () -> "");
   protected static final StdScope.SymbolBoolean VAR_BOOLEAN_TRUE = new StdScope.SymbolBoolean(false, () -> true);
   protected static final StdScope.SymbolBoolean VAR_BOOLEAN_FALSE = new StdScope.SymbolBoolean(false, () -> false);
   protected static final StdScope.SymbolStringArray CONST_EMPTY_STRING_ARRAY = new StdScope.SymbolStringArray(true, () -> ArrayUtil.EMPTY_STRING_ARRAY);
   protected static final StdScope.SymbolNumberArray CONST_EMPTY_NUMBER_ARRAY = new StdScope.SymbolNumberArray(true, () -> ArrayUtil.EMPTY_DOUBLE_ARRAY);
   protected static final StdScope.SymbolBooleanArray CONST_EMPTY_BOOLEAN_ARRAY = new StdScope.SymbolBooleanArray(true, () -> ArrayUtil.EMPTY_BOOLEAN_ARRAY);
   protected static final StdScope.SymbolStringArray CONST_NULL_STRING_ARRAY = new StdScope.SymbolStringArray(true, () -> null);
   protected static final StdScope.SymbolNumberArray CONST_NULL_NUMBER_ARRAY = new StdScope.SymbolNumberArray(true, () -> null);
   protected static final StdScope.SymbolBooleanArray CONST_NULL_BOOLEAN_ARRAY = new StdScope.SymbolBooleanArray(true, () -> null);
   protected static final StdScope.SymbolString CONST_NULL_STRING = new StdScope.SymbolString(true, () -> null);
   protected static final StdScope.SymbolString CONST_EMPTY_STRING = new StdScope.SymbolString(true, () -> "");
   protected static final StdScope.SymbolBoolean CONST_BOOLEAN_TRUE = new StdScope.SymbolBoolean(true, () -> true);
   protected static final StdScope.SymbolBoolean CONST_BOOLEAN_FALSE = new StdScope.SymbolBoolean(true, () -> false);
   protected Scope parent;
   protected Map<String, StdScope.Symbol> symbolTable;

   public StdScope(Scope parent) {
      this.parent = parent;
      this.symbolTable = new HashMap<>();
   }

   @Nonnull
   public static StdScope copyOf(@Nonnull StdScope other) {
      StdScope scope = new StdScope(other.parent);
      scope.mergeSymbols(other);
      return scope;
   }

   @Nonnull
   public StdScope merge(@Nonnull StdScope other) {
      this.mergeSymbols(other);
      return this;
   }

   @Nonnull
   public static StdScope mergeScopes(@Nonnull StdScope first, @Nonnull StdScope second) {
      return copyOf(first).merge(second);
   }

   protected void mergeSymbols(@Nonnull StdScope other) {
      other.symbolTable.forEach(this::add);
   }

   protected void add(String name, StdScope.Symbol symbol) {
      if (this.symbolTable.containsKey(name)) {
         throw new IllegalStateException("Trying to add symbol twice to scope " + name);
      } else {
         this.symbolTable.put(name, symbol);
      }
   }

   public void addConst(String name, @Nullable String value) {
      if (value == null) {
         this.add(name, CONST_NULL_STRING);
      } else if (value.isEmpty()) {
         this.add(name, CONST_EMPTY_STRING);
      } else {
         this.add(name, new StdScope.SymbolString(true, () -> value));
      }
   }

   public void addConst(String name, double value) {
      this.add(name, new StdScope.SymbolNumber(true, () -> value));
   }

   public void addConst(String name, boolean value) {
      this.add(name, value ? CONST_BOOLEAN_TRUE : CONST_BOOLEAN_FALSE);
   }

   public void addConst(String name, @Nullable String[] value) {
      if (value == null) {
         this.add(name, CONST_NULL_STRING_ARRAY);
      } else if (value.length == 0) {
         this.add(name, CONST_EMPTY_STRING_ARRAY);
      } else {
         this.add(name, new StdScope.SymbolStringArray(true, () -> value));
      }
   }

   public void addConst(String name, @Nullable double[] value) {
      if (value == null) {
         this.add(name, CONST_NULL_NUMBER_ARRAY);
      } else if (value.length == 0) {
         this.add(name, CONST_EMPTY_NUMBER_ARRAY);
      } else {
         this.add(name, new StdScope.SymbolNumberArray(true, () -> value));
      }
   }

   public void addConst(String name, @Nullable boolean[] value) {
      if (value == null) {
         this.add(name, CONST_NULL_BOOLEAN_ARRAY);
      } else if (value.length == 0) {
         this.add(name, CONST_EMPTY_BOOLEAN_ARRAY);
      } else {
         this.add(name, new StdScope.SymbolBooleanArray(true, () -> value));
      }
   }

   public void addConstEmptyArray(String name) {
      this.add(name, new StdScope.Symbol(true, ValueType.EMPTY_ARRAY));
   }

   public void addVar(String name, @Nullable String value) {
      if (value == null) {
         this.add(name, VAR_NULL_STRING);
      } else if (value.isEmpty()) {
         this.add(name, VAR_EMPTY_STRING);
      } else {
         this.add(name, new StdScope.SymbolString(false, () -> value));
      }
   }

   public void addVar(String name, double value) {
      this.add(name, new StdScope.SymbolNumber(false, () -> value));
   }

   public void addVar(String name, boolean value) {
      this.add(name, value ? VAR_BOOLEAN_TRUE : VAR_BOOLEAN_FALSE);
   }

   public void addVar(String name, @Nullable String[] value) {
      if (value == null) {
         this.add(name, VAR_NULL_STRING_ARRAY);
      } else if (value.length == 0) {
         this.add(name, VAR_EMPTY_STRING_ARRAY);
      } else {
         this.add(name, new StdScope.SymbolStringArray(false, () -> value));
      }
   }

   public void addVar(String name, @Nullable double[] value) {
      if (value == null) {
         this.add(name, VAR_NULL_NUMBER_ARRAY);
      } else if (value.length == 0) {
         this.add(name, VAR_EMPTY_NUMBER_ARRAY);
      } else {
         this.add(name, new StdScope.SymbolNumberArray(false, () -> value));
      }
   }

   public void addVar(String name, @Nullable boolean[] value) {
      if (value == null) {
         this.add(name, VAR_NULL_BOOLEAN_ARRAY);
      } else if (value.length == 0) {
         this.add(name, VAR_EMPTY_BOOLEAN_ARRAY);
      } else {
         this.add(name, new StdScope.SymbolBooleanArray(false, () -> value));
      }
   }

   public void addInvariant(@Nonnull String name, Scope.Function function, ValueType returnType, @Nonnull ValueType... argumentTypes) {
      this.add(Scope.encodeFunctionName(name, argumentTypes), new StdScope.SymbolFunction(true, returnType, function));
      this.add(name, new StdScope.SymbolFunction(false, returnType, null));
   }

   public void addVariant(@Nonnull String name, Scope.Function function, ValueType returnType, @Nonnull ValueType... argumentTypes) {
      this.add(Scope.encodeFunctionName(name, argumentTypes), new StdScope.SymbolFunction(false, returnType, function));
      this.add(name, new StdScope.SymbolFunction(false, returnType, null));
   }

   public void addSupplier(String name, Supplier<String> value) {
      this.add(name, new StdScope.SymbolString(false, value));
   }

   public void addSupplier(String name, DoubleSupplier value) {
      this.add(name, new StdScope.SymbolNumber(false, value));
   }

   public void addSupplier(String name, BooleanSupplier value) {
      this.add(name, new StdScope.SymbolBoolean(false, value));
   }

   public void addStringArraySupplier(String name, Supplier<String[]> value) {
      this.add(name, new StdScope.SymbolStringArray(false, value));
   }

   public void addDoubleArraySupplier(String name, Supplier<double[]> value) {
      this.add(name, new StdScope.SymbolNumberArray(false, value));
   }

   public void addBooleanArraySupplier(String name, Supplier<boolean[]> value) {
      this.add(name, new StdScope.SymbolBooleanArray(false, value));
   }

   protected StdScope.Symbol get(String name) {
      return this.symbolTable.get(name);
   }

   @Nonnull
   protected StdScope.Symbol get(String name, ValueType valueType) {
      StdScope.Symbol symbol = this.symbolTable.get(name);
      if (symbol == null) {
         throw new IllegalStateException("Can't find symbol " + name + " in symbol table");
      } else if (!ValueType.isAssignableType(valueType, symbol.valueType)) {
         throw new IllegalStateException("Type mismatch with " + name + ". Got " + valueType + " but expected " + symbol.valueType);
      } else {
         return symbol;
      }
   }

   protected void replace(String name, @Nonnull StdScope.Symbol symbol) {
      StdScope.Symbol oldSymbol = this.get(name, symbol.valueType);
      if (oldSymbol.isConstant) {
         throw new IllegalStateException("Can't replace a constant in symbol table: " + name);
      } else if (symbol.isConstant) {
         throw new IllegalStateException("Can't replace a variable with a constant: " + name);
      } else {
         this.symbolTable.put(name, symbol);
      }
   }

   public void changeValue(String name, @Nullable String value) {
      if (value == null) {
         this.replace(name, VAR_NULL_STRING);
      } else if (value.isEmpty()) {
         this.replace(name, VAR_EMPTY_STRING);
      } else {
         this.replace(name, new StdScope.SymbolString(false, () -> value));
      }
   }

   public void changeValue(String name, double value) {
      this.replace(name, new StdScope.SymbolNumber(false, () -> value));
   }

   public void changeValue(String name, boolean value) {
      this.replace(name, value ? VAR_BOOLEAN_TRUE : VAR_BOOLEAN_FALSE);
   }

   public void changeValue(String name, @Nullable String[] value) {
      if (value == null) {
         this.replace(name, VAR_NULL_STRING_ARRAY);
      } else if (value.length == 0) {
         this.replace(name, VAR_EMPTY_STRING_ARRAY);
      } else {
         this.replace(name, new StdScope.SymbolStringArray(false, () -> value));
      }
   }

   public void changeValue(String name, @Nullable double[] value) {
      if (value == null) {
         this.replace(name, VAR_NULL_NUMBER_ARRAY);
      } else if (value.length == 0) {
         this.replace(name, VAR_EMPTY_NUMBER_ARRAY);
      } else {
         this.replace(name, new StdScope.SymbolNumberArray(false, () -> value));
      }
   }

   public void changeValue(String name, @Nullable boolean[] value) {
      if (value == null) {
         this.replace(name, VAR_NULL_BOOLEAN_ARRAY);
      } else if (value.length == 0) {
         this.replace(name, VAR_EMPTY_BOOLEAN_ARRAY);
      } else {
         this.replace(name, new StdScope.SymbolBooleanArray(false, () -> value));
      }
   }

   public void changeValueToEmptyArray(String name) {
      StdScope.Symbol symbol = this.get(name);
      Objects.requireNonNull(symbol, "Can't find symbol in symbol table in changeValue()");
      if (symbol.isConstant) {
         throw new IllegalStateException("Can't replace a constant in symbol table: " + name);
      } else {
         switch (symbol.valueType) {
            case VOID:
            case NUMBER:
            case STRING:
            case BOOLEAN:
            default:
               throw new IllegalStateException("Can't assign an empty array to symbol " + name + "  of type " + symbol.valueType);
            case EMPTY_ARRAY:
               return;
            case NUMBER_ARRAY:
               this.symbolTable.put(name, VAR_EMPTY_NUMBER_ARRAY);
               break;
            case STRING_ARRAY:
               this.symbolTable.put(name, VAR_EMPTY_STRING_ARRAY);
               break;
            case BOOLEAN_ARRAY:
               this.symbolTable.put(name, VAR_EMPTY_BOOLEAN_ARRAY);
         }
      }
   }

   @Override
   public Supplier<String> getStringSupplier(String name) {
      StdScope.Symbol symbol = this.get(name);
      if (symbol == null) {
         if (this.parent != null) {
            return this.parent.getStringSupplier(name);
         } else {
            throw new IllegalStateException("Unable to find symbol: " + name);
         }
      } else if (symbol instanceof StdScope.SymbolString) {
         return ((StdScope.SymbolString)symbol).value;
      } else {
         throw new IllegalStateException("Symbol is not a string: " + name);
      }
   }

   @Override
   public DoubleSupplier getNumberSupplier(String name) {
      StdScope.Symbol symbol = this.get(name);
      if (symbol == null) {
         if (this.parent != null) {
            return this.parent.getNumberSupplier(name);
         } else {
            throw new IllegalStateException("Unable to find symbol: " + name);
         }
      } else if (symbol instanceof StdScope.SymbolNumber) {
         return ((StdScope.SymbolNumber)symbol).value;
      } else {
         throw new IllegalStateException("Symbol is not a number: " + name);
      }
   }

   @Override
   public BooleanSupplier getBooleanSupplier(String name) {
      StdScope.Symbol symbol = this.get(name);
      if (symbol == null) {
         if (this.parent != null) {
            return this.parent.getBooleanSupplier(name);
         } else {
            throw new IllegalStateException("Unable to find symbol: " + name);
         }
      } else if (symbol instanceof StdScope.SymbolBoolean) {
         return ((StdScope.SymbolBoolean)symbol).value;
      } else {
         throw new IllegalStateException("Symbol is not a boolean: " + name);
      }
   }

   @Override
   public Supplier<String[]> getStringArraySupplier(String name) {
      StdScope.Symbol symbol = this.get(name);
      if (symbol == null) {
         if (this.parent != null) {
            return this.parent.getStringArraySupplier(name);
         } else {
            throw new IllegalStateException("Unable to find symbol: " + name);
         }
      } else if (symbol.valueType == ValueType.EMPTY_ARRAY) {
         return () -> ArrayUtil.EMPTY_STRING_ARRAY;
      } else if (symbol instanceof StdScope.SymbolStringArray) {
         return ((StdScope.SymbolStringArray)symbol).value;
      } else {
         throw new IllegalStateException("Symbol is not a string array: " + name);
      }
   }

   @Override
   public Supplier<double[]> getNumberArraySupplier(String name) {
      StdScope.Symbol symbol = this.get(name);
      if (symbol == null) {
         if (this.parent != null) {
            return this.parent.getNumberArraySupplier(name);
         } else {
            throw new IllegalStateException("Unable to find symbol: " + name);
         }
      } else if (symbol.valueType == ValueType.EMPTY_ARRAY) {
         return () -> ArrayUtil.EMPTY_DOUBLE_ARRAY;
      } else if (symbol instanceof StdScope.SymbolNumberArray) {
         return ((StdScope.SymbolNumberArray)symbol).value;
      } else {
         throw new IllegalStateException("Symbol is not a number array: " + name);
      }
   }

   @Override
   public Supplier<boolean[]> getBooleanArraySupplier(String name) {
      StdScope.Symbol symbol = this.get(name);
      if (symbol == null) {
         if (this.parent != null) {
            return this.parent.getBooleanArraySupplier(name);
         } else {
            throw new IllegalStateException("Unable to find symbol: " + name);
         }
      } else if (symbol.valueType == ValueType.EMPTY_ARRAY) {
         return () -> ArrayUtil.EMPTY_BOOLEAN_ARRAY;
      } else if (symbol instanceof StdScope.SymbolBooleanArray) {
         return ((StdScope.SymbolBooleanArray)symbol).value;
      } else {
         throw new IllegalStateException("Symbol is not a boolean array: " + name);
      }
   }

   @Override
   public Scope.Function getFunction(String name) {
      StdScope.Symbol symbol = this.get(name);
      if (symbol == null) {
         if (this.parent != null) {
            return this.parent.getFunction(name);
         } else {
            throw new IllegalStateException("Unable to find function: " + name);
         }
      } else if (symbol instanceof StdScope.SymbolFunction) {
         return ((StdScope.SymbolFunction)symbol).value;
      } else {
         throw new IllegalStateException("Symbol is not a function: " + name);
      }
   }

   @Override
   public boolean isConstant(String name) {
      StdScope.Symbol symbol = this.get(name);
      if (symbol != null) {
         return symbol.isConstant;
      } else if (this.parent == null) {
         throw new IllegalStateException("Unable to find symbol: " + name);
      } else {
         return this.parent.isConstant(name);
      }
   }

   @Nullable
   @Override
   public ValueType getType(String name) {
      StdScope.Symbol symbol = this.get(name);
      if (symbol != null) {
         return symbol.valueType;
      } else {
         return this.parent != null ? this.parent.getType(name) : null;
      }
   }

   protected static class Symbol {
      public final boolean isConstant;
      public final ValueType valueType;

      public Symbol(boolean isConstant, ValueType valueType) {
         this.isConstant = isConstant;
         this.valueType = valueType;
      }
   }

   protected static class SymbolBoolean extends StdScope.Symbol {
      public final BooleanSupplier value;

      public SymbolBoolean(boolean isConstant, BooleanSupplier value) {
         super(isConstant, ValueType.BOOLEAN);
         this.value = value;
      }
   }

   protected static class SymbolBooleanArray extends StdScope.Symbol {
      public final Supplier<boolean[]> value;

      public SymbolBooleanArray(boolean isConstant, Supplier<boolean[]> value) {
         super(isConstant, ValueType.BOOLEAN_ARRAY);
         this.value = value;
      }
   }

   protected static class SymbolFunction extends StdScope.Symbol {
      public final Scope.Function value;

      public SymbolFunction(boolean isConstant, ValueType returnType, Scope.Function value) {
         super(isConstant, returnType);
         this.value = value;
      }
   }

   protected static class SymbolNumber extends StdScope.Symbol {
      public final DoubleSupplier value;

      public SymbolNumber(boolean isConstant, DoubleSupplier value) {
         super(isConstant, ValueType.NUMBER);
         this.value = value;
      }
   }

   protected static class SymbolNumberArray extends StdScope.Symbol {
      public final Supplier<double[]> value;

      public SymbolNumberArray(boolean isConstant, Supplier<double[]> value) {
         super(isConstant, ValueType.NUMBER_ARRAY);
         this.value = value;
      }
   }

   protected static class SymbolString extends StdScope.Symbol {
      public final Supplier<String> value;

      public SymbolString(boolean isConstant, Supplier<String> value) {
         super(isConstant, ValueType.STRING);
         this.value = value;
      }
   }

   protected static class SymbolStringArray extends StdScope.Symbol {
      public final Supplier<String[]> value;

      public SymbolStringArray(boolean isConstant, Supplier<String[]> value) {
         super(isConstant, ValueType.STRING_ARRAY);
         this.value = value;
      }
   }
}
