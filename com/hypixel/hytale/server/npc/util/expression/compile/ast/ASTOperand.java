package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import com.hypixel.hytale.server.npc.util.expression.compile.Parser;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import javax.annotation.Nonnull;

public abstract class ASTOperand extends AST {
   public ASTOperand(@Nonnull ValueType valueType, @Nonnull Token token, int tokenPosition) {
      super(valueType, token, tokenPosition);
   }

   @Nonnull
   public static ASTOperand createFromParsedToken(@Nonnull Parser.ParsedToken operand, @Nonnull CompileContext compileContext) {
      Token token = operand.token;
      int tokenPosition = operand.tokenPosition;
      String tokenString = operand.tokenString;
      switch (token) {
         case STRING:
            return new ASTOperandString(token, tokenPosition, tokenString);
         case NUMBER:
            return new ASTOperandNumber(token, tokenPosition, operand.tokenNumber);
         case IDENTIFIER:
            Scope scope = compileContext.getScope();
            if (scope.isConstant(tokenString)) {
               return createFromScopeConstant(token, tokenPosition, scope, tokenString);
            }

            return new ASTOperandIdentifier(scope.getType(tokenString), token, tokenPosition, tokenString);
         default:
            throw new IllegalStateException("Unknown parser operand type in AST" + operand.token);
      }
   }

   @Nonnull
   private static ASTOperand createFromScopeConstant(@Nonnull Token param0, int param1, @Nonnull Scope param2, String param3) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.IllegalStateException: Invalid switch case set: [[const(0)], [const(1)], [const(2)], [const(3)], [const(4)], [const(5)], [const(6)], [const(null), null]] for selector of type Lcom/hypixel/hytale/server/npc/util/expression/ValueType;
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchHeadExprent.checkExprTypeBounds(SwitchHeadExprent.java:66)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExpr(VarTypeProcessor.java:140)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExprent(VarTypeProcessor.java:126)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.lambda$processVarTypes$2(VarTypeProcessor.java:114)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph.iterateExprents(DirectGraph.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.processVarTypes(VarTypeProcessor.java:114)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.calculateVarTypes(VarTypeProcessor.java:44)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsProcessor.setVarVersions(VarVersionsProcessor.java:68)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor.setVarVersions(VarProcessor.java:47)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:302)
      //
      // Bytecode:
      // 00: aload 2
      // 01: aload 3
      // 02: invokeinterface com/hypixel/hytale/server/npc/util/expression/Scope.getType (Ljava/lang/String;)Lcom/hypixel/hytale/server/npc/util/expression/ValueType; 2
      // 07: astore 4
      // 09: aload 4
      // 0b: astore 5
      // 0d: bipush 0
      // 0e: istore 6
      // 10: aload 5
      // 12: iload 6
      // 14: invokedynamic typeSwitch (Ljava/lang/Object;I)I bsm=java/lang/runtime/SwitchBootstraps.typeSwitch (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc; ]
      // 19: tableswitch 143 -1 6 143 47 61 75 89 103 117 131
      // 48: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandNumber
      // 4b: dup
      // 4c: aload 0
      // 4d: iload 1
      // 4e: aload 2
      // 4f: aload 3
      // 50: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandNumber.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;ILcom/hypixel/hytale/server/npc/util/expression/Scope;Ljava/lang/String;)V
      // 53: goto ba
      // 56: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandString
      // 59: dup
      // 5a: aload 0
      // 5b: iload 1
      // 5c: aload 2
      // 5d: aload 3
      // 5e: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandString.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;ILcom/hypixel/hytale/server/npc/util/expression/Scope;Ljava/lang/String;)V
      // 61: goto ba
      // 64: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandBoolean
      // 67: dup
      // 68: aload 0
      // 69: iload 1
      // 6a: aload 2
      // 6b: aload 3
      // 6c: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandBoolean.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;ILcom/hypixel/hytale/server/npc/util/expression/Scope;Ljava/lang/String;)V
      // 6f: goto ba
      // 72: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandNumberArray
      // 75: dup
      // 76: aload 0
      // 77: iload 1
      // 78: aload 2
      // 79: aload 3
      // 7a: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandNumberArray.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;ILcom/hypixel/hytale/server/npc/util/expression/Scope;Ljava/lang/String;)V
      // 7d: goto ba
      // 80: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandStringArray
      // 83: dup
      // 84: aload 0
      // 85: iload 1
      // 86: aload 2
      // 87: aload 3
      // 88: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandStringArray.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;ILcom/hypixel/hytale/server/npc/util/expression/Scope;Ljava/lang/String;)V
      // 8b: goto ba
      // 8e: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandBooleanArray
      // 91: dup
      // 92: aload 0
      // 93: iload 1
      // 94: aload 2
      // 95: aload 3
      // 96: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandBooleanArray.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;ILcom/hypixel/hytale/server/npc/util/expression/Scope;Ljava/lang/String;)V
      // 99: goto ba
      // 9c: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandEmptyArray
      // 9f: dup
      // a0: aload 0
      // a1: iload 1
      // a2: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandEmptyArray.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;I)V
      // a5: goto ba
      // a8: new java/lang/IllegalStateException
      // ab: dup
      // ac: aload 4
      // ae: invokestatic java/lang/String.valueOf (Ljava/lang/Object;)Ljava/lang/String;
      // b1: invokedynamic makeConcatWithConstants (Ljava/lang/String;)Ljava/lang/String; bsm=java/lang/invoke/StringConcatFactory.makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ "Illegal constant type encountered\u0001" ]
      // b6: invokespecial java/lang/IllegalStateException.<init> (Ljava/lang/String;)V
      // b9: athrow
      // ba: areturn
   }

   @Nonnull
   public static ASTOperand createFromOperand(@Nonnull Token param0, int param1, @Nonnull ExecutionContext.Operand param2) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.IllegalStateException: Invalid switch case set: [[const(0)], [const(1)], [const(2)], [const(3)], [const(4)], [const(5)], [const(6)], [const(null), null]] for selector of type Lcom/hypixel/hytale/server/npc/util/expression/ValueType;
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchHeadExprent.checkExprTypeBounds(SwitchHeadExprent.java:66)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExpr(VarTypeProcessor.java:140)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExprent(VarTypeProcessor.java:126)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.lambda$processVarTypes$2(VarTypeProcessor.java:114)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph.iterateExprents(DirectGraph.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.processVarTypes(VarTypeProcessor.java:114)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.calculateVarTypes(VarTypeProcessor.java:44)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsProcessor.setVarVersions(VarVersionsProcessor.java:68)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor.setVarVersions(VarProcessor.java:47)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:302)
      //
      // Bytecode:
      // 00: aload 2
      // 01: getfield com/hypixel/hytale/server/npc/util/expression/ExecutionContext$Operand.type Lcom/hypixel/hytale/server/npc/util/expression/ValueType;
      // 04: astore 3
      // 05: aload 3
      // 06: astore 4
      // 08: bipush 0
      // 09: istore 5
      // 0b: aload 4
      // 0d: iload 5
      // 0f: invokedynamic typeSwitch (Ljava/lang/Object;I)I bsm=java/lang/runtime/SwitchBootstraps.typeSwitch (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc; ]
      // 14: tableswitch 156 -1 6 156 48 64 80 96 112 128 144
      // 44: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandNumber
      // 47: dup
      // 48: aload 0
      // 49: iload 1
      // 4a: aload 2
      // 4b: getfield com/hypixel/hytale/server/npc/util/expression/ExecutionContext$Operand.number D
      // 4e: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandNumber.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;ID)V
      // 51: goto c1
      // 54: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandString
      // 57: dup
      // 58: aload 0
      // 59: iload 1
      // 5a: aload 2
      // 5b: getfield com/hypixel/hytale/server/npc/util/expression/ExecutionContext$Operand.string Ljava/lang/String;
      // 5e: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandString.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;ILjava/lang/String;)V
      // 61: goto c1
      // 64: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandBoolean
      // 67: dup
      // 68: aload 0
      // 69: iload 1
      // 6a: aload 2
      // 6b: getfield com/hypixel/hytale/server/npc/util/expression/ExecutionContext$Operand.bool Z
      // 6e: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandBoolean.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;IZ)V
      // 71: goto c1
      // 74: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandNumberArray
      // 77: dup
      // 78: aload 0
      // 79: iload 1
      // 7a: aload 2
      // 7b: getfield com/hypixel/hytale/server/npc/util/expression/ExecutionContext$Operand.numberArray [D
      // 7e: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandNumberArray.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;I[D)V
      // 81: goto c1
      // 84: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandStringArray
      // 87: dup
      // 88: aload 0
      // 89: iload 1
      // 8a: aload 2
      // 8b: getfield com/hypixel/hytale/server/npc/util/expression/ExecutionContext$Operand.stringArray [Ljava/lang/String;
      // 8e: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandStringArray.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;I[Ljava/lang/String;)V
      // 91: goto c1
      // 94: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandBooleanArray
      // 97: dup
      // 98: aload 0
      // 99: iload 1
      // 9a: aload 2
      // 9b: getfield com/hypixel/hytale/server/npc/util/expression/ExecutionContext$Operand.boolArray [Z
      // 9e: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandBooleanArray.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;I[Z)V
      // a1: goto c1
      // a4: new com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandEmptyArray
      // a7: dup
      // a8: aload 0
      // a9: iload 1
      // aa: invokespecial com/hypixel/hytale/server/npc/util/expression/compile/ast/ASTOperandEmptyArray.<init> (Lcom/hypixel/hytale/server/npc/util/expression/compile/Token;I)V
      // ad: goto c1
      // b0: new java/lang/IllegalStateException
      // b3: dup
      // b4: aload 3
      // b5: invokestatic java/lang/String.valueOf (Ljava/lang/Object;)Ljava/lang/String;
      // b8: invokedynamic makeConcatWithConstants (Ljava/lang/String;)Ljava/lang/String; bsm=java/lang/invoke/StringConcatFactory.makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ "Illegal operand type encountered\u0001" ]
      // bd: invokespecial java/lang/IllegalStateException.<init> (Ljava/lang/String;)V
      // c0: athrow
      // c1: areturn
   }
}
