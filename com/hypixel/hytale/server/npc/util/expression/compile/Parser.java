package com.hypixel.hytale.server.npc.util.expression.compile;

import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Parser {
   public static final String MISMATCHED_CLOSING_BRACKET = "Mismatched closing bracket";
   public static final String TOO_MANY_OPERANDS = "Too many operands";
   public static final String NOT_ENOUGH_OPERANDS = "Not enough operands";
   public static final String EXPECTED_UNARY_OPERATOR = "Expected unary operator";
   public static final String EXPECTED_BINARY_OPERATOR = "Expected binary operator";
   public static final String MISSING_CLOSING_BRACKET = "Missing closing bracket";
   public static final String ILLEGAL_USE_OF_ARGUMENT_LIST = "Illegal use of argument list";
   private Lexer<Token> lexer;
   private LexerContext<Token> context;
   @Nonnull
   private Deque<Parser.ParsedToken> operatorStack = new ArrayDeque<>();
   @Nonnull
   private Deque<Parser.ParsedToken> bracketStack = new ArrayDeque<>();

   public Parser(Lexer<Token> lexer) {
      this.lexer = lexer;
      this.context = new LexerContext<>();
   }

   @Nonnull
   private Parser.ParsedToken nextToken() throws ParseException {
      return Parser.ParsedToken.fromLexer(this.lexer, this.context);
   }

   public void parse(@Nonnull String expression, @Nonnull Parser.ParsedTokenConsumer tokenConsumer) throws ParseException {
      this.operatorStack.clear();
      this.bracketStack.clear();
      this.bracketStack.push(new Parser.ParsedToken(Token.END));
      this.context.init(expression);
      Parser.ParsedToken parsedToken = this.nextToken();
      Token token = parsedToken.token;
      Token lastToken = null;

      Parser.ParsedToken bracket;
      for (bracket = this.bracketStack.peek(); !token.isEndToken(); token = parsedToken.token) {
         if (token.isOperand()) {
            tokenConsumer.pushOperand(parsedToken);
            bracket.operandCount++;
         } else if (token.isOpenBracket()) {
            if (token == Token.OPEN_BRACKET) {
               if (lastToken == Token.IDENTIFIER) {
                  parsedToken.isTuple = true;
                  parsedToken.isFunctionCall = true;
               }
            } else if (token.isOpenTuple()) {
               parsedToken.isTuple = true;
               parsedToken.isFunctionCall = false;
            }

            this.operatorStack.push(parsedToken);
            this.bracketStack.push(parsedToken);
            bracket = this.bracketStack.peek();
         } else if (token.isCloseBracket()) {
            Token otherBracket = token.getMatchingBracket();
            if (bracket.token != otherBracket) {
               throw new ParseException("Mismatched closing bracket", parsedToken.tokenPosition);
            }

            for (Parser.ParsedToken first = this.operatorStack.pop(); !first.token.isOpenBracket(); first = this.operatorStack.pop()) {
               bracket.operandCount = this.adjustOperandCount(first, bracket.operandCount);
               tokenConsumer.processOperator(first);
            }

            this.validateOperandCount(bracket);
            int deltaArity;
            if (bracket.isFunctionCall) {
               bracket.tupleLength = bracket.tupleLength + bracket.operandCount;
               tokenConsumer.processFunction(bracket.tupleLength);
               deltaArity = 0;
            } else if (bracket.isTuple) {
               bracket.tupleLength = bracket.tupleLength + bracket.operandCount;
               tokenConsumer.processTuple(bracket, bracket.tupleLength);
               deltaArity = 1;
            } else {
               deltaArity = 1;
            }

            this.bracketStack.pop();
            bracket = this.bracketStack.peek();
            bracket.operandCount += deltaArity;
         } else if (token.isList()) {
            if (!bracket.isTuple) {
               throw new ParseException("Illegal use of argument list", parsedToken.tokenPosition);
            }

            for (Parser.ParsedToken first = this.peekOperator(); !first.token.isOpenBracket(); first = this.peekOperator()) {
               bracket.operandCount = this.adjustOperandCount(first, bracket.operandCount);
               tokenConsumer.processOperator(first);
               this.operatorStack.pop();
            }

            this.validateOperandCount(bracket);
            bracket.tupleLength++;
            bracket.operandCount = 0;
         } else {
            if (!token.isOperator()) {
               throw new RuntimeException("Internal parser error: " + token);
            }

            boolean mustBeUnary = lastToken == null || lastToken.containsAnyFlag(EnumSet.of(TokenFlags.OPERATOR, TokenFlags.LIST, TokenFlags.OPENING_BRACKET));
            if (token.canBeUnary() && mustBeUnary) {
               token = token.getUnaryVariant();
               parsedToken.token = token;
            } else {
               if (mustBeUnary && !token.isUnary()) {
                  throw new ParseException("Expected unary operator", parsedToken.tokenPosition);
               }

               if (token.isUnary() && !mustBeUnary) {
                  throw new ParseException("Expected binary operator", parsedToken.tokenPosition);
               }
            }

            for (Parser.ParsedToken stackToken = this.peekOperator(); this.hasLowerPrecedence(token, stackToken); stackToken = this.peekOperator()) {
               bracket.operandCount = this.adjustOperandCount(stackToken, bracket.operandCount);
               tokenConsumer.processOperator(stackToken);
               this.operatorStack.pop();
            }

            this.operatorStack.push(parsedToken);
         }

         lastToken = token;
         parsedToken = this.nextToken();
      }

      if (bracket.token != Token.END) {
         throw new ParseException("Missing closing bracket", bracket.tokenPosition);
      } else {
         while (!this.operatorStack.isEmpty()) {
            parsedToken = this.operatorStack.pop();
            bracket.operandCount = this.adjustOperandCount(parsedToken, bracket.operandCount);
            tokenConsumer.processOperator(parsedToken);
         }

         this.validateOperandCount(bracket);
         tokenConsumer.done();
      }
   }

   @Nullable
   public Parser.ParsedToken peekOperator() {
      return this.operatorStack.isEmpty() ? null : this.operatorStack.peek();
   }

   private void validateOperandCount(@Nonnull Parser.ParsedToken bracket) throws ParseException {
      if (!bracket.isTuple || bracket.tupleLength != 0 || bracket.operandCount != 0) {
         if (bracket.operandCount <= 0) {
            throw new ParseException("Not enough operands", 0);
         } else if (bracket.operandCount > 1) {
            throw new ParseException("Too many operands", 0);
         }
      }
   }

   private int adjustOperandCount(@Nonnull Parser.ParsedToken parsedToken, int operandCount) throws ParseException {
      int requiredOperands = this.arity(parsedToken.token);
      if (operandCount < requiredOperands) {
         throw new ParseException("Not enough operands", parsedToken.tokenPosition);
      } else {
         return operandCount - requiredOperands + 1;
      }
   }

   private boolean hasLowerPrecedence(@Nonnull Token token, @Nullable Parser.ParsedToken stackToken) {
      if (stackToken != null && !stackToken.token.isList() && !stackToken.token.isOpenBracket()) {
         int tokenPrecedence = token.getPrecedence();
         int stackTokenPrecedence = stackToken.token.getPrecedence();
         return tokenPrecedence == stackTokenPrecedence ? !token.isRightToLeft() : tokenPrecedence < stackTokenPrecedence;
      } else {
         return false;
      }
   }

   private int arity(@Nonnull Token operator) {
      if (!operator.isOperator()) {
         throw new RuntimeException("Arity only possible with operators");
      } else {
         return operator.isUnary() ? 1 : 2;
      }
   }

   public static class ParsedToken {
      @Nullable
      public Token token;
      @Nullable
      public String tokenString;
      public double tokenNumber;
      public int tokenPosition;
      public int operandCount;
      public boolean isTuple;
      public boolean isFunctionCall;
      public int tupleLength;

      public ParsedToken(@Nonnull LexerContext<Token> context) {
         this(context.getToken());
         this.tokenString = context.getTokenString();
         this.tokenNumber = context.getTokenNumber();
         this.tokenPosition = context.getTokenPosition();
      }

      public ParsedToken(Token token) {
         this.token = token;
         this.tokenString = null;
         this.tokenNumber = 0.0;
         this.tokenPosition = 0;
         this.operandCount = 0;
         this.isTuple = false;
         this.isFunctionCall = false;
         this.tupleLength = 0;
      }

      @Nonnull
      static Parser.ParsedToken fromLexer(@Nonnull Lexer<Token> lexer, @Nonnull LexerContext<Token> context) throws ParseException {
         lexer.nextToken(context);
         return new Parser.ParsedToken(context);
      }
   }

   public interface ParsedTokenConsumer {
      void pushOperand(Parser.ParsedToken var1);

      void processOperator(Parser.ParsedToken var1) throws ParseException;

      void processFunction(int var1) throws ParseException;

      void processTuple(Parser.ParsedToken var1, int var2);

      void done();
   }
}
