package com.hypixel.hytale.server.core.command.commands.world.entity.stats;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetEntityCommand;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import javax.annotation.Nonnull;

public class EntityStatsSetToMaxCommand extends AbstractTargetEntityCommand {
   @Nonnull
   private final RequiredArg<String> entityStatNameArg = this.withRequiredArg(
      "statName", "server.commands.entity.stats.settomax.statName.desc", ArgTypes.STRING
   );

   public EntityStatsSetToMaxCommand() {
      super("settomax", "server.commands.entity.stats.settomax.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull ObjectList<Ref<EntityStore>> entities, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      String entityStatName = this.entityStatNameArg.get(context);
      setEntityStatMax(context, entities, entityStatName, store);
   }

   public static void setEntityStatMax(
      @Nonnull CommandContext context, @Nonnull List<Ref<EntityStore>> entities, @Nonnull String entityStatName, @Nonnull Store<EntityStore> store
   ) {
      int entityStatIndex = EntityStatType.getAssetMap().getIndex(entityStatName);
      if (entityStatIndex == Integer.MIN_VALUE) {
         context.sendMessage(Message.translation("server.commands.entityStats.entityStatNotFound").param("name", entityStatName));
         context.sendMessage(
            Message.translation("server.general.failed.didYouMean")
               .param(
                  "choices",
                  StringUtil.sortByFuzzyDistance(entityStatName, EntityStatType.getAssetMap().getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT).toString()
               )
         );
      } else {
         for (Ref<EntityStore> entity : entities) {
            EntityStatMap entityStatMap = store.getComponent(entity, EntityStatsModule.get().getEntityStatMapComponentType());
            if (entityStatMap != null) {
               EntityStatValue entityStatValue = entityStatMap.get(entityStatIndex);
               if (entityStatValue == null) {
                  context.sendMessage(Message.translation("server.commands.entityStats.entityStatNotFound").param("name", entityStatName));
                  context.sendMessage(
                     Message.translation("server.general.failed.didYouMean")
                        .param(
                           "choices",
                           StringUtil.sortByFuzzyDistance(entityStatName, EntityStatType.getAssetMap().getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT)
                              .toString()
                        )
                  );
               } else {
                  float newValueOfStat = entityStatMap.setStatValue(entityStatIndex, entityStatValue.getMax());
                  context.sendMessage(Message.translation("server.commands.entityStats.success").param("name", entityStatName).param("value", newValueOfStat));
               }
            }
         }
      }
   }
}
