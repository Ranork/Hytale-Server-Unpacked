package com.hypixel.hytale.server.core.entity.entities.player.windows;

import com.google.gson.JsonObject;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.SyncEventBusRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.window.WindowAction;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Window {
   public static final Map<WindowType, Supplier<? extends Window>> CLIENT_REQUESTABLE_WINDOW_TYPES = new ConcurrentHashMap<>();
   @Nonnull
   protected static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   protected final SyncEventBusRegistry<Void, Window.WindowCloseEvent> closeEventRegistry = new SyncEventBusRegistry<>(LOGGER, Window.WindowCloseEvent.class);
   @Nonnull
   protected final WindowType windowType;
   @Nonnull
   protected final AtomicBoolean isDirty = new AtomicBoolean();
   @Nonnull
   protected final AtomicBoolean needRebuild = new AtomicBoolean();
   private int id;
   @Nullable
   private WindowManager manager;
   @Nullable
   private PlayerRef playerRef;

   public Window(@Nonnull WindowType windowType) {
      this.windowType = windowType;
   }

   public void init(@Nonnull PlayerRef playerRef, @Nonnull WindowManager manager) {
      this.playerRef = playerRef;
      this.manager = manager;
   }

   @Nonnull
   public abstract JsonObject getData();

   protected boolean onOpen(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      return this.onOpen0(ref, store);
   }

   protected abstract boolean onOpen0(@Nonnull Ref<EntityStore> var1, @Nonnull Store<EntityStore> var2);

   protected void onClose(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      try {
         this.onClose0(ref, componentAccessor);
      } finally {
         this.closeEventRegistry.dispatchFor(null).dispatch(new Window.WindowCloseEvent());
      }
   }

   protected abstract void onClose0(@Nonnull Ref<EntityStore> var1, @Nonnull ComponentAccessor<EntityStore> var2);

   public void handleAction(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WindowAction action) {
   }

   @Nonnull
   public WindowType getType() {
      return this.windowType;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int getId() {
      return this.id;
   }

   @Nullable
   public PlayerRef getPlayerRef() {
      return this.playerRef;
   }

   public void close(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      assert this.manager != null;

      this.manager.closeWindow(ref, this.id, componentAccessor);
   }

   protected void invalidate() {
      this.isDirty.set(true);
   }

   protected void setNeedRebuild() {
      this.needRebuild.set(true);
      this.getData().addProperty("needRebuild", Boolean.TRUE);
   }

   protected boolean consumeIsDirty() {
      return this.isDirty.getAndSet(false);
   }

   protected void consumeNeedRebuild() {
      if (this.needRebuild.get()) {
         this.getData().remove("needRebuild");
         this.needRebuild.set(false);
      }
   }

   @Nonnull
   public EventRegistration registerCloseEvent(@Nonnull Consumer<Window.WindowCloseEvent> consumer) {
      return this.closeEventRegistry.register((short)0, null, consumer);
   }

   @Nonnull
   public EventRegistration registerCloseEvent(short priority, @Nonnull Consumer<Window.WindowCloseEvent> consumer) {
      return this.closeEventRegistry.register(priority, null, consumer);
   }

   @Nonnull
   public EventRegistration registerCloseEvent(@Nonnull EventPriority priority, @Nonnull Consumer<Window.WindowCloseEvent> consumer) {
      return this.closeEventRegistry.register(priority.getValue(), null, consumer);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Window window = (Window)o;
         if (this.id != window.id) {
            return false;
         } else {
            return !Objects.equals(this.windowType, window.windowType) ? false : Objects.equals(this.playerRef, window.playerRef);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.windowType.hashCode();
      result = 31 * result + this.id;
      return 31 * result + (this.playerRef != null ? this.playerRef.hashCode() : 0);
   }

   public static class WindowCloseEvent implements IEvent<Void> {
   }
}
