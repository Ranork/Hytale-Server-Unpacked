package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.protocol.BoolParamValue;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.DoubleParamValue;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.IntParamValue;
import com.hypixel.hytale.protocol.LongParamValue;
import com.hypixel.hytale.protocol.ParamValue;
import com.hypixel.hytale.protocol.StringParamValue;
import com.hypixel.hytale.protocol.packets.asseteditor.FailureReply;
import com.hypixel.hytale.protocol.packets.asseteditor.SuccessReply;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Colors;

public class MessageUtil {
   private static final String[] ICU_PLURAL_KEYWORDS = new String[]{"zero", "one", "two", "few", "many", "other"};

   public static AttributedString toAnsiString(@Nonnull Message message) {
      AttributedStyle style = AttributedStyle.DEFAULT;
      String color = message.getColor();
      if (color != null) {
         style = hexToStyle(color);
      }

      AttributedStringBuilder sb = new AttributedStringBuilder();
      sb.style(style).append(message.getAnsiMessage());

      for (Message child : message.getChildren()) {
         sb.append(toAnsiString(child));
      }

      return sb.toAttributedString();
   }

   public static AttributedStyle hexToStyle(@Nonnull String str) {
      Color color = ColorParseUtil.parseColor(str);
      if (color == null) {
         return AttributedStyle.DEFAULT;
      } else {
         int colorId = Colors.roundRgbColor(color.red & 255, color.green & 255, color.blue & 255, 256);
         return AttributedStyle.DEFAULT.foreground(colorId);
      }
   }

   @Deprecated
   public static void sendSuccessReply(@Nonnull PlayerRef playerRef, int token) {
      sendSuccessReply(playerRef, token, null);
   }

   @Deprecated
   public static void sendSuccessReply(@Nonnull PlayerRef playerRef, int token, @Nullable Message message) {
      FormattedMessage msg = message != null ? message.getFormattedMessage() : null;
      playerRef.getPacketHandler().writeNoCache(new SuccessReply(token, msg));
   }

   @Deprecated
   public static void sendFailureReply(@Nonnull PlayerRef playerRef, int token, @Nonnull Message message) {
      FormattedMessage msg = message != null ? message.getFormattedMessage() : null;
      playerRef.getPacketHandler().writeNoCache(new FailureReply(token, msg));
   }

   @Nonnull
   public static String formatText(String text, @Nullable Map<String, ParamValue> params, @Nullable Map<String, FormattedMessage> messageParams) {
      if (text == null) {
         throw new IllegalArgumentException("text cannot be null");
      } else if (params == null && messageParams == null) {
         return text;
      } else {
         int len = text.length();
         StringBuilder sb = new StringBuilder(text.length());
         int lastWritePos = 0;

         for (int i = 0; i < len; i++) {
            char ch = text.charAt(i);
            if (ch == '{') {
               if (i + 1 < len && text.charAt(i + 1) == '{') {
                  if (i > lastWritePos) {
                     sb.append(text, lastWritePos, i);
                  }

                  sb.append('{');
                  lastWritePos = ++i + 1;
               } else {
                  int end = findMatchingBrace(text, i);
                  if (end >= 0) {
                     if (i > lastWritePos) {
                        sb.append(text, lastWritePos, i);
                     }

                     int contentStart = i + 1;
                     int c1 = text.indexOf(44, contentStart, end);
                     int c2 = c1 >= 0 ? text.indexOf(44, c1 + 1, end) : -1;
                     int nameEndExclusive = c1 >= 0 && c1 < end ? c1 : end;
                     int ns = trimStart(text, contentStart, nameEndExclusive - 1);
                     int nl = trimEnd(text, ns, nameEndExclusive - 1);
                     String key = nl > 0 ? text.substring(ns, ns + nl) : "";
                     String format = null;
                     if (c1 >= 0 && c1 < end) {
                        int formatStart = c1 + 1;
                        int formatEndExclusive = c2 >= 0 ? c2 : end;
                        int fs = trimStart(text, formatStart, formatEndExclusive - 1);
                        int fl = trimEnd(text, fs, formatEndExclusive - 1);
                        if (fl > 0) {
                           format = text.substring(fs, fs + fl);
                        }
                     }

                     String options = null;
                     if (c2 >= 0 && c2 < end) {
                        int optionsStart = c2 + 1;
                        int os = trimStart(text, optionsStart, end - 1);
                        int ol = trimEnd(text, os, end - 1);
                        if (ol > 0) {
                           options = text.substring(os, os + ol);
                        }
                     }

                     ParamValue replacement = params != null ? params.get(key) : null;
                     FormattedMessage replacementMessage = messageParams != null ? messageParams.get(key) : null;
                     if (replacementMessage != null) {
                        if (replacementMessage.rawText != null) {
                           sb.append(replacementMessage.rawText);
                        } else if (replacementMessage.messageId != null) {
                           String message = I18nModule.get().getMessage("en-US", replacementMessage.messageId);
                           if (message != null) {
                              sb.append(formatText(message, replacementMessage.params, replacementMessage.messageParams));
                           } else {
                              sb.append(replacementMessage.messageId);
                           }
                        }
                     } else if (replacement != null) {
                        String formattedReplacement;
                        formattedReplacement = "";
                        label171:
                        switch (format) {
                           case "upper":
                              if (replacement instanceof StringParamValue s) {
                                 formattedReplacement = s.value.toUpperCase();
                              }
                              break;
                           case "lower":
                              if (replacement instanceof StringParamValue s) {
                                 formattedReplacement = s.value.toLowerCase();
                              }
                              break;
                           case "number":
                              switch (options) {
                                 case "integer":
                                    formattedReplacement = switch (replacement) {
                                       case StringParamValue s -> s.value;
                                       case BoolParamValue b -> b.value ? "1" : "0";
                                       case DoubleParamValue d -> Integer.toString((int)d.value);
                                       case IntParamValue iv -> Integer.toString(iv.value);
                                       case LongParamValue l -> Long.toString(l.value);
                                       default -> "";
                                    };
                                    break label171;
                                 case "decimal":
                                 case null:
                                 default:
                                    formattedReplacement = switch (replacement) {
                                       case StringParamValue s -> s.value;
                                       case BoolParamValue b -> b.value ? "1" : "0";
                                       case DoubleParamValue d -> Double.toString((int)d.value);
                                       case IntParamValue iv -> Integer.toString(iv.value);
                                       case LongParamValue l -> Long.toString(l.value);
                                       default -> "";
                                    };
                                    break label171;
                              }
                           case "plural":
                              if (options != null) {
                                 Map<String, String> pluralTexts = parsePluralOptions(options);
                                 int value = Integer.parseInt(replacement.toString());
                                 String category = getPluralCategory(value, "en-US");
                                 String selected;
                                 if (pluralTexts.containsKey(category)) {
                                    selected = pluralTexts.get(category);
                                 } else if (pluralTexts.containsKey("other")) {
                                    selected = pluralTexts.get("other");
                                 } else {
                                    selected = pluralTexts.isEmpty() ? "" : pluralTexts.values().iterator().next();
                                 }

                                 formattedReplacement = formatText(selected, params, messageParams);
                              }
                              break;
                           case "date":
                              Instant instantx = parseDateTime(replacement);
                              if (instantx != null) {
                                 DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.getDefault());
                                 formattedReplacement = formatter.format(instantx.atZone(ZoneId.systemDefault()));
                              } else {
                                 formattedReplacement = "";
                              }
                              break;
                           case "time":
                              Instant instant = parseDateTime(replacement);
                              if (instant != null) {
                                 DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault());
                                 formattedReplacement = formatter.format(instant.atZone(ZoneId.systemDefault()));
                              } else {
                                 formattedReplacement = "";
                              }
                              break;
                           case "select":
                              if (options != null) {
                                 Map<String, String> selectOptions = parseSelectOptions(options);
                                 String selectKey = replacement.toString();
                                 String selected;
                                 if (selectOptions.containsKey(selectKey)) {
                                    selected = selectOptions.get(selectKey);
                                 } else if (selectOptions.containsKey("other")) {
                                    selected = selectOptions.get("other");
                                 } else {
                                    selected = selectOptions.isEmpty() ? "" : selectOptions.values().iterator().next();
                                 }

                                 formattedReplacement = formatText(selected, params, messageParams);
                              }
                           case null:
                        }

                        if (format == null) {
                           formattedReplacement = switch (replacement) {
                              case StringParamValue s -> s.value;
                              case BoolParamValue b -> Boolean.toString(b.value);
                              case DoubleParamValue d -> Double.toString(d.value);
                              case IntParamValue iv -> Integer.toString(iv.value);
                              case LongParamValue l -> Long.toString(l.value);
                              default -> "";
                           };
                        }

                        sb.append(formattedReplacement);
                     } else {
                        sb.append(text, i, end);
                     }

                     i = end;
                     lastWritePos = end + 1;
                  }
               }
            } else if (ch == '}' && i + 1 < len && text.charAt(i + 1) == '}') {
               if (i > lastWritePos) {
                  sb.append(text, lastWritePos, i);
               }

               sb.append('}');
               lastWritePos = ++i + 1;
            }
         }

         if (lastWritePos < len) {
            sb.append(text, lastWritePos, len);
         }

         return sb.toString();
      }
   }

   private static int findMatchingBrace(@Nonnull String text, int start) {
      int depth = 0;
      int len = text.length();

      for (int i = start; i < len; i++) {
         if (text.charAt(i) == '{') {
            depth++;
         } else if (text.charAt(i) == '}') {
            if (--depth == 0) {
               return i;
            }
         }
      }

      return -1;
   }

   private static int trimStart(@Nonnull String text, int start, int end) {
      int i = start;

      while (i <= end && Character.isWhitespace(text.charAt(i))) {
         i++;
      }

      return i;
   }

   private static int trimEnd(@Nonnull String text, int start, int end) {
      int i = start;

      while (end >= i && Character.isWhitespace(text.charAt(i))) {
         end--;
      }

      return end >= i ? end - i + 1 : 0;
   }

   @Nonnull
   private static Map<String, String> parsePluralOptions(@Nonnull String options) {
      HashMap<String, String> result = new HashMap<>();

      for (String keyword : ICU_PLURAL_KEYWORDS) {
         String searchPattern = keyword + " {";
         int idx = options.indexOf(searchPattern);
         if (idx >= 0) {
            int braceStart = idx + keyword.length() + 1;
            int end = findMatchingBrace(options, braceStart);
            if (end > braceStart + 1) {
               result.put(keyword, options.substring(braceStart + 1, end));
            }
         }
      }

      return result;
   }

   @Nonnull
   private static String getPluralCategory(int n, @Nonnull String locale) {
      String lang = locale.contains("-") ? locale.substring(0, locale.indexOf(45)) : locale;

      return switch (lang) {
         case "en" -> getEnglishPluralCategory(n);
         case "fr" -> getFrenchPluralCategory(n);
         case "de" -> getGermanPluralCategory(n);
         case "pt" -> !locale.equals("pt-BR") && !locale.equals("pt_BR") ? getPortuguesePluralCategory(n) : getPortugueseBrazilianPluralCategory(n);
         case "ru" -> getRussianPluralCategory(n);
         case "es" -> getSpanishPluralCategory(n);
         case "pl" -> getPolishPluralCategory(n);
         case "tr" -> getTurkishPluralCategory(n);
         case "uk" -> getUkrainianPluralCategory(n);
         case "it" -> getItalianPluralCategory(n);
         case "nl" -> getDutchPluralCategory(n);
         case "da" -> getDanishPluralCategory(n);
         case "fi" -> getFinnishPluralCategory(n);
         case "no", "nb", "nn" -> getNorwegianPluralCategory(n);
         case "zh" -> getChinesePluralCategory(n);
         case "ja" -> getJapanesePluralCategory(n);
         case "ko" -> getKoreanPluralCategory(n);
         default -> getEnglishPluralCategory(n);
      };
   }

   @Nonnull
   private static String getEnglishPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getFrenchPluralCategory(int n) {
      return n != 0 && n != 1 ? "other" : "one";
   }

   @Nonnull
   private static String getGermanPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getPortuguesePluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getPortugueseBrazilianPluralCategory(int n) {
      return n != 0 && n != 1 ? "other" : "one";
   }

   @Nonnull
   private static String getRussianPluralCategory(int n) {
      int absN = Math.abs(n);
      int mod10 = absN % 10;
      int mod100 = absN % 100;
      if (mod10 == 1 && mod100 != 11) {
         return "one";
      } else if (mod10 < 2 || mod10 > 4 || mod100 >= 12 && mod100 <= 14) {
         return mod10 != 0 && (mod10 < 5 || mod10 > 9) && (mod100 < 11 || mod100 > 14) ? "other" : "many";
      } else {
         return "few";
      }
   }

   @Nonnull
   private static String getSpanishPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getPolishPluralCategory(int n) {
      int absN = Math.abs(n);
      int mod10 = absN % 10;
      int mod100 = absN % 100;
      if (n == 1) {
         return "one";
      } else if (mod10 < 2 || mod10 > 4 || mod100 >= 12 && mod100 <= 14) {
         return mod10 != 0 && mod10 != 1 && (mod10 < 5 || mod10 > 9) && (mod100 < 12 || mod100 > 14) ? "other" : "many";
      } else {
         return "few";
      }
   }

   @Nonnull
   private static String getTurkishPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getUkrainianPluralCategory(int n) {
      int absN = Math.abs(n);
      int mod10 = absN % 10;
      int mod100 = absN % 100;
      if (mod10 == 1 && mod100 != 11) {
         return "one";
      } else if (mod10 < 2 || mod10 > 4 || mod100 >= 12 && mod100 <= 14) {
         return mod10 != 0 && (mod10 < 5 || mod10 > 9) && (mod100 < 11 || mod100 > 14) ? "other" : "many";
      } else {
         return "few";
      }
   }

   @Nonnull
   private static String getItalianPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getDutchPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getDanishPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getFinnishPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getNorwegianPluralCategory(int n) {
      return n == 1 ? "one" : "other";
   }

   @Nonnull
   private static String getChinesePluralCategory(int n) {
      return "other";
   }

   @Nonnull
   private static String getJapanesePluralCategory(int n) {
      return "other";
   }

   @Nonnull
   private static String getKoreanPluralCategory(int n) {
      return "other";
   }

   @Nonnull
   private static Map<String, String> parseSelectOptions(@Nonnull String options) {
      HashMap<String, String> result = new HashMap<>();
      int i = 0;
      int len = options.length();

      while (i < len) {
         while (i < len && Character.isWhitespace(options.charAt(i))) {
            i++;
         }

         if (i >= len) {
            break;
         }

         int keyStart = i;

         while (i < len && !Character.isWhitespace(options.charAt(i)) && options.charAt(i) != '{') {
            i++;
         }

         if (i == keyStart) {
            break;
         }

         String key = options.substring(keyStart, i);

         while (i < len && Character.isWhitespace(options.charAt(i))) {
            i++;
         }

         if (i >= len || options.charAt(i) != '{') {
            break;
         }

         int braceEnd = findMatchingBrace(options, i);
         if (braceEnd < 0) {
            break;
         }

         if (braceEnd > i + 1) {
            result.put(key, options.substring(i + 1, braceEnd));
         } else {
            result.put(key, "");
         }

         i = braceEnd + 1;
      }

      return result;
   }

   @Nullable
   private static Instant parseDateTime(@Nonnull ParamValue value) {
      return switch (value) {
         case LongParamValue l -> {
            Instant var10 = Instant.ofEpochMilli(l.value);
            yield var10;
         }
         case StringParamValue s -> {
            Instant var9;
            try {
               var9 = Instant.parse(s.value);
            } catch (DateTimeParseException var7) {
               var9 = null;
               yield var9;
            }

            yield var9;
         }
         default -> {
            Object var3 = null;
            yield var3;
         }
      };
   }
}
