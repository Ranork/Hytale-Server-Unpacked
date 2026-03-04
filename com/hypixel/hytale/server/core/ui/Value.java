package com.hypixel.hytale.server.core.ui;

import javax.annotation.Nonnull;

public class Value<T> {
   private T value;
   private String documentPath;
   private String valueName;

   private Value(String documentPath, String valueName) {
      this.documentPath = documentPath;
      this.valueName = valueName;
   }

   private Value(T value) {
      this.value = value;
   }

   public T getValue() {
      return this.value;
   }

   public String getDocumentPath() {
      return this.documentPath;
   }

   public String getValueName() {
      return this.valueName;
   }

   @Nonnull
   public static <T> Value<T> ref(String document, String value) {
      return new Value<>(document, value);
   }

   @Nonnull
   public static <T> Value<T> of(T value) {
      return new Value<>(value);
   }
}
