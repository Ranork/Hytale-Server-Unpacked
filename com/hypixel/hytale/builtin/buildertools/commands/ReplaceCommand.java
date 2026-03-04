package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReplaceCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<BlockPattern> toArg = this.withRequiredArg("to", "server.commands.replace.toBlock.desc", ArgTypes.BLOCK_PATTERN);
   @Nonnull
   private final FlagArg substringSwapFlag = this.withFlagArg("substringSwap", "server.commands.replace.substringSwap.desc");
   @Nonnull
   private final FlagArg regexFlag = this.withFlagArg("regex", "server.commands.replace.regex.desc");

   public ReplaceCommand() {
      super("replace", "server.commands.replace.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addUsageVariant(new ReplaceCommand.ReplaceFromToCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      executeReplace(context, store, ref, playerRef, null, this.toArg.get(context), this.substringSwapFlag.get(context), this.regexFlag.get(context));
   }

   private static void executeReplace(
      @Nonnull CommandContext context,
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nullable String fromValue,
      @Nonnull BlockPattern toPattern,
      boolean substringSwap,
      boolean regex
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         if (toPattern != null && !toPattern.isEmpty()) {
            String toValue = toPattern.toString();
            Material fromMaterial = fromValue != null ? Material.fromKey(fromValue) : null;
            Material toMaterial = Material.fromPattern(toPattern, ThreadLocalRandom.current());
            if (toMaterial.isFluid() && !substringSwap && !regex) {
               if (fromMaterial == null) {
                  context.sendMessage(Message.translation("server.commands.replace.fromRequired"));
               } else {
                  BuilderToolsPlugin.addToQueue(
                     playerComponent, playerRef, (r, s, componentAccessor) -> s.replace(r, fromMaterial, toMaterial, componentAccessor)
                  );
                  context.sendMessage(Message.translation("server.builderTools.replace.replacementBlockDone").param("from", fromValue).param("to", toValue));
               }
            } else if (fromMaterial != null && fromMaterial.isFluid()) {
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.replace(r, fromMaterial, toMaterial, componentAccessor));
               context.sendMessage(Message.translation("server.builderTools.replace.replacementBlockDone").param("from", fromValue).param("to", toValue));
            } else {
               BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
               if (fromValue == null && !substringSwap && !regex) {
                  BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.replace(r, null, toPattern, componentAccessor));
                  context.sendMessage(Message.translation("server.builderTools.replace.replacementAllDone").param("to", toValue));
               } else if (fromValue == null) {
                  context.sendMessage(Message.translation("server.commands.replace.fromRequired"));
               } else if (regex) {
                  Pattern pattern;
                  try {
                     pattern = Pattern.compile(fromValue);
                  } catch (PatternSyntaxException var24) {
                     context.sendMessage(Message.translation("server.commands.replace.invalidRegex").param("error", var24.getMessage()));
                     return;
                  }

                  BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
                     s.replace(r, value -> {
                        String valueKey = assetMap.getAsset(value).getId();
                        return pattern.matcher(valueKey).matches();
                     }, toPattern, componentAccessor);
                     context.sendMessage(Message.translation("server.commands.replace.success").param("regex", fromValue).param("replacement", toValue));
                  });
               } else if (fromMaterial == null) {
                  context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", fromValue).param("key", fromValue));
               } else if (!substringSwap) {
                  int fromBlockId = fromMaterial.getBlockId();
                  BuilderToolsPlugin.addToQueue(
                     playerComponent, playerRef, (r, s, componentAccessor) -> s.replace(r, block -> block == fromBlockId, toPattern, componentAccessor)
                  );
                  context.sendMessage(Message.translation("server.builderTools.replace.replacementBlockDone").param("from", fromValue).param("to", toValue));
               } else {
                  String[] blockKeys = fromValue.split(",");
                  Int2IntArrayMap swapMap = new Int2IntArrayMap();

                  for (int blockId = 0; blockId < assetMap.getAssetCount(); blockId++) {
                     BlockType blockType = assetMap.getAsset(blockId);
                     String blockKeyStr = blockType.getId();

                     for (String from : blockKeys) {
                        if (blockKeyStr.contains(from.trim())) {
                           String replacedKey;
                           try {
                              replacedKey = blockKeyStr.replace(from.trim(), toValue);
                           } catch (Exception var25) {
                              continue;
                           }

                           int index = assetMap.getIndex(replacedKey);
                           if (index != Integer.MIN_VALUE) {
                              swapMap.put(blockId, index);
                              break;
                           }
                        }
                     }
                  }

                  if (!swapMap.isEmpty()) {
                     BuilderToolsPlugin.addToQueue(
                        playerComponent, playerRef, (r, s, componentAccessor) -> s.replace(r, value -> swapMap.getOrDefault(value, value), componentAccessor)
                     );
                     context.sendMessage(Message.translation("server.builderTools.replace.replacementDone").param("nb", swapMap.size()).param("to", toValue));
                  } else {
                     context.sendMessage(Message.translation("server.commands.replace.noMatchingBlocks").param("blockType", fromValue));
                  }
               }
            }
         } else {
            context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", "").param("key", ""));
         }
      }
   }

   private static class ReplaceFromToCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> fromArg = this.withRequiredArg("from", "server.commands.replace.from.desc", ArgTypes.STRING);
      @Nonnull
      private final RequiredArg<BlockPattern> toArg = this.withRequiredArg("to", "server.commands.replace.toBlock.desc", ArgTypes.BLOCK_PATTERN);
      @Nonnull
      private final FlagArg substringSwapFlag = this.withFlagArg("substringSwap", "server.commands.replace.substringSwap.desc");
      @Nonnull
      private final FlagArg regexFlag = this.withFlagArg("regex", "server.commands.replace.regex.desc");

      public ReplaceFromToCommand() {
         super("server.commands.replace.desc");
         this.setPermissionGroup(GameMode.Creative);
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         ReplaceCommand.executeReplace(
            context,
            store,
            ref,
            playerRef,
            this.fromArg.get(context),
            this.toArg.get(context),
            this.substringSwapFlag.get(context),
            this.regexFlag.get(context)
         );
      }
   }
}
