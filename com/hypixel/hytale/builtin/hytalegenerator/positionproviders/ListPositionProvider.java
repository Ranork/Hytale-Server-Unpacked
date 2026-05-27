package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class ListPositionProvider extends PositionProvider {
   @Nonnull
   private final List<Vector3d> positions = new ArrayList<>();
   @Nonnull
   private final Control rControl;
   @Nonnull
   private final Vector3d rPosition;

   public ListPositionProvider(@Nonnull List<Vector3d> positions) {
      for (Vector3d p : positions) {
         this.positions.add(new Vector3d(p));
      }

      this.rControl = new Control();
      this.rPosition = new Vector3d();
   }

   @Override
   public void generate(@Nonnull PositionProvider.Context context) {
      this.rControl.reset();

      for (Vector3d position : this.positions) {
         if (this.rControl.stop) {
            return;
         }

         if (context.bounds.contains(position)) {
            this.rPosition.set(position);
            context.pipe.accept(this.rPosition, this.rControl);
         }
      }
   }
}
