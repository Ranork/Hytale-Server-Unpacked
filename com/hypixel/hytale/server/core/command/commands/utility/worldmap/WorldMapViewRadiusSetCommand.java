package com.hypixel.hytale.server.core.command.commands.utility.worldmap;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldMapViewRadiusSetCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private final RequiredArg<Integer> radiusArg = this.withRequiredArg("radius", "server.commands.worldmap.viewradius.set.radius.desc", ArgTypes.INTEGER);
   @Nonnull
   private final FlagArg bypassArg = this.withFlagArg("bypass", "server.commands.worldmap.viewradius.set.bypass.desc")
      .setPermission("server.commands.worldmap.viewradius.set.bypass");

   public WorldMapViewRadiusSetCommand() {
      super("set", "server.commands.worldmap.viewradius.set.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      int viewRadius = this.radiusArg.get(context);
      boolean bypass = this.bypassArg.get(context);
      if (viewRadius < 0) {
         context.sendMessage(Message.translation("server.commands.worldmap.viewradius.set.mustBePositive"));
      } else if (viewRadius > 512 && !bypass) {
         context.sendMessage(Message.translation("server.commands.worldmap.viewradius.set.noHigherThan").param("radius", 512));
      } else {
         playerComponent.getWorldMapTracker().setViewRadiusOverride(viewRadius);
         context.sendMessage(Message.translation("server.commands.worldmap.viewradius.set.success").param("radius", viewRadius));
      }
   }
}
