package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GameModeArgumentType extends SingleArgumentType<GameMode> {
   @Nonnull
   private static final Object2ObjectMap<String, GameMode> GAMEMODE_MAP = new Object2ObjectOpenHashMap();

   public GameModeArgumentType() {
      super("server.commands.parsing.argtype.gamemode.name", "server.commands.parsing.argtype.gamemode.usage", "adventure", "creative", "a", "c");
   }

   @Nullable
   public GameMode parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
      String inputLowerCase = input.toLowerCase();
      GameMode gameMode = (GameMode)GAMEMODE_MAP.get(inputLowerCase);
      if (gameMode != null) {
         return gameMode;
      } else {
         List<String> validModes = StringUtil.sortByFuzzyDistance(inputLowerCase, GAMEMODE_MAP.keySet(), CommandUtil.RECOMMEND_COUNT);
         parseResult.fail(
            Message.translation("server.commands.parsing.argtype.gamemode.invalid").param("input", input).param("suggestions", validModes.toString())
         );
         return null;
      }
   }

   static {
      GAMEMODE_MAP.put("adventure", GameMode.Adventure);
      GAMEMODE_MAP.put("creative", GameMode.Creative);
      GAMEMODE_MAP.put("a", GameMode.Adventure);
      GAMEMODE_MAP.put("c", GameMode.Creative);
   }
}
