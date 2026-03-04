package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ThrowingValidationResults;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

@Deprecated
public class EmptyExtraInfo extends ExtraInfo {
   public static final EmptyExtraInfo EMPTY = new EmptyExtraInfo();

   private EmptyExtraInfo() {
      super(Integer.MAX_VALUE, ThrowingValidationResults::new);
   }

   @Override
   public void pushKey(String key) {
   }

   @Override
   public void pushIntKey(int i) {
   }

   @Override
   public void pushKey(String key, RawJsonReader reader) {
   }

   @Override
   public void pushIntKey(int key, RawJsonReader reader) {
   }

   @Override
   public void popKey() {
   }

   @Override
   public void addUnknownKey(@Nonnull String key) {
   }

   @Override
   public void ignoreUnusedKey(String key) {
   }

   @Override
   public void popIgnoredUnusedKey() {
   }

   @Nonnull
   @Override
   public String peekKey() {
      return "<empty>";
   }

   @Nonnull
   @Override
   public String peekKey(char separator) {
      return "<empty>";
   }

   @Nonnull
   @Override
   public List<String> getUnknownKeys() {
      return Collections.emptyList();
   }

   @Override
   public void appendDetailsTo(@Nonnull StringBuilder sb) {
      sb.append("EmptyExtraInfo\n");
   }

   @Nonnull
   @Override
   public String toString() {
      return "EmptyExtraInfo{} " + super.toString();
   }
}
