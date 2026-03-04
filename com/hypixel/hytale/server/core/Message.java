package com.hypixel.hytale.server.core;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.function.FunctionCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.BooleanSchema;
import com.hypixel.hytale.codec.schema.config.IntegerSchema;
import com.hypixel.hytale.codec.schema.config.NullSchema;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.BoolParamValue;
import com.hypixel.hytale.protocol.DoubleParamValue;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.IntParamValue;
import com.hypixel.hytale.protocol.LongParamValue;
import com.hypixel.hytale.protocol.MaybeBool;
import com.hypixel.hytale.protocol.ParamValue;
import com.hypixel.hytale.protocol.StringParamValue;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.util.MessageUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonBoolean;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.BsonValue;

public class Message {
   private static final BuilderCodec.Builder<FormattedMessage> MESSAGE_CODEC_BUILDER = BuilderCodec.builder(FormattedMessage.class, FormattedMessage::new);
   private static final BuilderCodec<FormattedMessage> MESSAGE_CODEC = MESSAGE_CODEC_BUILDER.build();
   private static final Codec<ParamValue> PARAM_CODEC = new Message.ParamValueCodec();
   private static final Codec<MaybeBool> MAYBE_BOOL_CODEC = new Message.MaybeBoolCodec();
   public static final FunctionCodec<FormattedMessage, Message> CODEC = new FunctionCodec<>(MESSAGE_CODEC, Message::new, Message::getFormattedMessage);
   private final FormattedMessage message;

   protected Message(@Nonnull String message, boolean i18n) {
      this();
      if (i18n) {
         this.message.messageId = message;
      } else {
         this.message.rawText = message;
      }
   }

   protected Message() {
      this.message = new FormattedMessage();
   }

   public Message(@Nonnull FormattedMessage message) {
      this.message = message;
   }

   @Nonnull
   public Message param(@Nonnull String key, @Nonnull String value) {
      if (this.message.params == null) {
         this.message.params = new HashMap<>();
      }

      StringParamValue val = new StringParamValue();
      val.value = value;
      this.message.params.put(key, val);
      return this;
   }

   @Nonnull
   public Message param(@Nonnull String key, boolean value) {
      if (this.message.params == null) {
         this.message.params = new HashMap<>();
      }

      BoolParamValue val = new BoolParamValue();
      val.value = value;
      this.message.params.put(key, val);
      return this;
   }

   @Nonnull
   public Message param(@Nonnull String key, double value) {
      if (this.message.params == null) {
         this.message.params = new HashMap<>();
      }

      DoubleParamValue val = new DoubleParamValue();
      val.value = value;
      this.message.params.put(key, val);
      return this;
   }

   @Nonnull
   public Message param(@Nonnull String key, int value) {
      if (this.message.params == null) {
         this.message.params = new HashMap<>();
      }

      IntParamValue val = new IntParamValue();
      val.value = value;
      this.message.params.put(key, val);
      return this;
   }

   @Nonnull
   public Message param(@Nonnull String key, long value) {
      if (this.message.params == null) {
         this.message.params = new HashMap<>();
      }

      LongParamValue val = new LongParamValue();
      val.value = value;
      this.message.params.put(key, val);
      return this;
   }

   @Nonnull
   public Message param(@Nonnull String key, float value) {
      if (this.message.params == null) {
         this.message.params = new HashMap<>();
      }

      DoubleParamValue val = new DoubleParamValue();
      val.value = value;
      this.message.params.put(key, val);
      return this;
   }

   @Nonnull
   public Message param(@Nonnull String key, @Nonnull Message formattedMessage) {
      if (this.message.messageParams == null) {
         this.message.messageParams = new HashMap<>();
      }

      this.message.messageParams.put(key, formattedMessage.message);
      return this;
   }

   @Nonnull
   public Message bold(boolean bold) {
      this.message.bold = bold ? MaybeBool.True : MaybeBool.False;
      return this;
   }

   @Nonnull
   public Message italic(boolean italic) {
      this.message.italic = italic ? MaybeBool.True : MaybeBool.False;
      return this;
   }

   @Nonnull
   public Message monospace(boolean monospace) {
      this.message.monospace = monospace ? MaybeBool.True : MaybeBool.False;
      return this;
   }

   @Nonnull
   public Message color(@Nonnull String color) {
      this.message.color = color;
      return this;
   }

   @Nonnull
   public Message color(@Nonnull Color color) {
      this.message.color = ColorParseUtil.colorToHex(color);
      return this;
   }

   @Nonnull
   public Message link(@Nonnull String url) {
      this.message.link = url;
      return this;
   }

   @Nonnull
   public Message insert(@Nonnull Message formattedMessage) {
      this.message.children = ArrayUtil.append(this.message.children, formattedMessage.message);
      return this;
   }

   @Nonnull
   public Message insert(@Nonnull String message) {
      return this.insert(raw(message));
   }

   @Nonnull
   public Message insertAll(@Nonnull Message... formattedMessages) {
      int offset = 0;
      if (this.message.children != null) {
         offset = this.message.children.length;
         this.message.children = Arrays.copyOf(this.message.children, this.message.children.length + formattedMessages.length);
      } else {
         this.message.children = new FormattedMessage[formattedMessages.length];
      }

      for (Message formattedMessage : formattedMessages) {
         this.message.children[offset++] = formattedMessage.message;
      }

      return this;
   }

   @Nonnull
   public Message insertAll(@Nonnull List<Message> formattedMessages) {
      int offset = 0;
      if (this.message.children != null) {
         offset = this.message.children.length;
         this.message.children = Arrays.copyOf(this.message.children, this.message.children.length + formattedMessages.size());
      } else {
         this.message.children = new FormattedMessage[formattedMessages.size()];
      }

      for (Message formattedMessage : formattedMessages) {
         this.message.children[offset++] = formattedMessage.message;
      }

      return this;
   }

   @Nullable
   public String getRawText() {
      return this.message.rawText;
   }

   @Nullable
   public String getMessageId() {
      return this.message.messageId;
   }

   @Nullable
   public String getColor() {
      return this.message.color;
   }

   @Nonnull
   public List<Message> getChildren() {
      if (this.message.children == null) {
         return Collections.emptyList();
      } else {
         List<Message> children = new ObjectArrayList();

         for (FormattedMessage value : this.message.children) {
            children.add(new Message(value));
         }

         return children;
      }
   }

   @Nonnull
   public String getAnsiMessage() {
      String rawText = this.getRawText();
      if (rawText != null) {
         return rawText;
      } else {
         String messageId = this.getMessageId();
         if (messageId == null) {
            return "";
         } else {
            String message = I18nModule.get().getMessage("en-US", messageId);
            if (message != null) {
               return MessageUtil.formatText(message, this.message.params, this.message.messageParams);
            } else {
               StringBuilder rawMessage = new StringBuilder(messageId);
               if (this.message.params != null) {
                  rawMessage.append(this.message.params);
               }

               if (this.message.messageParams != null) {
                  for (Entry<String, FormattedMessage> p : this.message.messageParams.entrySet()) {
                     rawMessage.append(p.getValue()).append("=").append(new Message(p.getValue()).getAnsiMessage());
                  }
               }

               return rawMessage.toString();
            }
         }
      }
   }

   public FormattedMessage getFormattedMessage() {
      return this.message;
   }

   @Override
   public String toString() {
      return this.message.toString();
   }

   @Nonnull
   public static Message empty() {
      return new Message();
   }

   @Nonnull
   public static Message translation(@Nonnull String messageId) {
      return new Message(messageId, true);
   }

   @Nonnull
   public static Message raw(@Nonnull String message) {
      return new Message(message, false);
   }

   @Nonnull
   public static Message parse(@Nonnull String message) {
      try {
         return CODEC.decodeJson(new RawJsonReader(message.toCharArray()), EmptyExtraInfo.EMPTY);
      } catch (IOException var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   @Nonnull
   public static Message join(@Nonnull Message... messages) {
      return new Message().insertAll(messages);
   }

   static {
      MESSAGE_CODEC_BUILDER.appendInherited(new KeyedCodec<>("RawText", Codec.STRING), (o, v) -> o.rawText = v, o -> o.rawText, (o, p) -> o.rawText = p.rawText)
         .add()
         .appendInherited(new KeyedCodec<>("MessageId", Codec.STRING), (o, v) -> o.messageId = v, o -> o.messageId, (o, p) -> o.messageId = p.messageId)
         .add()
         .appendInherited(
            new KeyedCodec<>("Params", new MapCodec<>(PARAM_CODEC, HashMap::new)), (o, v) -> o.params = v, o -> o.params, (o, p) -> o.params = p.params
         )
         .add()
         .appendInherited(
            new KeyedCodec<>("MessageParams", new MapCodec<>(MESSAGE_CODEC, HashMap::new)),
            (o, v) -> o.messageParams = v,
            o -> o.messageParams,
            (o, p) -> o.messageParams = p.messageParams
         )
         .add()
         .appendInherited(
            new KeyedCodec<>("Children", new ArrayCodec<>(MESSAGE_CODEC, FormattedMessage[]::new)),
            (o, v) -> o.children = v,
            o -> o.children,
            (o, p) -> o.children = p.children
         )
         .add()
         .appendInherited(new KeyedCodec<>("Bold", MAYBE_BOOL_CODEC), (o, v) -> o.bold = v != null ? v : MaybeBool.Null, o -> o.bold, (o, p) -> o.bold = p.bold)
         .add()
         .appendInherited(
            new KeyedCodec<>("Italic", MAYBE_BOOL_CODEC), (o, v) -> o.italic = v != null ? v : MaybeBool.Null, o -> o.italic, (o, p) -> o.italic = p.italic
         )
         .add()
         .appendInherited(
            new KeyedCodec<>("Monospace", MAYBE_BOOL_CODEC),
            (o, v) -> o.monospace = v != null ? v : MaybeBool.Null,
            o -> o.monospace,
            (o, p) -> o.monospace = p.monospace
         )
         .add()
         .appendInherited(
            new KeyedCodec<>("Underline", MAYBE_BOOL_CODEC),
            (o, v) -> o.underlined = v != null ? v : MaybeBool.Null,
            o -> o.underlined,
            (o, p) -> o.underlined = p.underlined
         )
         .add()
         .appendInherited(new KeyedCodec<>("Color", Codec.STRING), (o, v) -> o.color = v, o -> o.color, (o, p) -> o.color = p.color)
         .add()
         .appendInherited(new KeyedCodec<>("Link", Codec.STRING), (o, v) -> o.link = v, o -> o.link, (o, p) -> o.link = p.link)
         .add();
   }

   private static class MaybeBoolCodec implements Codec<MaybeBool> {
      @Nullable
      public MaybeBool decode(BsonValue bsonValue, ExtraInfo extraInfo) {
         if (bsonValue.isNull()) {
            return MaybeBool.Null;
         } else {
            return bsonValue.asBoolean().getValue() ? MaybeBool.True : MaybeBool.False;
         }
      }

      public BsonValue encode(MaybeBool maybeBool, ExtraInfo extraInfo) {
         return (BsonValue)(switch (maybeBool) {
            case Null -> BsonNull.VALUE;
            case False -> BsonBoolean.FALSE;
            case True -> BsonBoolean.TRUE;
         });
      }

      @Nullable
      public MaybeBool decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
         if (reader.peekFor('n')) {
            if (!reader.tryConsume("null")) {
               throw new IllegalArgumentException("Invalid null value");
            } else {
               return MaybeBool.Null;
            }
         } else if (reader.peekFor('N')) {
            if (!reader.tryConsume("NULL")) {
               throw new IllegalArgumentException("Invalid null value");
            } else {
               return MaybeBool.Null;
            }
         } else {
            return reader.readBooleanValue() ? MaybeBool.True : MaybeBool.False;
         }
      }

      @Nonnull
      @Override
      public Schema toSchema(@Nonnull SchemaContext context) {
         return Schema.anyOf(new BooleanSchema(), new NullSchema());
      }
   }

   private static class ParamValueCodec implements Codec<ParamValue> {
      @Nullable
      public ParamValue decode(BsonValue bsonValue, ExtraInfo extraInfo) {
         return (ParamValue)(switch (bsonValue.getBsonType()) {
            case DOUBLE -> {
               DoubleParamValue value = new DoubleParamValue();
               value.value = bsonValue.asDouble().getValue();
               yield value;
            }
            case STRING -> {
               StringParamValue value = new StringParamValue();
               value.value = bsonValue.asString().getValue();
               yield value;
            }
            case BOOLEAN -> {
               BoolParamValue value = new BoolParamValue();
               value.value = bsonValue.asBoolean().getValue();
               yield value;
            }
            case INT32 -> {
               IntParamValue value = new IntParamValue();
               value.value = bsonValue.asInt32().getValue();
               yield value;
            }
            case INT64 -> {
               LongParamValue value = new LongParamValue();
               value.value = bsonValue.asInt64().getValue();
               yield value;
            }
            default -> throw new IllegalArgumentException("Unsupported bson type: " + bsonValue.getBsonType());
         });
      }

      public BsonValue encode(ParamValue paramValue, ExtraInfo extraInfo) {
         return (BsonValue)(switch (paramValue) {
            case StringParamValue s -> new BsonString(s.value);
            case BoolParamValue b -> BsonBoolean.valueOf(b.value);
            case DoubleParamValue d -> new BsonDouble(d.value);
            case IntParamValue i -> new BsonInt32(i.value);
            case LongParamValue l -> new BsonInt64(l.value);
            default -> throw new IllegalArgumentException("Unknown ParamValue type: " + paramValue.getClass());
         });
      }

      @Nullable
      public ParamValue decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
         if (reader.peekFor('"')) {
            StringParamValue param = new StringParamValue();
            param.value = reader.readString();
            return param;
         } else if (!reader.peekFor('f') && !reader.peekFor('t')) {
            double value = reader.readDoubleValue();
            DoubleParamValue param = new DoubleParamValue();
            param.value = value;
            return param;
         } else if (!reader.tryConsume("false")) {
            throw new IllegalArgumentException("Invalid boolean value");
         } else {
            BoolParamValue param = new BoolParamValue();
            param.value = reader.readBooleanValue();
            return param;
         }
      }

      @Nonnull
      @Override
      public Schema toSchema(@Nonnull SchemaContext context) {
         return Schema.anyOf(new BooleanSchema(), new NumberSchema(), new IntegerSchema(), new StringSchema());
      }
   }
}
