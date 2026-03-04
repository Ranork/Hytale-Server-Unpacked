package com.hypixel.hytale.server.core.modules.interaction.interaction;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateUnarmedInteractions;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class UnarmedInteractionsPacketGenerator extends DefaultAssetPacketGenerator<String, UnarmedInteractions> {
   @Nonnull
   @Override
   public ToClientPacket generateInitPacket(@Nonnull DefaultAssetMap<String, UnarmedInteractions> assetMap, Map<String, UnarmedInteractions> assets) {
      UpdateUnarmedInteractions packet = new UpdateUnarmedInteractions();
      packet.type = UpdateType.Init;
      UnarmedInteractions unarmedInteraction = assetMap.getAsset("Empty");
      Object2IntOpenHashMap<InteractionType> intMap = new Object2IntOpenHashMap();

      for (Entry<InteractionType, String> e : unarmedInteraction.getInteractions().entrySet()) {
         intMap.put(e.getKey(), RootInteraction.getAssetMap().getIndex(e.getValue()));
      }

      packet.interactions = intMap;
      return packet;
   }

   @Nonnull
   @Override
   public ToClientPacket generateUpdatePacket(@Nonnull Map<String, UnarmedInteractions> loadedAssets) {
      UpdateUnarmedInteractions packet = new UpdateUnarmedInteractions();
      packet.type = UpdateType.AddOrUpdate;
      UnarmedInteractions unarmedInteraction = loadedAssets.get("Empty");
      Object2IntOpenHashMap<InteractionType> intMap = new Object2IntOpenHashMap();

      for (Entry<InteractionType, String> e : unarmedInteraction.getInteractions().entrySet()) {
         intMap.put(e.getKey(), RootInteraction.getAssetMap().getIndex(e.getValue()));
      }

      packet.interactions = intMap;
      return packet;
   }

   @Nonnull
   @Override
   public ToClientPacket generateRemovePacket(Set<String> removed) {
      UpdateUnarmedInteractions packet = new UpdateUnarmedInteractions();
      packet.type = UpdateType.Remove;
      return packet;
   }
}
