package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.packets.auth.ClientReferral;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerSetupConnectEvent implements IEvent<Void>, ICancellable {
   public static final String DEFAULT_REASON = "You have been disconnected from the server!";
   private final PacketHandler packetHandler;
   private final String username;
   private final UUID uuid;
   private final PlayerAuthentication auth;
   private final byte[] referralData;
   private final HostAddress referralSource;
   private boolean cancelled;
   private String reason;
   private ClientReferral clientReferral;

   public PlayerSetupConnectEvent(
      PacketHandler packetHandler, String username, UUID uuid, PlayerAuthentication auth, byte[] referralData, HostAddress referralSource
   ) {
      this.packetHandler = packetHandler;
      this.username = username;
      this.uuid = uuid;
      this.auth = auth;
      this.referralData = referralData;
      this.referralSource = referralSource;
      this.reason = "You have been disconnected from the server!";
      this.cancelled = false;
   }

   public PacketHandler getPacketHandler() {
      return this.packetHandler;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public String getUsername() {
      return this.username;
   }

   public PlayerAuthentication getAuth() {
      return this.auth;
   }

   @Nullable
   public byte[] getReferralData() {
      return this.referralData;
   }

   public boolean isReferralConnection() {
      return this.referralData != null && this.referralData.length > 0;
   }

   @Nullable
   public HostAddress getReferralSource() {
      return this.referralSource;
   }

   @Nullable
   public ClientReferral getClientReferral() {
      return this.clientReferral;
   }

   public void referToServer(@Nonnull String host, int port) {
      this.referToServer(host, port, null);
   }

   public void referToServer(@Nonnull String host, int port, @Nullable byte[] data) {
      int MAX_REFERRAL_DATA_SIZE = 4096;
      Objects.requireNonNull(host, "Host cannot be null");
      if (port > 0 && port <= 65535) {
         if (data != null && data.length > 4096) {
            throw new IllegalArgumentException("Referral data exceeds maximum size of 4096 bytes (got " + data.length + ")");
         } else {
            HytaleLogger.getLogger()
               .at(Level.INFO)
               .log("Referring player %s (%s) to %s:%d with %d bytes of data", this.username, this.uuid, host, port, data != null ? data.length : 0);
            this.clientReferral = new ClientReferral(new HostAddress(host, (short)port), data);
         }
      } else {
         throw new IllegalArgumentException("Port must be between 1 and 65535");
      }
   }

   public String getReason() {
      return this.reason;
   }

   public void setReason(String reason) {
      Objects.requireNonNull(reason, "Reason can't be null");
      this.reason = reason;
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerSetupConnectEvent{username='"
         + this.username
         + "', uuid="
         + this.uuid
         + ", auth="
         + this.auth
         + ", referralData="
         + (this.referralData != null ? this.referralData.length + " bytes" : "null")
         + ", referralSource="
         + (this.referralSource != null ? this.referralSource.host + ":" + this.referralSource.port : "null")
         + ", cancelled="
         + this.cancelled
         + ", reason='"
         + this.reason
         + "'}";
   }
}
