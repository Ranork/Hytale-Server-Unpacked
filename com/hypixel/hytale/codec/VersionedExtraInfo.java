package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.store.CodecStore;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ValidationResults;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class VersionedExtraInfo extends ExtraInfo {
   private final int version;
   private final ExtraInfo delegate;

   public VersionedExtraInfo(int version, ExtraInfo delegate) {
      this.version = version;
      this.delegate = delegate;
   }

   @Override
   public int getVersion() {
      return this.version;
   }

   @Override
   public int getKeysSize() {
      return this.delegate.getKeysSize();
   }

   @Override
   public CodecStore getCodecStore() {
      return this.delegate.getCodecStore();
   }

   @Override
   public void pushKey(String key) {
      this.delegate.pushKey(key);
   }

   @Override
   public void pushIntKey(int key) {
      this.delegate.pushIntKey(key);
   }

   @Override
   public void pushKey(String key, RawJsonReader reader) {
      this.delegate.pushKey(key, reader);
   }

   @Override
   public void pushIntKey(int key, RawJsonReader reader) {
      this.delegate.pushIntKey(key, reader);
   }

   @Override
   public void popKey() {
      this.delegate.popKey();
   }

   @Override
   public void ignoreUnusedKey(String key) {
      this.delegate.ignoreUnusedKey(key);
   }

   @Override
   public void popIgnoredUnusedKey() {
      this.delegate.popIgnoredUnusedKey();
   }

   @Override
   public boolean consumeIgnoredUnknownKey(@Nonnull RawJsonReader reader) throws IOException {
      return this.delegate.consumeIgnoredUnknownKey(reader);
   }

   @Override
   public boolean consumeIgnoredUnknownKey(@Nonnull String key) {
      return this.delegate.consumeIgnoredUnknownKey(key);
   }

   @Override
   public void readUnknownKey(@Nonnull RawJsonReader reader) throws IOException {
      this.delegate.readUnknownKey(reader);
   }

   @Override
   public void addUnknownKey(@Nonnull String key) {
      this.delegate.addUnknownKey(key);
   }

   @Override
   public String peekKey() {
      return this.delegate.peekKey();
   }

   @Override
   public String peekKey(char separator) {
      return this.delegate.peekKey(separator);
   }

   @Override
   public List<String> getUnknownKeys() {
      return this.delegate.getUnknownKeys();
   }

   @Override
   public ValidationResults getValidationResults() {
      return this.delegate.getValidationResults();
   }

   @Override
   public Map<String, Object> getMetadata() {
      return this.delegate.getMetadata();
   }

   @Override
   public void appendDetailsTo(@Nonnull StringBuilder sb) {
      this.delegate.appendDetailsTo(sb);
   }

   @Override
   public int getLegacyVersion() {
      return this.delegate.getLegacyVersion();
   }
}
