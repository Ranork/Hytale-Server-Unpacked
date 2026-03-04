package com.hypixel.hytale.server.npc.util.expression;

import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import com.hypixel.hytale.server.npc.util.expression.compile.Lexer;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

public class Expression {
   private static final Lexer<Token> lexer = new Lexer<>(
      Token.END, Token.IDENTIFIER, Token.STRING, Token.NUMBER, Arrays.stream(Token.values()).filter(token -> token.get() != null)
   );
   @Nonnull
   private final ExecutionContext executionContext;
   @Nonnull
   private final CompileContext compileContext = new CompileContext();

   public Expression() {
      this.executionContext = this.compileContext.getExecutionContext();
   }

   public ValueType compile(@Nonnull String expression, Scope scope, @Nonnull List<ExecutionContext.Instruction> instructions, boolean fullResolve) {
      this.compileContext.compile(expression, scope, fullResolve);
      instructions.clear();
      instructions.addAll(this.compileContext.getInstructions());
      return this.compileContext.getResultType();
   }

   public ValueType compile(@Nonnull String expression, Scope compileScope, @Nonnull List<ExecutionContext.Instruction> instructions) {
      return this.compile(expression, compileScope, instructions, false);
   }

   @Nonnull
   public ExecutionContext execute(@Nonnull List<ExecutionContext.Instruction> instructions, Scope scope) {
      this.executionContext.execute(instructions, scope);
      return this.executionContext;
   }

   @Nonnull
   public ExecutionContext execute(@Nonnull ExecutionContext.Instruction[] instructions, Scope scope) {
      this.executionContext.execute(instructions, scope);
      return this.executionContext;
   }

   @Nonnull
   public ExecutionContext evaluate(@Nonnull String expression, Scope scope) {
      List<ExecutionContext.Instruction> instructions = new ObjectArrayList();
      this.compile(expression, scope, instructions, true);
      return this.execute(instructions, scope);
   }

   public static ValueType compileStatic(@Nonnull String expression, Scope scope, @Nonnull List<ExecutionContext.Instruction> instructions) {
      CompileContext compileContext = new CompileContext();
      compileContext.compile(expression, scope, false);
      instructions.clear();
      instructions.addAll(compileContext.getInstructions());
      return compileContext.getResultType();
   }

   @Nonnull
   public static Lexer<Token> getLexerInstance() {
      return lexer;
   }
}
