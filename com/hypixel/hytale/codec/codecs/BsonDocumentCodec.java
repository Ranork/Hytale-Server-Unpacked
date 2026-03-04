package com.hypixel.hytale.codec.codecs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;
import org.bson.BsonValue;

@Deprecated
public class BsonDocumentCodec implements Codec<BsonDocument> {
   public BsonDocument decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      return bsonValue.asDocument();
   }

   public BsonValue encode(BsonDocument document, ExtraInfo extraInfo) {
      return document;
   }

   public BsonDocument decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      return RawJsonReader.readBsonValue(reader).asDocument();
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      return new ObjectSchema();
   }
}
