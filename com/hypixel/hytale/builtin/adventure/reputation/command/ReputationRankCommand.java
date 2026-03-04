package com.hypixel.hytale.builtin.adventure.reputation.command;

import com.hypixel.hytale.builtin.adventure.reputation.ReputationPlugin;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationGroup;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationRank;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReputationRankCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private static final SingleArgumentType<ReputationGroup> REPUTATION_GROUP_ARG_TYPE = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.reputationgroup.name", ReputationGroup.class, "server.commands.parsing.argtype.asset.reputationgroup.usage"
   );
   @Nonnull
   private final RequiredArg<ReputationGroup> reputationGroupIdArg = this.withRequiredArg(
      "reputationGroupId", "server.commands.reputation.check.rank.reputationGroupId.desc", REPUTATION_GROUP_ARG_TYPE
   );

   public ReputationRankCommand() {
      super("rank", "server.commands.reputation.check.rank.desc");
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
      ReputationRank rank = ReputationPlugin.get().getReputationRank(store, ref, reputationGroup.getId());
      if (rank != null) {
         context.sendMessage(
            Message.translation("server.modules.reputation.valueForGroup").param("id", reputationGroup.getId()).param("value", rank.toString())
         );
      } else {
         context.sendMessage(
            Message.translation("server.modules.reputation.noRankFoundForValue")
               .param("value", ReputationPlugin.get().getReputationValue(store, ref, reputationGroup.getId()))
         );
      }
   }
}
