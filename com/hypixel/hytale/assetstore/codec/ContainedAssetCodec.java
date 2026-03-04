package com.hypixel.hytale.assetstore.codec;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.RawAsset;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ValidatableCodec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonValue;

public class ContainedAssetCodec<K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> implements Codec<K>, ValidatableCodec<K> {
   private static final boolean DISABLE_DIRECT_LOADING = true;
   private final Class<T> assetClass;
   private final AssetCodec<K, T> codec;
   @Nonnull
   private final ContainedAssetCodec.Mode mode;
   private final Function<AssetExtraInfo<K>, K> keyGenerator;

   public ContainedAssetCodec(Class<T> assetClass, AssetCodec<K, T> codec) {
      this(assetClass, codec, ContainedAssetCodec.Mode.GENERATE_ID);
   }

   public ContainedAssetCodec(Class<T> assetClass, AssetCodec<K, T> codec, @Nonnull ContainedAssetCodec.Mode mode) {
      this(assetClass, codec, mode, assetExtraInfo -> AssetRegistry.<K, T, M>getAssetStore(assetClass).transformKey(assetExtraInfo.generateKey()));
   }

   public ContainedAssetCodec(Class<T> assetClass, AssetCodec<K, T> codec, @Nonnull ContainedAssetCodec.Mode mode, Function<AssetExtraInfo<K>, K> keyGenerator) {
      if (mode == ContainedAssetCodec.Mode.NONE) {
         throw new UnsupportedOperationException("Contained asset mode can't be NONE!");
      } else {
         this.assetClass = assetClass;
         this.codec = codec;
         this.mode = mode;
         this.keyGenerator = keyGenerator;
      }
   }

   public Class<T> getAssetClass() {
      return this.assetClass;
   }

   @Nullable
   @Override
   public K decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      if (!(extraInfo instanceof AssetExtraInfo<K> assetExtraInfo)) {
         throw new UnsupportedOperationException("Unable to decode asset from codec used outside of an AssetStore");
      } else if (bsonValue.isString()) {
         return this.codec.getKeyCodec().getChildCodec().decode(bsonValue, extraInfo);
      } else {
         KeyedCodec<K> parentCodec = this.codec.getParentCodec();
         K parentId = parentCodec != null ? parentCodec.getOrNull(bsonValue.asDocument(), extraInfo) : null;
         AssetStore<K, T, M> assetStore = AssetRegistry.getAssetStore(this.assetClass);
         K id;
         switch (this.mode) {
            case GENERATE_ID: {
               id = this.keyGenerator.apply(assetExtraInfo);
               boolean inheritContainerTags = false;
               break;
            }
            case INHERIT_ID: {
               id = assetStore.transformKey(assetExtraInfo.getKey());
               boolean inheritContainerTags = true;
               break;
            }
            case INHERIT_ID_AND_PARENT: {
               id = assetStore.transformKey(assetExtraInfo.getKey());
               if (parentId == null) {
                  Object thisAssetParentId = assetExtraInfo.getData().getParentKey();
                  if (thisAssetParentId != null) {
                     parentId = assetStore.transformKey(thisAssetParentId);
                  }
               }

               boolean inheritContainerTags = true;
               break;
            }
            case INJECT_PARENT: {
               id = this.keyGenerator.apply(assetExtraInfo);
               if (parentId == null && !assetExtraInfo.getKey().equals(id)) {
                  parentId = assetExtraInfo.getKey();
               }

               boolean inheritContainerTags = true;
               break;
            }
            default:
               throw new UnsupportedOperationException("Contained asset mode can't be NONE!");
         }

         T parent = parentId != null ? assetStore.getAssetMap().getAsset(parentId) : null;
         if (parentId != null && parent != null) {
         }

         char[] clone = bsonValue.asDocument().toJson().toCharArray();
         Path path = assetExtraInfo.getAssetPath();
         assetExtraInfo.getData().addContainedAsset(this.assetClass, new RawAsset<>(path, id, parentId, 0, clone, assetExtraInfo.getData(), this.mode));
         return id;
      }
   }

   @Override
   public BsonValue encode(@Nonnull K key, ExtraInfo extraInfo) {
      if (key.toString().startsWith("*")) {
         T asset = (T)AssetRegistry.<K, T, M>getAssetStore(this.assetClass).getAssetMap().getAsset(key);
         if (asset != null) {
            return this.codec.encode(asset, extraInfo);
         }
      }

      return this.codec.getKeyCodec().getChildCodec().encode(key, extraInfo);
   }

   @Nullable
   @Override
   public K decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      if (!(extraInfo instanceof AssetExtraInfo<K> assetExtraInfo)) {
         throw new UnsupportedOperationException("Unable to decode asset from codec used outside of an AssetStore");
      } else {
         int lineStart = reader.getLine() - 1;
         if (reader.peekFor('"')) {
            return this.codec.getKeyCodec().getChildCodec().decodeJson(reader, extraInfo);
         } else {
            reader.mark();
            K parentId = null;
            boolean needsSkip = false;
            KeyedCodec<K> parentCodec = this.codec.getParentCodec();
            if (parentCodec != null && RawJsonReader.seekToKey(reader, parentCodec.getKey())) {
               parentId = parentCodec.getChildCodec().decodeJson(reader, extraInfo);
               needsSkip = true;
            }

            AssetStore<K, T, M> assetStore = AssetRegistry.getAssetStore(this.assetClass);
            K id;
            switch (this.mode) {
               case GENERATE_ID: {
                  id = this.keyGenerator.apply(assetExtraInfo);
                  boolean inheritContainerTags = false;
                  break;
               }
               case INHERIT_ID: {
                  id = assetStore.transformKey(assetExtraInfo.getKey());
                  boolean inheritContainerTags = true;
                  break;
               }
               case INHERIT_ID_AND_PARENT: {
                  id = assetStore.transformKey(assetExtraInfo.getKey());
                  if (parentId == null) {
                     Object thisAssetParentId = assetExtraInfo.getData().getParentKey();
                     if (thisAssetParentId != null) {
                        parentId = assetStore.transformKey(thisAssetParentId);
                     }
                  }

                  boolean inheritContainerTags = true;
                  break;
               }
               case INJECT_PARENT: {
                  id = this.keyGenerator.apply(assetExtraInfo);
                  if (parentId == null && !assetExtraInfo.getKey().equals(id)) {
                     parentId = assetExtraInfo.getKey();
                  }

                  boolean inheritContainerTags = true;
                  break;
               }
               default:
                  throw new UnsupportedOperationException("Contained asset mode can't be NONE!");
            }

            T parent = parentId != null ? assetStore.getAssetMap().getAsset(parentId) : null;
            if (parentId != null && parent != null) {
            }

            if (needsSkip) {
               reader.skipObjectContinued();
            }

            char[] clone = reader.cloneMark();
            reader.unmark();
            Path path = assetExtraInfo.getAssetPath();
            assetExtraInfo.getData()
               .addContainedAsset(this.assetClass, new RawAsset<>(path, id, parentId, lineStart, clone, assetExtraInfo.getData(), this.mode));
            return id;
         }
      }
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      Schema keySchema = context.refDefinition(this.codec.getKeyCodec().getChildCodec());
      keySchema.setTitle("Reference to " + this.assetClass.getSimpleName());
      Schema nestedSchema = context.refDefinition(this.codec);
      Schema s = Schema.anyOf(keySchema, nestedSchema);
      s.setHytaleAssetRef(this.assetClass.getSimpleName());
      return s;
   }

   @Override
   public void validate(K k, @Nonnull ExtraInfo extraInfo) {
      AssetRegistry.<K, T, M>getAssetStore(this.assetClass).validate(k, extraInfo.getValidationResults(), extraInfo);
   }

   @Override
   public void validateDefaults(ExtraInfo extraInfo, @Nonnull Set<Codec<?>> tested) {
      if (tested.add(this)) {
         ;
      }
   }

   public static enum Mode {
      NONE,
      GENERATE_ID,
      INHERIT_ID,
      INHERIT_ID_AND_PARENT,
      INJECT_PARENT;
   }
}
