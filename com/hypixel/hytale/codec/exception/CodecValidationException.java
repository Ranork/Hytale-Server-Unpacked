package com.hypixel.hytale.codec.exception;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import javax.annotation.Nonnull;
import org.bson.BsonValue;

public class CodecValidationException extends CodecException {
   public CodecValidationException(String message) {
      super(message);
   }

   public CodecValidationException(String message, Throwable cause) {
      super(message, cause);
   }

   public CodecValidationException(String message, BsonValue bsonValue, @Nonnull ExtraInfo extraInfo, Throwable cause) {
      super(message, bsonValue, extraInfo, cause);
   }

   public CodecValidationException(String message, RawJsonReader reader, @Nonnull ExtraInfo extraInfo, Throwable cause) {
      super(message, reader, extraInfo, cause);
   }

   public CodecValidationException(String message, Object obj, @Nonnull ExtraInfo extraInfo, Throwable cause) {
      super(message, obj, extraInfo, cause);
   }
}
