package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum RelativeDirection {
   FORWARD,
   BACKWARD,
   LEFT,
   RIGHT,
   UP,
   DOWN;

   public static final SingleArgumentType<RelativeDirection> ARGUMENT_TYPE = new SingleArgumentType<RelativeDirection>(
      "Relative Direction",
      "A direction relative to the player (forward, backward, left, right, up, down)",
      "forward",
      "backward",
      "left",
      "right",
      "up",
      "down"
   ) {
      @Nullable
      public RelativeDirection parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         String var3 = input.toLowerCase();

         return switch (var3) {
            case "forward", "f" -> RelativeDirection.FORWARD;
            case "backward", "back", "b" -> RelativeDirection.BACKWARD;
            case "left", "l" -> RelativeDirection.LEFT;
            case "right", "r" -> RelativeDirection.RIGHT;
            case "up", "u" -> RelativeDirection.UP;
            case "down", "d" -> RelativeDirection.DOWN;
            default -> {
               parseResult.fail(Message.raw("Invalid direction: " + input + ". Use: forward, backward, left, right, up, down"));
               yield null;
            }
         };
      }
   };

   @Nonnull
   public static Vector3i toDirectionVector(@Nullable RelativeDirection direction, @Nonnull HeadRotation headRotation) {
      if (direction == null) {
         return headRotation.getAxisDirection();
      } else {
         return switch (direction) {
            case FORWARD -> headRotation.getHorizontalAxisDirection();
            case BACKWARD -> headRotation.getHorizontalAxisDirection().clone().scale(-1);
            case LEFT -> rotateLeft(headRotation.getHorizontalAxisDirection());
            case RIGHT -> rotateRight(headRotation.getHorizontalAxisDirection());
            case UP -> new Vector3i(0, 1, 0);
            case DOWN -> new Vector3i(0, -1, 0);
         };
      }
   }

   @Nonnull
   public static Axis toAxis(@Nonnull RelativeDirection direction, @Nonnull HeadRotation headRotation) {
      return switch (direction) {
         case FORWARD, BACKWARD -> getHorizontalAxis(headRotation);
         case LEFT, RIGHT -> getPerpendicularHorizontalAxis(headRotation);
         case UP, DOWN -> Axis.Y;
      };
   }

   @Nonnull
   private static Axis getHorizontalAxis(@Nonnull HeadRotation headRotation) {
      Vector3i horizontalDir = headRotation.getHorizontalAxisDirection();
      return horizontalDir.getX() != 0 ? Axis.X : Axis.Z;
   }

   @Nonnull
   private static Axis getPerpendicularHorizontalAxis(@Nonnull HeadRotation headRotation) {
      Vector3i horizontalDir = headRotation.getHorizontalAxisDirection();
      return horizontalDir.getX() != 0 ? Axis.Z : Axis.X;
   }

   @Nonnull
   private static Vector3i rotateLeft(@Nonnull Vector3i dir) {
      return new Vector3i(dir.z, 0, -dir.x);
   }

   @Nonnull
   private static Vector3i rotateRight(@Nonnull Vector3i dir) {
      return new Vector3i(-dir.z, 0, dir.x);
   }
}
