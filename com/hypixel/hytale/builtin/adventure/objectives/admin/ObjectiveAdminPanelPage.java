package com.hypixel.hytale.builtin.adventure.objectives.admin;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ObjectiveAdminPanelPage extends BasicCustomUIPage {
   public ObjectiveAdminPanelPage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismiss);
   }

   @Override
   public void build(@Nonnull UICommandBuilder commandBuilder) {
      commandBuilder.append("Pages/ObjectiveAdminPanelPage.ui");
      Collection<Objective> objectives = ObjectivePlugin.get().getObjectiveDataStore().getObjectiveCollection();
      int index = 0;

      for (Objective objective : objectives) {
         String selector = "#ObjectiveList[" + index + "]";
         commandBuilder.append("#ObjectiveList", "Pages/ObjectiveAdminPanelDataSlot.ui");
         commandBuilder.set(selector + " #Id.Text", objective.getObjectiveId());
         commandBuilder.set(selector + " #UUID.Text", "Objective UUID: " + objective.getObjectiveUUID().toString());
         StringBuilder stringBuilder = new StringBuilder();
         Universe universe = Universe.get();

         for (UUID playerUUID : objective.getActivePlayerUUIDs()) {
            PlayerRef player = universe.getPlayer(playerUUID);
            if (player != null) {
               if (!stringBuilder.isEmpty()) {
                  stringBuilder.append(", ");
               }

               stringBuilder.append(player.getUsername());
            }
         }

         commandBuilder.set(selector + " #CurrentPlayers.Text", "Current players: " + stringBuilder.toString());
         commandBuilder.set(selector + " #AllTimePlayers.Text", "All time players: " + Arrays.toString(objective.getPlayerUUIDs().toArray()));
         index++;
      }
   }
}
