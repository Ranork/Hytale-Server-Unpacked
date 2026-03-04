package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.event.IAsyncEvent;
import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.List;
import javax.annotation.Nonnull;

public class PlayerChatEvent implements IAsyncEvent<String>, ICancellable {
   @Nonnull
   public static final PlayerChatEvent.Formatter DEFAULT_FORMATTER = (playerRef, msg) -> Message.translation("server.chat.playerMessage")
      .param("username", playerRef.getUsername())
      .param("message", msg);
   @Nonnull
   private PlayerRef sender;
   @Nonnull
   private List<PlayerRef> targets;
   @Nonnull
   private String content;
   private PlayerChatEvent.Formatter formatter;
   private boolean cancelled;

   public PlayerChatEvent(@Nonnull PlayerRef sender, @Nonnull List<PlayerRef> targets, @Nonnull String content) {
      this.sender = sender;
      this.targets = targets;
      this.content = content;
      this.formatter = DEFAULT_FORMATTER;
      this.cancelled = false;
   }

   @Nonnull
   public PlayerRef getSender() {
      return this.sender;
   }

   public void setSender(@Nonnull PlayerRef sender) {
      this.sender = sender;
   }

   @Nonnull
   public List<PlayerRef> getTargets() {
      return this.targets;
   }

   public void setTargets(@Nonnull List<PlayerRef> targets) {
      this.targets = targets;
   }

   @Nonnull
   public String getContent() {
      return this.content;
   }

   public void setContent(@Nonnull String content) {
      this.content = content;
   }

   @Nonnull
   public PlayerChatEvent.Formatter getFormatter() {
      return this.formatter;
   }

   public void setFormatter(@Nonnull PlayerChatEvent.Formatter formatter) {
      this.formatter = formatter;
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
      return "PlayerChatEvent{message="
         + this.content
         + ", targets="
         + this.targets
         + ", formatter="
         + this.formatter
         + ", cancelled="
         + this.cancelled
         + "} "
         + super.toString();
   }

   public interface Formatter {
      @Nonnull
      Message format(@Nonnull PlayerRef var1, @Nonnull String var2);
   }
}
