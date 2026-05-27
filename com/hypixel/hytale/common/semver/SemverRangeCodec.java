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

public class SemverRangeCodec implements Codec<SemverRange> {
   public static final SemverRangeCodec INSTANCE = new SemverRangeCodec();

   public SemverRange decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      return this.parse(bsonValue.asString().getValue());
   }

   @Nonnull
   public BsonValue encode(@Nonnull SemverRange range, ExtraInfo extraInfo) {
      return new BsonString(range.toString());
   }

   @Nonnull
   public SemverRange decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      return this.parse(reader.readString());
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      StringSchema schema = new StringSchema();
      schema.setTitle("SemverRange");
      schema.setDescription(
         "A semver range expression with npm-strict pre-release semantics. Examples: *, >=0.5.0, 0.5.x, ^0.5.0, >=0.5.0 <0.6.0, >=0.5.0-pre <0.6.0, 1.0.0 || ^2.0.0."
      );
      return schema;
   }

   protected SemverRange parse(String str) {
      return SemverRange.fromString(str);
   }
}
