package com.hypixel.hytale.builtin.adventure.camera.asset.cameraeffect;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.AccumulationMode;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShakeIntensity {
   @Nonnull
   public static final BuilderCodec<ShakeIntensity> CODEC = BuilderCodec.builder(ShakeIntensity.class, ShakeIntensity::new)
      .appendInherited(
         new KeyedCodec<>("Value", Codec.FLOAT),
         (cameraShakeEffect, s) -> cameraShakeEffect.value = s,
         cameraShakeEffect -> cameraShakeEffect.value,
         (cameraShakeEffect, parent) -> cameraShakeEffect.value = parent.value
      )
      .documentation("The intensity used when no contextual value (such as damage) is present.")
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .<AccumulationMode>appendInherited(
         new KeyedCodec<>("AccumulationMode", ProtocolCodecs.ACCUMULATION_MODE_CODEC),
         (intensity, mode) -> intensity.accumulationMode = mode,
         intensity -> intensity.accumulationMode,
         (intensity, parent) -> intensity.accumulationMode = parent.accumulationMode
      )
      .documentation("The method by which intensity is combined when multiple instances of the same camera effect overlap.")
      .addValidator(Validators.nonNull())
      .add()
      .<ShakeIntensity.Modifier>appendInherited(
         new KeyedCodec<>("Modifier", ShakeIntensity.Modifier.CODEC),
         (intensity, modifier) -> intensity.modifier = modifier,
         intensity -> intensity.modifier,
         (intensity, parent) -> intensity.modifier = parent.modifier
      )
      .documentation("Converts a contextual-intensity value (such as damage) to a camera shake intensity value.")
      .add()
      .build();
   @Nonnull
   static final AccumulationMode DEFAULT_ACCUMULATION_MODE = AccumulationMode.Set;
   public static final float DEFAULT_CONTEXT_VALUE = 0.0F;
   protected float value = 0.0F;
   @Nonnull
   protected AccumulationMode accumulationMode = DEFAULT_ACCUMULATION_MODE;
   @Nullable
   protected ShakeIntensity.Modifier modifier;

   public float getValue() {
      return this.value;
   }

   @Nonnull
   public AccumulationMode getAccumulationMode() {
      return this.accumulationMode;
   }

   @Nullable
   public ShakeIntensity.Modifier getModifier() {
      return this.modifier;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ShakeIntensity{value=" + this.value + ", accumulationMode=" + this.accumulationMode + ", modifier=" + this.modifier + "}";
   }

   public static class Modifier implements FloatUnaryOperator {
      @Nonnull
      public static final BuilderCodec<ShakeIntensity.Modifier> CODEC = BuilderCodec.builder(ShakeIntensity.Modifier.class, ShakeIntensity.Modifier::new)
         .appendInherited(
            new KeyedCodec<>("Input", Codec.FLOAT_ARRAY),
            (modifier, v) -> modifier.input = v,
            modifier -> modifier.input,
            (modifier, parent) -> modifier.input = parent.input
         )
         .addValidator(Validators.nonEmptyFloatArray())
         .addValidator(Validators.nonNull())
         .add()
         .<float[]>appendInherited(
            new KeyedCodec<>("Output", Codec.FLOAT_ARRAY),
            (modifier, v) -> modifier.output = v,
            modifier -> modifier.output,
            (modifier, parent) -> modifier.output = parent.output
         )
         .addValidator(Validators.nonEmptyFloatArray())
         .addValidator(Validators.nonNull())
         .add()
         .build();
      private float[] input;
      private float[] output;

      public float apply(float intensityContext) {
         float inputMin = this.input[0];
         float outputMin = this.output[0];
         if (intensityContext < inputMin) {
            return outputMin;
         } else {
            int length = Math.min(this.input.length, this.output.length);

            for (int i = 1; i < length; i++) {
               float inputMax = this.input[i];
               float outputMax = this.output[i];
               if (!(intensityContext > inputMax)) {
                  return MathUtil.mapToRange(intensityContext, inputMin, inputMax, outputMin, outputMax);
               }

               inputMin = inputMax;
               outputMin = outputMax;
            }

            return outputMin;
         }
      }

      @Nonnull
      @Override
      public String toString() {
         return "Modifier{input=" + Arrays.toString(this.input) + ", output=" + Arrays.toString(this.output) + "}";
      }
   }
}
