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

public class EntityStatsGetCommand extends AbstractTargetEntityCommand {
   @Nonnull
   private final RequiredArg<String> entityStatNameArg = this.withRequiredArg("statName", "server.commands.entity.stats.get.statName.desc", ArgTypes.STRING);

   public EntityStatsGetCommand() {
      super("get", "server.commands.entity.stats.get.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull ObjectList<Ref<EntityStore>> entities, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      String entityStat = this.entityStatNameArg.get(context);
      getEntityStat(context, entities, entityStat, store);
   }

   public static void getEntityStat(
      @Nonnull CommandContext context, @Nonnull List<Ref<EntityStore>> entities, @Nonnull String entityStat, @Nonnull Store<EntityStore> store
   ) {
      int entityStatIndex = EntityStatType.getAssetMap().getIndex(entityStat);
      if (entityStatIndex == Integer.MIN_VALUE) {
         context.sendMessage(Message.translation("server.commands.entityStats.entityStatNotFound").param("name", entityStat));
         context.sendMessage(
            Message.translation("server.general.failed.didYouMean")
               .param(
                  "choices",
                  StringUtil.sortByFuzzyDistance(entityStat, EntityStatType.getAssetMap().getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT).toString()
               )
         );
      } else {
         for (Ref<EntityStore> entity : entities) {
            EntityStatMap entityStatMapComponent = store.getComponent(entity, EntityStatsModule.get().getEntityStatMapComponentType());
            if (entityStatMapComponent != null) {
               EntityStatValue value = entityStatMapComponent.get(entityStatIndex);
               if (value == null) {
                  context.sendMessage(Message.translation("server.commands.entityStats.entityStatNotFound").param("name", entityStat));
                  context.sendMessage(
                     Message.translation("server.general.failed.didYouMean")
                        .param(
                           "choices",
                           StringUtil.sortByFuzzyDistance(entityStat, EntityStatType.getAssetMap().getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT)
                              .toString()
                        )
                  );
               } else {
                  context.sendMessage(Message.translation("server.commands.entityStats.value").param("name", value.getId()).param("value", value.get()));
               }
            }
         }
      }
   }
}
