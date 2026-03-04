package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.server.core.Message;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParserContext {
   private static final HashSet<String> SPECIAL_TOKENS = new HashSet<>(
      List.of(Tokenizer.MULTI_ARG_BEGIN, Tokenizer.MULTI_ARG_END, Tokenizer.MULTI_ARG_SEPARATOR)
   );
   private static final int MAX_LIST_ITEMS = 10;
   @Nonnull
   private final String inputString;
   @Nonnull
   private final BooleanArrayList parameterForwardingMap;
   @Nonnull
   private final Int2ObjectMap<String> preOptionalSingleValueTokens;
   @Nonnull
   private final Int2ObjectMap<ParserContext.PreOptionalListContext> preOptionalListTokens;
   @Nonnull
   private final Object2ObjectLinkedOpenHashMap<String, List<List<String>>> optionalArgs;
   private String lastInsertedOptionalArgName;
   private int numPreOptSingleValueTokensBeforeListTokens;
   private int subCommandIndex;
   private static final Pattern ARG_NAME_PATTERN = Pattern.compile("--(\\w*)");
   private static final Matcher ARG_NAME_MATCHER = ARG_NAME_PATTERN.matcher("");
   private static final Pattern ARG_NAME_AND_VALUE_PATTERN = Pattern.compile("--(\\w+)=\"*(.*)\"*");
   private static final Matcher ARG_NAME_AND_VALUE_MATCHER = ARG_NAME_AND_VALUE_PATTERN.matcher("");

   public ParserContext(@Nonnull List<String> tokens, @Nonnull ParseResult parseResult) {
      this.inputString = String.join(" ", tokens);
      this.parameterForwardingMap = new BooleanArrayList();
      this.preOptionalSingleValueTokens = new Int2ObjectOpenHashMap();
      this.preOptionalListTokens = new Int2ObjectOpenHashMap();
      this.optionalArgs = new Object2ObjectLinkedOpenHashMap();
      this.contextualizeTokens(tokens, parseResult);
   }

   @Nonnull
   public static ParserContext of(@Nonnull List<String> tokens, @Nonnull ParseResult parseResult) {
      return new ParserContext(tokens, parseResult);
   }

   private void contextualizeTokens(@Nonnull List<String> tokens, @Nonnull ParseResult parseResult) {
      boolean beganParsingOptionals = false;
      boolean inList = false;
      boolean isSingleValueList = false;
      boolean wasLastTokenASpecialValue = false;
      boolean hasEnteredListBefore = false;

      for (int i = 0; i < tokens.size(); i++) {
         String token = tokens.get(i);
         if (inList) {
            hasEnteredListBefore = true;
         }

         if (SPECIAL_TOKENS.contains(token)) {
            boolean isListEndingAndStartingNew = tokens.get(i - 1).equals(Tokenizer.MULTI_ARG_END) && !inList && token.equals(Tokenizer.MULTI_ARG_BEGIN);
            if (wasLastTokenASpecialValue && !isListEndingAndStartingNew) {
               StringBuilder stringBuilder = new StringBuilder();

               for (int i1 = 0; i1 < tokens.size(); i1++) {
                  stringBuilder.append(tokens.get(i1)).append(" ");
                  if (i1 == i) {
                     stringBuilder.append(" <--- *HERE!* ");
                  }
               }

               parseResult.fail(Message.translation("server.commands.parsing.error.cantDoublePlaceSpecialTokens"), Message.raw(stringBuilder.toString()));
               return;
            }

            wasLastTokenASpecialValue = true;
         } else {
            wasLastTokenASpecialValue = false;
         }

         ARG_NAME_MATCHER.reset(token);
         if (ARG_NAME_MATCHER.lookingAt()) {
            beganParsingOptionals = true;
            this.addNewOptionalArg(ARG_NAME_MATCHER.group(1));
            ARG_NAME_AND_VALUE_MATCHER.reset(token);
            if (ARG_NAME_AND_VALUE_MATCHER.matches()) {
               this.appendOptionalParameter(ARG_NAME_AND_VALUE_MATCHER.group(2), parseResult);
               if (parseResult.failed()) {
                  return;
               }
            }
         } else if (beganParsingOptionals) {
            this.appendOptionalParameter(token, parseResult);
            if (parseResult.failed()) {
               return;
            }
         } else if (token.equals(Tokenizer.MULTI_ARG_BEGIN)) {
            inList = true;
            isSingleValueList = false;
            this.parameterForwardingMap.add(true);
            this.preOptionalListTokens.put(this.parameterForwardingMap.size() - 1, new ParserContext.PreOptionalListContext());
         } else if (token.equals(Tokenizer.MULTI_ARG_END)) {
            inList = false;
         } else if (inList) {
            ((ParserContext.PreOptionalListContext)this.preOptionalListTokens.get(this.parameterForwardingMap.size() - 1)).addToken(token, parseResult);
            if (parseResult.failed()) {
               return;
            }

            if (isSingleValueList && !wasLastTokenASpecialValue && tokens.size() > i + 1 && !tokens.get(i + 1).equals(Tokenizer.MULTI_ARG_SEPARATOR)) {
               inList = false;
            }
         } else if (tokens.size() > i + 1 && tokens.get(i + 1).equals(Tokenizer.MULTI_ARG_SEPARATOR)) {
            inList = true;
            isSingleValueList = true;
            this.parameterForwardingMap.add(true);
            this.preOptionalListTokens.put(this.parameterForwardingMap.size() - 1, new ParserContext.PreOptionalListContext().addToken(token, parseResult));
            if (parseResult.failed()) {
               return;
            }
         } else {
            if (!hasEnteredListBefore) {
               this.numPreOptSingleValueTokensBeforeListTokens++;
            }

            this.parameterForwardingMap.add(false);
            this.preOptionalSingleValueTokens.put(this.parameterForwardingMap.size() - 1, token);
         }
      }

      if (inList && !isSingleValueList) {
         parseResult.fail(Message.translation("server.commands.parsing.error.endCommandWithOpenList").param("listEndToken", Tokenizer.MULTI_ARG_END));
      }
   }

   public void addNewOptionalArg(String name) {
      name = name.toLowerCase();
      this.lastInsertedOptionalArgName = name;
      this.optionalArgs.put(name, new ObjectArrayList());
   }

   public void appendOptionalParameter(@Nonnull String value, @Nonnull ParseResult parseResult) {
      if (!this.optionalArgs.isEmpty() && this.lastInsertedOptionalArgName != null) {
         List<List<String>> args = (List<List<String>>)this.optionalArgs.get(this.lastInsertedOptionalArgName);
         if (!value.equals(Tokenizer.MULTI_ARG_BEGIN) && !value.equals(Tokenizer.MULTI_ARG_END)) {
            if (value.equals(Tokenizer.MULTI_ARG_SEPARATOR)) {
               args.add(new ObjectArrayList());
            } else if (args.isEmpty()) {
               ObjectArrayList<String> values = new ObjectArrayList();
               values.add(value);
               args.add(values);
            } else {
               args.getLast().add(value);
            }
         }
      } else {
         parseResult.fail(Message.translation("server.commands.parsing.error.noOptionalParameterToAddValueTo"));
      }
   }

   @Nonnull
   public String getInputString() {
      return this.inputString;
   }

   public boolean isListToken(int index) {
      index += this.subCommandIndex;
      return this.parameterForwardingMap.size() <= index ? false : this.parameterForwardingMap.getBoolean(index);
   }

   public int getNumPreOptSingleValueTokensBeforeListTokens() {
      return this.numPreOptSingleValueTokensBeforeListTokens - this.subCommandIndex;
   }

   public int getNumPreOptionalTokens() {
      int numPreOptionalTokens = 0;
      numPreOptionalTokens += this.preOptionalSingleValueTokens.size();
      ObjectIterator var2 = this.preOptionalListTokens.values().iterator();

      while (var2.hasNext()) {
         ParserContext.PreOptionalListContext value = (ParserContext.PreOptionalListContext)var2.next();
         numPreOptionalTokens += value.numTokensPerArgument;
      }

      return numPreOptionalTokens - this.subCommandIndex;
   }

   public String getPreOptionalSingleValueToken(int index) {
      index += this.subCommandIndex;
      return (String)this.preOptionalSingleValueTokens.get(index);
   }

   public ParserContext.PreOptionalListContext getPreOptionalListToken(int index) {
      index += this.subCommandIndex;
      return (ParserContext.PreOptionalListContext)this.preOptionalListTokens.get(index);
   }

   @Nullable
   public String getFirstToken() {
      if (this.parameterForwardingMap.size() <= this.subCommandIndex) {
         return null;
      } else if (this.parameterForwardingMap.getBoolean(this.subCommandIndex)) {
         ParserContext.PreOptionalListContext preOptionalListContext = (ParserContext.PreOptionalListContext)this.preOptionalListTokens
            .get(this.subCommandIndex);
         return preOptionalListContext.tokens.isEmpty() ? null : preOptionalListContext.tokens.getFirst();
      } else {
         return (String)this.preOptionalSingleValueTokens.get(this.subCommandIndex);
      }
   }

   @Nonnull
   public ObjectSortedSet<Entry<String, List<List<String>>>> getOptionalArgs() {
      return this.optionalArgs.entrySet();
   }

   public boolean isHelpSpecified() {
      return this.optionalArgs.containsKey("help") || this.optionalArgs.containsKey("?");
   }

   public boolean isConfirmationSpecified() {
      return this.optionalArgs.containsKey("confirm");
   }

   public void convertToSubCommand() {
      this.subCommandIndex++;
   }

   public static class PreOptionalListContext {
      private final List<String> tokens = new ObjectArrayList();
      private boolean hasReachedFirstMultiArgSeparator = false;
      private int numTokensPerArgument = 0;
      private int numTokensSinceLastSeparator = 0;
      private int numberOfListItems = 0;

      @Nullable
      public ParserContext.PreOptionalListContext addToken(@Nonnull String token, @Nonnull ParseResult parseResult) {
         if (token.equals(Tokenizer.MULTI_ARG_SEPARATOR)) {
            if (!this.hasReachedFirstMultiArgSeparator) {
               this.hasReachedFirstMultiArgSeparator = true;
               this.numTokensSinceLastSeparator = 0;
               this.numberOfListItems++;
               this.verifyNumberOfListItems(parseResult);
               return this;
            } else if (this.numTokensSinceLastSeparator != this.numTokensPerArgument) {
               this.tokens.add(token);
               parseResult.fail(
                  Message.translation("server.commands.parsing.error.allArgumentsInListNeedSameLength").param("error", this.getStringRepresentation(true))
               );
               return null;
            } else {
               this.numTokensSinceLastSeparator = 0;
               this.numberOfListItems++;
               this.verifyNumberOfListItems(parseResult);
               return this;
            }
         } else {
            this.numTokensSinceLastSeparator++;
            if (this.numberOfListItems == 0) {
               this.numTokensPerArgument++;
            }

            if (this.hasReachedFirstMultiArgSeparator && this.numTokensSinceLastSeparator > this.numTokensPerArgument) {
               this.tokens.add(token);
               parseResult.fail(
                  Message.translation("server.commands.parsing.error.allArgumentsInListNeedSameLength").param("error", this.getStringRepresentation(true))
               );
               return null;
            } else {
               this.tokens.add(token);
               return this;
            }
         }
      }

      @Nonnull
      private String getStringRepresentation(boolean asTooLongFailure) {
         StringBuilder stringBuilder = new StringBuilder(Tokenizer.MULTI_ARG_BEGIN);

         for (int i = 0; i < this.tokens.size(); i++) {
            if (i != 0 && i % this.numTokensPerArgument == 0 && i != this.tokens.size() - 1) {
               stringBuilder.append(" ").append(Tokenizer.MULTI_ARG_SEPARATOR);
            }

            stringBuilder.append(" ").append(this.tokens.get(i));
         }

         if (asTooLongFailure) {
            stringBuilder.append("<-- *HERE* ... ]");
         } else {
            stringBuilder.append(" ]");
         }

         return stringBuilder.toString();
      }

      public void verifyNumberOfListItems(@Nonnull ParseResult parseResult) {
         if (this.numberOfListItems > 10) {
            parseResult.fail(Message.translation("server.commands.parsing.error.tooManyListItems").param("amount", 10));
         }
      }

      @Nonnull
      public String[] getTokens() {
         return this.tokens.toArray(String[]::new);
      }

      public int getNumTokensPerArgument() {
         return this.numTokensPerArgument;
      }

      public int getNumberOfListItems() {
         return this.numberOfListItems;
      }
   }
}
