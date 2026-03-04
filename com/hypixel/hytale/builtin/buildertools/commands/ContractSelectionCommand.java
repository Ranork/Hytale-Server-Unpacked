package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class ContractSelectionCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<Integer> distanceArg = this.withRequiredArg("distance", "server.commands.contract.arg.distance.desc", ArgTypes.INTEGER);
   @Nonnull
   private final OptionalArg<List<Axis>> axisArg = this.withListOptionalArg(
      "axis", "server.commands.contract.arg.axis.desc", ArgTypes.forEnum("Axis", Axis.class)
   );

   public ContractSelectionCommand() {
      super("contractSelection", "server.commands.contract.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addAliases("contract");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         int distance = this.distanceArg.get(context);
         List<Vector3i> directions = new ObjectArrayList();
         if (this.axisArg.provided(context)) {
            for (Axis axis : this.axisArg.get(context)) {
               directions.add(axis.getDirection().scale(distance));
            }
         } else {
            directions.add(headRotationComponent.getAxisDirection().scale(distance));
         }

         for (Vector3i direction : directions) {
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.contract(r, direction, componentAccessor));
         }
      }
   }
}
