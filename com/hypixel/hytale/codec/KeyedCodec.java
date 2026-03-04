package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.exception.CodecException;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonSerializationException;
import org.bson.BsonValue;

public class KeyedCodec<T> {
   @Nonnull
   private final String key;
   @Nonnull
   private final Codec<T> codec;
   private final boolean required;

   public KeyedCodec(@Nonnull String key, Codec<T> codec) {
      this(key, codec, false);
   }

   public KeyedCodec(@Nonnull String key, Codec<T> codec, boolean required) {
      this.key = Objects.requireNonNull(key, "key parameter can't be null");
      this.codec = Objects.requireNonNull(codec, "codec parameter can't be null");
      this.required = required;
      if (key.isEmpty()) {
         throw new IllegalArgumentException("Key must not be empty! Key: '" + key + "'");
      } else {
         char firstCharFromKey = key.charAt(0);
         if (Character.isLetter(firstCharFromKey) && !Character.isUpperCase(firstCharFromKey)) {
            throw new IllegalArgumentException("Key must start with an upper case character! Key: '" + key + "'");
         }
      }
   }

   @Deprecated
   public KeyedCodec(@Nonnull String key, Codec<T> codec, boolean required, boolean bypassCaseCheck) {
      this.key = Objects.requireNonNull(key, "key parameter can't be null");
      this.codec = Objects.requireNonNull(codec, "codec parameter can't be null");
      this.required = required;
      if (key.isEmpty()) {
         throw new IllegalArgumentException("Key must not be empty! Key: '" + key + "'");
      } else {
         char firstCharFromKey = key.charAt(0);
         if (!bypassCaseCheck && Character.isLetter(firstCharFromKey) && !Character.isUpperCase(firstCharFromKey)) {
            throw new IllegalArgumentException("Key must start with an upper case character! Key: '" + key + "'");
         }
      }
   }

   @Nonnull
   public String getKey() {
      return this.key;
   }

   @Deprecated
   public T getNow(BsonDocument document) {
      return this.getNow(document, EmptyExtraInfo.EMPTY);
   }

   public T getNow(BsonDocument document, @Nonnull ExtraInfo extraInfo) {
      return this.get(document, extraInfo).orElseThrow(() -> new BsonSerializationException(this.key + " does not exist in document!"));
   }

   @Nullable
   @Deprecated
   public T getOrNull(BsonDocument document) {
      return this.getOrNull(document, EmptyExtraInfo.EMPTY);
   }

   @Nullable
   public T getOrNull(BsonDocument document, @Nonnull ExtraInfo extraInfo) {
      return this.get(document, extraInfo).orElse(null);
   }

   @Nonnull
   @Deprecated
   public Optional<T> get(BsonDocument document) {
      return this.get(document, EmptyExtraInfo.EMPTY);
   }

   @Nonnull
   public Optional<T> get(@Nullable BsonDocument document, @Nonnull ExtraInfo extraInfo) {
      extraInfo.pushKey(this.key);

      Optional var4;
      try {
         if (document == null) {
            return Optional.empty();
         }

         BsonValue bsonValue = document.get(this.key);
         if (!Codec.isNullBsonValue(bsonValue)) {
            return Optional.ofNullable(this.decode(bsonValue, extraInfo));
         }

         var4 = Optional.empty();
      } catch (Exception var8) {
         throw new CodecException("Failed decode", (BsonValue)document, extraInfo, var8);
      } finally {
         extraInfo.popKey();
      }

      return var4;
   }

   @Nullable
   public T getOrDefault(@Nullable BsonDocument document, @Nonnull ExtraInfo extraInfo, T def) {
      extraInfo.pushKey(this.key);

      Object var5;
      try {
         if (document == null) {
            return def;
         }

         BsonValue bsonValue = document.get(this.key);
         if (!Codec.isNullBsonValue(bsonValue)) {
            return this.codec.decode(bsonValue, extraInfo);
         }

         var5 = def;
      } catch (Exception var9) {
         throw new CodecException("Failed decode", (BsonValue)document, extraInfo, var9);
      } finally {
         extraInfo.popKey();
      }

      return (T)var5;
   }

   @Nonnull
   public Optional<T> getAndInherit(@Nullable BsonDocument document, T parent, @Nonnull ExtraInfo extraInfo) {
      extraInfo.pushKey(this.key);

      Optional var5;
      try {
         if (document == null) {
            return Optional.ofNullable(this.decodeAndInherit(null, parent, extraInfo));
         }

         BsonValue bsonValue = document.get(this.key);
         if (!Codec.isNullBsonValue(bsonValue)) {
            return Optional.ofNullable(this.decodeAndInherit(bsonValue, parent, extraInfo));
         }

         var5 = Optional.ofNullable(this.decodeAndInherit(null, parent, extraInfo));
      } catch (Exception var9) {
         throw new CodecException("Failed decode", (BsonValue)document, extraInfo, var9);
      } finally {
         extraInfo.popKey();
      }

      return var5;
   }

   @Deprecated
   public void put(@Nonnull BsonDocument document, T t) {
      this.put(document, t, EmptyExtraInfo.EMPTY);
   }

   public void put(@Nonnull BsonDocument document, @Nullable T t, @Nonnull ExtraInfo extraInfo) {
      if (t != null) {
         try {
            document.put(this.key, this.encode(t, extraInfo));
         } catch (Exception var5) {
            throw new CodecException("Failed encode", t, extraInfo, var5);
         }
      }
   }

   @Nullable
   protected T decode(BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
      if (!this.required && Codec.isNullBsonValue(bsonValue)) {
         return null;
      } else {
         try {
            return this.codec.decode(bsonValue, extraInfo);
         } catch (Exception var4) {
            throw new CodecException("Failed to decode", bsonValue, extraInfo, var4);
         }
      }
   }

   @Nullable
   protected T decodeAndInherit(@Nullable BsonValue bsonValue, T parent, @Nonnull ExtraInfo extraInfo) {
      if (!this.required && Codec.isNullBsonValue(bsonValue)) {
         return null;
      } else {
         try {
            return bsonValue != null && bsonValue.isDocument() && this.codec instanceof InheritCodec
               ? ((InheritCodec)this.codec).decodeAndInherit(bsonValue.asDocument(), parent, extraInfo)
               : this.codec.decode(bsonValue, extraInfo);
         } catch (Exception var5) {
            throw new CodecException("Failed to decode", bsonValue, extraInfo, var5);
         }
      }
   }

   protected BsonValue encode(T t, ExtraInfo extraInfo) {
      return this.codec.encode(t, extraInfo);
   }

   @Nonnull
   public Codec<T> getChildCodec() {
      return this.codec;
   }

   public boolean isRequired() {
      return this.required;
   }

   @Nonnull
   @Override
   public String toString() {
      return "KeyedCodec{key='" + this.key + "', codec=" + this.codec + "}";
   }
}
