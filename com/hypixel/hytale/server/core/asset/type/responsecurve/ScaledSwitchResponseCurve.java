package com.hypixel.hytale.server.core.asset.type.responsecurve;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class ScaledSwitchResponseCurve extends ScaledResponseCurve {
   public static final BuilderCodec<ScaledSwitchResponseCurve> CODEC = BuilderCodec.builder(ScaledSwitchResponseCurve.class, ScaledSwitchResponseCurve::new)
      .documentation(
         "A special type of scaled response curve which returns the initial state value before the defined switch point and the final state value after reaching it."
      )
      .<Double>append(new KeyedCodec<>("InitialState", Codec.DOUBLE), (curve, d) -> curve.initialState = d, curve -> curve.initialState)
      .addValidator(Validators.range(0.0, 1.0))
      .documentation("The y value to return before the switch point.")
      .add()
      .<Double>append(new KeyedCodec<>("FinalState", Codec.DOUBLE), (curve, d) -> curve.finalState = d, curve -> curve.finalState)
      .addValidator(Validators.range(0.0, 1.0))
      .documentation("The y value to return at and beyond the switch point.")
      .add()
      .<Double>append(new KeyedCodec<>("SwitchPoint", Codec.DOUBLE), (curve, d) -> curve.switchPoint = d, curve -> curve.switchPoint)
      .addValidator(Validators.nonNull())
      .documentation("The value at which to switch from the initial state to the final state.")
      .add()
      .build();
   protected double initialState = 0.0;
   protected double finalState = 1.0;
   protected double switchPoint;

   protected ScaledSwitchResponseCurve() {
   }

   @Override
   public double computeY(double x) {
      return x < this.switchPoint ? this.initialState : this.finalState;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ScaledSwitchResponseCurve{initialState="
         + this.initialState
         + ", finalState="
         + this.finalState
         + ", switchPoint="
         + this.switchPoint
         + "}"
         + super.toString();
   }
}
