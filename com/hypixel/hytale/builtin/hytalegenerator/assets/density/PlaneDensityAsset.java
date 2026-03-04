package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.ConstantCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.PlaneDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class PlaneDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<PlaneDensityAsset> CODEC = BuilderCodec.builder(
         PlaneDensityAsset.class, PlaneDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Curve", CurveAsset.CODEC, true), (t, k) -> t.distanceCurveAsset = k, k -> k.distanceCurveAsset)
      .add()
      .append(new KeyedCodec<>("IsAnchored", Codec.BOOLEAN, false), (t, k) -> t.isAnchored = k, k -> k.isAnchored)
      .add()
      .<Vector3d>append(new KeyedCodec<>("PlaneNormal", Vector3d.CODEC, false), (t, k) -> t.planeNormal = k, k -> k.planeNormal)
      .addValidator((LegacyValidator<? super Vector3d>)((v, r) -> {
         if (v.length() == 0.0) {
            r.fail("Plane normal can't be a zero vector.");
         }
      }))
      .add()
      .build();
   private CurveAsset distanceCurveAsset = new ConstantCurveAsset();
   private Vector3d planeNormal = new Vector3d(0.0, 1.0, 0.0);
   private boolean isAnchored;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(!this.isSkipped() && this.distanceCurveAsset != null
         ? new PlaneDensity(this.distanceCurveAsset.build(), this.planeNormal, this.isAnchored)
         : new ConstantValueDensity(0.0));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
      this.distanceCurveAsset.cleanUp();
   }
}
