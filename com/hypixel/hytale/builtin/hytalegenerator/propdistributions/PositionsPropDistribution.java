package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PositionsPropDistribution extends PropDistribution {
   @Nonnull
   private final PositionProvider positionProvider;
   @Nonnull
   private final PositionProvider.Context rPositionProviderContext;
   @Nonnull
   private PropDistribution.Context rContext;
   @Nonnull
   private final Pipe.One<Vector3d> rPositionsPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Control control) {
         assert PositionsPropDistribution.this.rContext.bounds.contains(position);

         PositionsPropDistribution.this.rContext.pipe.accept(position, EmptyProp.INSTANCE, control);
      }
   };

   public PositionsPropDistribution(@Nonnull PositionProvider positionProvider) {
      this.positionProvider = positionProvider;
      this.rPositionProviderContext = new PositionProvider.Context();
      this.rContext = new PropDistribution.Context();
   }

   @Override
   public void distribute(@Nonnull PropDistribution.Context context) {
      this.rContext = context;
      this.rPositionProviderContext.assign(context);
      this.rPositionProviderContext.pipe = (position, control) -> {
         assert context.bounds.contains(position);

         context.pipe.accept(position, EmptyProp.INSTANCE, control);
      };
      this.positionProvider.generate(this.rPositionProviderContext);
   }

   @Override
   public void forEachPossibleProp(@NonNullDecl Consumer<Prop> consumer) {
   }
}
