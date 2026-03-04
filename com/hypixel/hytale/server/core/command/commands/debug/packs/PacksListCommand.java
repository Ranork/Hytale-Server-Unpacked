package com.hypixel.hytale.server.core.command.commands.debug.packs;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;

public class PacksListCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_PACKS_NOT_INITIALIZED = Message.translation("server.commands.packs.notInitialized");
   @Nonnull
   private static final Message MESSAGE_PACKS_NONE_LOADED = Message.translation("server.commands.packs.noneLoaded");

   public PacksListCommand() {
      super("list", "server.commands.packs.list.desc");
      this.addAliases("ls");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      AssetModule assetModule = AssetModule.get();
      if (assetModule == null) {
         context.sendMessage(MESSAGE_PACKS_NOT_INITIALIZED);
      } else {
         List<AssetPack> assetPacks = assetModule.getAssetPacks();
         if (assetPacks.isEmpty()) {
            context.sendMessage(MESSAGE_PACKS_NONE_LOADED);
         } else {
            ObjectArrayList<Message> packs = new ObjectArrayList();
            assetPacks.stream()
               .sorted(Comparator.comparing(AssetPack::getName, String.CASE_INSENSITIVE_ORDER))
               .map(PacksListCommand::formatPack)
               .forEach(packs::add);
            context.sendMessage(MessageFormat.list(Message.translation("server.commands.packs.listHeader"), packs));
         }
      }
   }

   @Nonnull
   private static Message formatPack(@Nonnull AssetPack pack) {
      String name = pack.getName();
      String root = pack.getRoot() != null ? pack.getRoot().toString() : "<unknown>";
      return Message.translation("server.commands.packs.listEntry").param("name", name).param("root", root);
   }
}
