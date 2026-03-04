package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderField;
import com.hypixel.hytale.codec.function.BsonFunctionCodec;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;

@Deprecated
public class DocumentContainingCodec<T> extends BsonFunctionCodec<T> {
   public DocumentContainingCodec(@Nonnull BuilderCodec<T> codec, @Nonnull BiConsumer<T, BsonDocument> setter, @Nonnull Function<T, BsonDocument> getter) {
      super(codec, (value, bsonValue) -> {
         BsonDocument document = bsonValue.asDocument().clone();

         for (List<BuilderField<T, ?>> entry : codec.getEntries().values()) {
            for (BuilderField<T, ?> field : entry) {
               document.remove(field.getCodec().getKey());
            }
         }

         setter.accept(value, document);
         return value;
      }, (bsonValue, value) -> {
         BsonDocument document = getter.apply(value);
         if (document != null) {
            bsonValue.asDocument().putAll(document);
         }

         return bsonValue;
      });
   }
}
