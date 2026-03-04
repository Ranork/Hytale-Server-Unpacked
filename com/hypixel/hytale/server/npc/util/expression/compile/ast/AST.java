package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AST {
   @Nonnull
   private final ValueType valueType;
   @Nonnull
   private final Token token;
   private final int tokenPosition;
   private AST parent;
   @Nullable
   protected Function<Scope, ExecutionContext.Instruction> codeGen;

   public AST(@Nonnull ValueType valueType, @Nonnull Token token, int tokenPosition) {
      Objects.requireNonNull(valueType, "ValueType can't be null");
      Objects.requireNonNull(token, "Token can't be null");
      this.valueType = valueType;
      this.token = token;
      this.tokenPosition = tokenPosition;
      this.codeGen = null;
   }

   public AST getParent() {
      return this.parent;
   }

   public void setParent(AST parent) {
      this.parent = parent;
   }

   @Nonnull
   public ValueType getValueType() {
      return this.valueType;
   }

   @Nonnull
   public Token getToken() {
      return this.token;
   }

   public int getTokenPosition() {
      return this.tokenPosition;
   }

   @Nullable
   public Function<Scope, ExecutionContext.Instruction> getCodeGen() {
      return this.codeGen;
   }

   public abstract boolean isConstant();

   public ExecutionContext.Operand asOperand() {
      throw new IllegalStateException("AST: Cannot be returned as operand");
   }

   public String getString() {
      throw new IllegalStateException("AST: Cannot return string");
   }

   public boolean getBoolean() {
      throw new IllegalStateException("AST: Cannot return boolean");
   }

   public double getNumber() {
      throw new IllegalStateException("AST: Cannot return number");
   }

   @Nonnull
   public ValueType returnType() {
      return this.getValueType();
   }

   public ValueType genCode(@Nonnull List<ExecutionContext.Instruction> list, Scope scope) {
      Objects.requireNonNull(this.getCodeGen(), "Missing CodeGen in AST");
      list.add(this.getCodeGen().apply(scope));
      return this.getValueType();
   }
}
