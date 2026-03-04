package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundFileValidators {
   public static final SoundFileValidators.ChannelValidator MONO = new SoundFileValidators.ChannelValidator(1);
   public static final SoundFileValidators.ChannelValidator STEREO = new SoundFileValidators.ChannelValidator(2);
   private static final String MONO_STRING = "Mono";
   private static final String STEREO_STRING = "Stereo";

   @Nonnull
   public static String getEncoding(int channelCount) {
      return switch (channelCount) {
         case 1 -> "Mono";
         case 2 -> "Stereo";
         default -> throw new IllegalArgumentException("Invalid channel count: " + channelCount);
      };
   }

   public static class ChannelValidator implements Validator<String> {
      private final int channelCount;

      public ChannelValidator(int channelCount) {
         assert channelCount == 1 || channelCount == 2;

         this.channelCount = channelCount;
      }

      public void accept(@Nullable String s, @Nonnull ValidationResults results) {
         if (s != null) {
            OggVorbisInfoCache.OggVorbisInfo info = OggVorbisInfoCache.getNow(s);
            if (info == null) {
               results.fail("No such ogg file: " + s);
            } else {
               if (info.channels != this.channelCount) {
                  results.fail(
                     "Sound file '"
                        + s
                        + "' is "
                        + SoundFileValidators.getEncoding(info.channels)
                        + " instead of "
                        + SoundFileValidators.getEncoding(this.channelCount)
                  );
               }
            }
         }
      }

      @Override
      public void updateSchema(SchemaContext context, Schema target) {
      }
   }
}
