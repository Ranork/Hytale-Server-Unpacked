package com.hypixel.hytale.builtin.crafting.interaction;

import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.builtin.crafting.window.CraftingWindow;
import com.hypixel.hytale.builtin.crafting.window.DiagramCraftingWindow;
import com.hypixel.hytale.builtin.crafting.window.SimpleCraftingWindow;
import com.hypixel.hytale.builtin.crafting.window.StructuralCraftingWindow;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpenBenchPageInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final OpenBenchPageInteraction SIMPLE_CRAFTING = new OpenBenchPageInteraction(
      "*Simple_Crafting_Default", OpenBenchPageInteraction.PageType.SIMPLE_CRAFTING
   );
   @Nonnull
   public static final RootInteraction SIMPLE_CRAFTING_ROOT = new RootInteraction(SIMPLE_CRAFTING.getId(), SIMPLE_CRAFTING.getId());
   @Nonnull
   public static final OpenBenchPageInteraction DIAGRAM_CRAFTING = new OpenBenchPageInteraction(
      "*Diagram_Crafting_Default", OpenBenchPageInteraction.PageType.DIAGRAM_CRAFTING
   );
   @Nonnull
   public static final RootInteraction DIAGRAM_CRAFTING_ROOT = new RootInteraction(DIAGRAM_CRAFTING.getId(), DIAGRAM_CRAFTING.getId());
   @Nonnull
   public static final OpenBenchPageInteraction STRUCTURAL_CRAFTING = new OpenBenchPageInteraction(
      "*Structural_Crafting_Default", OpenBenchPageInteraction.PageType.STRUCTURAL_CRAFTING
   );
   @Nonnull
   public static final RootInteraction STRUCTURAL_CRAFTING_ROOT = new RootInteraction(STRUCTURAL_CRAFTING.getId(), STRUCTURAL_CRAFTING.getId());
   @Nonnull
   public static final BuilderCodec<OpenBenchPageInteraction> CODEC = BuilderCodec.builder(
         OpenBenchPageInteraction.class, OpenBenchPageInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Opens the given crafting bench page.")
      .<OpenBenchPageInteraction.PageType>appendInherited(
         new KeyedCodec<>("Page", new EnumCodec<>(OpenBenchPageInteraction.PageType.class)),
         (o, v) -> o.pageType = v,
         o -> o.pageType,
         (o, p) -> o.pageType = p.pageType
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   @Nonnull
   private OpenBenchPageInteraction.PageType pageType = OpenBenchPageInteraction.PageType.SIMPLE_CRAFTING;

   public OpenBenchPageInteraction(@Nonnull String id, @Nonnull OpenBenchPageInteraction.PageType pageType) {
      super(id);
      this.pageType = pageType;
   }

   protected OpenBenchPageInteraction() {
   }

   @Override
   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      Ref<EntityStore> ref = context.getEntity();
      Store<EntityStore> store = ref.getStore();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         CraftingManager craftingManagerComponent = commandBuffer.getComponent(ref, CraftingManager.getComponentType());
         if (craftingManagerComponent != null && !craftingManagerComponent.hasBenchSet()) {
            if (world.getState(targetBlock.x, targetBlock.y, targetBlock.z, true) instanceof BenchState benchState) {
               CraftingWindow benchWindow = (CraftingWindow)(switch (this.pageType) {
                  case SIMPLE_CRAFTING -> new SimpleCraftingWindow(benchState);
                  case DIAGRAM_CRAFTING -> new DiagramCraftingWindow(ref, commandBuffer, benchState);
                  case STRUCTURAL_CRAFTING -> new StructuralCraftingWindow(benchState);
               });
               UUIDComponent uuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());
               if (uuidComponent == null) {
                  return;
               }

               UUID uuid = uuidComponent.getUuid();
               if (benchState.getWindows().putIfAbsent(uuid, benchWindow) == null) {
                  benchWindow.registerCloseEvent(event -> benchState.getWindows().remove(uuid, benchWindow));
               }

               playerComponent.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, benchWindow);
            }
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }

   public static enum PageType {
      SIMPLE_CRAFTING,
      DIAGRAM_CRAFTING,
      STRUCTURAL_CRAFTING;
   }
}
