package com.hypixel.hytale.math.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.math.vector.Vector3i;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonInt32;
import org.bson.BsonValue;

@Deprecated
public class Vector3iArrayCodec implements Codec<Vector3i> {
   @Nonnull
   public Vector3i decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonArray document = bsonValue.asArray();
      return new Vector3i(document.get(0).asNumber().intValue(), document.get(1).asNumber().intValue(), document.get(2).asNumber().intValue());
   }

   @Nonnull
   public BsonValue encode(@Nonnull Vector3i t, ExtraInfo extraInfo) {
      BsonArray array = new BsonArray();
      array.add(new BsonInt32(t.getX()));
      array.add(new BsonInt32(t.getY()));
      array.add(new BsonInt32(t.getZ()));
      return array;
   }

   @Nonnull
   public Vector3i decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      int x = reader.readIntValue();
      reader.consumeWhiteSpace();
      reader.expect(',');
      reader.consumeWhiteSpace();
      int y = reader.readIntValue();
      reader.consumeWhiteSpace();
      reader.expect(',');
      reader.consumeWhiteSpace();
      int z = reader.readIntValue();
      reader.consumeWhiteSpace();
      reader.expect(']');
      reader.consumeWhiteSpace();
      return new Vector3i(x, y, z);
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      ArraySchema s = new ArraySchema();
      s.setTitle("Vector3i");
      s.setItems(new NumberSchema(), new NumberSchema(), new NumberSchema());
      s.setMinItems(3);
      s.setMaxItems(3);
      return s;
   }
}
