package com.hypixel.hytale.builtin.buildertools.prefabeditor;

import com.hypixel.hytale.server.core.Message;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabLoadingState {
   @Nonnull
   private PrefabLoadingState.Phase currentPhase = PrefabLoadingState.Phase.INITIALIZING;
   private int totalPrefabs;
   private int loadedPrefabs;
   private int pastedPrefabs;
   @Nullable
   private Path currentPrefabPath;
   @Nonnull
   private final List<PrefabLoadingState.LoadingError> errors = new ObjectArrayList();
   private long startTimeNanos = System.nanoTime();
   private long lastUpdateTimeNanos = this.startTimeNanos;
   private long lastNotifyTimeNanos;

   public void setTotalPrefabs(int totalPrefabs) {
      this.totalPrefabs = totalPrefabs;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public void setPhase(@Nonnull PrefabLoadingState.Phase phase) {
      this.currentPhase = phase;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public void onPrefabLoaded(@Nullable Path path) {
      this.loadedPrefabs++;
      this.currentPrefabPath = path;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public void onPrefabPasted(@Nullable Path path) {
      this.pastedPrefabs++;
      this.currentPrefabPath = path;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public void addError(@Nonnull PrefabLoadingState.LoadingError error) {
      this.errors.add(error);
      this.currentPhase = PrefabLoadingState.Phase.ERROR;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public void addError(@Nonnull String translationKey) {
      this.addError(new PrefabLoadingState.LoadingError(translationKey));
   }

   public void addError(@Nonnull String translationKey, @Nullable String details) {
      this.addError(new PrefabLoadingState.LoadingError(translationKey, details));
   }

   @Nonnull
   public PrefabLoadingState.Phase getCurrentPhase() {
      return this.currentPhase;
   }

   public int getTotalPrefabs() {
      return this.totalPrefabs;
   }

   public int getLoadedPrefabs() {
      return this.loadedPrefabs;
   }

   public int getPastedPrefabs() {
      return this.pastedPrefabs;
   }

   @Nullable
   public Path getCurrentPrefabPath() {
      return this.currentPrefabPath;
   }

   @Nonnull
   public List<PrefabLoadingState.LoadingError> getErrors() {
      return this.errors;
   }

   public boolean hasErrors() {
      return !this.errors.isEmpty();
   }

   public boolean isShuttingDown() {
      return this.currentPhase == PrefabLoadingState.Phase.CANCELLING
         || this.currentPhase == PrefabLoadingState.Phase.SHUTTING_DOWN_WORLD
         || this.currentPhase == PrefabLoadingState.Phase.DELETING_WORLD;
   }

   public boolean isShutdownComplete() {
      return this.currentPhase == PrefabLoadingState.Phase.SHUTDOWN_COMPLETE;
   }

   public float getProgressPercentage() {
      if (this.totalPrefabs == 0) {
         return switch (this.currentPhase) {
            case INITIALIZING -> 0.0F;
            case CREATING_WORLD -> 0.1F;
            case LOADING_PREFABS -> 0.2F;
            case PASTING_PREFABS -> 0.5F;
            case FINALIZING -> 0.99F;
            case COMPLETE -> 1.0F;
            case ERROR -> 0.0F;
            case CANCELLING -> 0.1F;
            case SHUTTING_DOWN_WORLD -> 0.4F;
            case DELETING_WORLD -> 0.8F;
            case SHUTDOWN_COMPLETE -> 1.0F;
         };
      } else {
         return switch (this.currentPhase) {
            case INITIALIZING -> 0.0F;
            case CREATING_WORLD -> 0.02F;
            case LOADING_PREFABS -> 0.02F + 0.08F * this.loadedPrefabs / this.totalPrefabs;
            case PASTING_PREFABS -> 0.1F + 0.89F * this.pastedPrefabs / this.totalPrefabs;
            case FINALIZING -> 0.99F;
            case COMPLETE -> 1.0F;
            case ERROR -> 0.0F;
            case CANCELLING -> 0.1F;
            case SHUTTING_DOWN_WORLD -> 0.4F;
            case DELETING_WORLD -> 0.8F;
            case SHUTDOWN_COMPLETE -> 1.0F;
         };
      }
   }

   public long getElapsedTimeMillis() {
      return (System.nanoTime() - this.startTimeNanos) / 1000000L;
   }

   public long getLastNotifyTimeNanos() {
      return this.lastNotifyTimeNanos;
   }

   public void setLastNotifyTimeNanos(long nanos) {
      this.lastNotifyTimeNanos = nanos;
   }

   @Nonnull
   public Message getStatusMessage() {
      if (this.hasErrors()) {
         return this.errors.getLast().toMessage();
      } else {
         Message message = Message.translation(this.currentPhase.getTranslationKey());
         if (this.currentPhase == PrefabLoadingState.Phase.LOADING_PREFABS && this.totalPrefabs > 0) {
            message = message.param("current", this.loadedPrefabs).param("total", this.totalPrefabs);
         } else if (this.currentPhase == PrefabLoadingState.Phase.PASTING_PREFABS && this.totalPrefabs > 0) {
            message = message.param("current", this.pastedPrefabs).param("total", this.totalPrefabs);
         }

         if (this.currentPrefabPath != null) {
            message = message.param("path", this.currentPrefabPath.getFileName().toString());
         }

         return message;
      }
   }

   public void markComplete() {
      this.currentPhase = PrefabLoadingState.Phase.COMPLETE;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public record LoadingError(@Nonnull String translationKey, @Nullable String details) {
      public LoadingError(@Nonnull String translationKey) {
         this(translationKey, null);
      }

      @Nonnull
      public Message toMessage() {
         Message message = Message.translation(this.translationKey);
         if (this.details != null) {
            message = message.param("details", this.details);
         }

         return message;
      }
   }

   public static enum Phase {
      INITIALIZING("server.commands.editprefab.loading.phase.initializing"),
      CREATING_WORLD("server.commands.editprefab.loading.phase.creatingWorld"),
      LOADING_PREFABS("server.commands.editprefab.loading.phase.loadingPrefabs"),
      PASTING_PREFABS("server.commands.editprefab.loading.phase.pastingPrefabs"),
      FINALIZING("server.commands.editprefab.loading.phase.finalizing"),
      COMPLETE("server.commands.editprefab.loading.phase.complete"),
      ERROR("server.commands.editprefab.loading.phase.error"),
      CANCELLING("server.commands.editprefab.loading.phase.cancelling"),
      SHUTTING_DOWN_WORLD("server.commands.editprefab.loading.phase.shuttingDownWorld"),
      DELETING_WORLD("server.commands.editprefab.loading.phase.deletingWorld"),
      SHUTDOWN_COMPLETE("server.commands.editprefab.loading.phase.shutdownComplete");

      private final String translationKey;

      private Phase(String translationKey) {
         this.translationKey = translationKey;
      }

      @Nonnull
      public String getTranslationKey() {
         return this.translationKey;
      }
   }
}
