package com.hypixel.hytale.server.core.entity.entities.player.pages.itemrepair;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceBasePage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class ItemRepairPage extends ChoiceBasePage {
   public ItemRepairPage(@Nonnull PlayerRef playerRef, @Nonnull ItemContainer itemContainer, double repairPenalty, ItemContext heldItemContext) {
      super(playerRef, getItemElements(itemContainer, repairPenalty, heldItemContext), "Pages/ItemRepairPage.ui");
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      if (this.getElements().length > 0) {
         super.build(ref, commandBuilder, eventBuilder, store);
      } else {
         commandBuilder.append(this.getPageLayout());
         commandBuilder.clear("#ElementList");
         commandBuilder.appendInline("#ElementList", "Label { Text: %server.customUI.itemRepairPage.noItems; Style: (Alignment: Center); }");
      }
   }

   @Nonnull
   protected static ChoiceElement[] getItemElements(@Nonnull ItemContainer itemContainer, double repairPenalty, ItemContext heldItemContext) {
      List<ChoiceElement> elements = new ObjectArrayList();

      for (short slot = 0; slot < itemContainer.getCapacity(); slot++) {
         ItemStack itemStack = itemContainer.getItemStack(slot);
         if (!ItemStack.isEmpty(itemStack) && !itemStack.isUnbreakable() && !(itemStack.getDurability() >= itemStack.getMaxDurability())) {
            ItemContext itemContext = new ItemContext(itemContainer, slot, itemStack);
            elements.add(new ItemRepairElement(itemStack, new RepairItemInteraction(itemContext, repairPenalty, heldItemContext)));
         }
      }

      return elements.toArray(ChoiceElement[]::new);
   }
}
