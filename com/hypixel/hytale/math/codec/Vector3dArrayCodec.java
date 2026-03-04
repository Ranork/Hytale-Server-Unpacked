package com.hypixel.hytale.math.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.math.vector.Vector3d;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.BsonValue;

@Deprecated
public class Vector3dArrayCodec implements Codec<Vector3d> {
   @Nonnull
   public Vector3d decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonArray document = bsonValue.asArray();
      return new Vector3d(document.get(0).asNumber().doubleValue(), document.get(1).asNumber().doubleValue(), document.get(2).asNumber().doubleValue());
   }

   @Nonnull
   public BsonValue encode(@Nonnull Vector3d t, ExtraInfo extraInfo) {
      BsonArray array = new BsonArray();
      array.add(new BsonDouble(t.getX()));
      array.add(new BsonDouble(t.getY()));
      array.add(new BsonDouble(t.getZ()));
      return array;
   }

   @Nonnull
   public Vector3d decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      double x = reader.readDoubleValue();
      reader.consumeWhiteSpace();
      reader.expect(',');
      reader.consumeWhiteSpace();
      double y = reader.readDoubleValue();
      reader.consumeWhiteSpace();
      reader.expect(',');
      reader.consumeWhiteSpace();
      double z = reader.readDoubleValue();
      reader.consumeWhiteSpace();
      reader.expect(']');
      reader.consumeWhiteSpace();
      return new Vector3d(x, y, z);
   }

   @Nonnull
   @Override
   public Schema toSchema(@Nonnull SchemaContext context) {
      ArraySchema s = new ArraySchema();
      s.setTitle("Vector3d");
      s.setItems(new NumberSchema(), new NumberSchema(), new NumberSchema());
      s.setMinItems(3);
      s.setMaxItems(3);
      return s;
   }
}
