package com.hypixel.hytale.builtin.portals.commands.voidevent;

import com.hypixel.hytale.builtin.portals.integrations.PortalGameplayConfig;
import com.hypixel.hytale.builtin.portals.integrations.PortalRemovalCondition;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.portalworld.PortalType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class StartVoidEventCommand extends AbstractWorldCommand {
   @Nonnull
   private final FlagArg overrideWorld = this.withFlagArg("override", "server.commands.voidevent.start.overrideArg");
   @Nonnull
   private static final String HARDCODED_GAMEPLAY_CONFIG = "Portal";
   @Nonnull
   private static final String HARDCODED_PORTAL_TYPE = "Hederas_Lair";

   public StartVoidEventCommand() {
      super("start", "server.commands.voidevent.start.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      PortalWorld portalWorld = store.getResource(PortalWorld.getResourceType());
      if (portalWorld.exists() && portalWorld.isVoidEventActive()) {
         context.sendMessage(Message.translation("server.commands.voidevent.start.alreadyRunning"));
      } else {
         if (!portalWorld.exists()) {
            if (!this.overrideWorld.get(context)) {
               context.sendMessage(Message.translation("server.commands.portals.notInPortal"));
               return;
            }

            GameplayConfig gameplayConfig = GameplayConfig.getAssetMap().getAsset("Portal");
            PortalGameplayConfig portalGameplayConfig = gameplayConfig == null ? null : gameplayConfig.getPluginConfig().get(PortalGameplayConfig.class);
            if (portalGameplayConfig == null) {
               context.sendMessage(Message.translation("server.commands.voidevent.start.botchedConfig").param("config", "Portal"));
               return;
            }

            world.getWorldConfig().setGameplayConfig("Portal");
            portalWorld.init(PortalType.getAssetMap().getAsset("Hederas_Lair"), 10000, new PortalRemovalCondition(10000.0), portalGameplayConfig);
            portalWorld.setSpawnPoint(new Transform(0.0, 100.0, 0.0));
            context.sendMessage(Message.translation("server.commands.voidevent.start.overrode"));
         }

         PortalWorld.setRemainingSeconds(world, 1.0);
         context.sendMessage(Message.translation("server.commands.voidevent.start.success"));
      }
   }
}
