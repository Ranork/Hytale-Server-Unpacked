package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;

public enum VariantRotation implements NetworkSerializable<com.hypixel.hytale.protocol.VariantRotation> {
   None(
      com.hypixel.hytale.protocol.VariantRotation.None,
      RotationTuple.EMPTY_ARRAY,
      pair -> RotationTuple.NONE,
      (pair, rotation) -> RotationTuple.NONE,
      (pair, rotation) -> RotationTuple.NONE
   ),
   Wall(
      com.hypixel.hytale.protocol.VariantRotation.Wall,
      new RotationTuple[]{RotationTuple.of(Rotation.Ninety, Rotation.None)},
      pair -> pair.yaw() != Rotation.Ninety && pair.yaw() != Rotation.TwoSeventy
         ? RotationTuple.of(Rotation.None, Rotation.None)
         : RotationTuple.of(Rotation.Ninety, Rotation.None),
      (pair, rotation) -> pair,
      (pair, rotation) -> pair
   ),
   UpDown(
      com.hypixel.hytale.protocol.VariantRotation.UpDown,
      new RotationTuple[]{RotationTuple.of(Rotation.None, Rotation.OneEighty)},
      pair -> pair.pitch() == Rotation.OneEighty ? RotationTuple.of(Rotation.None, Rotation.OneEighty) : RotationTuple.of(Rotation.None, Rotation.None),
      (pair, rotation) -> RotationTuple.of(pair.yaw(), pair.pitch().add(rotation)),
      (pair, rotation) -> pair.pitch().add(rotation) == Rotation.OneEighty
         ? RotationTuple.of(pair.yaw(), Rotation.OneEighty)
         : RotationTuple.of(pair.yaw(), Rotation.None)
   ),
   Pipe(
      com.hypixel.hytale.protocol.VariantRotation.Pipe,
      new RotationTuple[]{RotationTuple.of(Rotation.None, Rotation.Ninety), RotationTuple.of(Rotation.Ninety, Rotation.Ninety)},
      pair -> pair.pitch() != Rotation.Ninety && pair.pitch() != Rotation.TwoSeventy
         ? RotationTuple.of(Rotation.None, validatePipe(pair.pitch()))
         : RotationTuple.of(validatePipe(pair.yaw()), validatePipe(pair.pitch())),
      (pair, rotation) -> RotationTuple.of(pair.yaw(), pair.pitch().add(rotation)),
      (pair, rotation) -> {
         if (pair.yaw() == Rotation.None && pair.pitch() == Rotation.Ninety) {
            return pair;
         } else {
            return switch (pair.yaw().add(rotation)) {
               case None, OneEighty -> RotationTuple.of(Rotation.None, Rotation.None);
               case Ninety, TwoSeventy -> RotationTuple.of(Rotation.Ninety, Rotation.Ninety);
            };
         }
      }
   ),
   DoublePipe(
      com.hypixel.hytale.protocol.VariantRotation.DoublePipe,
      new RotationTuple[]{
         RotationTuple.of(Rotation.None, Rotation.Ninety),
         RotationTuple.of(Rotation.Ninety, Rotation.Ninety),
         RotationTuple.of(Rotation.OneEighty, Rotation.Ninety),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety),
         RotationTuple.of(Rotation.None, Rotation.OneEighty)
      },
      pair -> {
         return switch (pair.pitch()) {
            case OneEighty -> RotationTuple.of(Rotation.None, Rotation.OneEighty);
            case Ninety -> pair;
            case TwoSeventy -> RotationTuple.of(pair.yaw().flip(), Rotation.Ninety);
            default -> RotationTuple.NONE;
         };
      },
      (pair, rotation) -> (pair.yaw() == Rotation.Ninety || pair.yaw() == Rotation.TwoSeventy) && pair.pitch() == Rotation.Ninety
         ? pair
         : RotationTuple.getRotation(
            new RotationTuple[]{
               RotationTuple.NONE,
               RotationTuple.of(Rotation.None, Rotation.Ninety),
               RotationTuple.of(Rotation.None, Rotation.OneEighty),
               RotationTuple.of(Rotation.OneEighty, Rotation.Ninety)
            },
            pair,
            rotation
         ),
      (pair, rotation) -> pair.yaw() != Rotation.None || pair.pitch() != Rotation.Ninety && pair.pitch() != Rotation.TwoSeventy
         ? RotationTuple.getRotation(
            new RotationTuple[]{
               RotationTuple.NONE,
               RotationTuple.of(Rotation.Ninety, Rotation.Ninety),
               RotationTuple.of(Rotation.None, Rotation.OneEighty),
               RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety)
            },
            pair,
            rotation
         )
         : pair
   ),
   NESW(
      com.hypixel.hytale.protocol.VariantRotation.NESW,
      new RotationTuple[]{
         RotationTuple.of(Rotation.Ninety, Rotation.None),
         RotationTuple.of(Rotation.OneEighty, Rotation.None),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.None)
      },
      pair -> RotationTuple.of(pair.yaw(), Rotation.None),
      (pair, rotation) -> pair,
      (pair, rotation) -> pair
   ),
   UpDownNESW(
      com.hypixel.hytale.protocol.VariantRotation.UpDownNESW,
      new RotationTuple[]{
         RotationTuple.of(Rotation.Ninety, Rotation.None),
         RotationTuple.of(Rotation.OneEighty, Rotation.None),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.None),
         RotationTuple.of(Rotation.None, Rotation.OneEighty),
         RotationTuple.of(Rotation.Ninety, Rotation.OneEighty),
         RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty)
      },
      pair -> pair.pitch() == Rotation.OneEighty ? RotationTuple.of(pair.yaw(), Rotation.OneEighty) : RotationTuple.of(pair.yaw(), Rotation.None),
      (pair, rotation) -> RotationTuple.of(pair.yaw(), pair.pitch().add(rotation)),
      (pair, rotation) -> pair.pitch().add(rotation) == Rotation.OneEighty ? RotationTuple.of(pair.yaw(), Rotation.OneEighty) : pair
   ),
   Debug(
      com.hypixel.hytale.protocol.VariantRotation.UpDownNESW,
      new RotationTuple[]{
         RotationTuple.of(Rotation.Ninety, Rotation.None),
         RotationTuple.of(Rotation.OneEighty, Rotation.None),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.None),
         RotationTuple.of(Rotation.None, Rotation.Ninety),
         RotationTuple.of(Rotation.Ninety, Rotation.Ninety),
         RotationTuple.of(Rotation.OneEighty, Rotation.Ninety),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety),
         RotationTuple.of(Rotation.None, Rotation.OneEighty),
         RotationTuple.of(Rotation.Ninety, Rotation.OneEighty),
         RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty),
         RotationTuple.of(Rotation.None, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.Ninety, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.OneEighty, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.TwoSeventy)
      },
      Function.identity(),
      (pair, rotation) -> RotationTuple.of(pair.yaw(), pair.pitch().add(rotation)),
      (pair, rotation) -> pair
   ),
   All(
      com.hypixel.hytale.protocol.VariantRotation.All,
      new RotationTuple[]{
         RotationTuple.of(Rotation.None, Rotation.None, Rotation.Ninety),
         RotationTuple.of(Rotation.None, Rotation.None, Rotation.OneEighty),
         RotationTuple.of(Rotation.None, Rotation.None, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.None, Rotation.Ninety, Rotation.None),
         RotationTuple.of(Rotation.None, Rotation.Ninety, Rotation.Ninety),
         RotationTuple.of(Rotation.None, Rotation.Ninety, Rotation.OneEighty),
         RotationTuple.of(Rotation.None, Rotation.Ninety, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.None, Rotation.OneEighty, Rotation.None),
         RotationTuple.of(Rotation.None, Rotation.OneEighty, Rotation.Ninety),
         RotationTuple.of(Rotation.None, Rotation.OneEighty, Rotation.OneEighty),
         RotationTuple.of(Rotation.None, Rotation.OneEighty, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.None, Rotation.TwoSeventy, Rotation.None),
         RotationTuple.of(Rotation.None, Rotation.TwoSeventy, Rotation.Ninety),
         RotationTuple.of(Rotation.None, Rotation.TwoSeventy, Rotation.OneEighty),
         RotationTuple.of(Rotation.None, Rotation.TwoSeventy, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.Ninety, Rotation.None, Rotation.None),
         RotationTuple.of(Rotation.Ninety, Rotation.None, Rotation.Ninety),
         RotationTuple.of(Rotation.Ninety, Rotation.None, Rotation.OneEighty),
         RotationTuple.of(Rotation.Ninety, Rotation.None, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.Ninety, Rotation.Ninety, Rotation.None),
         RotationTuple.of(Rotation.Ninety, Rotation.Ninety, Rotation.Ninety),
         RotationTuple.of(Rotation.Ninety, Rotation.Ninety, Rotation.OneEighty),
         RotationTuple.of(Rotation.Ninety, Rotation.Ninety, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.Ninety, Rotation.OneEighty, Rotation.None),
         RotationTuple.of(Rotation.Ninety, Rotation.OneEighty, Rotation.Ninety),
         RotationTuple.of(Rotation.Ninety, Rotation.OneEighty, Rotation.OneEighty),
         RotationTuple.of(Rotation.Ninety, Rotation.OneEighty, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.Ninety, Rotation.TwoSeventy, Rotation.None),
         RotationTuple.of(Rotation.Ninety, Rotation.TwoSeventy, Rotation.Ninety),
         RotationTuple.of(Rotation.Ninety, Rotation.TwoSeventy, Rotation.OneEighty),
         RotationTuple.of(Rotation.Ninety, Rotation.TwoSeventy, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.OneEighty, Rotation.None, Rotation.None),
         RotationTuple.of(Rotation.OneEighty, Rotation.None, Rotation.Ninety),
         RotationTuple.of(Rotation.OneEighty, Rotation.None, Rotation.OneEighty),
         RotationTuple.of(Rotation.OneEighty, Rotation.None, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.OneEighty, Rotation.Ninety, Rotation.None),
         RotationTuple.of(Rotation.OneEighty, Rotation.Ninety, Rotation.Ninety),
         RotationTuple.of(Rotation.OneEighty, Rotation.Ninety, Rotation.OneEighty),
         RotationTuple.of(Rotation.OneEighty, Rotation.Ninety, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty, Rotation.None),
         RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty, Rotation.Ninety),
         RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty, Rotation.OneEighty),
         RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.OneEighty, Rotation.TwoSeventy, Rotation.None),
         RotationTuple.of(Rotation.OneEighty, Rotation.TwoSeventy, Rotation.Ninety),
         RotationTuple.of(Rotation.OneEighty, Rotation.TwoSeventy, Rotation.OneEighty),
         RotationTuple.of(Rotation.OneEighty, Rotation.TwoSeventy, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.None, Rotation.None),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.None, Rotation.Ninety),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.None, Rotation.OneEighty),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.None, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety, Rotation.None),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety, Rotation.Ninety),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety, Rotation.OneEighty),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty, Rotation.None),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty, Rotation.Ninety),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty, Rotation.OneEighty),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty, Rotation.TwoSeventy),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.TwoSeventy, Rotation.None),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.TwoSeventy, Rotation.Ninety),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.TwoSeventy, Rotation.OneEighty),
         RotationTuple.of(Rotation.TwoSeventy, Rotation.TwoSeventy, Rotation.TwoSeventy)
      },
      Function.identity(),
      (pair, rotation) -> RotationTuple.of(pair.yaw(), pair.pitch().add(rotation)),
      (pair, rotation) -> pair
   );

   public static final VariantRotation[] EMPTY_ARRAY = new VariantRotation[0];
   private final com.hypixel.hytale.protocol.VariantRotation protocolType;
   private final RotationTuple[] rotations;
   private final Function<RotationTuple, RotationTuple> verify;
   private final BiFunction<RotationTuple, Rotation, RotationTuple> rotateX;
   private final BiFunction<RotationTuple, Rotation, RotationTuple> rotateZ;

   @Nonnull
   private static Rotation validatePipe(@Nonnull Rotation yaw) {
      return switch (yaw) {
         case None, Ninety -> yaw;
         case OneEighty -> Rotation.None;
         case TwoSeventy -> Rotation.Ninety;
      };
   }

   private VariantRotation(
      com.hypixel.hytale.protocol.VariantRotation protocolType,
      RotationTuple[] rotations,
      Function<RotationTuple, RotationTuple> verify,
      BiFunction<RotationTuple, Rotation, RotationTuple> rotateX,
      BiFunction<RotationTuple, Rotation, RotationTuple> rotateZ
   ) {
      this.protocolType = protocolType;
      this.rotations = rotations;
      this.verify = verify;
      this.rotateX = rotateX;
      this.rotateZ = rotateZ;
   }

   public RotationTuple[] getRotations() {
      return this.rotations;
   }

   public RotationTuple rotateX(RotationTuple pair, Rotation rotation) {
      return this.rotateX.apply(pair, rotation);
   }

   public RotationTuple rotateZ(RotationTuple pair, Rotation rotation) {
      return this.rotateZ.apply(pair, rotation);
   }

   public RotationTuple verify(RotationTuple pair) {
      return this.verify.apply(pair);
   }

   public com.hypixel.hytale.protocol.VariantRotation toPacket() {
      return this.protocolType;
   }
}
