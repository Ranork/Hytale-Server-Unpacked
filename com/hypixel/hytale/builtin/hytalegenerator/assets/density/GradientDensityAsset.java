package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.GradientDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class GradientDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<GradientDensityAsset> CODEC = BuilderCodec.builder(
         GradientDensityAsset.class, GradientDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Axis", Vector3dUtil.CODEC, false), (t, k) -> t.axis = k, k -> k.axis)
      .addValidator(new Validator<Vector3d>() {
         public void accept(Vector3d v, ValidationResults r) {
            if (v.x == 0.0 && v.y == 0.0 && v.z == 0.0) {
               r.fail("Axis can't be zero.");
            }
         }

         @Override
         public void updateSchema(SchemaContext context, Schema target) {
         }
      })
      .add()
      .<Double>append(new KeyedCodec<>("SampleRange", Codec.DOUBLE, false), (t, k) -> t.sampleRange = k, t -> t.sampleRange)
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .build();
   private Vector3d axis = new Vector3d(0.0, 1.0, 0.0);
   private double sampleRange = 1.0;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped()
         ? new ConstantValueDensity(0.0)
         : new GradientDensity(this.buildFirstInput(argument), this.sampleRange, new Vector3d(this.axis)));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
