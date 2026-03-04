package com.hypixel.hytale.assetstore.codec;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetBuilderCodec<K, T extends JsonAsset<K>> extends BuilderCodec<T> implements AssetCodec<K, T> {
   public static final KeyedCodec<Map<String, String[]>> TAGS_CODEC = new KeyedCodec<>("Tags", new MapCodec<>(Codec.STRING_ARRAY, HashMap::new));
   private static final String TAG_DOCUMENTATION = "Tags are a general way to describe an asset that can be interpreted by other systems in a way they see fit.\n\nFor example you could tag something with a **Material** tag with the values **Solid** and **Stone**, And another single tag **Ore**.\n\nTags will be expanded into a single list of tags automatically. Using the above example with **Material** and **Ore** the end result would be the following list of tags: **Ore**, **Material**, **Solid**, **Stone**, **Material=Solid** and **Material=Stone**.";
   @Nonnull
   protected final KeyedCodec<K> idCodec;
   @Nonnull
   protected final KeyedCodec<K> parentCodec;
   protected final BiConsumer<T, K> idSetter;
   protected final BiConsumer<T, AssetExtraInfo.Data> dataSetter;
   @Nonnull
   protected final Function<T, AssetExtraInfo.Data> dataGetter;

   protected AssetBuilderCodec(@Nonnull AssetBuilderCodec.Builder<K, T> builder) {
      super(builder);
      this.idCodec = builder.idCodec;
      this.parentCodec = new KeyedCodec<>("Parent", this.idCodec.getChildCodec());
      this.idSetter = builder.idSetter;
      this.dataSetter = builder.dataSetter;
      this.dataGetter = builder.dataGetter;
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

   @Override
   public T decodeJsonAsset(@Nonnull RawJsonReader reader, @Nonnull AssetExtraInfo<K> extraInfo) throws IOException {
      return this.decodeAndInheritJsonAsset(reader, null, extraInfo);
   }

   @Override
   public T decodeAndInheritJsonAsset(@Nonnull RawJsonReader reader, @Nullable T parent, @Nonnull AssetExtraInfo<K> extraInfo) throws IOException {
      T t = this.supplier.get();
      this.dataSetter.accept(t, extraInfo.getData());
      if (parent != null) {
         this.inherit(t, parent, extraInfo);
      }

      this.decodeAndInheritJson0(reader, t, parent, extraInfo);
      this.idSetter.accept(t, extraInfo.getKey());
      this.afterDecodeAndValidate(t, extraInfo);
      return t;
   }

   @Nonnull
   @Override
   public ObjectSchema toSchema(@Nonnull SchemaContext context) {
      return this.toSchema(context, this.supplier.get());
   }

   @Nonnull
   public ObjectSchema toSchema(@Nonnull SchemaContext context, @Nullable T def) {
      ObjectSchema schema = super.toSchema(context, def);
      KeyedCodec<K> parent = this.getParentCodec();
      Schema parentSchema = parent.getChildCodec().toSchema(context);
      parentSchema.setMarkdownDescription(
         "When set this asset will inherit properties from the named asset.\n\nWhen inheriting from another **"
            + this.tClass.getSimpleName()
            + "** most properties will simply be copied from the parent asset to this asset. In the case where both child and parent provide a field the child field will simply replace the value provided by the parent, in the case of nested structures this will apply to the fields within the structure. In some cases the field may decide to act differently, for example: by merging the parent and child fields together."
      );
      Class<? super T> rootClass = this.tClass;

      for (BuilderCodec<? super T> rootCodec = this; rootCodec.getParent() != null; rootClass = rootCodec.getInnerClass()) {
         rootCodec = rootCodec.getParent();
      }

      parentSchema.setHytaleParent(new Schema.InheritSettings(rootClass.getSimpleName()));
      LinkedHashMap<String, Schema> props = new LinkedHashMap<>();
      props.put(parent.getKey(), parentSchema);
      props.putAll(schema.getProperties());
      schema.setProperties(props);
      return schema;
   }

   @Nonnull
   public static <K, T extends JsonAsset<K>> AssetBuilderCodec.Builder<K, T> builder(
      Class<T> tClass,
      Supplier<T> supplier,
      Codec<K> idCodec,
      BiConsumer<T, K> idSetter,
      Function<T, K> idGetter,
      BiConsumer<T, AssetExtraInfo.Data> dataSetter,
      @Nonnull Function<T, AssetExtraInfo.Data> dataGetter
   ) {
      return new AssetBuilderCodec.Builder<>(tClass, supplier, idCodec, idSetter, idGetter, dataSetter, dataGetter);
   }

   @Nonnull
   public static <K, T extends JsonAsset<K>> AssetBuilderCodec.Builder<K, T> builder(
      Class<T> tClass,
      Supplier<T> supplier,
      BuilderCodec<? super T> parentCodec,
      Codec<K> idCodec,
      BiConsumer<T, K> idSetter,
      Function<T, K> idGetter,
      BiConsumer<T, AssetExtraInfo.Data> dataSetter,
      @Nonnull Function<T, AssetExtraInfo.Data> dataGetter
   ) {
      return new AssetBuilderCodec.Builder<>(tClass, supplier, parentCodec, idCodec, idSetter, idGetter, dataSetter, dataGetter);
   }

   @Nonnull
   public static <K, T extends JsonAsset<K>> AssetBuilderCodec<K, T> wrap(
      @Nonnull BuilderCodec<T> codec,
      Codec<K> idCodec,
      BiConsumer<T, K> idSetter,
      Function<T, K> idGetter,
      BiConsumer<T, AssetExtraInfo.Data> dataSetter,
      @Nonnull Function<T, AssetExtraInfo.Data> dataGetter
   ) {
      return builder(codec.getInnerClass(), codec.getSupplier(), codec, idCodec, idSetter, idGetter, dataSetter, dataGetter)
         .documentation(codec.getDocumentation())
         .build();
   }

   public static class Builder<K, T extends JsonAsset<K>> extends BuilderCodec.BuilderBase<T, AssetBuilderCodec.Builder<K, T>> {
      @Nonnull
      protected final KeyedCodec<K> idCodec;
      protected final BiConsumer<T, K> idSetter;
      protected final BiConsumer<T, AssetExtraInfo.Data> dataSetter;
      @Nonnull
      protected final Function<T, AssetExtraInfo.Data> dataGetter;

      public Builder(
         Class<T> tClass,
         Supplier<T> supplier,
         Codec<K> idCodec,
         BiConsumer<T, K> idSetter,
         Function<T, K> idGetter,
         BiConsumer<T, AssetExtraInfo.Data> dataSetter,
         @Nonnull Function<T, AssetExtraInfo.Data> dataGetter
      ) {
         super(tClass, supplier);
         this.idCodec = new KeyedCodec<>("Id", idCodec);
         this.idSetter = idSetter;
         this.dataSetter = dataSetter;
         this.dataGetter = dataGetter;
         this.<Map<String, String[]>>appendInherited(AssetBuilderCodec.TAGS_CODEC, (t, tags) -> dataGetter.apply(t).putTags(tags), t -> {
               AssetExtraInfo.Data data = dataGetter.apply(t);
               return data != null ? data.getRawTags() : null;
            }, (t, parent) -> {
               AssetExtraInfo.Data data = dataGetter.apply(t);
               AssetExtraInfo.Data parentData = dataGetter.apply(parent);
               if (data != null && parentData != null) {
                  data.putTags(parentData.getRawTags());
               }
            })
            .documentation(
               "Tags are a general way to describe an asset that can be interpreted by other systems in a way they see fit.\n\nFor example you could tag something with a **Material** tag with the values **Solid** and **Stone**, And another single tag **Ore**.\n\nTags will be expanded into a single list of tags automatically. Using the above example with **Material** and **Ore** the end result would be the following list of tags: **Ore**, **Material**, **Solid**, **Stone**, **Material=Solid** and **Material=Stone**."
            )
            .add();
      }

      public Builder(
         Class<T> tClass,
         Supplier<T> supplier,
         BuilderCodec<? super T> parentCodec,
         Codec<K> idCodec,
         BiConsumer<T, K> idSetter,
         Function<T, K> idGetter,
         BiConsumer<T, AssetExtraInfo.Data> dataSetter,
         @Nonnull Function<T, AssetExtraInfo.Data> dataGetter
      ) {
         super(tClass, supplier, parentCodec);
         this.idCodec = new KeyedCodec<>("Id", idCodec);
         this.idSetter = idSetter;
         this.dataSetter = dataSetter;
         this.dataGetter = dataGetter;
         this.<Map<String, String[]>>appendInherited(AssetBuilderCodec.TAGS_CODEC, (t, tags) -> dataGetter.apply(t).putTags(tags), t -> {
               AssetExtraInfo.Data data = dataGetter.apply(t);
               return data != null ? data.getRawTags() : null;
            }, (t, parent) -> {
               AssetExtraInfo.Data data = dataGetter.apply(t);
               AssetExtraInfo.Data parentData = dataGetter.apply(parent);
               if (data != null && parentData != null) {
                  data.putTags(parentData.getRawTags());
               }
            })
            .documentation(
               "Tags are a general way to describe an asset that can be interpreted by other systems in a way they see fit.\n\nFor example you could tag something with a **Material** tag with the values **Solid** and **Stone**, And another single tag **Ore**.\n\nTags will be expanded into a single list of tags automatically. Using the above example with **Material** and **Ore** the end result would be the following list of tags: **Ore**, **Material**, **Solid**, **Stone**, **Material=Solid** and **Material=Stone**."
            )
            .add();
      }

      @Nonnull
      public AssetBuilderCodec<K, T> build() {
         return new AssetBuilderCodec<>(this);
      }
   }
}
