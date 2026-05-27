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
   public static final SoundFileValidators.SampleRateValidator MUSIC_SAMPLE_RATE = new SoundFileValidators.SampleRateValidator(48000, 44100);
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

   public static class SampleRateValidator implements Validator<String> {
      private final int[] allowedHz;

      public SampleRateValidator(int... allowedHz) {
         assert allowedHz.length > 0;

         this.allowedHz = allowedHz;
      }

      public void accept(@Nullable String s, @Nonnull ValidationResults results) {
         if (s != null) {
            OggVorbisInfoCache.OggVorbisInfo info = OggVorbisInfoCache.getNow(s);
            if (info == null) {
               results.fail("No such ogg file: " + s);
            } else {
               for (int hz : this.allowedHz) {
                  if (info.sampleRate == hz) {
                     return;
                  }
               }

               StringBuilder sb = new StringBuilder();

               for (int i = 0; i < this.allowedHz.length; i++) {
                  if (i > 0) {
                     sb.append(i == this.allowedHz.length - 1 ? " or " : ", ");
                  }

                  sb.append(this.allowedHz[i]);
               }

               results.fail("Sound file '" + s + "' has sample rate " + info.sampleRate + " Hz; must be " + sb + " Hz");
            }
         }
      }

      @Override
      public void updateSchema(SchemaContext context, Schema target) {
      }
   }
}
