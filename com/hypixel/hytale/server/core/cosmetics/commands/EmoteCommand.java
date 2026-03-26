package com.hypixel.hytale.server.core.cosmetics.commands;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.cosmetics.Emote;
import com.hypixel.hytale.server.core.cosmetics.EmoteAsset;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class EmoteCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> emoteArg = this.withRequiredArg("emote", "server.commands.emote.emote.desc", ArgTypes.STRING);

   public EmoteCommand() {
      super("emote", "server.commands.emote.desc");
      this.setPermissionGroup(GameMode.Adventure);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      String emoteId = this.emoteArg.get(context);
      Map<String, Emote> builtinEmotes = CosmeticsModule.get().getRegistry().getEmotesInGame();
      IndexedLookupTableAssetMap<String, EmoteAsset> serverEmotes = EmoteAsset.getAssetMap();
      if (builtinEmotes.get(emoteId) == null && serverEmotes.getAsset(emoteId) == null) {
         context.sendMessage(Message.translation("server.commands.emote.emoteNotFound").param("id", emoteId));
         Set<Set<String>> allEmoteIdsSet = Set.of(builtinEmotes.keySet(), serverEmotes.getAssetMap().keySet());
         context.sendMessage(
            Message.translation("server.general.failed.didYouMean")
               .param("choices", StringUtil.sortByFuzzyDistance(emoteId, allEmoteIdsSet, CommandUtil.RECOMMEND_COUNT).toString())
         );
      } else {
         AnimationUtils.playAnimation(ref, AnimationSlot.Emote, null, emoteId, true, store);
      }
   }
}
