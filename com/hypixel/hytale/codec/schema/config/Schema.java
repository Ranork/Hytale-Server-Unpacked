package com.hypixel.hytale.codec.schema.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.lookup.ObjectCodecMapCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.metadata.ui.UIButton;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorFeatures;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorPreview;
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches;
import com.hypixel.hytale.codec.util.Documentation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.BsonValue;

public class Schema {
   public static final ObjectCodecMapCodec<String, Schema> CODEC = new ObjectCodecMapCodec<>("type", new Schema.StringOrBlank(), true, false);
   public static final ArrayCodec<Schema> ARRAY_CODEC = new ArrayCodec<>(CODEC, Schema[]::new);
   public static final BuilderCodec<Schema> BASE_CODEC = BuilderCodec.builder(Schema.class, Schema::new)
      .addField(new KeyedCodec<>("$id", Codec.STRING, false, true), (o, i) -> o.id = i, o -> o.id)
      .addField(new KeyedCodec<>("type", new Schema.ArrayOrNull(), false, true), (o, i) -> o.types = i, o -> o.types)
      .addField(new KeyedCodec<>("title", Codec.STRING, false, true), (o, i) -> o.title = i, o -> o.title)
      .addField(
         new KeyedCodec<>("description", Codec.STRING, false, true),
         (o, i) -> o.description = i,
         o -> o.description == null && o.markdownDescription != null ? Documentation.stripMarkdown(o.markdownDescription) : o.description
      )
      .addField(new KeyedCodec<>("markdownDescription", Codec.STRING, false, true), (o, i) -> o.markdownDescription = i, o -> o.markdownDescription)
      .addField(new KeyedCodec<>("enumDescriptions", Codec.STRING_ARRAY, false, true), (o, i) -> o.enumDescriptions = i, o -> {
         if (o.enumDescriptions == null && o.markdownEnumDescriptions != null) {
            String[] enumDescriptions = new String[o.markdownEnumDescriptions.length];

            for (int i = 0; i < enumDescriptions.length; i++) {
               enumDescriptions[i] = Documentation.stripMarkdown(o.markdownEnumDescriptions[i]);
            }

            return enumDescriptions;
         } else {
            return o.enumDescriptions;
         }
      })
      .addField(
         new KeyedCodec<>("markdownEnumDescriptions", Codec.STRING_ARRAY, false, true),
         (o, i) -> o.markdownEnumDescriptions = i,
         o -> o.markdownEnumDescriptions
      )
      .addField(new KeyedCodec<>("anyOf", ARRAY_CODEC, false, true), (o, i) -> o.anyOf = i, o -> o.anyOf)
      .addField(new KeyedCodec<>("oneOf", ARRAY_CODEC, false, true), (o, i) -> o.oneOf = i, o -> o.oneOf)
      .addField(new KeyedCodec<>("allOf", ARRAY_CODEC, false, true), (o, i) -> o.allOf = i, o -> o.allOf)
      .addField(new KeyedCodec<>("not", CODEC, false, true), (o, i) -> o.not = i, o -> o.not)
      .addField(new KeyedCodec<>("if", CODEC, false, true), (o, i) -> o.if_ = i, o -> o.if_)
      .addField(new KeyedCodec<>("then", CODEC, false, true), (o, i) -> o.then = i, o -> o.then)
      .addField(new KeyedCodec<>("else", new Schema.BooleanOrSchema(), false, true), (o, i) -> o.else_ = i, o -> o.else_)
      .addField(new KeyedCodec<>("required", Codec.STRING_ARRAY, false, true), (o, i) -> o.required = i, o -> o.required)
      .addField(new KeyedCodec<>("default", Codec.BSON_DOCUMENT, false, true), (o, i) -> o.default_ = i, o -> o.default_)
      .addField(new KeyedCodec<>("definitions", new MapCodec<>(CODEC, HashMap::new), false, true), (o, i) -> o.definitions = i, o -> o.definitions)
      .addField(new KeyedCodec<>("$ref", Codec.STRING, false, true), (o, i) -> o.ref = i, o -> o.ref)
      .addField(new KeyedCodec<>("$data", Codec.STRING, false, true), (o, i) -> o.data = i, o -> o.data)
      .addField(new KeyedCodec<>("doNotSuggest", Codec.BOOLEAN, false, true), (o, i) -> o.doNotSuggest = i, o -> o.doNotSuggest)
      .addField(new KeyedCodec<>("hytaleAssetRef", Codec.STRING, false, true), (o, i) -> o.hytaleAssetRef = i, o -> o.hytaleAssetRef)
      .addField(new KeyedCodec<>("hytaleCustomAssetRef", Codec.STRING, false, true), (o, i) -> o.hytaleCustomAssetRef = i, o -> o.hytaleCustomAssetRef)
      .addField(new KeyedCodec<>("hytaleParent", Schema.InheritSettings.CODEC, false, true), (o, i) -> o.hytaleParent = i, o -> o.hytaleParent)
      .addField(
         new KeyedCodec<>("hytaleSchemaTypeField", Schema.SchemaTypeField.CODEC, false, true),
         (o, i) -> o.hytaleSchemaTypeField = i,
         o -> o.hytaleSchemaTypeField
      )
      .addField(new KeyedCodec<>("hytale", Schema.HytaleMetadata.CODEC, false, true), (o, i) -> {
         if (i.type == null) {
            i.type = CODEC.getIdFor((Class<? extends Schema>)o.getClass());
         }

         o.hytale = i;
      }, o -> o.hytale)
      .build();
   private String id;
   private String[] types;
   private String title;
   private String description;
   private String markdownDescription;
   private Schema[] anyOf;
   private Schema[] oneOf;
   private Schema[] allOf;
   private Schema not;
   private String[] required;
   private String[] enumDescriptions;
   private String[] markdownEnumDescriptions;
   private Map<String, Schema> definitions;
   private String ref;
   private String data;
   private BsonDocument default_;
   private Schema if_;
   private Schema then;
   private Object else_;
   private Schema.HytaleMetadata hytale;
   private Schema.InheritSettings hytaleParent;
   private Schema.SchemaTypeField hytaleSchemaTypeField;
   private String hytaleAssetRef;
   private String hytaleCustomAssetRef;
   private Boolean doNotSuggest;

   public Schema() {
      String id = CODEC.getIdFor((Class<? extends Schema>)this.getClass());
      if (id != null && !id.isBlank()) {
         this.hytale = new Schema.HytaleMetadata(id);
      }
   }

   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String[] getTypes() {
      return this.types;
   }

   public void setTypes(String[] types) {
      this.types = types;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getMarkdownDescription() {
      return this.markdownDescription;
   }

   public void setMarkdownDescription(String markdownDescription) {
      this.markdownDescription = markdownDescription;
   }

   public String[] getEnumDescriptions() {
      return this.enumDescriptions;
   }

   public void setEnumDescriptions(String[] enumDescriptions) {
      this.enumDescriptions = enumDescriptions;
   }

   public String[] getMarkdownEnumDescriptions() {
      return this.markdownEnumDescriptions;
   }

   public void setMarkdownEnumDescriptions(String[] markdownEnumDescriptions) {
      this.markdownEnumDescriptions = markdownEnumDescriptions;
   }

   public Schema[] getAnyOf() {
      return this.anyOf;
   }

   public void setAnyOf(Schema... anyOf) {
      this.anyOf = anyOf;
   }

   public Schema[] getOneOf() {
      return this.oneOf;
   }

   public void setOneOf(Schema... oneOf) {
      this.oneOf = oneOf;
   }

   public Schema[] getAllOf() {
      return this.allOf;
   }

   public void setAllOf(Schema... allOf) {
      this.allOf = allOf;
   }

   public String[] getRequired() {
      return this.required;
   }

   public void setRequired(String... required) {
      this.required = required;
   }

   public BsonDocument getDefaultRaw() {
      return this.default_;
   }

   public void setDefaultRaw(BsonDocument default_) {
      this.default_ = default_;
   }

   public Map<String, Schema> getDefinitions() {
      return this.definitions;
   }

   public void setDefinitions(Map<String, Schema> definitions) {
      this.definitions = definitions;
   }

   public String getRef() {
      return this.ref;
   }

   public void setRef(String ref) {
      this.ref = ref;
   }

   public String getData() {
      return this.data;
   }

   public void setData(String data) {
      this.data = data;
   }

   public Schema getIf() {
      return this.if_;
   }

   public void setIf(Schema if_) {
      this.if_ = if_;
   }

   public Schema getThen() {
      return this.then;
   }

   public void setThen(Schema then) {
      this.then = then;
   }

   public Schema getElse() {
      return (Schema)this.else_;
   }

   public void setElse(Schema else_) {
      this.else_ = else_;
   }

   public void setElse(boolean else_) {
      this.else_ = else_;
   }

   public Boolean isDoNotSuggest() {
      return this.doNotSuggest;
   }

   public void setDoNotSuggest(boolean doNotSuggest) {
      this.doNotSuggest = doNotSuggest;
   }

   @Nullable
   public Schema.HytaleMetadata getHytale() {
      return this.getHytale(true);
   }

   @Nullable
   public Schema.HytaleMetadata getHytale(boolean createInstance) {
      if (createInstance && this.hytale == null) {
         this.hytale = new Schema.HytaleMetadata();
         this.hytale.type = CODEC.getIdFor((Class<? extends Schema>)this.getClass());
      }

      return this.hytale;
   }

   public String getHytaleAssetRef() {
      return this.hytaleAssetRef;
   }

   public void setHytaleAssetRef(String hytaleAssetRef) {
      this.hytaleAssetRef = hytaleAssetRef;
   }

   public Schema.InheritSettings getHytaleParent() {
      return this.hytaleParent;
   }

   public void setHytaleParent(Schema.InheritSettings hytaleParent) {
      this.hytaleParent = hytaleParent;
   }

   public Schema.SchemaTypeField getHytaleSchemaTypeField() {
      return this.hytaleSchemaTypeField;
   }

   public void setHytaleSchemaTypeField(Schema.SchemaTypeField hytaleSchemaTypeField) {
      this.hytaleSchemaTypeField = hytaleSchemaTypeField;
   }

   public String getHytaleCustomAssetRef() {
      return this.hytaleCustomAssetRef;
   }

   public void setHytaleCustomAssetRef(String hytaleCustomAssetRef) {
      this.hytaleCustomAssetRef = hytaleCustomAssetRef;
   }

   @Nonnull
   public static Schema ref(String file) {
      Schema s = new Schema();
      s.setRef(file);
      return s;
   }

   @Nonnull
   public static Schema data(String file) {
      Schema s = new Schema();
      s.setData(file);
      return s;
   }

   @Nonnull
   public static Schema anyOf(Schema... anyOf) {
      Schema s = new Schema();
      s.anyOf = anyOf;
      return s;
   }

   @Nonnull
   public static Schema not(Schema not) {
      Schema s = new Schema();
      s.not = not;
      return s;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Schema schema = (Schema)o;
         if (this.id != null ? this.id.equals(schema.id) : schema.id == null) {
            if (!Arrays.equals((Object[])this.types, (Object[])schema.types)) {
               return false;
            } else if (this.title != null ? this.title.equals(schema.title) : schema.title == null) {
               if (this.description != null ? this.description.equals(schema.description) : schema.description == null) {
                  if (this.markdownDescription != null ? this.markdownDescription.equals(schema.markdownDescription) : schema.markdownDescription == null) {
                     if (!Arrays.equals((Object[])this.anyOf, (Object[])schema.anyOf)) {
                        return false;
                     } else if (!Arrays.equals((Object[])this.oneOf, (Object[])schema.oneOf)) {
                        return false;
                     } else if (!Arrays.equals((Object[])this.allOf, (Object[])schema.allOf)) {
                        return false;
                     } else if (this.not != null ? this.not.equals(schema.not) : schema.not == null) {
                        if (!Arrays.equals((Object[])this.required, (Object[])schema.required)) {
                           return false;
                        } else if (!Arrays.equals((Object[])this.enumDescriptions, (Object[])schema.enumDescriptions)) {
                           return false;
                        } else if (!Arrays.equals((Object[])this.markdownEnumDescriptions, (Object[])schema.markdownEnumDescriptions)) {
                           return false;
                        } else if (this.definitions != null ? this.definitions.equals(schema.definitions) : schema.definitions == null) {
                           if (this.ref != null ? this.ref.equals(schema.ref) : schema.ref == null) {
                              if (this.data != null ? this.data.equals(schema.data) : schema.data == null) {
                                 if (this.default_ != null ? this.default_.equals(schema.default_) : schema.default_ == null) {
                                    if (this.hytale != null ? this.hytale.equals(schema.hytale) : schema.hytale == null) {
                                       if (this.hytaleParent != null ? this.hytaleParent.equals(schema.hytaleParent) : schema.hytaleParent == null) {
                                          if (this.hytaleSchemaTypeField != null
                                             ? this.hytaleSchemaTypeField.equals(schema.hytaleSchemaTypeField)
                                             : schema.hytaleSchemaTypeField == null) {
                                             if (this.hytaleAssetRef != null
                                                ? this.hytaleAssetRef.equals(schema.hytaleAssetRef)
                                                : schema.hytaleAssetRef == null) {
                                                return this.hytaleCustomAssetRef != null
                                                   ? this.hytaleCustomAssetRef.equals(schema.hytaleCustomAssetRef)
                                                   : schema.hytaleCustomAssetRef == null;
                                             } else {
                                                return false;
                                             }
                                          } else {
                                             return false;
                                          }
                                       } else {
                                          return false;
                                       }
                                    } else {
                                       return false;
                                    }
                                 } else {
                                    return false;
                                 }
                              } else {
                                 return false;
                              }
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.id != null ? this.id.hashCode() : 0;
      result = 31 * result + Arrays.hashCode((Object[])this.types);
      result = 31 * result + (this.title != null ? this.title.hashCode() : 0);
      result = 31 * result + (this.description != null ? this.description.hashCode() : 0);
      result = 31 * result + (this.markdownDescription != null ? this.markdownDescription.hashCode() : 0);
      result = 31 * result + Arrays.hashCode((Object[])this.anyOf);
      result = 31 * result + Arrays.hashCode((Object[])this.oneOf);
      result = 31 * result + Arrays.hashCode((Object[])this.allOf);
      result = 31 * result + (this.not != null ? this.not.hashCode() : 0);
      result = 31 * result + Arrays.hashCode((Object[])this.required);
      result = 31 * result + Arrays.hashCode((Object[])this.enumDescriptions);
      result = 31 * result + Arrays.hashCode((Object[])this.markdownEnumDescriptions);
      result = 31 * result + (this.definitions != null ? this.definitions.hashCode() : 0);
      result = 31 * result + (this.ref != null ? this.ref.hashCode() : 0);
      result = 31 * result + (this.data != null ? this.data.hashCode() : 0);
      result = 31 * result + (this.default_ != null ? this.default_.hashCode() : 0);
      result = 31 * result + (this.hytale != null ? this.hytale.hashCode() : 0);
      result = 31 * result + (this.hytaleParent != null ? this.hytaleParent.hashCode() : 0);
      result = 31 * result + (this.hytaleSchemaTypeField != null ? this.hytaleSchemaTypeField.hashCode() : 0);
      result = 31 * result + (this.hytaleAssetRef != null ? this.hytaleAssetRef.hashCode() : 0);
      return 31 * result + (this.hytaleCustomAssetRef != null ? this.hytaleCustomAssetRef.hashCode() : 0);
   }

   public static void init() {
      CODEC.register(Priority.DEFAULT, "", Schema.class, BASE_CODEC);
      CODEC.register("null", NullSchema.class, NullSchema.CODEC);
      CODEC.register("string", StringSchema.class, StringSchema.CODEC);
      CODEC.register("number", NumberSchema.class, NumberSchema.CODEC);
      CODEC.register("integer", IntegerSchema.class, IntegerSchema.CODEC);
      CODEC.register("array", ArraySchema.class, ArraySchema.CODEC);
      CODEC.register("boolean", BooleanSchema.class, BooleanSchema.CODEC);
      CODEC.register("object", ObjectSchema.class, ObjectSchema.CODEC);
      UIEditor.init();
   }

   @Deprecated
   private static class ArrayOrNull implements Codec<String[]> {
      @Nullable
      public String[] decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
         return bsonValue.isArray() ? Codec.STRING_ARRAY.decode(bsonValue, extraInfo) : null;
      }

      @Nonnull
      public BsonValue encode(@Nullable String[] o, ExtraInfo extraInfo) {
         return (BsonValue)(o != null ? Codec.STRING_ARRAY.encode(o, extraInfo) : new BsonNull());
      }

      @Nonnull
      @Override
      public Schema toSchema(@Nonnull SchemaContext context) {
         return Schema.anyOf(new ArraySchema(), new NullSchema());
      }
   }

   @Deprecated
   protected static class BooleanOrSchema implements Codec<Object> {
      @Override
      public Object decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
         return bsonValue.isBoolean() ? Codec.BOOLEAN.decode(bsonValue, extraInfo) : Schema.CODEC.decode(bsonValue, extraInfo);
      }

      @Override
      public BsonValue encode(Object o, ExtraInfo extraInfo) {
         return o instanceof Boolean ? Codec.BOOLEAN.encode((Boolean)o, extraInfo) : Schema.CODEC.encode((Schema)o, extraInfo);
      }

      @Nonnull
      @Override
      public Schema toSchema(@Nonnull SchemaContext context) {
         return Schema.anyOf(new BooleanSchema(), Schema.CODEC.toSchema(context));
      }
   }

   public static class HytaleMetadata {
      public static final BuilderCodec<Schema.HytaleMetadata> CODEC = BuilderCodec.builder(Schema.HytaleMetadata.class, Schema.HytaleMetadata::new)
         .addField(new KeyedCodec<>("type", Codec.STRING, false, true), (o, i) -> o.type = i, o -> o.type != null && o.type.isEmpty() ? null : o.type)
         .addField(new KeyedCodec<>("internalKeys", Codec.STRING_ARRAY, false, true), (o, i) -> o.internalKeys = i, o -> o.internalKeys)
         .addField(new KeyedCodec<>("path", Codec.STRING, false, true), (o, i) -> o.path = i, o -> o.path)
         .addField(new KeyedCodec<>("virtualPath", Codec.STRING, false, true), (o, i) -> o.virtualPath = i, o -> o.virtualPath)
         .addField(new KeyedCodec<>("extension", Codec.STRING, false, true), (o, i) -> o.extension = i, o -> o.extension)
         .addField(new KeyedCodec<>("idProvider", Codec.STRING, false, true), (o, i) -> o.idProvider = i, o -> o.idProvider)
         .addField(new KeyedCodec<>("inheritsProperty", Codec.BOOLEAN, false, true), (o, i) -> o.inheritsProperty = i, o -> o.inheritsProperty)
         .addField(new KeyedCodec<>("mergesProperties", Codec.BOOLEAN, false, true), (o, i) -> o.mergesProperties = i, o -> o.mergesProperties)
         .addField(
            new KeyedCodec<>("uiDisplayMode", new EnumCodec<>(UIDisplayMode.DisplayMode.class), false, true),
            (o, i) -> o.uiDisplayMode = i,
            o -> o.uiDisplayMode
         )
         .addField(new KeyedCodec<>("uiEditorComponent", UIEditor.CODEC, false, true), (o, i) -> o.uiEditorComponent = i, o -> o.uiEditorComponent)
         .addField(new KeyedCodec<>("allowEmptyObject", Codec.BOOLEAN, false, true), (o, i) -> o.allowEmptyObject = i, o -> o.allowEmptyObject)
         .addField(new KeyedCodec<>("uiEditorIgnore", Codec.BOOLEAN, false, true), (o, i) -> o.uiEditorIgnore = i, o -> o.uiEditorIgnore)
         .addField(
            new KeyedCodec<>(
               "uiEditorFeatures", new ArrayCodec<>(new EnumCodec<>(UIEditorFeatures.EditorFeature.class), UIEditorFeatures.EditorFeature[]::new), false, true
            ),
            (o, i) -> o.uiEditorFeatures = i,
            o -> o.uiEditorFeatures
         )
         .addField(
            new KeyedCodec<>("uiEditorPreview", new EnumCodec<>(UIEditorPreview.PreviewType.class), false, true),
            (o, i) -> o.uiEditorPreview = i,
            o -> o.uiEditorPreview
         )
         .addField(new KeyedCodec<>("uiTypeIcon", Codec.STRING, false, true), (o, i) -> o.uiTypeIcon = i, o -> o.uiTypeIcon)
         .addField(new KeyedCodec<>("uiPropertyTitle", Codec.STRING, false, true), (o, i) -> o.uiPropertyTitle = i, o -> o.uiPropertyTitle)
         .addField(new KeyedCodec<>("uiSectionStart", Codec.STRING, false, true), (o, i) -> o.uiSectionStart = i, o -> o.uiSectionStart)
         .addField(
            new KeyedCodec<>(
               "uiRebuildCaches", new ArrayCodec<>(new EnumCodec<>(UIRebuildCaches.ClientCache.class), UIRebuildCaches.ClientCache[]::new), false, true
            ),
            (o, i) -> o.uiRebuildCaches = i,
            o -> o.uiRebuildCaches
         )
         .addField(
            new KeyedCodec<>("uiSidebarButtons", new ArrayCodec<>(UIButton.CODEC, UIButton[]::new), false, true),
            (o, i) -> o.uiSidebarButtons = i,
            o -> o.uiSidebarButtons
         )
         .addField(
            new KeyedCodec<>("uiRebuildCachesForChildProperties", Codec.BOOLEAN, false, true),
            (o, i) -> o.uiRebuildCachesForChildProperties = i,
            o -> o.uiRebuildCachesForChildProperties
         )
         .addField(new KeyedCodec<>("uiCollapsedByDefault", Codec.BOOLEAN, false, true), (o, i) -> o.uiCollapsedByDefault = i, o -> o.uiCollapsedByDefault)
         .addField(
            new KeyedCodec<>("uiCreateButtons", new ArrayCodec<>(UIButton.CODEC, UIButton[]::new), false, true),
            (o, i) -> o.uiCreateButtons = i,
            o -> o.uiCreateButtons
         )
         .build();
      private String type;
      private String path;
      private String virtualPath;
      private String extension;
      private String idProvider;
      private String[] internalKeys;
      private Boolean inheritsProperty;
      private Boolean mergesProperties;
      private UIEditorFeatures.EditorFeature[] uiEditorFeatures;
      private UIEditorPreview.PreviewType uiEditorPreview;
      private String uiTypeIcon;
      private Boolean uiEditorIgnore;
      private Boolean allowEmptyObject;
      private UIDisplayMode.DisplayMode uiDisplayMode;
      private UIEditor.EditorComponent uiEditorComponent;
      private String uiPropertyTitle;
      private String uiSectionStart;
      private UIRebuildCaches.ClientCache[] uiRebuildCaches;
      private Boolean uiRebuildCachesForChildProperties;
      private UIButton[] uiSidebarButtons;
      private Boolean uiCollapsedByDefault;
      private UIButton[] uiCreateButtons;

      public HytaleMetadata(String type) {
         this.type = type;
      }

      public HytaleMetadata() {
      }

      public String getType() {
         return this.type;
      }

      public void setType(String type) {
         this.type = type;
      }

      public String getPath() {
         return this.path;
      }

      public void setPath(String path) {
         this.path = path;
      }

      public String getVirtualPath() {
         return this.virtualPath;
      }

      public void setVirtualPath(String virtualPath) {
         this.virtualPath = virtualPath;
      }

      public String getExtension() {
         return this.extension;
      }

      public void setExtension(String extension) {
         this.extension = extension;
      }

      public String getIdProvider() {
         return this.idProvider;
      }

      public void setIdProvider(String idProvider) {
         this.idProvider = idProvider;
      }

      public String[] getInternalKeys() {
         return this.internalKeys;
      }

      public void setInternalKeys(String[] internalKeys) {
         this.internalKeys = internalKeys;
      }

      public UIDisplayMode.DisplayMode getUiDisplayMode() {
         return this.uiDisplayMode;
      }

      public void setUiDisplayMode(UIDisplayMode.DisplayMode uiDisplayMode) {
         this.uiDisplayMode = uiDisplayMode;
      }

      public UIEditor.EditorComponent getUiEditorComponent() {
         return this.uiEditorComponent;
      }

      public void setUiEditorComponent(UIEditor.EditorComponent uiEditorComponent) {
         this.uiEditorComponent = uiEditorComponent;
      }

      public UIEditorFeatures.EditorFeature[] getUiEditorFeatures() {
         return this.uiEditorFeatures;
      }

      public void setUiEditorFeatures(UIEditorFeatures.EditorFeature[] uiEditorFeatures) {
         this.uiEditorFeatures = uiEditorFeatures;
      }

      public UIEditorPreview.PreviewType getUiEditorPreview() {
         return this.uiEditorPreview;
      }

      public void setUiEditorPreview(UIEditorPreview.PreviewType uiEditorPreview) {
         this.uiEditorPreview = uiEditorPreview;
      }

      public String getUiTypeIcon() {
         return this.uiTypeIcon;
      }

      public void setUiTypeIcon(String uiTypeIcon) {
         this.uiTypeIcon = uiTypeIcon;
      }

      public Boolean getUiEditorIgnore() {
         return this.uiEditorIgnore;
      }

      public void setUiEditorIgnore(Boolean uiEditorIgnore) {
         this.uiEditorIgnore = uiEditorIgnore;
      }

      public Boolean getAllowEmptyObject() {
         return this.allowEmptyObject;
      }

      public void setAllowEmptyObject(Boolean allowEmptyObject) {
         this.allowEmptyObject = allowEmptyObject;
      }

      public String getUiPropertyTitle() {
         return this.uiPropertyTitle;
      }

      public void setUiPropertyTitle(String uiPropertyTitle) {
         this.uiPropertyTitle = uiPropertyTitle;
      }

      public String getUiSectionStart() {
         return this.uiSectionStart;
      }

      public void setUiSectionStart(String uiSectionStart) {
         this.uiSectionStart = uiSectionStart;
      }

      public boolean isInheritsProperty() {
         return this.inheritsProperty;
      }

      public void setInheritsProperty(boolean inheritsProperty) {
         this.inheritsProperty = inheritsProperty;
      }

      public boolean getMergesProperties() {
         return this.mergesProperties;
      }

      public void setMergesProperties(boolean mergesProperties) {
         this.mergesProperties = mergesProperties;
      }

      public UIRebuildCaches.ClientCache[] getUiRebuildCaches() {
         return this.uiRebuildCaches;
      }

      public void setUiRebuildCaches(UIRebuildCaches.ClientCache[] uiRebuildCaches) {
         this.uiRebuildCaches = uiRebuildCaches;
      }

      public Boolean getUiRebuildCachesForChildProperties() {
         return this.uiRebuildCachesForChildProperties;
      }

      public void setUiRebuildCachesForChildProperties(Boolean uiRebuildCachesForChildProperties) {
         this.uiRebuildCachesForChildProperties = uiRebuildCachesForChildProperties;
      }

      public UIButton[] getUiSidebarButtons() {
         return this.uiSidebarButtons;
      }

      public void setUiSidebarButtons(UIButton[] uiSidebarButtons) {
         this.uiSidebarButtons = uiSidebarButtons;
      }

      public Boolean getUiCollapsedByDefault() {
         return this.uiCollapsedByDefault;
      }

      public void setUiCollapsedByDefault(Boolean uiCollapsedByDefault) {
         this.uiCollapsedByDefault = uiCollapsedByDefault;
      }

      public UIButton[] getUiCreateButtons() {
         return this.uiCreateButtons;
      }

      public void setUiCreateButtons(UIButton[] uiCreateButtons) {
         this.uiCreateButtons = uiCreateButtons;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Schema.HytaleMetadata that = (Schema.HytaleMetadata)o;
            if (this.type != null ? this.type.equals(that.type) : that.type == null) {
               if (this.path != null ? this.path.equals(that.path) : that.path == null) {
                  if (this.virtualPath != null ? this.virtualPath.equals(that.virtualPath) : that.virtualPath == null) {
                     if (this.extension != null ? this.extension.equals(that.extension) : that.extension == null) {
                        if (this.idProvider != null ? this.idProvider.equals(that.idProvider) : that.idProvider == null) {
                           if (!Arrays.equals((Object[])this.internalKeys, (Object[])that.internalKeys)) {
                              return false;
                           } else if (this.inheritsProperty != null ? this.inheritsProperty.equals(that.inheritsProperty) : that.inheritsProperty == null) {
                              if (this.mergesProperties != null ? this.mergesProperties.equals(that.mergesProperties) : that.mergesProperties == null) {
                                 if (!Arrays.equals((Object[])this.uiEditorFeatures, (Object[])that.uiEditorFeatures)) {
                                    return false;
                                 } else if (this.uiEditorPreview != that.uiEditorPreview) {
                                    return false;
                                 } else if (this.uiTypeIcon != null ? this.uiTypeIcon.equals(that.uiTypeIcon) : that.uiTypeIcon == null) {
                                    if (this.uiEditorIgnore != null ? this.uiEditorIgnore.equals(that.uiEditorIgnore) : that.uiEditorIgnore == null) {
                                       if (this.allowEmptyObject != null ? this.allowEmptyObject.equals(that.allowEmptyObject) : that.allowEmptyObject == null) {
                                          if (this.uiDisplayMode != that.uiDisplayMode) {
                                             return false;
                                          } else if (this.uiEditorComponent != null
                                             ? this.uiEditorComponent.equals(that.uiEditorComponent)
                                             : that.uiEditorComponent == null) {
                                             if (this.uiPropertyTitle != null
                                                ? this.uiPropertyTitle.equals(that.uiPropertyTitle)
                                                : that.uiPropertyTitle == null) {
                                                if (this.uiSectionStart != null ? this.uiSectionStart.equals(that.uiSectionStart) : that.uiSectionStart == null
                                                   )
                                                 {
                                                   if (!Arrays.equals((Object[])this.uiRebuildCaches, (Object[])that.uiRebuildCaches)) {
                                                      return false;
                                                   } else if (this.uiRebuildCachesForChildProperties != null
                                                      ? this.uiRebuildCachesForChildProperties.equals(that.uiRebuildCachesForChildProperties)
                                                      : that.uiRebuildCachesForChildProperties == null) {
                                                      if (!Arrays.equals((Object[])this.uiSidebarButtons, (Object[])that.uiSidebarButtons)) {
                                                         return false;
                                                      } else {
                                                         return (
                                                               this.uiCollapsedByDefault != null
                                                                  ? this.uiCollapsedByDefault.equals(that.uiCollapsedByDefault)
                                                                  : that.uiCollapsedByDefault == null
                                                            )
                                                            ? Arrays.equals((Object[])this.uiCreateButtons, (Object[])that.uiCreateButtons)
                                                            : false;
                                                      }
                                                   } else {
                                                      return false;
                                                   }
                                                } else {
                                                   return false;
                                                }
                                             } else {
                                                return false;
                                             }
                                          } else {
                                             return false;
                                          }
                                       } else {
                                          return false;
                                       }
                                    } else {
                                       return false;
                                    }
                                 } else {
                                    return false;
                                 }
                              } else {
                                 return false;
                              }
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.type != null ? this.type.hashCode() : 0;
         result = 31 * result + (this.path != null ? this.path.hashCode() : 0);
         result = 31 * result + (this.virtualPath != null ? this.virtualPath.hashCode() : 0);
         result = 31 * result + (this.extension != null ? this.extension.hashCode() : 0);
         result = 31 * result + (this.idProvider != null ? this.idProvider.hashCode() : 0);
         result = 31 * result + Arrays.hashCode((Object[])this.internalKeys);
         result = 31 * result + (this.inheritsProperty != null ? this.inheritsProperty.hashCode() : 0);
         result = 31 * result + (this.mergesProperties != null ? this.mergesProperties.hashCode() : 0);
         result = 31 * result + Arrays.hashCode((Object[])this.uiEditorFeatures);
         result = 31 * result + (this.uiEditorPreview != null ? this.uiEditorPreview.hashCode() : 0);
         result = 31 * result + (this.uiTypeIcon != null ? this.uiTypeIcon.hashCode() : 0);
         result = 31 * result + (this.uiEditorIgnore != null ? this.uiEditorIgnore.hashCode() : 0);
         result = 31 * result + (this.allowEmptyObject != null ? this.allowEmptyObject.hashCode() : 0);
         result = 31 * result + (this.uiDisplayMode != null ? this.uiDisplayMode.hashCode() : 0);
         result = 31 * result + (this.uiEditorComponent != null ? this.uiEditorComponent.hashCode() : 0);
         result = 31 * result + (this.uiPropertyTitle != null ? this.uiPropertyTitle.hashCode() : 0);
         result = 31 * result + (this.uiSectionStart != null ? this.uiSectionStart.hashCode() : 0);
         result = 31 * result + Arrays.hashCode((Object[])this.uiRebuildCaches);
         result = 31 * result + (this.uiRebuildCachesForChildProperties != null ? this.uiRebuildCachesForChildProperties.hashCode() : 0);
         result = 31 * result + Arrays.hashCode((Object[])this.uiSidebarButtons);
         result = 31 * result + (this.uiCollapsedByDefault != null ? this.uiCollapsedByDefault.hashCode() : 0);
         return 31 * result + Arrays.hashCode((Object[])this.uiCreateButtons);
      }
   }

   public static class InheritSettings {
      public static final BuilderCodec<Schema.InheritSettings> CODEC = BuilderCodec.builder(Schema.InheritSettings.class, Schema.InheritSettings::new)
         .addField(new KeyedCodec<>("type", Codec.STRING, false, true), (o, i) -> o.type = i, o -> o.type)
         .addField(new KeyedCodec<>("mapKey", Codec.STRING, false, true), (o, i) -> o.mapKey = i, o -> o.mapKey)
         .addField(new KeyedCodec<>("mapKeyValue", Codec.STRING, false, true), (o, i) -> o.mapKeyValue = i, o -> o.mapKeyValue)
         .build();
      private String type;
      private String mapKey;
      private String mapKeyValue;

      public InheritSettings(String type) {
         this.type = type;
      }

      protected InheritSettings() {
      }

      public String getType() {
         return this.type;
      }

      public void setType(String type) {
         this.type = type;
      }

      public String getMapKey() {
         return this.mapKey;
      }

      public void setMapKey(String mapKey) {
         this.mapKey = mapKey;
      }

      public String getMapKeyValue() {
         return this.mapKeyValue;
      }

      public void setMapKeyValue(String mapKeyValue) {
         this.mapKeyValue = mapKeyValue;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Schema.InheritSettings that = (Schema.InheritSettings)o;
            if (this.type != null ? this.type.equals(that.type) : that.type == null) {
               if (this.mapKey != null ? this.mapKey.equals(that.mapKey) : that.mapKey == null) {
                  return this.mapKeyValue != null ? this.mapKeyValue.equals(that.mapKeyValue) : that.mapKeyValue == null;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.type != null ? this.type.hashCode() : 0;
         result = 31 * result + (this.mapKey != null ? this.mapKey.hashCode() : 0);
         return 31 * result + (this.mapKeyValue != null ? this.mapKeyValue.hashCode() : 0);
      }
   }

   public static class SchemaTypeField {
      public static final BuilderCodec<Schema.SchemaTypeField> CODEC = BuilderCodec.builder(Schema.SchemaTypeField.class, Schema.SchemaTypeField::new)
         .addField(new KeyedCodec<>("property", Codec.STRING, false, true), (o, i) -> o.property = i, o -> o.property)
         .addField(new KeyedCodec<>("defaultValue", Codec.STRING, false, true), (o, i) -> o.defaultValue = i, o -> o.defaultValue)
         .addField(new KeyedCodec<>("values", Codec.STRING_ARRAY, false, true), (o, i) -> o.values = i, o -> o.values)
         .addField(new KeyedCodec<>("parentPropertyKey", Codec.STRING, false, true), (o, i) -> o.parentPropertyKey = i, o -> o.parentPropertyKey)
         .build();
      private String property;
      private String defaultValue;
      private String[] values;
      private String parentPropertyKey;

      public SchemaTypeField(String property, String defaultValue, String... values) {
         this.property = property;
         this.defaultValue = defaultValue;
         this.values = values;
      }

      protected SchemaTypeField() {
      }

      public String getProperty() {
         return this.property;
      }

      public String getDefaultValue() {
         return this.defaultValue;
      }

      public String[] getValues() {
         return this.values;
      }

      public String getParentPropertyKey() {
         return this.parentPropertyKey;
      }

      public void setParentPropertyKey(String parentPropertyKey) {
         this.parentPropertyKey = parentPropertyKey;
      }

      @Override
      public boolean equals(Object o) {
         if (o != null && this.getClass() == o.getClass()) {
            Schema.SchemaTypeField that = (Schema.SchemaTypeField)o;
            return Objects.equals(this.property, that.property)
               && Objects.equals(this.defaultValue, that.defaultValue)
               && Arrays.deepEquals(this.values, that.values)
               && Objects.equals(this.parentPropertyKey, that.parentPropertyKey);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = Objects.hashCode(this.property);
         result = 31 * result + Objects.hashCode(this.defaultValue);
         result = 31 * result + Arrays.hashCode((Object[])this.values);
         return 31 * result + Objects.hashCode(this.parentPropertyKey);
      }
   }

   @Deprecated
   private static class StringOrBlank implements Codec<String> {
      public String decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
         if (bsonValue.isString()) {
            return Codec.STRING.decode(bsonValue, extraInfo);
         } else if (bsonValue.isArray()) {
            BsonArray arr = bsonValue.asArray();

            for (int i = 0; i < arr.size(); i++) {
               BsonValue val = arr.get(i);
               if (!val.asString().getValue().equals("null")) {
                  return Codec.STRING.decode(val, extraInfo);
               }
            }

            throw new IllegalArgumentException("Unknown type (in array)");
         } else {
            return "";
         }
      }

      @Nonnull
      public BsonValue encode(@Nonnull String o, ExtraInfo extraInfo) {
         return Codec.STRING.encode(o, extraInfo);
      }

      @Nonnull
      @Override
      public Schema toSchema(@Nonnull SchemaContext context) {
         return Schema.anyOf(new ArraySchema(), new StringSchema());
      }
   }
}
