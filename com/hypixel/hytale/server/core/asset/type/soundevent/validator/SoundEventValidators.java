package com.hypixel.hytale.server.core.asset.type.soundevent.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.asset.common.SoundFileValidators;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEventLayer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundEventValidators {
   public static final SoundEventValidators.LoopValidator LOOPING = new SoundEventValidators.LoopValidator(true);
   public static final SoundEventValidators.LoopValidator ONESHOT = new SoundEventValidators.LoopValidator(false);
   public static final SoundEventValidators.ChannelValidator MONO = new SoundEventValidators.ChannelValidator(1);
   public static final SoundEventValidators.ChannelValidator STEREO = new SoundEventValidators.ChannelValidator(2);
   public static final ValidatorCache<String> MONO_VALIDATOR_CACHE = new ValidatorCache<>(MONO);
   public static final ValidatorCache<String> STEREO_VALIDATOR_CACHE = new ValidatorCache<>(STEREO);
   public static final ValidatorCache<String> ONESHOT_VALIDATOR_CACHE = new ValidatorCache<>(ONESHOT);

   public static class ChannelValidator implements Validator<String> {
      private final int channelCount;

      public ChannelValidator(int channelCount) {
         assert channelCount == 1 || channelCount == 2;

         this.channelCount = channelCount;
      }

      public void accept(@Nullable String s, @Nonnull ValidationResults results) {
         if (s != null) {
            SoundEvent soundEvent = SoundEvent.getAssetMap().getAsset(s);
            if (soundEvent == null) {
               results.fail("Sound event with name '" + s + "' does not exist");
            } else {
               if (soundEvent.getHighestNumberOfChannels() != this.channelCount) {
                  results.fail(
                     "Sound event with name '"
                        + s
                        + "' is "
                        + SoundFileValidators.getEncoding(soundEvent.getHighestNumberOfChannels())
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

   public static class LoopValidator implements Validator<String> {
      private final boolean looping;

      private LoopValidator(boolean looping) {
         this.looping = looping;
      }

      public void accept(@Nullable String s, @Nonnull ValidationResults results) {
         if (s != null) {
            SoundEvent soundEvent = SoundEvent.getAssetMap().getAsset(s);
            if (soundEvent == null) {
               results.fail("Sound event with name '" + s + "' does not exist");
            } else if (soundEvent.getLayers() != null) {
               if (this.looping) {
                  for (SoundEventLayer layer : soundEvent.getLayers()) {
                     if (layer.isLooping()) {
                        return;
                     }
                  }

                  results.fail("Sound event with name '" + s + "' does not have a looping layer");
               } else {
                  for (SoundEventLayer layerx : soundEvent.getLayers()) {
                     if (layerx.isLooping()) {
                        results.fail("Sound event with name '" + s + "' has a looping layer and is not a oneshot sound");
                        return;
                     }
                  }
               }
            }
         }
      }

      @Override
      public void updateSchema(SchemaContext context, Schema target) {
      }
   }
}
