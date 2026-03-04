package com.hypixel.hytale.server.npc.util.expression.compile;

import java.text.ParseException;
import javax.annotation.Nonnull;

public class LexerContext<Token> {
   private String expression;
   private int length;
   private int position;
   private Token token;
   private int tokenPosition;
   @Nonnull
   private StringBuilder tokenString = new StringBuilder(200);
   private double tokenNumber;

   public void init(@Nonnull String expression) {
      this.expression = expression;
      this.length = expression.length();
      this.position = 0;
   }

   public void resetToken() {
      this.tokenPosition = this.position;
      this.tokenString.setLength(0);
   }

   public Token setToken(Token token) {
      this.token = token;
      return token;
   }

   public String getExpression() {
      return this.expression;
   }

   public Token getToken() {
      return this.token;
   }

   public int getTokenPosition() {
      return this.tokenPosition;
   }

   @Nonnull
   public String getTokenString() {
      return this.tokenString.toString();
   }

   public double getTokenNumber() {
      return this.tokenNumber;
   }

   protected char nextChar(String error) throws ParseException {
      this.position++;
      if (this.position >= this.length) {
         throw new ParseException(error, this.tokenPosition);
      } else {
         return this.expression.charAt(this.position);
      }
   }

   protected boolean haveChar() {
      return this.position < this.length;
   }

   protected char currentChar() {
      return this.expression.charAt(this.position);
   }

   protected char peekChar(char defaultChar) {
      return this.position < this.length ? this.currentChar() : defaultChar;
   }

   protected char peekChar() {
      return this.position < this.length ? this.currentChar() : '\u0000';
   }

   protected char peekChar(int lookahead, char defaultChar) {
      return this.position + lookahead < this.length ? this.expression.charAt(this.position + lookahead) : defaultChar;
   }

   protected char peekChar(int lookahead) {
      return this.position + lookahead < this.length ? this.expression.charAt(this.position + lookahead) : '\u0000';
   }

   protected boolean eatWhiteSpace() {
      while (this.position < this.length && Character.isWhitespace(this.expression.charAt(this.position))) {
         this.position++;
      }

      return this.position < this.length;
   }

   protected char addTokenCharacter(char ch) {
      this.tokenString.append(ch);
      this.position++;
      return this.peekChar();
   }

   protected int getPosition() {
      return this.position;
   }

   protected void setPosition(int position) {
      this.position = position;
   }

   protected void adjustPosition(int newPosition) {
      if (newPosition < this.position) {
         this.tokenString.setLength(this.tokenString.length() - (this.position - newPosition));
      }

      this.position = newPosition;
   }

   protected boolean isNumber(char firstLetter) {
      return Character.isDigit(firstLetter) || firstLetter == '.' && Character.isDigit(this.peekChar(1, '\u0000'));
   }

   protected void parseNumber(char firstChar) throws ParseException {
      this.tokenNumber = 0.0;
      char ch = this.copyDigits(firstChar);
      if (this.position < this.length && ch == '.') {
         this.tokenString.append(ch);
         this.position++;
         if (!Character.isDigit(this.currentChar())) {
            throw new ParseException("Invalid number format", this.tokenPosition);
         }

         ch = this.copyDigits(ch);
      }

      if (this.position < this.length && (ch == 'e' || ch == 'E')) {
         this.tokenString.append(ch);
         ch = this.nextChar("Invalid number format");
         if (ch == '-' || ch == '+') {
            this.tokenString.append(ch);
            ch = this.nextChar("Invalid number format");
         }

         if (!Character.isDigit(ch)) {
            throw new ParseException("Invalid number format", this.tokenPosition);
         }

         this.copyDigits(ch);
      }

      this.tokenNumber = Double.parseDouble(this.tokenString.toString());
   }

   private char copyDigits(char ch) {
      while (this.position < this.length) {
         ch = this.currentChar();
         if (Character.isDigit(ch)) {
            this.tokenString.append(ch);
            this.position++;
            continue;
         }
         break;
      }

      return ch;
   }

   protected void parseIdent(char firstLetter) {
      this.tokenString.append(firstLetter);
      this.position++;

      while (this.position < this.length && (Character.isLetterOrDigit(this.currentChar()) || this.currentChar() == '_')) {
         this.tokenString.append(this.currentChar());
         this.position++;
      }
   }

   protected void parseString(char delimiter) throws ParseException {
      this.tokenPosition = this.position;

      for (char ch = this.nextChar("Unterminated string"); ch != delimiter; ch = this.nextChar("Unterminated string")) {
         this.tokenString.append(ch != '\\' ? ch : this.nextChar("Unterminated string"));
      }

      this.position++;
   }
}
