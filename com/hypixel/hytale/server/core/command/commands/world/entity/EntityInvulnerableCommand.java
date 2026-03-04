package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetEntityCommand;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import javax.annotation.Nonnull;

public class EntityInvulnerableCommand extends AbstractTargetEntityCommand {
   @Nonnull
   private final FlagArg removeFlag = this.withFlagArg("remove", "server.commands.entity.invulnerable.remove.desc");

   public EntityInvulnerableCommand() {
      super("invulnerable", "server.commands.entity.invulnerable.desc");
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
            store.tryRemoveComponent(entity, Invulnerable.getComponentType());
         } else {
            store.ensureComponent(entity, Invulnerable.getComponentType());
         }
      }

      context.sendMessage(Message.translation("server.commands.entity.invulnerable.success." + (remove ? "unset" : "set")).param("amount", entities.size()));
   }
}
