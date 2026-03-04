package com.hypixel.hytale.builtin.crafting.commands;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.awt.Color;
import java.util.Set;
import javax.annotation.Nonnull;

public class RecipeCommand extends AbstractCommandCollection {
   public RecipeCommand() {
      super("recipe", "server.commands.recipe.desc");
      this.addSubCommand(new RecipeCommand.Learn());
      this.addSubCommand(new RecipeCommand.Forget());
      this.addSubCommand(new RecipeCommand.List());
   }

   static class Forget extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Item> itemArg = this.withRequiredArg("item", "server.commands.recipe.forget.item.desc", ArgTypes.ITEM_ASSET);

      Forget() {
         super("forget", "server.commands.recipe.forget.desc");
         this.addUsageVariant(new RecipeCommand.Forget.ForgetOther());
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Item item = this.itemArg.get(context);
         String itemId = item.getId();
         if (CraftingPlugin.forgetRecipe(ref, itemId, store)) {
            context.sendMessage(Message.translation("server.commands.recipe.forgotten").param("id", itemId).color(Color.GREEN));
         } else {
            context.sendMessage(Message.translation("server.commands.recipe.alreadyNotKnown").param("id", itemId).color(Color.RED));
         }
      }

      private static class ForgetOther extends CommandBase {
         @Nonnull
         private final RequiredArg<Item> itemArg = this.withRequiredArg("item", "server.commands.recipe.forget.item.desc", ArgTypes.ITEM_ASSET);
         @Nonnull
         private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

         ForgetOther() {
            super("server.commands.recipe.forget.other.desc");
         }

         @Override
         protected void executeSync(@Nonnull CommandContext context) {
            PlayerRef targetPlayerRef = this.playerArg.get(context);
            Ref<EntityStore> ref = targetPlayerRef.getReference();
            if (ref != null && ref.isValid()) {
               Store<EntityStore> store = ref.getStore();
               World world = store.getExternalData().getWorld();
               world.execute(
                  () -> {
                     Player playerComponent = store.getComponent(ref, Player.getComponentType());
                     if (playerComponent == null) {
                        context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
                     } else {
                        Item item = this.itemArg.get(context);
                        String itemId = item.getId();
                        if (CraftingPlugin.forgetRecipe(ref, itemId, store)) {
                           context.sendMessage(
                              Message.translation("server.commands.recipe.forgotten.other")
                                 .param("username", targetPlayerRef.getUsername())
                                 .param("id", itemId)
                                 .color(Color.GREEN)
                           );
                        } else {
                           context.sendMessage(
                              Message.translation("server.commands.recipe.alreadyNotKnown.other")
                                 .param("username", targetPlayerRef.getUsername())
                                 .param("id", itemId)
                                 .color(Color.RED)
                           );
                        }
                     }
                  }
               );
            } else {
               context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
            }
         }
      }
   }

   static class Learn extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Item> itemArg = this.withRequiredArg("item", "server.commands.recipe.learn.item.desc", ArgTypes.ITEM_ASSET);

      Learn() {
         super("learn", "server.commands.recipe.learn.desc");
         this.addUsageVariant(new RecipeCommand.Learn.LearnOther());
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Item item = this.itemArg.get(context);
         String itemId = item.getId();
         Message itemMessage = Message.translation(item.getTranslationKey());
         if (CraftingPlugin.learnRecipe(ref, itemId, store)) {
            context.sendMessage(Message.translation("server.modules.learnrecipe.success").param("name", itemMessage).color(Color.GREEN));
         } else {
            context.sendMessage(Message.translation("server.modules.learnrecipe.alreadyKnown").param("name", itemMessage).color(Color.RED));
         }
      }

      private static class LearnOther extends CommandBase {
         @Nonnull
         private final RequiredArg<Item> itemArg = this.withRequiredArg("item", "server.commands.recipe.learn.item.desc", ArgTypes.ITEM_ASSET);
         @Nonnull
         private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

         LearnOther() {
            super("server.commands.recipe.learn.other.desc");
         }

         @Override
         protected void executeSync(@Nonnull CommandContext context) {
            PlayerRef targetPlayerRef = this.playerArg.get(context);
            Ref<EntityStore> ref = targetPlayerRef.getReference();
            if (ref != null && ref.isValid()) {
               Store<EntityStore> store = ref.getStore();
               World world = store.getExternalData().getWorld();
               world.execute(
                  () -> {
                     Player playerComponent = store.getComponent(ref, Player.getComponentType());
                     if (playerComponent == null) {
                        context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
                     } else {
                        Item item = this.itemArg.get(context);
                        String itemId = item.getId();
                        Message itemMessage = Message.translation(item.getTranslationKey());
                        if (CraftingPlugin.learnRecipe(ref, itemId, store)) {
                           context.sendMessage(
                              Message.translation("server.commands.recipe.learn.success.other")
                                 .param("username", targetPlayerRef.getUsername())
                                 .param("name", itemMessage)
                                 .color(Color.GREEN)
                           );
                        } else {
                           context.sendMessage(
                              Message.translation("server.commands.recipe.learn.alreadyKnown.other")
                                 .param("username", targetPlayerRef.getUsername())
                                 .param("name", itemMessage)
                                 .color(Color.RED)
                           );
                        }
                     }
                  }
               );
            } else {
               context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
            }
         }
      }
   }

   static class List extends AbstractPlayerCommand {
      List() {
         super("list", "server.commands.recipe.list.desc");
         this.addUsageVariant(new RecipeCommand.List.ListOther());
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         Set<String> knownRecipes = playerComponent.getPlayerConfigData().getKnownRecipes();
         context.sendMessage(Message.translation("server.commands.recipe.knownRecipes").param("list", knownRecipes.toString()));
      }

      private static class ListOther extends CommandBase {
         @Nonnull
         private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

         ListOther() {
            super("server.commands.recipe.list.other.desc");
         }

         @Override
         protected void executeSync(@Nonnull CommandContext context) {
            PlayerRef targetPlayerRef = this.playerArg.get(context);
            Ref<EntityStore> ref = targetPlayerRef.getReference();
            if (ref != null && ref.isValid()) {
               Store<EntityStore> store = ref.getStore();
               World world = store.getExternalData().getWorld();
               world.execute(
                  () -> {
                     Player playerComponent = store.getComponent(ref, Player.getComponentType());
                     if (playerComponent == null) {
                        context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
                     } else {
                        Set<String> knownRecipes = playerComponent.getPlayerConfigData().getKnownRecipes();
                        context.sendMessage(
                           Message.translation("server.commands.recipe.knownRecipes.other")
                              .param("username", targetPlayerRef.getUsername())
                              .param("list", knownRecipes.toString())
                        );
                     }
                  }
               );
            } else {
               context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
            }
         }
      }
   }
}
