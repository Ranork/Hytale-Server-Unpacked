package com.hypixel.hytale.server.core.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import java.util.Map;
import javax.annotation.Nonnull;
import org.bson.BsonString;
import org.bson.BsonValue;

public class LocalizableString {
   public static final LocalizableString.LocalizableStringCodec CODEC = new LocalizableString.LocalizableStringCodec();
   public static final BuilderCodec<LocalizableString> MESSAGE_OBJECT_CODEC = BuilderCodec.builder(LocalizableString.class, LocalizableString::new)
      .addField(new KeyedCodec<>("MessageId", Codec.STRING), (p, t) -> p.messageId = t, p -> p.messageId)
      .addField(new KeyedCodec<>("MessageParams", MapCodec.STRING_HASH_MAP_CODEC), (p, t) -> p.messageParams = t, p -> p.messageParams)
      .build();
   private String stringValue;
   private String messageId;
   private Map<String, String> messageParams;

   @Nonnull
   public static LocalizableString fromString(String str) {
      LocalizableString instance = new LocalizableString();
      instance.stringValue = str;
      return instance;
   }

   @Nonnull
   public static LocalizableString fromMessageId(String messageId) {
      return fromMessageId(messageId, null);
   }

   @Nonnull
   public static LocalizableString fromMessageId(String messageId, Map<String, String> params) {
      LocalizableString instance = new LocalizableString();
      instance.messageId = messageId;
      instance.messageParams = params;
      return instance;
   }

   public static class LocalizableStringCodec implements Codec<LocalizableString> {
      public LocalizableString decode(BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
         return bsonValue instanceof BsonString
            ? LocalizableString.fromString(bsonValue.asString().getValue())
            : LocalizableString.MESSAGE_OBJECT_CODEC.decode(bsonValue, extraInfo);
      }

      @Nonnull
      public BsonValue encode(@Nonnull LocalizableString t, @Nonnull ExtraInfo extraInfo) {
         return (BsonValue)(t.stringValue != null ? new BsonString(t.stringValue) : LocalizableString.MESSAGE_OBJECT_CODEC.encode(t, extraInfo));
      }

      @Nonnull
      @Override
      public Schema toSchema(@Nonnull SchemaContext context) {
         return Schema.anyOf(new StringSchema(), LocalizableString.MESSAGE_OBJECT_CODEC.toSchema(context));
      }
   }
}
