package com.hypixel.hytale.server.npc.corecomponents.debug;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.debug.builders.BuilderActionTest;
import java.util.Arrays;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ActionTest extends ActionBase {
   public ActionTest(@Nonnull BuilderActionTest builder, @Nonnull BuilderSupport support) {
      super(builder);
      HytaleLogger logger = NPCPlugin.get().getLogger();
      logger.at(Level.INFO).log("==== Test Action Build Start ===");
      logger.at(Level.INFO).log("Boolean %s", builder.getBoolean(support));
      logger.at(Level.INFO).log("Double %s", builder.getDouble(support));
      logger.at(Level.INFO).log("Float %s", builder.getFloat(support));
      logger.at(Level.INFO).log("Int %s", builder.getInt(support));
      logger.at(Level.INFO).log("String %s", builder.getString(support));
      logger.at(Level.INFO).log("Enum %s", builder.getEnum(support));
      logger.at(Level.INFO).log("EnumSet %s", builder.getEnumSet(support));
      logger.at(Level.INFO).log("Asset %s", builder.getAsset(support));
      logger.at(Level.INFO).log("DoubleArray %s", Arrays.toString(builder.getNumberArray(support)));
      logger.at(Level.INFO).log("StringArray %s", Arrays.toString((Object[])builder.getStringArray(support)));
      logger.at(Level.INFO).log("===== Test Action Build End ====");
   }
}
