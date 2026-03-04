package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.HotbarManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class HotbarSwitchCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<Integer> hotbarSlotArg = this.withRequiredArg("hotbarSlot", "server.commands.hotbar.hotbarSlot.desc", ArgTypes.INTEGER)
      .addValidator(Validators.range(0, 9));
   @Nonnull
   private final FlagArg saveInsteadOfLoadArg = this.withFlagArg("save", "server.commands.hotbar.save.desc");

   public HotbarSwitchCommand() {
      super("hotbar", "server.commands.hotbar.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      HotbarManager hotbarManager = playerComponent.getHotbarManager();
      if (this.saveInsteadOfLoadArg.get(context)) {
         hotbarManager.saveHotbar(ref, this.hotbarSlotArg.get(context).shortValue(), store);
      } else {
         hotbarManager.loadHotbar(ref, this.hotbarSlotArg.get(context).shortValue(), store);
      }
   }
}
