package com.hypixel.hytale.server.npc.util.expression.compile;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum Token implements Supplier<String> {
   INVALID(null, 0),
   END(null, 0),
   STRING(null, 0, EnumSet.of(TokenFlags.OPERAND, TokenFlags.LITERAL)),
   NUMBER(null, 0, EnumSet.of(TokenFlags.OPERAND, TokenFlags.LITERAL)),
   IDENTIFIER(null, 0, EnumSet.of(TokenFlags.OPERAND)),
   OPEN_BRACKET("(", 20, EnumSet.of(TokenFlags.OPENING_BRACKET), null, null),
   CLOSE_BRACKET(")", 20, EnumSet.of(TokenFlags.CLOSING_BRACKET), OPEN_BRACKET, null),
   OPEN_SQUARE_BRACKET("[", 19, EnumSet.of(TokenFlags.OPENING_BRACKET, TokenFlags.OPENING_TUPLE), null, null),
   CLOSE_SQUARE_BRACKET("]", 19, EnumSet.of(TokenFlags.CLOSING_BRACKET), OPEN_SQUARE_BRACKET, null),
   COMMA(",", 20, EnumSet.of(TokenFlags.LIST)),
   FUNCTION_CALL(null, 19),
   UNARY_MINUS(null, 16, EnumSet.of(TokenFlags.OPERATOR, TokenFlags.RIGHT_TO_LEFT, TokenFlags.UNARY)),
   UNARY_PLUS(null, 16, EnumSet.of(TokenFlags.OPERATOR, TokenFlags.RIGHT_TO_LEFT, TokenFlags.UNARY)),
   LOGICAL_NOT("!", 16, EnumSet.of(TokenFlags.OPERATOR, TokenFlags.RIGHT_TO_LEFT, TokenFlags.UNARY)),
   BITWISE_NOT("~", 16, EnumSet.of(TokenFlags.OPERATOR, TokenFlags.RIGHT_TO_LEFT, TokenFlags.UNARY)),
   EXPONENTIATION("**", 15, EnumSet.of(TokenFlags.OPERATOR, TokenFlags.RIGHT_TO_LEFT)),
   REMAINDER("%", 14, EnumSet.of(TokenFlags.OPERATOR)),
   DIVIDE("/", 14, EnumSet.of(TokenFlags.OPERATOR)),
   MULTIPLY("*", 14, EnumSet.of(TokenFlags.OPERATOR)),
   MINUS("-", 13, EnumSet.of(TokenFlags.OPERATOR), null, UNARY_MINUS),
   PLUS("+", 13, EnumSet.of(TokenFlags.OPERATOR), null, UNARY_PLUS),
   GREATER_EQUAL(">=", 11, EnumSet.of(TokenFlags.OPERATOR)),
   GREATER(">", 11, EnumSet.of(TokenFlags.OPERATOR)),
   LESS_EQUAL("<=", 11, EnumSet.of(TokenFlags.OPERATOR)),
   LESS("<", 11, EnumSet.of(TokenFlags.OPERATOR)),
   NOT_EQUAL("!=", 10, EnumSet.of(TokenFlags.OPERATOR)),
   EQUAL("==", 10, EnumSet.of(TokenFlags.OPERATOR)),
   BITWISE_AND("&", 9, EnumSet.of(TokenFlags.OPERATOR)),
   BITWISE_XOR("^", 8, EnumSet.of(TokenFlags.OPERATOR)),
   BITWISE_OR("|", 7, EnumSet.of(TokenFlags.OPERATOR)),
   LOGICAL_AND("&&", 6, EnumSet.of(TokenFlags.OPERATOR)),
   LOGICAL_OR("||", 5, EnumSet.of(TokenFlags.OPERATOR));

   private final String text;
   private final int precedence;
   private final EnumSet<TokenFlags> flags;
   @Nullable
   private final Token matchingBracket;
   @Nullable
   private final Token unaryVariant;

   private Token(String text, int precedence) {
      this.text = text;
      this.precedence = precedence;
      this.flags = EnumSet.noneOf(TokenFlags.class);
      this.matchingBracket = null;
      this.unaryVariant = null;
   }

   private Token(String text, int precedence, EnumSet<TokenFlags> flags) {
      this.text = text;
      this.precedence = precedence;
      this.flags = flags;
      this.matchingBracket = null;
      this.unaryVariant = null;
   }

   private Token(String text, int precedence, EnumSet<TokenFlags> flags, Token matchingBracket, Token unaryVariant) {
      this.text = text;
      this.precedence = precedence;
      this.flags = flags;
      this.matchingBracket = matchingBracket;
      this.unaryVariant = unaryVariant;
   }

   public String get() {
      return this.text;
   }

   public int getPrecedence() {
      return this.precedence;
   }

   public EnumSet<TokenFlags> getFlags() {
      return this.flags;
   }

   public boolean containsAnyFlag(@Nonnull EnumSet<TokenFlags> testFlags) {
      return !Collections.disjoint(this.flags, testFlags);
   }

   public boolean isEndToken() {
      return this == END;
   }

   public boolean isOperand() {
      return this.flags.contains(TokenFlags.OPERAND);
   }

   public boolean isLiteral() {
      return this.flags.contains(TokenFlags.LITERAL);
   }

   public boolean isOperator() {
      return this.flags.contains(TokenFlags.OPERATOR);
   }

   public boolean isRightToLeft() {
      return this.flags.contains(TokenFlags.RIGHT_TO_LEFT);
   }

   public boolean canBeUnary() {
      return this.unaryVariant != null;
   }

   @Nullable
   public Token getUnaryVariant() {
      return this.unaryVariant;
   }

   public boolean isUnary() {
      return this.flags.contains(TokenFlags.UNARY);
   }

   public boolean isOpenBracket() {
      return this.flags.contains(TokenFlags.OPENING_BRACKET);
   }

   public boolean isOpenTuple() {
      return this.flags.contains(TokenFlags.OPENING_TUPLE);
   }

   public boolean isCloseBracket() {
      return this.flags.contains(TokenFlags.CLOSING_BRACKET);
   }

   @Nullable
   public Token getMatchingBracket() {
      return this.matchingBracket;
   }

   public boolean isList() {
      return this.flags.contains(TokenFlags.LIST);
   }
}
