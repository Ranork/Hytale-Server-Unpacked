package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.builtin.path.waypoint.RelativeWaypointDefinition;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.ArrayDeque;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCPathCommand extends AbstractCommandCollection {
   public NPCPathCommand() {
      super("path", "server.commands.npc.path.desc");
      this.addSubCommand(new NPCPathCommand.SetPathCommand());
      this.addSubCommand(new NPCPathCommand.PolygonPathCommand());
   }

   public static class PolygonPathCommand extends NPCWorldCommandBase {
      @Nonnull
      private final RequiredArg<Integer> sidesArg = this.withRequiredArg("sides", "server.commands.npc.path.polygon.sides.desc", ArgTypes.INTEGER)
         .addValidator(Validators.greaterThan(0));
      @Nonnull
      private final OptionalArg<Double> lengthArg = this.withOptionalArg("length", "server.commands.npc.path.length.desc", ArgTypes.DOUBLE)
         .addValidator(Validators.greaterThan(0.0));

      public PolygonPathCommand() {
         super("polygon", "server.commands.npc.path.polygon.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull NPCEntity npc, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref
      ) {
         ArrayDeque<RelativeWaypointDefinition> instructions = new ArrayDeque<>();
         Integer sides = this.sidesArg.get(context);
         float angle = (float) (Math.PI * 2) / sides.intValue();
         double length = this.lengthArg.provided(context) ? this.lengthArg.get(context) : 5.0;

         for (int i = 0; i < sides; i++) {
            instructions.add(new RelativeWaypointDefinition(angle, length));
         }

         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3d position = transformComponent.getPosition();
         Vector3f headRotation = headRotationComponent.getRotation();
         npc.getPathManager().setTransientPath(TransientPath.buildPath(position, headRotation, instructions, 1.0));
      }
   }

   public static class SetPathCommand extends NPCWorldCommandBase {
      @Nonnull
      private final RequiredArg<String> instructionsArg = this.withRequiredArg("instructions", "server.commands.npc.path.instructions.desc", ArgTypes.STRING);

      public SetPathCommand() {
         super("", "server.commands.npc.path.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull NPCEntity npc, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref
      ) {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         String instructionsString = this.instructionsArg.get(context);
         ArrayDeque<RelativeWaypointDefinition> instructions = this.parseInstructions(context, instructionsString);
         if (instructions != null) {
            npc.getPathManager()
               .setTransientPath(TransientPath.buildPath(transformComponent.getPosition(), headRotationComponent.getRotation(), instructions, 1.0));
         }
      }

      @Nullable
      private ArrayDeque<RelativeWaypointDefinition> parseInstructions(@Nonnull CommandContext context, @Nonnull String str) {
         ArrayDeque<RelativeWaypointDefinition> instructions = new ArrayDeque<>();
         String[] parts = str.split(",");
         int index = 0;

         try {
            while (index < parts.length) {
               float rotation = Float.parseFloat(parts[index++]) * (float) (Math.PI / 180.0);
               double distance = Double.parseDouble(parts[index++]);
               instructions.add(new RelativeWaypointDefinition(rotation, distance));
            }

            return instructions;
         } catch (NumberFormatException var9) {
            context.sendMessage(Message.raw("Invalid number format: " + var9.getMessage()));
            return null;
         } catch (IndexOutOfBoundsException var10) {
            context.sendMessage(Message.raw("Instructions must be defined in pairs! Missing distance value."));
            return null;
         }
      }
   }
}
