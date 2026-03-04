package com.hypixel.hytale.server.core.command.commands.player.viewradius;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.setup.ViewRadius;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerViewRadiusSetCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private final RequiredArg<String> radiusArg = this.withRequiredArg("radius", "server.commands.player.viewradius.set.radius.desc", ArgTypes.STRING);
   @Nonnull
   private final FlagArg blocksArg = this.withFlagArg("blocks", "server.commands.player.viewradius.set.blocks.desc");
   @Nonnull
   private final FlagArg bypassArg = this.withFlagArg("bypass", "server.commands.player.viewradius.set.bypass.desc");

   public PlayerViewRadiusSetCommand() {
      super("set", "server.commands.player.viewradius.set.desc");
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

      EntityTrackerSystems.EntityViewer entityViewerComponent = store.getComponent(ref, EntityTrackerSystems.EntityViewer.getComponentType());

      assert entityViewerComponent != null;

      String radiusInput = this.radiusArg.get(context);
      boolean measureInBlocks = this.blocksArg.get(context);
      boolean bypass = this.bypassArg.get(context);
      int viewRadiusChunks;
      if ("default".equalsIgnoreCase(radiusInput)) {
         viewRadiusChunks = 32;
      } else {
         try {
            int value = Integer.parseInt(radiusInput);
            viewRadiusChunks = measureInBlocks ? (int)Math.ceil(value / 32.0F) : value;
         } catch (NumberFormatException var15) {
            context.sendMessage(Message.translation("server.commands.player.viewradius.set.invalidNumber").param("value", radiusInput));
            return;
         }
      }

      int maxViewRadius = HytaleServer.get().getConfig().getMaxViewRadius();
      if (viewRadiusChunks > maxViewRadius && !bypass) {
         context.sendMessage(Message.translation("server.commands.player.viewradius.set.noHigherThan").param("radius", maxViewRadius));
      } else {
         int viewRadiusBlocks = viewRadiusChunks * 32;
         playerComponent.setClientViewRadius(viewRadiusChunks);
         entityViewerComponent.viewRadiusBlocks = viewRadiusBlocks;
         playerRef.getPacketHandler().writeNoCache(new ViewRadius(viewRadiusBlocks));
         context.sendMessage(
            Message.translation("server.commands.player.viewradius.set.success").param("radius", viewRadiusChunks).param("radiusBlocks", viewRadiusBlocks)
         );
      }
   }
}
