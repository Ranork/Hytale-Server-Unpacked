package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.AssetMapWithIndexes;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class AssetTagsCommand extends CommandBase {
   @Nonnull
   private final RequiredArg<String> classArg = this.withRequiredArg("class", "server.commands.assets.tags.class.desc", ArgTypes.STRING);
   @Nonnull
   private final RequiredArg<String> tagArg = this.withRequiredArg("tag", "server.commands.assets.tags.tag.desc", ArgTypes.STRING);

   public AssetTagsCommand() {
      super("tags", "server.commands.assets.tags.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String assetClass = this.classArg.get(context);
      String tag = this.tagArg.get(context);
      int tagIndex = AssetRegistry.getTagIndex(tag);
      if (tagIndex == Integer.MIN_VALUE) {
         context.sendMessage(Message.translation("server.commands.assets.tags.tagNotFound").param("tag", tag));
      } else {
         for (Entry<Class<? extends JsonAssetWithMap>, AssetStore<?, ?, ?>> entry : AssetRegistry.getStoreMap().entrySet()) {
            String simpleName = entry.getKey().getSimpleName();
            if (simpleName.equalsIgnoreCase(assetClass)) {
               context.sendMessage(Message.translation("server.commands.assets.tags.assetsOfTypeWithTag").param("type", simpleName).param("tag", tag));
               AssetMap<?, ?> assetMap = entry.getValue().getAssetMap();
               Set<Message> keysForTag = assetMap.getKeysForTag(tagIndex).stream().map(Object::toString).map(Message::raw).collect(Collectors.toSet());
               context.sendMessage(MessageFormat.list(Message.translation("server.commands.assets.tags.assetKeys"), keysForTag));
               if (assetMap instanceof AssetMapWithIndexes<?, ?> assetMapWithIndexes) {
                  Set<Message> indexesForTag = assetMapWithIndexes.getIndexesForTag(tagIndex)
                     .intStream()
                     .mapToObj(Integer::toString)
                     .map(Message::raw)
                     .collect(Collectors.toSet());
                  context.sendMessage(MessageFormat.list(Message.translation("server.commands.assets.tags.assetIndexes"), indexesForTag));
               }
               break;
            }
         }
      }
   }
}
