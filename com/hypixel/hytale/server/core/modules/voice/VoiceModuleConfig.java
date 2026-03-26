package com.hypixel.hytale.server.core.modules.voice;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceModuleConfig {
   public static final BuilderCodec<VoiceModuleConfig> CODEC = BuilderCodec.builder(VoiceModuleConfig.class, VoiceModuleConfig::new)
      .addField(new KeyedCodec<>("VoiceEnabled", Codec.BOOLEAN), (config, b) -> config.voiceEnabled = b, config -> config.voiceEnabled)
      .addField(new KeyedCodec<>("MaxHearingDistance", Codec.FLOAT), (config, f) -> config.maxHearingDistance = f, config -> config.maxHearingDistance)
      .addField(new KeyedCodec<>("FullVolumeDistance", Codec.FLOAT), (config, f) -> config.fullVolumeDistance = f, config -> config.fullVolumeDistance)
      .addField(new KeyedCodec<>("DeadPlayersCanHear", Codec.BOOLEAN), (config, b) -> config.deadPlayersCanHear = b, config -> config.deadPlayersCanHear)
      .addField(new KeyedCodec<>("MutedPlayers", new SetCodec<>(Codec.UUID_STRING, HashSet::new, false)), (config, s) -> {
         config.mutedPlayers.clear();
         config.mutedPlayers.addAll(s);
      }, config -> config.mutedPlayers)
      .build();
   private boolean voiceEnabled = true;
   private float maxHearingDistance = 32.0F;
   private float fullVolumeDistance = 4.0F;
   private boolean deadPlayersCanHear = false;
   private final Set<UUID> mutedPlayers = ConcurrentHashMap.newKeySet();

   public boolean isVoiceEnabled() {
      return this.voiceEnabled;
   }

   public void setVoiceEnabled(boolean voiceEnabled) {
      this.voiceEnabled = voiceEnabled;
   }

   public float getMaxHearingDistance() {
      return this.maxHearingDistance;
   }

   public void setMaxHearingDistance(float maxHearingDistance) {
      this.maxHearingDistance = maxHearingDistance;
   }

   public float getFullVolumeDistance() {
      return this.fullVolumeDistance;
   }

   public void setFullVolumeDistance(float fullVolumeDistance) {
      this.fullVolumeDistance = fullVolumeDistance;
   }

   public Set<UUID> getMutedPlayers() {
      return Collections.unmodifiableSet(new HashSet<>(this.mutedPlayers));
   }

   public boolean isPlayerMuted(UUID playerId) {
      return this.mutedPlayers.contains(playerId);
   }

   public boolean mutePlayer(UUID playerId) {
      return this.mutedPlayers.add(playerId);
   }

   public boolean unmutePlayer(UUID playerId) {
      return this.mutedPlayers.remove(playerId);
   }

   public boolean isDeadPlayersCanHear() {
      return this.deadPlayersCanHear;
   }

   public void setDeadPlayersCanHear(boolean deadPlayersCanHear) {
      this.deadPlayersCanHear = deadPlayersCanHear;
   }
}
