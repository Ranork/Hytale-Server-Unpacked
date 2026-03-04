package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import com.hypixel.hytale.server.npc.util.expression.compile.Parser;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.text.ParseException;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;

public abstract class ASTOperator extends AST {
   private final List<AST> arguments = new ObjectArrayList();

   public ASTOperator(@Nonnull ValueType returnType, @Nonnull Token token, int tokenPosition) {
      super(returnType, token, tokenPosition);
   }

   public void addArgument(@Nonnull AST argument) {
      this.arguments.add(argument);
      argument.setParent(this);
   }

   @Nonnull
   public List<AST> getArguments() {
      return this.arguments;
   }

   @Override
   public ValueType genCode(@Nonnull List<ExecutionContext.Instruction> list, Scope scope) {
      this.arguments.forEach(ast -> ast.genCode(list, scope));
      return super.genCode(list, scope);
   }

   public static void fromParsedOperator(@Nonnull Parser.ParsedToken operand, @Nonnull CompileContext compileContext) throws ParseException {
      try {
         if (operand.token.isUnary()) {
            ASTOperatorUnary.fromUnaryOperator(operand, compileContext);
         } else {
            ASTOperatorBinary.fromBinaryOperator(operand, compileContext);
         }
      } catch (NoSuchElementException var3) {
         throw new ParseException("Not enough operands for operator '" + operand.tokenString, operand.tokenPosition);
      }
   }
}
