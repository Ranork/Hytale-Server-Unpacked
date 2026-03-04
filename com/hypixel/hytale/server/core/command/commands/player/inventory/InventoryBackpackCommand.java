package com.hypixel.hytale.server.core.command.commands.player.inventory;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
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
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      Inventory inventory = playerComponent.getInventory();
      if (!this.sizeArg.provided(context)) {
         context.sendMessage(Message.translation("server.commands.inventory.backpack.size").param("capacity", inventory.getBackpack().getCapacity()));
      } else {
         short capacity = this.sizeArg.get(context).shortValue();
         ObjectArrayList<ItemStack> remainder = new ObjectArrayList();
         inventory.resizeBackpack(capacity, remainder);
         ObjectListIterator var10 = remainder.iterator();

         while (var10.hasNext()) {
            ItemStack item = (ItemStack)var10.next();
            ItemUtils.dropItem(ref, item, store);
         }

         context.sendMessage(
            Message.translation("server.commands.inventory.backpack.resized")
               .param("capacity", inventory.getBackpack().getCapacity())
               .param("dropped", remainder.size())
         );
      }
   }
}
