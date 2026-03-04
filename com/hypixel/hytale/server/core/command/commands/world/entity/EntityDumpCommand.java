package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;

public class EntityDumpCommand extends AbstractWorldCommand {
   private static final Message MESSAGE_GENERAL_NO_ENTITY_IN_VIEW = Message.translation("server.general.noEntityInView");
   private static final Message MESSAGE_COMMANDS_ENTITY_DUMP_DUMP_DONE = Message.translation("server.commands.entity.dump.dumpDone");
   @Nonnull
   private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.dump.entity.desc", ArgTypes.ENTITY_ID);

   public EntityDumpCommand() {
      super("dump", "server.commands.entity.dump.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> entityRef = this.entityArg.get(store, context);
      if (entityRef != null && entityRef.isValid()) {
         Holder<EntityStore> holder = store.copyEntity(entityRef);
         BsonDocument document = EntityStore.REGISTRY.serialize(holder);
         HytaleLogger.getLogger().at(Level.INFO).log("Entity: %s\n%s\n%s", entityRef, holder, BsonUtil.toJson(document));
         context.sendMessage(MESSAGE_COMMANDS_ENTITY_DUMP_DUMP_DONE);
      } else {
         context.sendMessage(MESSAGE_GENERAL_NO_ENTITY_IN_VIEW);
      }
   }
}
