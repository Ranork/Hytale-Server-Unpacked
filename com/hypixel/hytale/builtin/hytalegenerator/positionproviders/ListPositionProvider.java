package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class ListPositionProvider extends PositionProvider {
   private List<Vector3i> positions3i;
   private List<Vector3d> positions3d;

   private ListPositionProvider() {
   }

   @Nonnull
   public static ListPositionProvider from3i(@Nonnull List<Vector3i> positions3i) {
      ListPositionProvider instance = new ListPositionProvider();
      instance.positions3i = new ArrayList<>();
      instance.positions3i.addAll(positions3i);
      instance.positions3d = new ArrayList<>(positions3i.size());
      instance.positions3i.forEach(p -> instance.positions3d.add(p.toVector3d()));
      return instance;
   }

   @Nonnull
   public static ListPositionProvider from3d(@Nonnull List<Vector3d> positions3d) {
      ListPositionProvider instance = new ListPositionProvider();
      instance.positions3d = new ArrayList<>();
      instance.positions3d.addAll(positions3d);
      instance.positions3i = new ArrayList<>(positions3d.size());
      instance.positions3d.forEach(p -> instance.positions3i.add(p.toVector3i()));
      return instance;
   }

   @Override
   public void positionsIn(@Nonnull PositionProvider.Context context) {
      for (Vector3d p : this.positions3d) {
         if (VectorUtil.isInside(p, context.minInclusive, context.maxExclusive)) {
         }

         context.consumer.accept(p);
      }
   }
}
