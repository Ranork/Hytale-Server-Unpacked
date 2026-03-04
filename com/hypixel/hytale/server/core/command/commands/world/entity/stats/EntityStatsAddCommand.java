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

public class EntityStatsAddCommand extends AbstractTargetEntityCommand {
   @Nonnull
   private final RequiredArg<String> entityStatNameArg = this.withRequiredArg("statName", "server.commands.entity.stats.add.statName.desc", ArgTypes.STRING);
   @Nonnull
   private final RequiredArg<Integer> statAmountArg = this.withRequiredArg("statAmount", "server.commands.entity.stats.add.statAmount.desc", ArgTypes.INTEGER);

   public EntityStatsAddCommand() {
      super("add", "server.commands.entity.stats.add.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull ObjectList<Ref<EntityStore>> entities, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      int statAmount = this.statAmountArg.get(context);
      String entityStatName = this.entityStatNameArg.get(context);
      addEntityStat(context, entities, statAmount, entityStatName, store);
   }

   public static void addEntityStat(
      @Nonnull CommandContext context,
      @Nonnull List<Ref<EntityStore>> entityRefs,
      int statAmount,
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
         for (Ref<EntityStore> entity : entityRefs) {
            EntityStatMap entityStatMapComponent = store.getComponent(entity, EntityStatsModule.get().getEntityStatMapComponentType());
            if (entityStatMapComponent != null) {
               if (entityStatMapComponent.get(entityStatIndex) == null) {
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
                  float newValueOfStat = entityStatMapComponent.addStatValue(entityStatIndex, statAmount);
                  context.sendMessage(Message.translation("server.commands.entityStats.success").param("name", entityStatName).param("value", newValueOfStat));
               }
            }
         }
      }
   }
}
