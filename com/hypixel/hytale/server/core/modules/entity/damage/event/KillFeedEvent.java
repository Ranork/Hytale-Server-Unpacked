package com.hypixel.hytale.server.core.modules.entity.damage.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KillFeedEvent {
   public static final class DecedentMessage extends CancellableEcsEvent {
      @Nonnull
      private final Damage damage;
      @Nullable
      private Message message = null;

      public DecedentMessage(@Nonnull Damage damage) {
         this.damage = damage;
      }

      public Damage getDamage() {
         return this.damage;
      }

      public void setMessage(@Nullable Message message) {
         this.message = message;
      }

      @Nullable
      public Message getMessage() {
         return this.message;
      }
   }

   public static final class Display extends CancellableEcsEvent {
      @Nonnull
      private final Damage damage;
      @Nullable
      private String icon;
      @Nonnull
      private final List<PlayerRef> broadcastTargets;

      public Display(@Nonnull Damage damage, @Nullable String icon, @Nonnull List<PlayerRef> broadcastTargets) {
         this.damage = damage;
         this.icon = icon;
         this.broadcastTargets = broadcastTargets;
      }

      @Nonnull
      public List<PlayerRef> getBroadcastTargets() {
         return this.broadcastTargets;
      }

      @Nonnull
      public Damage getDamage() {
         return this.damage;
      }

      @Nullable
      public String getIcon() {
         return this.icon;
      }

      public void setIcon(@Nullable String icon) {
         this.icon = icon;
      }
   }

   public static final class KillerMessage extends CancellableEcsEvent {
      @Nonnull
      private final Damage damage;
      @Nonnull
      private final Ref<EntityStore> targetRef;
      @Nullable
      private Message message = null;

      public KillerMessage(@Nonnull Damage damage, @Nonnull Ref<EntityStore> targetRef) {
         this.damage = damage;
         this.targetRef = targetRef;
      }

      @Nonnull
      public Damage getDamage() {
         return this.damage;
      }

      @Nonnull
      public Ref<EntityStore> getTargetRef() {
         return this.targetRef;
      }

      public void setMessage(@Nullable Message message) {
         this.message = message;
      }

      @Nullable
      public Message getMessage() {
         return this.message;
      }
   }
}
