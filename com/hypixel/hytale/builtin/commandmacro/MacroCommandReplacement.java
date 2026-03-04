package com.hypixel.hytale.builtin.commandmacro;

import javax.annotation.Nullable;

public class MacroCommandReplacement {
   private final String nameOfReplacingArg;
   @Nullable
   private final String optionalArgumentKey;
   private final String stringToReplaceWithValue;

   public MacroCommandReplacement(String nameOfReplacingArg, String stringToReplaceWithValue, @Nullable String optionalArgumentKey) {
      this.nameOfReplacingArg = nameOfReplacingArg;
      this.stringToReplaceWithValue = stringToReplaceWithValue;
      this.optionalArgumentKey = optionalArgumentKey == null ? null : "--" + optionalArgumentKey + (optionalArgumentKey.endsWith("=") ? "" : " ");
   }

   public MacroCommandReplacement(String replacementKey, String stringToReplaceWithValue) {
      this(replacementKey, stringToReplaceWithValue, null);
   }

   public String getNameOfReplacingArg() {
      return this.nameOfReplacingArg;
   }

   @Nullable
   public String getOptionalArgumentKey() {
      return this.optionalArgumentKey;
   }

   public String getStringToReplaceWithValue() {
      return this.stringToReplaceWithValue;
   }
}
