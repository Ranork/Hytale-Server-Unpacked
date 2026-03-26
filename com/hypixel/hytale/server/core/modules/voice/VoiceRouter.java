package com.hypixel.hytale.server.core.modules.voice;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.protocol.packets.voice.RelayedVoiceData;
import com.hypixel.hytale.protocol.packets.voice.VoiceCodec;
import com.hypixel.hytale.protocol.packets.voice.VoiceConfig;
import com.hypixel.hytale.protocol.packets.voice.VoiceData;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class VoiceRouter {
   private static final int VERBOSE_LOG_PACKET_FREQUENCY = 50;
   private static final int MAX_SPEAKERS_PER_LISTENER = 12;
   private final VoiceModule voiceModule;
   private final HytaleLogger logger;
   private final ConcurrentHashMap<Long, Set<UUID>> worldPlayerSets = new ConcurrentHashMap<>();
   private volatile boolean loggedFirstCacheRoute = false;

   public VoiceRouter(@Nonnull VoiceModule voiceModule) {
      this.voiceModule = voiceModule;
      this.logger = voiceModule.getLogger();
   }

   public void updateSpeakerPositionCache(
      @Nonnull PlayerRef speaker, @Nonnull Vector3d position, boolean isUnderwater, long worldId, int networkId, boolean isDead
   ) {
      UUID playerId = speaker.getUuid();
      VoiceModule.PositionSnapshot oldSnapshot = this.voiceModule.getCachedPosition(playerId);
      if (oldSnapshot != null && oldSnapshot.worldId() != worldId) {
         Set<UUID> oldWorldSet = this.worldPlayerSets.get(oldSnapshot.worldId());
         if (oldWorldSet != null) {
            oldWorldSet.remove(playerId);
         }
      }

      this.worldPlayerSets.computeIfAbsent(worldId, k -> ConcurrentHashMap.newKeySet()).add(playerId);
      this.voiceModule.updatePositionCache(playerId, position, isUnderwater, worldId, networkId, isDead);
   }

   public void removePlayerFromWorldSets(@Nonnull UUID playerId) {
      for (Set<UUID> worldSet : this.worldPlayerSets.values()) {
         worldSet.remove(playerId);
      }
   }

   public void sendVoiceConfig(@Nonnull PlayerRef player) {
      VoiceConfig config = new VoiceConfig();
      config.voiceEnabled = this.voiceModule.isVoiceEnabled();
      config.codec = VoiceCodec.Opus;
      config.sampleRate = 48000;
      config.channels = 1;
      config.maxHearingDistance = this.voiceModule.getMaxHearingDistance();
      config.referenceDistance = this.voiceModule.getReferenceDistance();
      config.supportsVoiceStream = true;
      config.maxPacketsPerSecond = (byte)this.voiceModule.getMaxPacketsPerSecond();
      player.getPacketHandler().writeNoCache(config);
      this.logger
         .at(Level.FINE)
         .log(
            "[VoiceConfig] Sent to %s: enabled=%s, codec=%s, sampleRate=%d, maxDistance=%.1f, refDistance=%.1f, supportsVoiceStream=true, maxPacketsPerSecond=%d",
            player.getUsername(),
            config.voiceEnabled,
            config.codec,
            config.sampleRate,
            config.maxHearingDistance,
            config.referenceDistance,
            config.maxPacketsPerSecond
         );
   }

   public void routeVoiceFromCache(@Nonnull PlayerRef speaker, @Nonnull VoiceData packet) {
      if (this.voiceModule.isVoiceEnabled()) {
         if (!this.voiceModule.isPlayerMuted(speaker.getUuid())) {
            if (packet.opusData != null && packet.opusData.length <= this.voiceModule.getMaxPacketSize()) {
               VoiceModule.PositionSnapshot speakerPos = this.voiceModule.getCachedPosition(speaker.getUuid());
               if (speakerPos != null) {
                  VoicePlayerState speakerState = this.voiceModule.getPlayerState(speaker.getUuid());
                  if (speakerState == null || !speakerState.isSilenced()) {
                     long now = System.currentTimeMillis();
                     double qx = Math.round(speakerPos.x() * 2.0) / 2.0;
                     double qy = Math.round(speakerPos.y() * 2.0) / 2.0;
                     double qz = Math.round(speakerPos.z() * 2.0) / 2.0;
                     RelayedVoiceData relay = new RelayedVoiceData();
                     relay.speakerId = speaker.getUuid();
                     relay.entityId = speakerPos.networkId();
                     relay.sequenceNumber = packet.sequenceNumber;
                     relay.timestamp = packet.timestamp;
                     relay.speakerPosition = new Position(qx, qy, qz);
                     relay.speakerIsUnderwater = speakerPos.isUnderwater();
                     relay.opusData = packet.opusData;
                     float maxDistSq = this.voiceModule.getMaxHearingDistance() * this.voiceModule.getMaxHearingDistance();
                     long speakerWorldId = speakerPos.worldId();
                     if (packet.sequenceNumber % 50 == 0) {
                        this.logger
                           .at(Level.FINE)
                           .log(
                              "[VoiceRouter] SPEAKER_WORLD: speaker=%s, worldId=%d, pos=(%.1f,%.1f,%.1f), seq=%d",
                              speaker.getUsername(),
                              speakerWorldId,
                              speakerPos.x(),
                              speakerPos.y(),
                              speakerPos.z(),
                              packet.sequenceNumber
                           );
                     }

                     int recipientCount = 0;
                     int skippedCount = 0;
                     Set<UUID> sameWorldPlayers = this.worldPlayerSets.get(speakerWorldId);
                     if (sameWorldPlayers != null && !sameWorldPlayers.isEmpty()) {
                        List<VoiceRouter.ListenerCandidate> candidates = null;

                        for (UUID listenerId : sameWorldPlayers) {
                           if (!listenerId.equals(speaker.getUuid())) {
                              VoiceModule.PositionSnapshot listenerPos = this.voiceModule.getCachedPosition(listenerId);
                              if (listenerPos != null) {
                                 if (listenerPos.worldId() != speakerWorldId) {
                                    this.logger
                                       .at(Level.FINE)
                                       .log(
                                          "[VoiceRouter] WORLD_ISOLATION: speakerWorldId=%d, listenerWorldId=%d, speaker=%s, listener=%s - BLOCKING",
                                          speakerWorldId,
                                          listenerPos.worldId(),
                                          speaker.getUsername(),
                                          listenerId.toString().substring(0, 8)
                                       );
                                 } else if (!this.voiceModule.isDeadPlayersCanHear() && listenerPos.isDead()) {
                                    skippedCount++;
                                 } else {
                                    double dx = speakerPos.x() - listenerPos.x();
                                    double dy = speakerPos.y() - listenerPos.y();
                                    double dz = speakerPos.z() - listenerPos.z();
                                    double distSq = dx * dx + dy * dy + dz * dz;
                                    if (distSq <= maxDistSq) {
                                       PacketHandler handler = this.getPlayerHandler(listenerId);
                                       if (handler != null) {
                                          Channel voiceChannel = handler.getChannel(StreamType.Voice);
                                          if (voiceChannel != null && voiceChannel.isActive()) {
                                             if (candidates == null) {
                                                candidates = new ArrayList<>();
                                             }

                                             candidates.add(new VoiceRouter.ListenerCandidate(listenerId, distSq, handler));
                                          } else {
                                             skippedCount++;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }

                        if (candidates != null) {
                           if (candidates.size() > 12) {
                              candidates.sort((a, b) -> Double.compare(a.distSq, b.distSq));
                              candidates = candidates.subList(0, 12);
                           }

                           for (VoiceRouter.ListenerCandidate candidate : candidates) {
                              RelayedVoiceData recipientRelay = createPerRecipientRelay(relay);
                              Channel voiceChannel = candidate.handler.getChannel(StreamType.Voice);
                              if (voiceChannel != null && voiceChannel.isActive()) {
                                 voiceChannel.writeAndFlush(recipientRelay);
                                 recipientCount++;
                              }
                           }
                        }

                        if (!this.loggedFirstCacheRoute && recipientCount > 0) {
                           this.loggedFirstCacheRoute = true;
                           this.logger
                              .at(Level.INFO)
                              .log(
                                 "[VoiceRouter] First voice packet routed via cache: speaker=%s, seq=%d, recipientCount=%d, skipped=%d",
                                 speaker.getUsername(),
                                 packet.sequenceNumber,
                                 recipientCount,
                                 skippedCount
                              );
                        }

                        if (packet.sequenceNumber % 50 == 0) {
                           this.logger
                              .at(Level.FINE)
                              .log(
                                 "[VoiceRouter] CacheRoute seq=%d, recipients=%d, skipped=%d, speakerCacheAge=%dms",
                                 packet.sequenceNumber,
                                 recipientCount,
                                 skippedCount,
                                 now - speakerPos.timestamp()
                              );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private PacketHandler getPlayerHandler(UUID playerId) {
      PlayerRef playerRef = Universe.get().getPlayer(playerId);
      return playerRef != null ? playerRef.getPacketHandler() : null;
   }

   private static RelayedVoiceData createPerRecipientRelay(@Nonnull RelayedVoiceData source) {
      RelayedVoiceData relay = new RelayedVoiceData();
      relay.speakerId = source.speakerId;
      relay.entityId = source.entityId;
      relay.sequenceNumber = source.sequenceNumber;
      relay.timestamp = source.timestamp;
      relay.speakerPosition = source.speakerPosition != null ? new Position(source.speakerPosition) : null;
      relay.speakerIsUnderwater = source.speakerIsUnderwater;
      relay.opusData = source.opusData;
      return relay;
   }

   private record ListenerCandidate(UUID listenerId, double distSq, PacketHandler handler) {
   }
}
