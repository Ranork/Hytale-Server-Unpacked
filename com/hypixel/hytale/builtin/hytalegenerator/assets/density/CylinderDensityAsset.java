package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.ConstantCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.CylinderDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.RotatorDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class CylinderDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<CylinderDensityAsset> CODEC = BuilderCodec.builder(
         CylinderDensityAsset.class, CylinderDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("RadialCurve", CurveAsset.CODEC, true), (t, k) -> t.radialCurveAsset = k, k -> k.radialCurveAsset)
      .add()
      .append(new KeyedCodec<>("AxialCurve", CurveAsset.CODEC, true), (t, k) -> t.axialCurveAsset = k, k -> k.axialCurveAsset)
      .add()
      .append(new KeyedCodec<>("NewYAxis", Vector3d.CODEC, false), (t, k) -> {
         if (k.length() != 0.0) {
            t.newYAxis = k;
         }
      }, k -> k.newYAxis)
      .add()
      .append(new KeyedCodec<>("Spin", Codec.DOUBLE, false), (t, k) -> t.spinAngle = k, k -> k.spinAngle)
      .add()
      .build();
   private CurveAsset radialCurveAsset = new ConstantCurveAsset();
   private CurveAsset axialCurveAsset = new ConstantCurveAsset();
   @Nonnull
   private Vector3d newYAxis = new Vector3d(0.0, 1.0, 0.0);
   private double spinAngle;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (!this.isSkipped() && this.axialCurveAsset != null && this.radialCurveAsset != null) {
         CylinderDensity cylinder = new CylinderDensity(this.radialCurveAsset.build(), this.axialCurveAsset.build());
         return new RotatorDensity(cylinder, this.newYAxis, this.spinAngle);
      } else {
         return new ConstantValueDensity(0.0);
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
      this.radialCurveAsset.cleanUp();
      this.axialCurveAsset.cleanUp();
   }
}
