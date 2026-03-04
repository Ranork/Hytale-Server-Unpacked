package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class AssetsCommand extends AbstractCommandCollection {
   public AssetsCommand() {
      super("assets", "server.commands.assets.desc");
      this.addSubCommand(new AssetTagsCommand());
      this.addSubCommand(new AssetsDuplicatesCommand());
      this.addSubCommand(new AssetsCommand.AssetLongestAssetNameCommand());
   }

   public static class AssetLongestAssetNameCommand extends AbstractAsyncCommand {
      public AssetLongestAssetNameCommand() {
         super("longest", "server.commands.assets.longest.desc");
      }

      @Nonnull
      @Override
      protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
         return CompletableFuture.runAsync(
            () -> {
               for (Entry<Class<? extends JsonAssetWithMap>, AssetStore<?, ?, ?>> e : AssetRegistry.getStoreMap().entrySet()) {
                  String longestName = "";

                  for (Object asset : e.getValue().getAssetMap().getAssetMap().keySet()) {
                     String name = e.getValue().transformKey(asset).toString();
                     if (name.length() > longestName.length()) {
                        longestName = name;
                     }
                  }

                  context.sendMessage(
                     Message.translation("server.commands.assets.longest.result")
                        .param("type", e.getKey().getSimpleName())
                        .param("assetName", longestName)
                        .param("length", longestName.length())
                  );
               }
            }
         );
      }
   }
}
