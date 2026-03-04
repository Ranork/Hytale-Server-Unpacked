package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.store.CodecStore;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ThrowingValidationResults;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.logger.util.GithubMessageUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class ExtraInfo {
   public static final ThreadLocal<ExtraInfo> THREAD_LOCAL = ThreadLocal.withInitial(ExtraInfo::new);
   public static final String GENERATED_ID_PREFIX = "*";
   public static final int UNSET_VERSION = Integer.MAX_VALUE;
   private final int legacyVersion;
   private final int keysInitialSize = this instanceof EmptyExtraInfo ? 0 : 128;
   @Nonnull
   private String[] stringKeys = new String[this.keysInitialSize];
   @Nonnull
   private int[] intKeys = new int[this.keysInitialSize];
   private int[] lineNumbers = GithubMessageUtil.isGithub() ? new int[this.keysInitialSize] : null;
   private int[] columnNumbers = GithubMessageUtil.isGithub() ? new int[this.keysInitialSize] : null;
   private int keysSize;
   @Nonnull
   private String[] ignoredUnknownKeys = new String[this.keysInitialSize];
   private int ignoredUnknownSize;
   private final List<String> unknownKeys = new ObjectArrayList();
   private final ValidationResults validationResults;
   private final CodecStore codecStore;
   @Deprecated
   private final Map<String, Object> metadata = new Object2ObjectOpenHashMap();

   public ExtraInfo() {
      this.legacyVersion = Integer.MAX_VALUE;
      this.validationResults = new ThrowingValidationResults(this);
      this.codecStore = CodecStore.STATIC;
   }

   @Deprecated
   public ExtraInfo(int version) {
      this.legacyVersion = version;
      this.validationResults = new ThrowingValidationResults(this);
      this.codecStore = CodecStore.STATIC;
   }

   @Deprecated
   public ExtraInfo(int version, @Nonnull Function<ExtraInfo, ValidationResults> validationResultsSupplier) {
      this.legacyVersion = version;
      this.validationResults = validationResultsSupplier.apply(this);
      this.codecStore = CodecStore.STATIC;
   }

   public int getVersion() {
      return Integer.MAX_VALUE;
   }

   @Deprecated
   public int getLegacyVersion() {
      return this.legacyVersion;
   }

   public int getKeysSize() {
      return this.keysSize;
   }

   public CodecStore getCodecStore() {
      return this.codecStore;
   }

   private int nextKeyIndex() {
      int index = this.keysSize++;
      if (this.stringKeys.length <= index) {
         int newLength = grow(index);
         this.stringKeys = Arrays.copyOf(this.stringKeys, newLength);
         this.intKeys = Arrays.copyOf(this.intKeys, newLength);
         if (GithubMessageUtil.isGithub()) {
            this.lineNumbers = Arrays.copyOf(this.lineNumbers, newLength);
            this.columnNumbers = Arrays.copyOf(this.columnNumbers, newLength);
         }
      }

      return index;
   }

   public void pushKey(String key) {
      int index = this.nextKeyIndex();
      this.stringKeys[index] = key;
   }

   public void pushIntKey(int key) {
      int index = this.nextKeyIndex();
      this.intKeys[index] = key;
   }

   public void pushKey(String key, RawJsonReader reader) {
      int index = this.nextKeyIndex();
      this.stringKeys[index] = key;
      if (GithubMessageUtil.isGithub()) {
         this.lineNumbers[index] = reader.getLine();
         this.columnNumbers[index] = reader.getColumn();
      }
   }

   public void pushIntKey(int key, RawJsonReader reader) {
      int index = this.nextKeyIndex();
      this.intKeys[index] = key;
      if (GithubMessageUtil.isGithub()) {
         this.lineNumbers[index] = reader.getLine();
         this.columnNumbers[index] = reader.getColumn();
      }
   }

   public void popKey() {
      this.stringKeys[this.keysSize] = null;
      this.keysSize--;
   }

   private int nextIgnoredUnknownIndex() {
      int index = this.ignoredUnknownSize++;
      if (this.ignoredUnknownKeys.length <= index) {
         this.ignoredUnknownKeys = Arrays.copyOf(this.ignoredUnknownKeys, grow(index));
      }

      return index;
   }

   public void ignoreUnusedKey(String key) {
      int index = this.nextIgnoredUnknownIndex();
      this.ignoredUnknownKeys[index] = key;
   }

   public void popIgnoredUnusedKey() {
      this.ignoredUnknownKeys[this.ignoredUnknownSize] = null;
      this.ignoredUnknownSize--;
   }

   public boolean consumeIgnoredUnknownKey(@Nonnull RawJsonReader reader) throws IOException {
      if (this.ignoredUnknownSize <= 0) {
         return false;
      } else {
         int lastIndex = this.ignoredUnknownSize - 1;
         String ignoredUnknownKey = this.ignoredUnknownKeys[lastIndex];
         if (ignoredUnknownKey == null) {
            return false;
         } else if (!reader.tryConsumeString(ignoredUnknownKey)) {
            return false;
         } else {
            this.ignoredUnknownKeys[lastIndex] = null;
            return true;
         }
      }
   }

   public boolean consumeIgnoredUnknownKey(@Nonnull String key) {
      if (this.ignoredUnknownSize <= 0) {
         return false;
      } else {
         int lastIndex = this.ignoredUnknownSize - 1;
         if (!key.equals(this.ignoredUnknownKeys[lastIndex])) {
            return false;
         } else {
            this.ignoredUnknownKeys[lastIndex] = null;
            return true;
         }
      }
   }

   public void readUnknownKey(@Nonnull RawJsonReader reader) throws IOException {
      if (!this.consumeIgnoredUnknownKey(reader)) {
         String key = reader.readString();
         if (this.keysSize == 0) {
            this.unknownKeys.add(key);
         } else {
            this.unknownKeys.add(this.peekKey() + "." + key);
         }
      }
   }

   public void addUnknownKey(@Nonnull String key) {
      switch (key) {
         case "$Title":
         case "$Comment":
         case "$TODO":
         case "$Author":
         case "$Position":
         case "$FloatingFunctionNodes":
         case "$Groups":
         case "$WorkspaceID":
         case "$NodeEditorMetadata":
         case "$NodeId":
            return;
         default:
            if (!this.consumeIgnoredUnknownKey(key)) {
               if (this.keysSize == 0) {
                  if ("Parent".equals(key)) {
                     return;
                  }

                  this.unknownKeys.add(key);
               } else {
                  this.unknownKeys.add(this.peekKey() + "." + key);
               }
            }
      }
   }

   public String peekKey() {
      return this.peekKey('.');
   }

   public String peekKey(char separator) {
      if (this.keysSize == 0) {
         return "";
      } else if (this.keysSize == 1) {
         String str = this.stringKeys[0];
         return str != null ? str : String.valueOf(this.intKeys[0]);
      } else {
         StringBuilder sb = new StringBuilder();

         for (int i = 0; i < this.keysSize; i++) {
            if (i > 0) {
               sb.append(separator);
            }

            String str = this.stringKeys[i];
            if (str != null) {
               sb.append(str);
            } else {
               sb.append(this.intKeys[i]);
            }
         }

         return sb.toString();
      }
   }

   public int peekLine() {
      return GithubMessageUtil.isGithub() && this.keysSize > 0 ? this.lineNumbers[this.keysSize - 1] : -1;
   }

   public int peekColumn() {
      return GithubMessageUtil.isGithub() && this.keysSize > 0 ? this.columnNumbers[this.keysSize - 1] : -1;
   }

   public List<String> getUnknownKeys() {
      return this.unknownKeys;
   }

   public ValidationResults getValidationResults() {
      return this.validationResults;
   }

   @Deprecated
   public Map<String, Object> getMetadata() {
      return this.metadata;
   }

   public void appendDetailsTo(@Nonnull StringBuilder sb) {
      sb.append("ExtraInfo\n");
   }

   @Nonnull
   @Override
   public String toString() {
      return "ExtraInfo{version="
         + this.legacyVersion
         + ", stringKeys="
         + Arrays.toString((Object[])this.stringKeys)
         + ", intKeys="
         + Arrays.toString(this.intKeys)
         + ", keysSize="
         + this.keysSize
         + ", ignoredUnknownKeys="
         + Arrays.toString((Object[])this.ignoredUnknownKeys)
         + ", ignoredUnknownSize="
         + this.ignoredUnknownSize
         + ", unknownKeys="
         + this.unknownKeys
         + ", validationResults="
         + this.validationResults
         + "}";
   }

   private static int grow(int oldSize) {
      return oldSize + (oldSize >> 1);
   }
}
