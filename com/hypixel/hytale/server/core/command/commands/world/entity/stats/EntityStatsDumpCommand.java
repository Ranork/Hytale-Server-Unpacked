package com.hypixel.hytale.server.core.command.commands.world.entity.stats;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetEntityCommand;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import javax.annotation.Nonnull;

public class EntityStatsDumpCommand extends AbstractTargetEntityCommand {
   public EntityStatsDumpCommand() {
      super("dump", "server.commands.entity.stats.dump.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull ObjectList<Ref<EntityStore>> entities, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      dumpEntityStatsData(context, entities, store);
   }

   public static void dumpEntityStatsData(@Nonnull CommandContext context, @Nonnull List<Ref<EntityStore>> entities, @Nonnull Store<EntityStore> store) {
      for (Ref<EntityStore> entity : entities) {
         ComponentType<EntityStore, EntityStatMap> component = EntityStatsModule.get().getEntityStatMapComponentType();
         EntityStatMap statMap = store.getComponent(entity, component);
         if (statMap != null) {
            ObjectArrayList<Message> values = new ObjectArrayList(statMap.size());

            for (int i = 0; i < statMap.size(); i++) {
               EntityStatValue entityStat = statMap.get(i);
               values.add(Message.translation("server.commands.entityStats.value").param("name", entityStat.getId()).param("value", entityStat.get()));
            }

            context.sendMessage(MessageFormat.list(null, values));
         }
      }
   }
}
