package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class ListPositionProvider extends PositionProvider {
   private List<Vector3d> positions = new ArrayList<>();
   @Nonnull
   private final Control rControl;

   public ListPositionProvider(@Nonnull List<Vector3d> positions) {
      positions.forEach(p -> this.positions.add(p.clone()));
      this.rControl = new Control();
   }

   @Override
   public void generate(@Nonnull PositionProvider.Context context) {
      this.rControl.reset();

      for (Vector3d p : this.positions) {
         if (this.rControl.stop) {
            return;
         }

         if (context.bounds.contains(p)) {
            context.pipe.accept(p, this.rControl);
         }
      }
   }
}
