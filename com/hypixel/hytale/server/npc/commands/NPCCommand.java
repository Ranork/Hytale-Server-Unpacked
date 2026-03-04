package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import com.hypixel.hytale.server.flock.commands.NPCFlockCommand;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import java.awt.Color;
import java.util.List;
import javax.annotation.Nonnull;

public class NPCCommand extends AbstractCommandCollection {
   public static final SingleArgumentType<BuilderInfo> NPC_ROLE = new SingleArgumentType<BuilderInfo>(
      "server.commands.parsing.argtype.npcrole.name", "server.commands.parsing.argtype.npcrole.usage"
   ) {
      public BuilderInfo parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         try {
            NPCPlugin npcPlugin = NPCPlugin.get();
            int index = npcPlugin.getIndex(input);
            if (index == Integer.MIN_VALUE) {
               List<String> roles = npcPlugin.getRoleTemplateNames(false);
               parseResult.fail(
                  Message.translation("server.commands.notfound").param("type", "NPC Role").param("id", input).color(Color.RED),
                  Message.translation("server.general.failed.didYouMean")
                     .param("choices", StringUtil.sortByFuzzyDistance(input, roles, CommandUtil.RECOMMEND_COUNT).toString())
               );
               return null;
            } else {
               BuilderInfo builderInfo = npcPlugin.getRoleBuilderInfo(index);
               if (builderInfo == null) {
                  parseResult.fail(Message.translation("server.commands.notfound").param("type", "NPC Role").param("id", input).color(Color.RED));
                  return null;
               } else {
                  return builderInfo;
               }
            }
         } catch (Exception var6) {
            parseResult.fail(Message.translation("server.commands.notfound").param("type", "NPC Role").param("id", input).color(Color.RED));
            return null;
         }
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         try {
            NPCPlugin npcPlugin = NPCPlugin.get();
            List<String> roles = npcPlugin.getRoleTemplateNames(false);
            textAlreadyEntered = textAlreadyEntered.toLowerCase();

            for (String role : roles) {
               if (role.toLowerCase().startsWith(textAlreadyEntered)) {
                  result.suggest(role);
               }
            }
         } catch (Exception var9) {
         }
      }
   };

   public NPCCommand() {
      super("npc", "server.commands.npc");
      this.addSubCommand(new NPCAllCommand());
      this.addSubCommand(new NPCAppearanceCommand());
      this.addSubCommand(new NPCAttackCommand());
      this.addSubCommand(new NPCBenchmarkCommand());
      this.addSubCommand(new NPCBlackboardCommand());
      this.addSubCommand(new NPCCleanCommand());
      this.addSubCommand(new NPCDebugCommand());
      this.addSubCommand(new NPCDumpCommand());
      this.addSubCommand(new NPCFlockCommand());
      this.addSubCommand(new NPCFreezeCommand());
      this.addSubCommand(new NPCGiveCommand());
      this.addSubCommand(new NPCPathCommand());
      this.addSubCommand(new NPCRoleCommand());
      this.addSubCommand(new NPCRunTestsCommand());
      this.addSubCommand(new NPCSensorStatsCommand());
      this.addSubCommand(new NPCSpawnCommand());
      this.addSubCommand(new NPCStepCommand());
      this.addSubCommand(new NPCTestCommand());
      this.addSubCommand(new NPCThawCommand());
      this.addSubCommand(new NPCMessageCommand());
   }
}
