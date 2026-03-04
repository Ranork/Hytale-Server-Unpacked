package com.hypixel.hytale.server.npc.util.expression;

import javax.annotation.Nullable;

public enum ValueType {
   VOID,
   NUMBER,
   STRING,
   BOOLEAN,
   EMPTY_ARRAY,
   NUMBER_ARRAY,
   STRING_ARRAY,
   BOOLEAN_ARRAY;

   public static boolean isAssignableType(@Nullable ValueType from, @Nullable ValueType to) {
      return to != null && from != null && to != VOID && from != VOID ? to == from || from == EMPTY_ARRAY && isTypedArray(to) : false;
   }

   public static boolean isTypedArray(ValueType valueType) {
      return valueType == BOOLEAN_ARRAY || valueType == NUMBER_ARRAY || valueType == STRING_ARRAY;
   }
}
