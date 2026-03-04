package com.hypixel.hytale.assetstore.codec;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.lookup.ACodecMapCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.codec.lookup.StringCodecMapCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class AssetCodecMapCodec<K, T extends JsonAsset<K>> extends StringCodecMapCodec<T, AssetBuilderCodec<K, T>> implements AssetCodec<K, T> {
   @Nonnull
   protected final KeyedCodec<K> idCodec;
   @Nonnull
   protected final KeyedCodec<K> parentCodec;
   protected final BiConsumer<T, K> idSetter;
   protected final Function<T, K> idGetter;
   protected final BiConsumer<T, AssetExtraInfo.Data> dataSetter;
   protected final Function<T, AssetExtraInfo.Data> dataGetter;

   public AssetCodecMapCodec(
      Codec<K> idCodec,
      BiConsumer<T, K> idSetter,
      Function<T, K> idGetter,
      BiConsumer<T, AssetExtraInfo.Data> dataSetter,
      Function<T, AssetExtraInfo.Data> dataGetter
   ) {
      super("Type");
      this.idCodec = new KeyedCodec<>("Id", idCodec);
      this.parentCodec = new KeyedCodec<>("Parent", idCodec);
      this.idSetter = idSetter;
      this.idGetter = idGetter;
      this.dataSetter = dataSetter;
      this.dataGetter = dataGetter;
   }

   public AssetCodecMapCodec(
      String key,
      Codec<K> idCodec,
      BiConsumer<T, K> idSetter,
      Function<T, K> idGetter,
      BiConsumer<T, AssetExtraInfo.Data> dataSetter,
      Function<T, AssetExtraInfo.Data> dataGetter
   ) {
      super(key);
      this.idCodec = new KeyedCodec<>("Id", idCodec);
      this.parentCodec = new KeyedCodec<>("Parent", idCodec);
      this.idSetter = idSetter;
      this.idGetter = idGetter;
      this.dataSetter = dataSetter;
      this.dataGetter = dataGetter;
   }

   public AssetCodecMapCodec(
      Codec<K> idCodec,
      BiConsumer<T, K> idSetter,
      Function<T, K> idGetter,
      BiConsumer<T, AssetExtraInfo.Data> dataSetter,
      Function<T, AssetExtraInfo.Data> dataGetter,
      boolean allowDefault
   ) {
      super("Type", allowDefault);
      this.idCodec = new KeyedCodec<>("Id", idCodec);
      this.parentCodec = new KeyedCodec<>("Parent", idCodec);
      this.idSetter = idSetter;
      this.idGetter = idGetter;
      this.dataSetter = dataSetter;
      this.dataGetter = dataGetter;
   }

   public AssetCodecMapCodec(
      String key,
      Codec<K> idCodec,
      BiConsumer<T, K> idSetter,
      Function<T, K> idGetter,
      BiConsumer<T, AssetExtraInfo.Data> dataSetter,
      Function<T, AssetExtraInfo.Data> dataGetter,
      boolean allowDefault
   ) {
      super(key, allowDefault);
      this.idCodec = new KeyedCodec<>("Id", idCodec);
      this.parentCodec = new KeyedCodec<>("Parent", idCodec);
      this.idSetter = idSetter;
      this.idGetter = idGetter;
      this.dataSetter = dataSetter;
      this.dataGetter = dataGetter;
   }

   @Nonnull
   @Override
   public KeyedCodec<K> getKeyCodec() {
      return this.idCodec;
   }

   @Nonnull
   @Override
   public KeyedCodec<K> getParentCodec() {
      return this.parentCodec;
   }

   @Override
   public AssetExtraInfo.Data getData(T t) {
      return this.dataGetter.apply(t);
   }

   @Nonnull
   public AssetCodecMapCodec<K, T> register(@Nonnull String id, Class<? extends T> aClass, BuilderCodec<? extends T> codec) {
      return this.register(Priority.NORMAL, id, aClass, codec);
   }

   @Nonnull
   public AssetCodecMapCodec<K, T> register(@Nonnull Priority priority, @Nonnull String id, Class<? extends T> aClass, BuilderCodec<? extends T> codec) {
      AssetBuilderCodec<K, T> assetCodec = AssetBuilderCodec.wrap(
         (BuilderCodec<T>)codec, this.idCodec.getChildCodec(), this.idSetter, this.idGetter, this.dataSetter, this.dataGetter
      );
      super.register(priority, id, aClass, assetCodec);
      return this;
   }

   public T decodeAndInherit(@Nonnull BsonDocument document, T parent, ExtraInfo extraInfo) {
      BsonValue id = document.get(this.key);
      AssetBuilderCodec<K, T> codec = this.idToCodec.get(id == null ? null : id.asString().getValue());
      if (codec == null) {
         AssetBuilderCodec<K, T> defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            throw new ACodecMapCodec.UnknownIdException("No codec registered with for '" + this.key + "': " + id);
         } else {
            return defaultCodec.decodeAndInherit(document, parent, extraInfo);
         }
      } else {
         return codec.decodeAndInherit(document, parent, extraInfo);
      }
   }

   public void decodeAndInherit(@Nonnull BsonDocument document, T t, T parent, ExtraInfo extraInfo) {
      BsonValue id = document.get(this.key);
      AssetBuilderCodec<K, T> codec = this.idToCodec.get(id == null ? null : id.asString().getValue());
      if (codec == null) {
         AssetBuilderCodec<K, T> defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            throw new ACodecMapCodec.UnknownIdException("No codec registered with for '" + this.key + "': " + id);
         } else {
            defaultCodec.decodeAndInherit(document, t, parent, extraInfo);
         }
      } else {
         codec.decodeAndInherit(document, t, parent, extraInfo);
      }
   }

   public T decodeAndInheritJson(@Nonnull RawJsonReader reader, @Nullable T parent, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.mark();
      String id = null;
      if (RawJsonReader.seekToKey(reader, this.key)) {
         id = reader.readString();
      } else if (parent != null) {
         id = this.getIdFor((Class<? extends T>)parent.getClass());
      }

      reader.reset();
      extraInfo.ignoreUnusedKey(this.key);

      JsonAsset var7;
      try {
         AssetBuilderCodec<K, T> codec = id == null ? null : this.idToCodec.get(id);
         if (codec != null) {
            return codec.decodeAndInheritJson(reader, parent, extraInfo);
         }

         AssetBuilderCodec<K, T> defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            throw new ACodecMapCodec.UnknownIdException("No codec registered with for '" + this.key + "': " + id);
         }

         var7 = defaultCodec.decodeAndInheritJson(reader, parent, extraInfo);
      } finally {
         extraInfo.popIgnoredUnusedKey();
      }

      return (T)var7;
   }

   public void decodeAndInheritJson(@Nonnull RawJsonReader reader, T t, @Nullable T parent, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.mark();
      String id = null;
      if (RawJsonReader.seekToKey(reader, this.key)) {
         id = reader.readString();
      } else if (parent != null) {
         id = this.getIdFor((Class<? extends T>)parent.getClass());
      }

      reader.reset();
      extraInfo.ignoreUnusedKey(this.key);

      try {
         AssetBuilderCodec<K, T> codec = id == null ? null : this.idToCodec.get(id);
         if (codec != null) {
            codec.decodeAndInheritJson(reader, t, parent, extraInfo);
            return;
         }

         AssetBuilderCodec<K, T> defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            throw new ACodecMapCodec.UnknownIdException("No codec registered with for '" + this.key + "': " + id);
         }

         defaultCodec.decodeAndInheritJson(reader, t, parent, extraInfo);
      } finally {
         extraInfo.popIgnoredUnusedKey();
      }
   }

   @Override
   public T decodeJsonAsset(@Nonnull RawJsonReader reader, @Nonnull AssetExtraInfo<K> extraInfo) throws IOException {
      return this.decodeAndInheritJsonAsset(reader, null, extraInfo);
   }

   @Override
   public T decodeAndInheritJsonAsset(@Nonnull RawJsonReader reader, @Nullable T parent, @Nonnull AssetExtraInfo<K> extraInfo) throws IOException {
      reader.mark();
      String id = null;
      if (RawJsonReader.seekToKey(reader, this.key)) {
         id = reader.readString();
      } else if (parent != null) {
         id = this.getIdFor((Class<? extends T>)parent.getClass());
      }

      reader.reset();
      extraInfo.ignoreUnusedKey(this.key);

      JsonAsset var8;
      try {
         AssetBuilderCodec<K, T> codec = id == null ? null : this.idToCodec.get(id);
         if (codec == null) {
            AssetBuilderCodec<K, T> defaultCodec = this.getDefaultCodec();
            if (defaultCodec == null) {
               throw new ACodecMapCodec.UnknownIdException("No codec registered with for '" + this.key + "': " + id);
            }

            codec = defaultCodec;
         }

         Supplier<T> supplier = codec.getSupplier();
         if (supplier == null) {
            throw new CodecException(
               "This BuilderCodec is for an abstract or direct codec. To use this codec you must specify an existing object to decode into."
            );
         }

         T t = supplier.get();
         this.dataSetter.accept(t, extraInfo.getData());
         if (parent != null) {
            codec.inherit(t, parent, extraInfo);
         }

         codec.decodeAndInheritJson0(reader, t, parent, extraInfo);
         this.idSetter.accept(t, extraInfo.getKey());
         codec.afterDecodeAndValidate(t, extraInfo);
         var8 = t;
      } finally {
         extraInfo.popIgnoredUnusedKey();
      }

      return (T)var8;
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      Schema schema = super.toSchema(context);
      schema.getHytaleSchemaTypeField().setParentPropertyKey(this.parentCodec.getKey());
      return schema;
   }

   @Override
   protected void mutateChildSchema(String key, @Nonnull SchemaContext context, BuilderCodec<? extends T> c, @Nonnull ObjectSchema objectSchema) {
      super.mutateChildSchema(key, context, c, objectSchema);
      AssetBuilderCodec<K, T> def = this.getDefaultCodec();
      if (!this.allowDefault || def != c) {
         Schema idField = new Schema();
         idField.setRequired(this.key);
         Schema parentField = new Schema();
         parentField.setRequired(this.parentCodec.getKey());
         AssetBuilderCodec<K, T> bc = (AssetBuilderCodec<K, T>)c;
         Schema parentSchema = objectSchema.getProperties().get(bc.getParentCodec().getKey());
         if (parentSchema != null) {
            Schema.InheritSettings settings = parentSchema.getHytaleParent();
            settings.setMapKey(this.key);
            settings.setMapKeyValue(key);
            objectSchema.setOneOf(idField, parentField);
         }
      }
   }
}
