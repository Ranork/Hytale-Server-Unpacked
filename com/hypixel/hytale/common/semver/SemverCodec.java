package com.hypixel.hytale.common.semver;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.bson.BsonString;
import org.bson.BsonValue;

public class SemverCodec implements Codec<Semver> {
   public static final SemverCodec INSTANCE = new SemverCodec();

   public Semver decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      return Semver.fromString(bsonValue.asString().getValue());
   }

   @Nonnull
   public BsonValue encode(@Nonnull Semver semver, ExtraInfo extraInfo) {
      return new BsonString(semver.toString());
   }

   @Nonnull
   public Semver decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      return Semver.fromString(reader.readString());
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      StringSchema schema = new StringSchema();
      schema.setTitle("Semver");
      schema.setDescription(
         "A semantic version: <major>.<minor>.<patch> with optional -pre.release and +build.metadata. Examples: 0.5.0, 1.2.3-pre.7, 0.5.0-dev+abc1234."
      );
      schema.setMinLength(1);
      return schema;
   }
}
