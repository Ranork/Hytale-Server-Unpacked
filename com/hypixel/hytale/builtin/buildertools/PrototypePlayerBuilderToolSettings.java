package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.BlockChange;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrototypePlayerBuilderToolSettings {
   @Nonnull
   private static final Message MESSAGE_BUILDER_TOOLS_CANNOT_PERFORM_COMMAND_IN_TRANSFORMATION_MODE = Message.translation(
      "server.builderTools.cannotPerformCommandInTransformationMode"
   );
   private final UUID player;
   private final LinkedList<LongOpenHashSet> ignoredPaintOperations = new LinkedList<>();
   private int maxLengthOfIgnoredPaintOperations;
   private boolean shouldShowEditorSettings;
   private boolean isLoadingBrush;
   private boolean usePrototypeBrushConfigurations;
   private String currentlyLoadedBrushConfigName = "";
   private BrushConfig brushConfig = new BrushConfig();
   private BrushConfigCommandExecutor brushConfigCommandExecutor;
   private boolean isInSelectionTransformationMode = false;
   @Nullable
   private BlockChange[] blockChangesForPlaySelectionToolPasteMode = null;
   @Nullable
   private PrototypePlayerBuilderToolSettings.FluidChange[] fluidChangesForPlaySelectionToolPasteMode = null;
   @Nullable
   private PrototypePlayerBuilderToolSettings.EntityChange[] entityChangesForPlaySelectionToolPasteMode = null;
   @Nullable
   private Vector3i lastBrushPosition = null;
   @Nullable
   private Vector3i blockChangeOffsetOrigin = null;
   @Nullable
   private List<Ref<EntityStore>> lastTransformEntityRefs = null;

   public PrototypePlayerBuilderToolSettings(UUID player) {
      this.player = player;
      this.brushConfigCommandExecutor = new BrushConfigCommandExecutor(this.brushConfig);
   }

   public UUID getPlayer() {
      return this.player;
   }

   public boolean isInSelectionTransformationMode() {
      return this.isInSelectionTransformationMode;
   }

   public void setInSelectionTransformationMode(boolean inSelectionTransformationMode) {
      this.isInSelectionTransformationMode = inSelectionTransformationMode;
      if (!this.isInSelectionTransformationMode) {
         this.blockChangesForPlaySelectionToolPasteMode = null;
         this.fluidChangesForPlaySelectionToolPasteMode = null;
         this.entityChangesForPlaySelectionToolPasteMode = null;
         this.blockChangeOffsetOrigin = null;
      }
   }

   public void setBlockChangesForPlaySelectionToolPasteMode(@Nullable BlockChange[] blockChangesForPlaySelectionToolPasteMode) {
      this.blockChangesForPlaySelectionToolPasteMode = blockChangesForPlaySelectionToolPasteMode;
   }

   public String getCurrentlyLoadedBrushConfigName() {
      return this.currentlyLoadedBrushConfigName;
   }

   public void setCurrentlyLoadedBrushConfigName(String currentlyLoadedBrushConfigName) {
      this.currentlyLoadedBrushConfigName = currentlyLoadedBrushConfigName;
   }

   public boolean isLoadingBrush() {
      return this.isLoadingBrush;
   }

   public void setLoadingBrush(boolean loadingBrush) {
      this.isLoadingBrush = loadingBrush;
   }

   @Nullable
   public BlockChange[] getBlockChangesForPlaySelectionToolPasteMode() {
      return this.blockChangesForPlaySelectionToolPasteMode;
   }

   public void setFluidChangesForPlaySelectionToolPasteMode(@Nullable PrototypePlayerBuilderToolSettings.FluidChange[] fluidChanges) {
      this.fluidChangesForPlaySelectionToolPasteMode = fluidChanges;
   }

   @Nullable
   public PrototypePlayerBuilderToolSettings.FluidChange[] getFluidChangesForPlaySelectionToolPasteMode() {
      return this.fluidChangesForPlaySelectionToolPasteMode;
   }

   public void setEntityChangesForPlaySelectionToolPasteMode(@Nullable PrototypePlayerBuilderToolSettings.EntityChange[] entityChanges) {
      this.entityChangesForPlaySelectionToolPasteMode = entityChanges;
   }

   @Nullable
   public PrototypePlayerBuilderToolSettings.EntityChange[] getEntityChangesForPlaySelectionToolPasteMode() {
      return this.entityChangesForPlaySelectionToolPasteMode;
   }

   public void setBlockChangeOffsetOrigin(@Nullable Vector3i blockChangeOffsetOrigin) {
      this.blockChangeOffsetOrigin = blockChangeOffsetOrigin;
   }

   @Nullable
   public Vector3i getBlockChangeOffsetOrigin() {
      return this.blockChangeOffsetOrigin;
   }

   public void setLastTransformEntityRefs(@Nullable List<Ref<EntityStore>> refs) {
      this.lastTransformEntityRefs = refs;
   }

   @Nullable
   public List<Ref<EntityStore>> getLastTransformEntityRefs() {
      return this.lastTransformEntityRefs;
   }

   public void clearLastTransformEntityRefs() {
      this.lastTransformEntityRefs = null;
   }

   @Nonnull
   public LongOpenHashSet addIgnoredPaintOperation() {
      LongOpenHashSet longs = new LongOpenHashSet();
      this.ignoredPaintOperations.add(longs);
      this.clearHistoryUntilFitMaxLength();
      return longs;
   }

   public void clearHistoryUntilFitMaxLength() {
      while (this.ignoredPaintOperations.size() > this.maxLengthOfIgnoredPaintOperations) {
         this.ignoredPaintOperations.removeFirst();
      }
   }

   public boolean containsLocation(int x, int y, int z) {
      long packedBlockLocation = BlockUtil.pack(x, y, z);

      for (LongOpenHashSet locations : this.ignoredPaintOperations) {
         if (locations.contains(packedBlockLocation)) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   public LinkedList<LongOpenHashSet> getIgnoredPaintOperations() {
      return this.ignoredPaintOperations;
   }

   public int getMaxLengthOfIgnoredPaintOperations() {
      return this.maxLengthOfIgnoredPaintOperations;
   }

   public void setMaxLengthOfIgnoredPaintOperations(int maxLengthOfIgnoredPaintOperations) {
      this.maxLengthOfIgnoredPaintOperations = maxLengthOfIgnoredPaintOperations;
      this.clearHistoryUntilFitMaxLength();
   }

   public boolean usePrototypeBrushConfigurations() {
      return this.usePrototypeBrushConfigurations;
   }

   public void setUsePrototypeBrushConfigurations(boolean usePrototypeBrushConfigurations) {
      this.usePrototypeBrushConfigurations = usePrototypeBrushConfigurations;
   }

   public BrushConfig getBrushConfig() {
      return this.brushConfig;
   }

   public BrushConfigCommandExecutor getBrushConfigCommandExecutor() {
      return this.brushConfigCommandExecutor;
   }

   public void setBrushConfig(BrushConfig brushConfig) {
      this.brushConfig = brushConfig;
   }

   public boolean isShouldShowEditorSettings() {
      return this.shouldShowEditorSettings;
   }

   public void setShouldShowEditorSettings(boolean shouldShowEditorSettings) {
      this.shouldShowEditorSettings = shouldShowEditorSettings;
   }

   @Nullable
   public Vector3i getLastBrushPosition() {
      return this.lastBrushPosition;
   }

   public void setLastBrushPosition(@Nullable Vector3i lastBrushPosition) {
      this.lastBrushPosition = lastBrushPosition;
   }

   public void clearLastBrushPosition() {
      this.lastBrushPosition = null;
   }

   public static boolean isOkayToDoCommandsOnSelection(Ref<EntityStore> ref, @Nonnull Player player, ComponentAccessor<EntityStore> componentAccessor) {
      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(uuidComponent.getUuid());
      if (prototypeSettings.isInSelectionTransformationMode()) {
         player.sendMessage(MESSAGE_BUILDER_TOOLS_CANNOT_PERFORM_COMMAND_IN_TRANSFORMATION_MODE);
         return false;
      } else {
         return true;
      }
   }

   public record EntityChange(double x, double y, double z, Holder<EntityStore> entityHolder) {
   }

   public record FluidChange(int x, int y, int z, int fluidId, byte fluidLevel) {
   }
}
