package com.hypixel.hytale.builtin.buildertools.scriptedbrushes;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.GlobalBrushOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BrushConfigCommandExecutor {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final Map<String, Integer> persistentStoredVariables;
   private final BrushConfig brushConfig;
   @Nonnull
   private final Map<String, GlobalBrushOperation> globalOperations;
   private int currentOperationIndex = 0;
   @Nonnull
   private final List<SequenceBrushOperation> sequentialOperations;
   private boolean inDebugSteppingMode;
   private boolean printOperations;
   private boolean enableBreakpoints;
   private BrushConfigCommandExecutor.DebugOutputTarget debugOutputTarget = BrushConfigCommandExecutor.DebugOutputTarget.Chat;
   private boolean breakOnError;
   @Nonnull
   private final Map<String, BrushConfig> brushConfigStoredSnapshots;
   private boolean allowOverwritingSavedSnapshots = true;
   @Nonnull
   private final Map<String, Integer> storedIndexes;
   private boolean ignoreExistingBrushData;
   private BrushConfigEditStore edit;
   private long startTime;

   public BrushConfigCommandExecutor(BrushConfig brushConfig) {
      this.persistentStoredVariables = new Object2IntOpenHashMap();
      this.sequentialOperations = new ObjectArrayList();
      this.globalOperations = new Object2ObjectOpenHashMap();
      this.brushConfigStoredSnapshots = new Object2ObjectOpenHashMap();
      this.storedIndexes = new Object2IntOpenHashMap();
      this.brushConfig = brushConfig;
   }

   public void resetInternalState() {
      this.currentOperationIndex = 0;
      this.brushConfigStoredSnapshots.clear();
      this.ignoreExistingBrushData = false;
      this.printOperations = false;
      this.inDebugSteppingMode = false;
      this.enableBreakpoints = false;
      this.debugOutputTarget = BrushConfigCommandExecutor.DebugOutputTarget.Chat;
      this.breakOnError = false;
      this.startTime = System.nanoTime();
      this.storedIndexes.clear();
   }

   public void execute(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Vector3i origin,
      boolean isHoldDownInteraction,
      @Nonnull InteractionType interactionType,
      @Nullable Consumer<BrushConfig> existingBrushDataLoadingConsumer,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      this.resetInternalState();
      this.brushConfig.resetToDefaultValues();
      this.brushConfig.beginExecution(origin, isHoldDownInteraction, interactionType);
      PrototypePlayerBuilderToolSettings prototypePlayerBuilderToolSettings = ToolOperation.PROTOTYPE_TOOL_SETTINGS.get(uuidComponent.getUuid());
      if (!isHoldDownInteraction) {
         prototypePlayerBuilderToolSettings.getIgnoredPaintOperations().clear();
      }

      this.edit = new BrushConfigEditStore(prototypePlayerBuilderToolSettings.addIgnoredPaintOperation(), this.brushConfig, world);

      for (int i = 0; i < this.sequentialOperations.size(); i++) {
         this.sequentialOperations.get(i).resetInternalState();
         this.sequentialOperations.get(i).preExecutionModifyBrushConfig(this, i);
      }

      for (GlobalBrushOperation globalOperation : this.globalOperations.values()) {
         globalOperation.resetInternalState();
         globalOperation.modifyBrushConfig(ref, this.brushConfig, this, componentAccessor);
      }

      if (!this.ignoreExistingBrushData && existingBrushDataLoadingConsumer != null) {
         existingBrushDataLoadingConsumer.accept(this.brushConfig);
      }

      if (!this.inDebugSteppingMode) {
         while (
            this.brushConfig.isCurrentlyExecuting()
               && !this.inDebugSteppingMode
               && this.step(ref, false, componentAccessor).equals(BrushConfig.BCExecutionStatus.Continue)
         ) {
         }
      }
   }

   public void execute(
      @Nonnull Ref<EntityStore> ref,
      World world,
      Vector3i origin,
      boolean isHoldDownInteraction,
      InteractionType interactionType,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.execute(ref, world, origin, isHoldDownInteraction, interactionType, null, componentAccessor);
   }

   @Nonnull
   public BrushConfig.BCExecutionStatus step(Ref<EntityStore> ref, boolean placePreviewAfterStep, ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.brushConfig.isCurrentlyExecuting()) {
         return BrushConfig.BCExecutionStatus.Error;
      } else if (this.sequentialOperations.isEmpty()) {
         this.brushConfig.setErrorFlag("No operations to execute");
         return this.completeStep(ref, placePreviewAfterStep, componentAccessor);
      } else {
         try {
            SequenceBrushOperation brushOperation = this.sequentialOperations.get(this.currentOperationIndex);
            if (this.printOperations) {
               LOGGER.at(Level.INFO).log("[%d] %s", this.currentOperationIndex, brushOperation.getName());
               PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());
               if (playerRefComponent != null) {
                  playerRefComponent.sendMessage(
                     Message.translation("server.builderTools.brushConfig.debug.operationExecuting")
                        .param("index", this.currentOperationIndex)
                        .param("name", brushOperation.getName())
                  );
               }
            }

            brushOperation.modifyBrushConfig(ref, this.brushConfig, this, componentAccessor);
            if (this.brushConfig.isHasExecutionContextEncounteredError() || !brushOperation.doesOperateOnBlocks()) {
               return this.completeStep(ref, placePreviewAfterStep, componentAccessor);
            }

            int numModifyBlockIterations = brushOperation.getNumModifyBlockIterations();

            for (int i = 0; i < numModifyBlockIterations; i++) {
               brushOperation.beginIterationIndex(i);
               ToolOperation.executeShapeOperation(
                  this.brushConfig.getOriginAfterOffset().x,
                  this.brushConfig.getOriginAfterOffset().y,
                  this.brushConfig.getOriginAfterOffset().z,
                  (x, y, z, unused) -> brushOperation.modifyBlocks(ref, this.brushConfig, this, this.edit, x, y, z, componentAccessor),
                  this.brushConfig.getShape(),
                  this.brushConfig.getShapeWidth(),
                  this.brushConfig.getShapeHeight(),
                  this.brushConfig.getShapeThickness(),
                  this.brushConfig.isCapped()
               );
               this.edit.flushCurrentEditsToPrevious();
            }
         } catch (Exception var7) {
            var7.printStackTrace();
            this.brushConfig.setErrorFlag(var7.getMessage());
         }

         return this.completeStep(ref, placePreviewAfterStep, componentAccessor);
      }
   }

   @Nonnull
   private BrushConfig.BCExecutionStatus completeStep(Ref<EntityStore> ref, boolean placePreviewAfterStep, ComponentAccessor<EntityStore> componentAccessor) {
      if (this.brushConfig.isHasExecutionContextEncounteredError()) {
         if (this.breakOnError) {
            PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());
            if (playerRefComponent != null) {
               playerRefComponent.sendMessage(
                  Message.translation("server.builderTools.brushConfig.debug.breakOnErrorTriggered").param("index", this.currentOperationIndex)
               );
            }

            LOGGER.at(Level.INFO).log("[Breakpoint] Error at operation #%d - Entering step-through mode", this.currentOperationIndex);
            this.inDebugSteppingMode = true;
            this.brushConfig.clearError();
            return BrushConfig.BCExecutionStatus.Continue;
         } else {
            this.exitExecution(ref, componentAccessor);
            return BrushConfig.BCExecutionStatus.Error;
         }
      } else {
         this.currentOperationIndex++;
         if (this.currentOperationIndex >= this.sequentialOperations.size()) {
            this.exitExecution(ref, componentAccessor);
            return BrushConfig.BCExecutionStatus.Complete;
         } else {
            Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

            assert playerComponent != null;

            PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

            assert playerRefComponent != null;

            if (placePreviewAfterStep) {
               BrushConfigEditStore returnEdit = this.edit;
               BuilderToolsPlugin.getState(playerComponent, playerRefComponent).addToQueue((r, s, c) -> s.placeBrushConfig(r, this.startTime, returnEdit, c));
            }

            return BrushConfig.BCExecutionStatus.Continue;
         }
      }
   }

   public void exitExecution(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      if (this.brushConfig.isHasExecutionContextEncounteredError() && this.currentOperationIndex < this.sequentialOperations.size()) {
         this.sendExecutionErrorMessage(playerRefComponent, this.sequentialOperations.get(this.currentOperationIndex));
      }

      if (this.currentOperationIndex != 0) {
         BrushConfigEditStore returnEdit = this.edit;
         BuilderToolsPlugin.getState(playerComponent, playerRefComponent)
            .addToQueue((r, s, compAccess) -> s.placeBrushConfig(r, this.startTime, returnEdit, compAccess));
      }

      this.brushConfig.endExecution();
   }

   private void sendExecutionErrorMessage(PlayerRef playerRef, @Nonnull SequenceBrushOperation brushOperation) {
      playerRef.sendMessage(
         Message.translation("server.builderTools.brushConfig.executionError")
            .param("index", this.currentOperationIndex)
            .param("type", brushOperation.getName())
            .param("error", this.brushConfig.getExecutionErrorMessage())
      );
      Message header = Message.translation("server.builderTools.brushConfig.settings.header");
      Set<Message> items = brushOperation.getRegisteredOperationSettings()
         .values()
         .stream()
         .map(
            value -> Message.translation("server.builderTools.brushConfig.settings.item").param("name", value.getName()).param("value", value.getValueString())
         )
         .collect(Collectors.toSet());
      playerRef.sendMessage(MessageFormat.list(header, items));
   }

   public void storeOperatingIndex(String name, int index) {
      name = name.toLowerCase();
      if (this.storedIndexes.containsKey(name)) {
         this.brushConfig.setErrorFlag("You already have a stored index with the name: '" + name + "'.");
      } else if (index >= this.sequentialOperations.size()) {
         this.brushConfig
            .setErrorFlag("Tried to store an index greater than the total size of sequential operations. Name: '" + name + "', index: '" + index + "'.");
      } else {
         this.storedIndexes.put(name, index);
      }
   }

   public void loadOperatingIndex(String name) {
      this.loadOperatingIndex(name, true);
   }

   public void loadOperatingIndex(String name, boolean allowFutureJump) {
      name = name.toLowerCase();
      if (!this.storedIndexes.containsKey(name)) {
         this.brushConfig.setErrorFlag("Could not find a stored index with the name: '" + name + "'.");
      } else {
         int newIndex = this.storedIndexes.get(name);
         if (!allowFutureJump && newIndex > this.currentOperationIndex) {
            this.brushConfig.setErrorFlag("This operation does not allow you to jump to an operation in the future, only the past. Index name: " + name + "'.");
         } else {
            this.currentOperationIndex = newIndex;
         }
      }
   }

   public void clearAllPersistentVariables() {
      this.persistentStoredVariables.clear();
   }

   public void clearPersistentVariable(String variableName) {
      variableName = variableName.toLowerCase();
      if (!this.persistentStoredVariables.containsKey(variableName)) {
         this.brushConfig.setErrorFlag("Could not find a stored persistent variable with the name: '" + variableName + "'.");
      } else {
         this.persistentStoredVariables.remove(variableName);
      }
   }

   public void setPersistentVariable(String variableName, int value) {
      variableName = variableName.toLowerCase();
      this.persistentStoredVariables.put(variableName, value);
   }

   public int getPersistentVariableOrDefault(String variableName, int defaultValue) {
      variableName = variableName.toLowerCase();
      return !this.persistentStoredVariables.containsKey(variableName) ? defaultValue : this.persistentStoredVariables.get(variableName);
   }

   public void storeBrushConfigSnapshot(@Nonnull String name) {
      this.brushConfigStoredSnapshots.put(name.toLowerCase(), new BrushConfig(this.brushConfig));
   }

   public void loadBrushConfigSnapshot(String name, @Nonnull BrushConfig.DataSettingFlags... dataToLoad) {
      name = name.toLowerCase();
      if (!this.brushConfigStoredSnapshots.containsKey(name)) {
         this.brushConfig.setErrorFlag("Could not find a stored brush config snapshot with the name: '" + name + "'.");
      } else {
         BrushConfig loadedBrushConfig = this.brushConfigStoredSnapshots.get(name);

         for (BrushConfig.DataSettingFlags dataLoadFlag : dataToLoad) {
            dataLoadFlag.loadData(this.brushConfig, loadedBrushConfig);
         }
      }
   }

   public void setAllowOverwritingSavedSnapshots(boolean allowOverwritingSavedSnapshots) {
      this.allowOverwritingSavedSnapshots = allowOverwritingSavedSnapshots;
   }

   @Nonnull
   public List<SequenceBrushOperation> getSequentialOperations() {
      return this.sequentialOperations;
   }

   @Nonnull
   public Map<String, GlobalBrushOperation> getGlobalOperations() {
      return this.globalOperations;
   }

   public boolean isIgnoreExistingBrushData() {
      return this.ignoreExistingBrushData;
   }

   public boolean isInDebugSteppingMode() {
      return this.inDebugSteppingMode;
   }

   public BrushConfigEditStore getEdit() {
      return this.edit;
   }

   public void setInDebugSteppingMode(boolean inDebugSteppingMode) {
      this.inDebugSteppingMode = inDebugSteppingMode;
   }

   public void setPrintOperations(boolean printOperations) {
      this.printOperations = printOperations;
   }

   public void setIgnoreExistingBrushData(boolean ignoreExistingBrushData) {
      this.ignoreExistingBrushData = ignoreExistingBrushData;
   }

   public void setCurrentlyExecutingActionIndex(int newCurrentOperationIndex) {
      if (newCurrentOperationIndex < 0) {
         this.brushConfig.setErrorFlag("Cannot set a negative operation index: " + newCurrentOperationIndex);
      } else if (newCurrentOperationIndex >= this.sequentialOperations.size()) {
         this.brushConfig
            .setErrorFlag(
               "Cannot set an operation index higher than the highest operation index: "
                  + newCurrentOperationIndex
                  + ". Highest operation index: "
                  + (this.sequentialOperations.size() - 1)
            );
      } else {
         this.currentOperationIndex = newCurrentOperationIndex - 1;
      }
   }

   public int getCurrentOperationIndex() {
      return this.currentOperationIndex;
   }

   public boolean isEnableBreakpoints() {
      return this.enableBreakpoints;
   }

   public void setEnableBreakpoints(boolean enableBreakpoints) {
      this.enableBreakpoints = enableBreakpoints;
   }

   public BrushConfigCommandExecutor.DebugOutputTarget getDebugOutputTarget() {
      return this.debugOutputTarget;
   }

   public void setDebugOutputTarget(BrushConfigCommandExecutor.DebugOutputTarget debugOutputTarget) {
      this.debugOutputTarget = debugOutputTarget;
   }

   public boolean isBreakOnError() {
      return this.breakOnError;
   }

   public void setBreakOnError(boolean breakOnError) {
      this.breakOnError = breakOnError;
   }

   public static enum DebugOutputTarget {
      Chat,
      Console,
      Both;
   }
}
