package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.Pair;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class LayerCommand extends AbstractPlayerCommand {
   private static Map<String, Vector3i> directions = Map.of(
      "up", Vector3i.UP, "down", Vector3i.DOWN, "north", Vector3i.NORTH, "south", Vector3i.SOUTH, "east", Vector3i.EAST, "west", Vector3i.WEST
   );
   @Nonnull
   private final RequiredArg<String> layerDirectionArg = this.withRequiredArg("direction", "server.commands.layer.direction.desc", ArgTypes.STRING);
   @Nonnull
   private final RequiredArg<List<Pair<Integer, String>>> layersArg = this.withListRequiredArg(
      "layers", "server.commands.layer.layers.desc", ArgTypes.LAYER_ENTRY_TYPE
   );

   public LayerCommand() {
      super("layer", "server.commands.layer.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.requirePermission("hytale.editor.selection.clipboard");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         String direction = this.layerDirectionArg.get(context).toLowerCase();
         List<Pair<Integer, String>> layers = this.layersArg.get(context);
         if (layers != null && direction != null) {
            boolean directionValid = directions.containsKey(direction);
            if (directionValid) {
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
                  HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

                  assert headRotationComponent != null;

                  Vector3i layerDirection = Vector3i.ZERO;
                  if (direction.equalsIgnoreCase("camera")) {
                     layerDirection = headRotationComponent.getAxisDirection();
                  } else {
                     layerDirection = directions.get(direction);
                  }

                  s.layer(layers, layerDirection, componentAccessor);
               });
            } else {
               context.sendMessage(Message.translation("server.commands.layer.directionInvalid").param("direction", direction));
               context.sendMessage(Message.translation("server.commands.help.useHelpToLearnMore").param("command", this.getFullyQualifiedName()));
            }
         } else {
            context.sendMessage(Message.translation("server.commands.help.useHelpToLearnMore").param("command", this.getFullyQualifiedName()));
         }
      }
   }
}
