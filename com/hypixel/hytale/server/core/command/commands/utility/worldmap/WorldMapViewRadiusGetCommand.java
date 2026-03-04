package com.hypixel.hytale.server.core.command.commands.utility.worldmap;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldMapViewRadiusGetCommand extends AbstractTargetPlayerCommand {
   public WorldMapViewRadiusGetCommand() {
      super("get", "server.commands.worldmap.viewradius.get.desc");
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

      WorldMapTracker worldMapTracker = playerComponent.getWorldMapTracker();
      Integer override = worldMapTracker.getViewRadiusOverride();
      int effectiveRadius = worldMapTracker.getEffectiveViewRadius(world);
      if (override != null) {
         context.sendMessage(
            Message.translation("server.commands.worldmap.viewradius.get.withOverride").param("radius", effectiveRadius).param("override", override)
         );
      } else {
         context.sendMessage(Message.translation("server.commands.worldmap.viewradius.get.noOverride").param("radius", effectiveRadius));
      }
   }
}
