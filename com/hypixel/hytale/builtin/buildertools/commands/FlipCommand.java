package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeDirection;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlipCommand extends AbstractPlayerCommand {
   static final SingleArgumentType<String> FLIP_TARGET_TYPE = new SingleArgumentType<String>(
      "server.commands.flip.direction.desc", "server.commands.flip.direction.desc", "forward", "backward", "left", "right", "up", "down", "X", "Y", "Z"
   ) {
      @Nullable
      public String parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         String var3 = input.toLowerCase();

         return switch (var3) {
            case "forward", "f", "backward", "back", "b", "left", "l", "right", "r", "up", "u", "down", "d", "x", "y", "z" -> input.toLowerCase();
            default -> {
               parseResult.fail(Message.translation("server.commands.flip.direction.invalid").param("input", input));
               yield null;
            }
         };
      }
   };

   public FlipCommand() {
      super("flip", "server.commands.flip.desc");
      this.setPermissionGroups("hytale:WorldEditor");
      this.addUsageVariant(new FlipCommand.FlipVariant());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      executeFlip(store, ref, playerRef, null);
   }

   @Nullable
   static Axis resolveTarget(@Nonnull String target, @Nullable HeadRotation headRotation) {
      return switch (target) {
         case "x" -> Axis.X;
         case "y" -> Axis.Y;
         case "z" -> Axis.Z;
         default -> {
            if (headRotation == null) {
               yield null;
            } else {
               RelativeDirection direction = switch (target) {
                  case "f", "forward" -> RelativeDirection.FORWARD;
                  case "b", "back", "backward" -> RelativeDirection.BACKWARD;
                  case "l", "left" -> RelativeDirection.LEFT;
                  case "r", "right" -> RelativeDirection.RIGHT;
                  case "u", "up" -> RelativeDirection.UP;
                  default -> RelativeDirection.DOWN;
               };
               yield RelativeDirection.toAxis(direction, headRotation);
            }
         }
      };
   }

   static void executeFlip(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nullable String target) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerRef, store)) {
         HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
         Axis axis;
         if (target == null) {
            if (headRotation == null) {
               return;
            }

            axis = headRotation.getAxis();
         } else {
            axis = resolveTarget(target, headRotation);
            if (axis == null) {
               return;
            }
         }

         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.flip(r, axis, componentAccessor));
      }
   }

   private static class FlipVariant extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> targetArg = this.withRequiredArg("direction", "server.commands.flip.direction.desc", FlipCommand.FLIP_TARGET_TYPE);

      public FlipVariant() {
         super("server.commands.flip.desc");
         this.setPermissionGroups("hytale:WorldEditor");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         FlipCommand.executeFlip(store, ref, playerRef, this.targetArg.get(context));
      }
   }
}
