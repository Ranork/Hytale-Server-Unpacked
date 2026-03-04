package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetEntityCommand;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import javax.annotation.Nonnull;

public class EntityIntangibleCommand extends AbstractTargetEntityCommand {
   @Nonnull
   private final FlagArg removeFlag = this.withFlagArg("remove", "server.commands.entity.intangible.remove.desc");

   public EntityIntangibleCommand() {
      super("intangible", "server.commands.entity.intangible.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull ObjectList<Ref<EntityStore>> entities, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      boolean remove = this.removeFlag.provided(context);
      ObjectListIterator var6 = entities.iterator();

      while (var6.hasNext()) {
         Ref<EntityStore> entity = (Ref<EntityStore>)var6.next();
         if (remove) {
            store.tryRemoveComponent(entity, Intangible.getComponentType());
         } else {
            store.ensureComponent(entity, Intangible.getComponentType());
         }
      }

      context.sendMessage(Message.translation("server.commands.entity.intangible.success." + (remove ? "unset" : "set")).param("amount", entities.size()));
   }
}
