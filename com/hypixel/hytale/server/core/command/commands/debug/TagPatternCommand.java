package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.tagpattern.config.TagPattern;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class TagPatternCommand extends CommandBase {
   @Nonnull
   private static final SingleArgumentType<TagPattern> TAG_PATTERN_ARG_TYPE = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.tagpattern.name", TagPattern.class, "server.commands.parsing.argtype.asset.tagpattern.usage"
   );
   @Nonnull
   private final RequiredArg<TagPattern> tagPatternArg = this.withRequiredArg("tagPattern", "server.commands.tagpattern.tagPattern.desc", TAG_PATTERN_ARG_TYPE);
   @Nonnull
   private final RequiredArg<BlockType> blockTypeArg = this.withRequiredArg("blockType", "server.commands.tagpattern.blockType.desc", ArgTypes.BLOCK_TYPE_ASSET);

   public TagPatternCommand() {
      super("tagpattern", "server.commands.tagpattern.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      TagPattern tagPattern = this.tagPatternArg.get(context);
      BlockType blockType = this.blockTypeArg.get(context);
      boolean result = tagPattern.test(blockType.getData().getTags());
      context.sendMessage(
         (result ? Message.translation("server.commands.tagpattern.matches") : Message.translation("server.commands.tagpattern.noMatches"))
            .param("blocktype", blockType.getId())
            .param("pattern", tagPattern.getId())
      );
   }
}
