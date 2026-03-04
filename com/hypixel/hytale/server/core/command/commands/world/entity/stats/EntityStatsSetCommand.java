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
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import javax.annotation.Nonnull;

public class EntityStatsSetCommand extends AbstractTargetEntityCommand {
   @Nonnull
   private final RequiredArg<String> entityStatNameArg = this.withRequiredArg("statName", "server.commands.entity.stats.set.statName.desc", ArgTypes.STRING);
   @Nonnull
   private final RequiredArg<Integer> statValueArg = this.withRequiredArg("statValue", "server.commands.entity.stats.set.statValue.desc", ArgTypes.INTEGER);

   public EntityStatsSetCommand() {
      super("set", "server.commands.entity.stats.set.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull ObjectList<Ref<EntityStore>> entities, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      int newStatValue = this.statValueArg.get(context);
      String entityStatName = this.entityStatNameArg.get(context);
      setEntityStat(context, entities, newStatValue, entityStatName, store);
   }

   public static void setEntityStat(
      @Nonnull CommandContext context,
      @Nonnull List<Ref<EntityStore>> entities,
      int newStatValue,
      @Nonnull String entityStatName,
      @Nonnull Store<EntityStore> store
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
               if (entityStatMap.get(entityStatIndex) == null) {
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
                  float newValueOfStat = entityStatMap.setStatValue(entityStatIndex, newStatValue);
                  context.sendMessage(Message.translation("server.commands.entityStats.success").param("name", entityStatName).param("value", newValueOfStat));
               }
            }
         }
      }
   }
}
