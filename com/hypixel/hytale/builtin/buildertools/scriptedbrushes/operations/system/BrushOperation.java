package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.global.DebugBrushOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.global.DisableHoldInteractionOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.global.IgnoreExistingBrushDataOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.BlockPatternOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.BreakpointOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.ClearOperationMaskOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.DeleteOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.EchoOnceOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.EchoOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.ErodeOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.HeightmapLayerOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.LayerOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.LiftOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.LoadIntFromToolArgOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.LoadMaterialFromToolArgOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.MaterialOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.MeltOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.PastePrefabOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.ReplaceOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.RunCommandOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.SetDensity;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.SetOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.ShapeOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.SmoothOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.dimensions.DimensionsOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.dimensions.RandomizeDimensionsOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.ExitOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.JumpIfBlockTypeOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.JumpIfClickType;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.JumpIfCompareOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.JumpIfStringMatchOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.JumpIfToolArgOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.JumpToIndexOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.JumpToRandomIndex;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.loops.CircleOffsetAndLoopOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.loops.CircleOffsetFromArgOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.loops.LoadLoopFromToolArgOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.loops.LoopOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.loops.LoopRandomOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks.AppendMaskFromToolArgOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks.AppendMaskOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks.HistoryMaskOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks.MaskOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks.UseBrushMaskOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks.UseOperationMaskOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.offsets.OffsetOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.offsets.RandomOffsetOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.saveandload.LoadBrushConfigOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.saveandload.LoadOperationsFromAssetOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.saveandload.PersistentDataOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.saveandload.SaveBrushConfigOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.saveandload.SaveIndexOperation;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public abstract class BrushOperation {
   public static final CodecMapCodec<BrushOperation> OPERATION_CODEC = new CodecMapCodec<>("Id");
   public static final Map<String, Supplier<BrushOperation>> BRUSH_OPERATION_REGISTRY = new ConcurrentHashMap<>();
   private final String name;
   private final String description;
   private final Map<String, BrushOperationSetting<?>> registeredOperationSettings = new LinkedHashMap<>();

   public BrushOperation(String name, String description) {
      this.name = name;
      this.description = description;
   }

   public abstract void modifyBrushConfig(
      @Nonnull Ref<EntityStore> var1, @Nonnull BrushConfig var2, @Nonnull BrushConfigCommandExecutor var3, @Nonnull ComponentAccessor<EntityStore> var4
   );

   public void resetInternalState() {
   }

   public void preExecutionModifyBrushConfig(BrushConfigCommandExecutor brushConfigCommandExecutor, int operationIndex) {
   }

   @Nonnull
   public <T> BrushOperationSetting<T> createBrushSetting(@Nonnull String name, String description, T defaultValue, ArgumentType<T> argumentType) {
      BrushOperationSetting<T> brushOperationSetting = new BrushOperationSetting<>(name, description, defaultValue, argumentType);
      this.registeredOperationSettings.put(name.toLowerCase(), brushOperationSetting);
      return brushOperationSetting;
   }

   @Nonnull
   public <T> BrushOperationSetting<T> createBrushSetting(
      @Nonnull String name, String description, T defaultValue, ArgumentType<T> argumentType, Function<BrushOperationSetting<T>, String> toStringFunction
   ) {
      BrushOperationSetting<T> brushOperationSetting = new BrushOperationSetting<>(name, description, defaultValue, argumentType, toStringFunction);
      this.registeredOperationSettings.put(name.toLowerCase(), brushOperationSetting);
      return brushOperationSetting;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   @Nonnull
   public Map<String, BrushOperationSetting<?>> getRegisteredOperationSettings() {
      return this.registeredOperationSettings;
   }

   static {
      BRUSH_OPERATION_REGISTRY.put("dimensions", DimensionsOperation::new);
      BRUSH_OPERATION_REGISTRY.put("randomdimensions", RandomizeDimensionsOperation::new);
      BRUSH_OPERATION_REGISTRY.put("runcommand", RunCommandOperation::new);
      BRUSH_OPERATION_REGISTRY.put("historymask", HistoryMaskOperation::new);
      BRUSH_OPERATION_REGISTRY.put("mask", MaskOperation::new);
      BRUSH_OPERATION_REGISTRY.put("clearoperationmask", ClearOperationMaskOperation::new);
      BRUSH_OPERATION_REGISTRY.put("usebrushmask", UseBrushMaskOperation::new);
      BRUSH_OPERATION_REGISTRY.put("useoperationmask", UseOperationMaskOperation::new);
      BRUSH_OPERATION_REGISTRY.put("appendmask", AppendMaskOperation::new);
      BRUSH_OPERATION_REGISTRY.put("appendmaskfromtoolarg", AppendMaskFromToolArgOperation::new);
      BRUSH_OPERATION_REGISTRY.put("ignorebrushsettings", IgnoreExistingBrushDataOperation::new);
      BRUSH_OPERATION_REGISTRY.put("debug", DebugBrushOperation::new);
      BRUSH_OPERATION_REGISTRY.put("loop", LoopOperation::new);
      BRUSH_OPERATION_REGISTRY.put("loadloop", LoadLoopFromToolArgOperation::new);
      BRUSH_OPERATION_REGISTRY.put("looprandom", LoopRandomOperation::new);
      BRUSH_OPERATION_REGISTRY.put("loopcircle", CircleOffsetAndLoopOperation::new);
      BRUSH_OPERATION_REGISTRY.put("loopcirclefromarg", CircleOffsetFromArgOperation::new);
      BRUSH_OPERATION_REGISTRY.put("savebrushconfig", SaveBrushConfigOperation::new);
      BRUSH_OPERATION_REGISTRY.put("loadbrushconfig", LoadBrushConfigOperation::new);
      BRUSH_OPERATION_REGISTRY.put("saveindex", SaveIndexOperation::new);
      BRUSH_OPERATION_REGISTRY.put("loadoperationsfromasset", LoadOperationsFromAssetOperation::new);
      BRUSH_OPERATION_REGISTRY.put("jump", JumpToIndexOperation::new);
      BRUSH_OPERATION_REGISTRY.put("exit", ExitOperation::new);
      BRUSH_OPERATION_REGISTRY.put("jumprandom", JumpToRandomIndex::new);
      BRUSH_OPERATION_REGISTRY.put("jumpifequal", JumpIfStringMatchOperation::new);
      BRUSH_OPERATION_REGISTRY.put("jumpifclicktype", JumpIfClickType::new);
      BRUSH_OPERATION_REGISTRY.put("jumpifcompare", JumpIfCompareOperation::new);
      BRUSH_OPERATION_REGISTRY.put("jumpifblocktype", JumpIfBlockTypeOperation::new);
      BRUSH_OPERATION_REGISTRY.put("jumpiftoolarg", JumpIfToolArgOperation::new);
      BRUSH_OPERATION_REGISTRY.put("pattern", BlockPatternOperation::new);
      BRUSH_OPERATION_REGISTRY.put("loadmaterial", LoadMaterialFromToolArgOperation::new);
      BRUSH_OPERATION_REGISTRY.put("loadint", LoadIntFromToolArgOperation::new);
      BRUSH_OPERATION_REGISTRY.put("lift", LiftOperation::new);
      BRUSH_OPERATION_REGISTRY.put("density", SetDensity::new);
      BRUSH_OPERATION_REGISTRY.put("set", SetOperation::new);
      BRUSH_OPERATION_REGISTRY.put("smooth", SmoothOperation::new);
      BRUSH_OPERATION_REGISTRY.put("shape", ShapeOperation::new);
      BRUSH_OPERATION_REGISTRY.put("offset", OffsetOperation::new);
      BRUSH_OPERATION_REGISTRY.put("layer", LayerOperation::new);
      BRUSH_OPERATION_REGISTRY.put("heightmaplayer", HeightmapLayerOperation::new);
      BRUSH_OPERATION_REGISTRY.put("melt", MeltOperation::new);
      BRUSH_OPERATION_REGISTRY.put("material", MaterialOperation::new);
      BRUSH_OPERATION_REGISTRY.put("delete", DeleteOperation::new);
      BRUSH_OPERATION_REGISTRY.put("disableonhold", DisableHoldInteractionOperation::new);
      BRUSH_OPERATION_REGISTRY.put("randomoffset", RandomOffsetOperation::new);
      BRUSH_OPERATION_REGISTRY.put("erode", ErodeOperation::new);
      BRUSH_OPERATION_REGISTRY.put("persistentdata", PersistentDataOperation::new);
      BRUSH_OPERATION_REGISTRY.put("pasteprefab", PastePrefabOperation::new);
      BRUSH_OPERATION_REGISTRY.put("echo", EchoOperation::new);
      BRUSH_OPERATION_REGISTRY.put("echoonce", EchoOnceOperation::new);
      BRUSH_OPERATION_REGISTRY.put("replace", ReplaceOperation::new);
      BRUSH_OPERATION_REGISTRY.put("breakpoint", BreakpointOperation::new);
   }
}
