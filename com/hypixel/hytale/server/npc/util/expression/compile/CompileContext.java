package com.hypixel.hytale.server.npc.util.expression.compile;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Expression;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.ast.AST;
import com.hypixel.hytale.server.npc.util.expression.compile.ast.ASTOperand;
import com.hypixel.hytale.server.npc.util.expression.compile.ast.ASTOperator;
import com.hypixel.hytale.server.npc.util.expression.compile.ast.ASTOperatorFunctionCall;
import com.hypixel.hytale.server.npc.util.expression.compile.ast.ASTOperatorTuple;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.text.ParseException;
import java.util.List;
import java.util.Stack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompileContext implements Parser.ParsedTokenConsumer {
   private final Parser parser = new Parser(Expression.getLexerInstance());
   private final Stack<AST> operandStack = new Stack<>();
   @Nonnull
   private final ExecutionContext executionContext;
   private Scope scope;
   private List<ExecutionContext.Instruction> instructions;
   private ValueType resultType = ValueType.VOID;

   public CompileContext() {
      this.executionContext = new ExecutionContext();
   }

   public CompileContext(Scope scope) {
      this();
      this.scope = scope;
   }

   public Scope getScope() {
      return this.scope;
   }

   @Nonnull
   public Stack<AST> getOperandStack() {
      return this.operandStack;
   }

   @Nonnull
   public ExecutionContext getExecutionContext() {
      return this.executionContext;
   }

   public ValueType compile(@Nonnull String expression, Scope compileScope, boolean fullResolve) {
      return this.compile0(expression, compileScope, fullResolve, null);
   }

   public ValueType compile(@Nonnull String expression, Scope compileScope, boolean fullResolve, List<ExecutionContext.Instruction> instructions) {
      ValueType valueType = this.compile0(expression, compileScope, fullResolve, instructions);
      this.setInstructions(null);
      return valueType;
   }

   protected ValueType compile0(@Nonnull String expression, Scope compileScope, boolean fullResolve, List<ExecutionContext.Instruction> instructions) {
      Scope saveScope = this.scope;
      Scope oldScope = this.executionContext.setScope(compileScope);
      this.scope = compileScope;
      this.setInstructions(instructions);
      this.compile(expression, fullResolve);
      this.executionContext.setScope(oldScope);
      this.scope = saveScope;
      return this.resultType;
   }

   public ValueType compile(@Nonnull String expression, boolean fullResolve) {
      try {
         this.operandStack.clear();
         this.resultType = ValueType.VOID;
         if (this.instructions == null) {
            this.instructions = new ObjectArrayList();
         }

         this.instructions.clear();
         this.parser.parse(expression, this);
         this.resultType = this.operandStack.getFirst().genCode(this.instructions, fullResolve ? this.scope : null);
         return this.resultType;
      } catch (Throwable var4) {
         throw new IllegalStateException("Error compiling expression '" + expression + "': " + var4.getMessage(), var4);
      }
   }

   public List<ExecutionContext.Instruction> getInstructions() {
      return this.instructions;
   }

   public void setInstructions(List<ExecutionContext.Instruction> instructionList) {
      this.instructions = instructionList;
   }

   public ValueType getResultType() {
      return this.resultType;
   }

   @Nullable
   public ExecutionContext.Operand getAsOperand() {
      if (this.operandStack.size() != 1) {
         throw new IllegalArgumentException("There must be 1 element on stack to get as operand");
      } else {
         AST ast = this.operandStack.getFirst();
         return ast.isConstant() ? ast.asOperand() : null;
      }
   }

   public void checkResultType(ValueType type) {
      if (type == ValueType.VOID) {
         throw new IllegalArgumentException("Result type can't be void");
      } else if (this.resultType == ValueType.VOID) {
         throw new IllegalArgumentException("Compiled expression result type can't be void");
      } else if (type != this.resultType) {
         throw new IllegalStateException("Result type expected is " + type + " but got " + this.resultType);
      }
   }

   @Override
   public void pushOperand(@Nonnull Parser.ParsedToken parsedToken) {
      this.operandStack.push(ASTOperand.createFromParsedToken(parsedToken, this));
   }

   @Override
   public void processOperator(@Nonnull Parser.ParsedToken operator) throws ParseException {
      ASTOperator.fromParsedOperator(operator, this);
   }

   @Override
   public void processFunction(int argumentCount) throws ParseException {
      ASTOperatorFunctionCall.fromParsedFunction(argumentCount, this);
   }

   @Override
   public void processTuple(@Nonnull Parser.ParsedToken openingToken, int argumentCount) {
      ASTOperatorTuple.fromParsedTuple(openingToken, argumentCount, this);
   }

   @Override
   public void done() {
      if (this.operandStack.size() != 1) {
         throw new IllegalStateException("Need exactly one returned value in expression");
      }
   }
}
