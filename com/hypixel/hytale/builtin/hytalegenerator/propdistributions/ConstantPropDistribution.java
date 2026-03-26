package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ConstantPropDistribution extends PropDistribution {
   @Nonnull
   private final PositionProvider positionProvider;
   @Nonnull
   private final Prop prop;
   @Nonnull
   private final PositionProvider.Context rPositionProviderContext;
   @Nonnull
   private final Control rControl;
   @Nonnull
   private PropDistribution.Context rContext;
   @Nonnull
   private final Pipe.One<Vector3d> rPositionsPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Control control) {
         if (ConstantPropDistribution.this.rControl.stop) {
            control.stop = true;
         } else {
            ConstantPropDistribution.this.rContext.pipe.accept(position, ConstantPropDistribution.this.prop, ConstantPropDistribution.this.rControl);
         }
      }
   };

   public ConstantPropDistribution(@Nonnull PositionProvider positionProvider, @Nonnull Prop prop) {
      this.positionProvider = positionProvider;
      this.prop = prop;
      this.rPositionProviderContext = new PositionProvider.Context();
      this.rControl = new Control();
      this.rContext = new PropDistribution.Context();
   }

   @Override
   public void distribute(@NonNullDecl PropDistribution.Context context) {
      this.rContext = context;
      this.rControl.stop = false;
      this.rPositionProviderContext.bounds.min.assign(context.bounds.min);
      this.rPositionProviderContext.bounds.max.assign(context.bounds.max);
      this.rPositionProviderContext.pipe = this.rPositionsPipe;
      this.positionProvider.generate(this.rPositionProviderContext);
   }

   @Override
   public void forEachPossibleProp(@NonNullDecl Consumer<Prop> consumer) {
      consumer.accept(this.prop);
   }
}
