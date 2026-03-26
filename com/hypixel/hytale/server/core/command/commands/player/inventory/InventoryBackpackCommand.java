package com.hypixel.hytale.server.core.command.commands.player.inventory;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import javax.annotation.Nonnull;

public class InventoryBackpackCommand extends AbstractPlayerCommand {
   @Nonnull
   private final OptionalArg<Integer> sizeArg = this.withOptionalArg("size", "server.commands.inventorybackpack.size.desc", ArgTypes.INTEGER);

   public InventoryBackpackCommand() {
      super("backpack", "server.commands.inventorybackpack.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      InventoryComponent.Backpack backpackInventoryComponent = store.getComponent(ref, InventoryComponent.Backpack.getComponentType());
      if (backpackInventoryComponent != null) {
         if (!this.sizeArg.provided(context)) {
            context.sendMessage(
               Message.translation("server.commands.inventory.backpack.size").param("capacity", backpackInventoryComponent.getInventory().getCapacity())
            );
         } else {
            short capacity = this.sizeArg.get(context).shortValue();
            ObjectArrayList<ItemStack> remainder = new ObjectArrayList();
            backpackInventoryComponent.resize(capacity, remainder);
            ObjectListIterator var9 = remainder.iterator();

            while (var9.hasNext()) {
               ItemStack item = (ItemStack)var9.next();
               ItemUtils.dropItem(ref, item, store);
            }

            context.sendMessage(
               Message.translation("server.commands.inventory.backpack.resized")
                  .param("capacity", backpackInventoryComponent.getInventory().getCapacity())
                  .param("dropped", remainder.size())
            );
         }
      }
   }
}
