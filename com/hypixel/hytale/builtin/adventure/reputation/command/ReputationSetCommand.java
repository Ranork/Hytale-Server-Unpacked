package com.hypixel.hytale.builtin.adventure.reputation.command;

import com.hypixel.hytale.builtin.adventure.reputation.ReputationPlugin;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationGroup;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReputationSetCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private static final SingleArgumentType<ReputationGroup> REPUTATION_GROUP_ARG_TYPE = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.reputationgroup.name", ReputationGroup.class, "server.commands.parsing.argtype.asset.reputationgroup.usage"
   );
   @Nonnull
   private final RequiredArg<ReputationGroup> reputationGroupIdArg = this.withRequiredArg(
      "reputationGroupId", "server.commands.reputation.set.reputationGroupId.desc", REPUTATION_GROUP_ARG_TYPE
   );
   @Nonnull
   private final RequiredArg<Integer> valueArg = this.withRequiredArg("value", "server.commands.reputation.set.value.desc", ArgTypes.INTEGER);

   public ReputationSetCommand() {
      super("set", "server.commands.reputation.set.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      ReputationGroup reputationGroup = this.reputationGroupIdArg.get(context);
      int value = this.valueArg.get(context);
      Player player = store.getComponent(ref, Player.getComponentType());

      assert player != null;

      int currentValue = ReputationPlugin.get().getReputationValue(store, ref, reputationGroup.getId());
      int newReputationAmount = ReputationPlugin.get().changeReputation(player, reputationGroup.getId(), value - currentValue, store);
      context.sendMessage(Message.translation("server.modules.reputation.success").param("id", reputationGroup.getId()).param("value", newReputationAmount));
   }
}
