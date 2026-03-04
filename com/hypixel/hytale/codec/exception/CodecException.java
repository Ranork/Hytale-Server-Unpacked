package com.hypixel.hytale.codec.exception;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import javax.annotation.Nonnull;
import org.bson.BsonValue;

public class CodecException extends RuntimeException {
   private final String message;

   public CodecException(String message) {
      super(message);
      this.message = message;
   }

   public CodecException(String message, Throwable cause) {
      super(message, cause);
      this.message = message;
   }

   public CodecException(String message, BsonValue bsonValue, @Nonnull ExtraInfo extraInfo, Throwable cause) {
      super(message + " '" + extraInfo.peekKey() + "' " + (cause instanceof CodecException ? "" : "\nFrom: '" + bsonValue + "'"), cause);
      this.message = message;
   }

   public CodecException(String message, RawJsonReader reader, @Nonnull ExtraInfo extraInfo, Throwable cause) {
      super(message + " '" + extraInfo.peekKey() + "' " + (cause instanceof CodecException ? "" : "\nFrom: " + reader + "'"), cause);
      this.message = message;
   }

   public CodecException(String message, Object obj, @Nonnull ExtraInfo extraInfo, Throwable cause) {
      super(message + " '" + extraInfo.peekKey() + "' " + (cause instanceof CodecException ? "" : "\nFor: '" + obj + "'"), cause);
      this.message = message;
   }

   @Override
   public String getMessage() {
      return this.message;
   }
}
