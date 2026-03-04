package com.hypixel.hytale.server.core.modules.i18n.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class LangFileParser {
   @Nonnull
   private static String literal(@Nonnull String value) {
      String literal = value.trim();
      return literal.length() > 1 && literal.charAt(0) == '"' && literal.charAt(literal.length() - 1) == '"'
         ? literal.substring(1, literal.length() - 1)
         : literal;
   }

   @Nonnull
   private static String escape(@Nonnull StringBuilder builder) {
      return builder.toString().replace("\\n", "\n").replace("\\t", "\t");
   }

   @Nonnull
   public static Map<String, String> parse(@Nonnull BufferedReader reader) throws IOException, LangFileParser.TranslationParseException {
      Map<String, String> translations = new LinkedHashMap<>();
      String currKey = null;
      StringBuilder currValue = null;
      int lineNumber = 0;

      String line;
      while ((line = reader.readLine()) != null) {
         lineNumber++;
         line = line.trim();
         if (!line.isEmpty() && line.charAt(0) != '#') {
            if (currKey == null) {
               int eqIdx = line.indexOf(61);
               if (eqIdx < 0) {
                  throw new LangFileParser.TranslationParseException("Missing '=' in key-value line", lineNumber, line);
               }

               String key = line.substring(0, eqIdx).trim();
               if (key.isEmpty()) {
                  throw new LangFileParser.TranslationParseException("Empty key in line", lineNumber, line);
               }

               String value = line.substring(eqIdx + 1).trim();
               if (value.isEmpty()) {
                  throw new LangFileParser.TranslationParseException("Empty value in line", lineNumber, line);
               }

               currKey = key;
               currValue = new StringBuilder();
               boolean isMultiline = value.charAt(value.length() - 1) == '\\';
               if (isMultiline) {
                  currValue.append(value, 0, value.length() - 1);
               } else {
                  currValue.append(literal(value));
                  String existing = translations.put(key, escape(currValue));
                  if (existing != null) {
                     throw new LangFileParser.TranslationParseException("Duplicate key in line", lineNumber, line);
                  }

                  currKey = null;
                  currValue = null;
               }
            } else {
               boolean isMultiline = line.charAt(line.length() - 1) == '\\';
               String valueLine = isMultiline ? line.substring(0, line.length() - 1) : line;
               currValue.append(valueLine.trim());
               if (!isMultiline) {
                  String existing = translations.put(currKey, escape(currValue));
                  if (existing != null) {
                     throw new LangFileParser.TranslationParseException("Duplicate key in line", lineNumber, line);
                  }

                  currKey = null;
                  currValue = null;
               }
            }
         }
      }

      if (currKey != null) {
         throw new LangFileParser.TranslationParseException("Unexpected end of key-value line", lineNumber, currKey);
      } else {
         return translations;
      }
   }

   public static class TranslationParseException extends Exception {
      TranslationParseException(String message, int lineNumber, String lineContent) {
         super(message + " (at line " + lineNumber + "): " + lineContent);
      }
   }
}
