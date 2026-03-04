package com.hypixel.hytale.server.core.modules.blockset.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class BlockSetCommand extends CommandBase {
   @Nonnull
   private final BlockSetModule blockSetModule;
   @Nonnull
   private final OptionalArg<String> blockSetArg = this.withOptionalArg("blockset", "server.commands.blockset.blockset.desc", ArgTypes.STRING);

   public BlockSetCommand(@Nonnull BlockSetModule blockSetModule) {
      super("blockset", "server.commands.blockset.desc");
      this.blockSetModule = blockSetModule;
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (!this.blockSetArg.provided(context)) {
         Set<Message> blockSetKeys = BlockSet.getAssetMap().getAssetMap().keySet().stream().map(Message::raw).collect(Collectors.toSet());
         context.sendMessage(MessageFormat.list(null, blockSetKeys));
      } else {
         String blockSetName = this.blockSetArg.get(context);
         int index = BlockSet.getAssetMap().getIndex(blockSetName);
         if (index == Integer.MIN_VALUE) {
            context.sendMessage(Message.translation("server.modules.blockset.setNotFound").param("name", blockSetName));
         } else {
            IntSet set = (IntSet)this.blockSetModule.getBlockSets().get(index);
            if (set == null) {
               context.sendMessage(Message.translation("server.modules.blockset.setNotFound").param("name", blockSetName));
            } else {
               List<Message> names = new ObjectArrayList();
               set.forEach(i -> names.add(Message.raw(BlockType.getAssetMap().getAsset(i).getId().toString())));
               names.sort(null);
               context.sendMessage(MessageFormat.list(null, names));
            }
         }
      }
   }
}
