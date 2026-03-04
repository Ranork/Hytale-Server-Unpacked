package com.hypixel.hytale.builtin.ambience.commands;

import com.hypixel.hytale.builtin.ambience.resources.AmbienceResource;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFX;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class AmbienceSetMusicCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<AmbienceFX> ambienceFxIdArg = this.withRequiredArg(
      "ambienceFxId", "server.commands.ambience.setmusic.arg.ambiencefxid.desc", ArgTypes.AMBIENCE_FX_ASSET
   );

   public AmbienceSetMusicCommand() {
      super("setmusic", "server.commands.ambience.setmusic.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      AmbienceFX ambienceFX = this.ambienceFxIdArg.get(context);
      AmbienceResource ambienceResource = store.getResource(AmbienceResource.getResourceType());
      ambienceResource.setForcedMusicAmbience(ambienceFX.getId());
      context.sendMessage(Message.translation("server.commands.ambience.setmusic.success").param("ambience", ambienceFX.getId()));
   }
}
