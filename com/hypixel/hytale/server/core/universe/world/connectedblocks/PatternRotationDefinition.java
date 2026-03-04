package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class PatternRotationDefinition {
   public static final BuilderCodec<PatternRotationDefinition> CODEC = BuilderCodec.builder(PatternRotationDefinition.class, PatternRotationDefinition::new)
      .append(
         new KeyedCodec<>("IsCardinallyRotatable", Codec.BOOLEAN, false),
         (o, isCardinallyRotatable) -> o.isCardinallyRotatable = isCardinallyRotatable,
         o -> o.isCardinallyRotatable
      )
      .add()
      .append(new KeyedCodec<>("MirrorZ", Codec.BOOLEAN, false), (o, isMirrorZ) -> o.isMirrorZ = isMirrorZ, o -> o.isMirrorZ)
      .add()
      .append(new KeyedCodec<>("MirrorX", Codec.BOOLEAN, false), (o, isMirrorX) -> o.isMirrorX = isMirrorX, o -> o.isMirrorX)
      .add()
      .build();
   @Nonnull
   public static PatternRotationDefinition DEFAULT = new PatternRotationDefinition();
   private boolean isCardinallyRotatable;
   private boolean isMirrorZ;
   private boolean isMirrorX;
   public static final List<Pair<Rotation, PatternRotationDefinition.MirrorAxis>> ROTATIONS = new ArrayList<>();

   @Nonnull
   public List<Pair<Rotation, PatternRotationDefinition.MirrorAxis>> getRotations() {
      return new AbstractList<Pair<Rotation, PatternRotationDefinition.MirrorAxis>>() {
         private final int[] enabledIndexes = this.computeEnabled();

         private int[] computeEnabled() {
            IntList idx = new IntArrayList();
            idx.add(0);
            if (PatternRotationDefinition.this.isCardinallyRotatable) {
               idx.addAll(IntList.of(1, 2, 3));
            }

            if (PatternRotationDefinition.this.isMirrorX) {
               idx.add(4);
               if (PatternRotationDefinition.this.isCardinallyRotatable) {
                  idx.addAll(IntList.of(5, 6, 7));
               }
            }

            if (PatternRotationDefinition.this.isMirrorZ) {
               idx.add(8);
               if (PatternRotationDefinition.this.isCardinallyRotatable) {
                  idx.addAll(IntList.of(9, 10, 11));
               }
            }

            return idx.toIntArray();
         }

         public Pair<Rotation, PatternRotationDefinition.MirrorAxis> get(int i) {
            return PatternRotationDefinition.ROTATIONS.get(this.enabledIndexes[i]);
         }

         @Override
         public int size() {
            return this.enabledIndexes.length;
         }
      };
   }

   static {
      for (PatternRotationDefinition.MirrorAxis mirrorAxis : PatternRotationDefinition.MirrorAxis.values()) {
         for (Rotation value : Rotation.VALUES) {
            ROTATIONS.add(Pair.of(value, mirrorAxis));
         }
      }
   }

   public static enum MirrorAxis {
      NONE,
      X,
      Z;
   }
}
