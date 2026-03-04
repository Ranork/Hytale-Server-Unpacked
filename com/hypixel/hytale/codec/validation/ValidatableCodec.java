package com.hypixel.hytale.codec.validation;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import java.util.Set;

public interface ValidatableCodec<T> extends Codec<T> {
   void validate(T var1, ExtraInfo var2);

   void validateDefaults(ExtraInfo var1, Set<Codec<?>> var2);

   static void validateDefaults(Codec<?> codec, ExtraInfo extraInfo, Set<Codec<?>> tested) {
      while (true) {
         if (codec instanceof WrappedCodec<?> wrappedCodec) {
            codec = wrappedCodec.getChildCodec();
            if (codec != null) {
               continue;
            }
         } else if (codec instanceof ValidatableCodec<?> validatableCodec) {
            validatableCodec.validateDefaults(extraInfo, tested);
         }

         return;
      }
   }
}
