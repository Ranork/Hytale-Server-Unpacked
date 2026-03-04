package com.hypixel.hytale.builtin.buildertools.scriptedbrushes;

import com.hypixel.hytale.builtin.buildertools.utils.FluidPatternHelper;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.buildertools.BrushShape;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BrushConfig {
   private static final Random random = new Random();
   @Nullable
   private InteractionType interactionType;
   private boolean isHoldDownInteraction;
   private Vector3i origin;
   private boolean isCurrentlyExecuting;
   private boolean hasExecutionContextEncounteredError;
   @Nullable
   private String executionErrorMessage;
   private Vector3i originOffset = new Vector3i(0, 0, 0);
   @Nullable
   private Vector3i originAfterOffset;
   private BrushShape shape;
   private int shapeWidth;
   private int shapeHeight;
   private int shapeThickness;
   private boolean capped;
   private BlockPattern pattern;
   private int density;
   private boolean enableBrushMask;
   private BlockMask brushMask;
   private boolean enableOperationMask;
   private BlockMask operationMask;
   private BlockMask combinedMasks;
   private BrushConfig.HistoryMask historyMask;

   public BrushConfig() {
      this.resetToDefaultValues();
   }

   public BrushConfig(@Nonnull BrushConfig other) {
      this.interactionType = other.interactionType;
      this.origin = other.origin;
      this.isCurrentlyExecuting = other.isCurrentlyExecuting;
      this.hasExecutionContextEncounteredError = other.hasExecutionContextEncounteredError;
      this.executionErrorMessage = other.executionErrorMessage;
      this.originOffset = other.originOffset.clone();
      this.originAfterOffset = other.originAfterOffset.clone();
      this.shape = other.shape;
      this.shapeWidth = other.shapeWidth;
      this.shapeHeight = other.shapeHeight;
      this.shapeThickness = other.shapeThickness;
      this.capped = other.capped;
      this.pattern = other.pattern;
      this.density = other.density;
      this.enableBrushMask = other.enableBrushMask;
      this.brushMask = other.brushMask;
      this.enableOperationMask = other.enableOperationMask;
      this.operationMask = other.operationMask;
      this.combinedMasks = other.combinedMasks;
      this.historyMask = other.historyMask;
      this.isHoldDownInteraction = other.isHoldDownInteraction;
   }

   public void beginExecution(Vector3i origin, boolean isHoldDownInteraction, InteractionType interactionType) {
      this.isCurrentlyExecuting = true;
      this.origin = origin;
      this.isHoldDownInteraction = isHoldDownInteraction;
      this.interactionType = interactionType;
      this.updateOriginWithOffsets();
   }

   public void endExecution() {
      this.resetToDefaultValues();
   }

   public void resetToDefaultValues() {
      this.interactionType = null;
      this.origin = new Vector3i(0, 0, 0);
      this.isCurrentlyExecuting = false;
      this.hasExecutionContextEncounteredError = false;
      this.executionErrorMessage = null;
      this.originOffset = new Vector3i(0, 0, 0);
      this.originAfterOffset = null;
      this.shape = BrushShape.Sphere;
      this.shapeWidth = 5;
      this.shapeHeight = 5;
      this.shapeThickness = 0;
      this.capped = false;
      this.pattern = BlockPattern.parse("Rock_Stone");
      this.density = 100;
      this.enableBrushMask = true;
      this.brushMask = BlockMask.EMPTY;
      this.enableOperationMask = true;
      this.operationMask = BlockMask.EMPTY;
      this.combinedMasks = BlockMask.EMPTY;
      this.historyMask = BrushConfig.HistoryMask.None;
   }

   public boolean isHoldDownInteraction() {
      return this.isHoldDownInteraction;
   }

   public boolean isCurrentlyExecuting() {
      return this.isCurrentlyExecuting;
   }

   @Nullable
   public InteractionType getInteractionType() {
      return this.interactionType;
   }

   @Nullable
   public Vector3i getOrigin() {
      return this.originAfterOffset;
   }

   @Nonnull
   public Vector3i getOriginOffset() {
      return this.originOffset.clone();
   }

   public void setOriginOffset(Vector3i originOffset) {
      this.originOffset = originOffset;
      this.updateOriginWithOffsets();
   }

   public void modifyOriginOffset(@Nonnull Vector3i originOffsetOffset) {
      this.originOffset = this.originOffset.add(originOffsetOffset);
      this.updateOriginWithOffsets();
   }

   public void updateOriginWithOffsets() {
      if (this.origin != null) {
         this.originAfterOffset = this.origin.clone().add(this.originOffset);
      }
   }

   @Nonnull
   public Random getRandom() {
      return random;
   }

   public int getNextBlock() {
      return this.pattern.nextBlock(random);
   }

   @Nonnull
   public Material getNextMaterial() {
      BlockPattern.BlockEntry blockEntry = this.pattern.nextBlockTypeKey(random);
      if (blockEntry != null) {
         FluidPatternHelper.FluidInfo fluidInfo = FluidPatternHelper.getFluidInfo(blockEntry.blockTypeKey());
         if (fluidInfo != null) {
            return Material.fluid(fluidInfo.fluidId(), fluidInfo.fluidLevel());
         }
      }

      return Material.block(this.pattern.nextBlock(random));
   }

   public BlockMask getBlockMask() {
      return this.combinedMasks;
   }

   public void setOperationMask(BlockMask mask) {
      this.operationMask = mask;
      this.refreshCombinedMasks();
   }

   public void appendOperationMask(BlockMask mask) {
      this.operationMask = BlockMask.combine(mask, this.operationMask);
      this.refreshCombinedMasks();
   }

   public void clearOperationMask() {
      this.operationMask = BlockMask.EMPTY;
      this.refreshCombinedMasks();
   }

   public void setUseBrushMask(boolean useBrushMask) {
      this.enableBrushMask = useBrushMask;
      this.refreshCombinedMasks();
   }

   public void setUseOperationMask(boolean useOperationMask) {
      this.enableOperationMask = useOperationMask;
      this.refreshCombinedMasks();
   }

   public void setBrushMask(BlockMask mask) {
      this.brushMask = mask;
      this.refreshCombinedMasks();
   }

   private void refreshCombinedMasks() {
      if (this.enableBrushMask && this.enableOperationMask) {
         this.combinedMasks = BlockMask.combine(this.brushMask, this.operationMask);
      } else if (this.enableBrushMask) {
         this.combinedMasks = this.brushMask;
      } else if (this.enableOperationMask) {
         this.combinedMasks = this.operationMask;
      }
   }

   public int getDensity() {
      return this.density;
   }

   public void setDensity(int density) {
      this.density = MathUtil.clamp(density, 1, 100);
   }

   public BrushConfig.HistoryMask getHistoryMask() {
      return this.historyMask;
   }

   public void setHistoryMask(BrushConfig.HistoryMask historyMask) {
      this.historyMask = historyMask;
   }

   public int getShapeWidth() {
      return this.shapeWidth;
   }

   public void setShapeWidth(int shapeWidth) {
      if (shapeWidth <= 0) {
         this.setErrorFlag("You cannot set shape width to be less than or equal to zero. Width: " + shapeWidth);
      } else {
         this.shapeWidth = shapeWidth;
      }
   }

   public int getShapeHeight() {
      return this.shapeHeight;
   }

   public void setShapeHeight(int shapeHeight) {
      if (shapeHeight <= 0) {
         this.setErrorFlag("You cannot set shape height to be less than or equal to zero. Height: " + shapeHeight);
      } else {
         this.shapeHeight = shapeHeight;
      }
   }

   public int getShapeThickness() {
      return this.shapeThickness;
   }

   public void setShapeThickness(int shapeThickness) {
      this.shapeThickness = shapeThickness;
   }

   public boolean isCapped() {
      return this.capped;
   }

   public void setCapped(boolean capped) {
      this.capped = capped;
   }

   public BrushShape getShape() {
      return this.shape;
   }

   public void setShape(BrushShape shape) {
      this.shape = shape;
   }

   public BlockPattern getPattern() {
      return this.pattern;
   }

   public void setPattern(BlockPattern pattern) {
      this.pattern = pattern;
   }

   public void setErrorFlag(String errorMessage) {
      this.hasExecutionContextEncounteredError = true;
      this.executionErrorMessage = errorMessage;
   }

   public void clearError() {
      this.hasExecutionContextEncounteredError = false;
      this.executionErrorMessage = null;
   }

   public boolean isHasExecutionContextEncounteredError() {
      return this.hasExecutionContextEncounteredError;
   }

   @Nullable
   public String getExecutionErrorMessage() {
      return this.executionErrorMessage;
   }

   @Nullable
   public Vector3i getOriginAfterOffset() {
      return this.originAfterOffset;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BrushConfig{, interactionType="
         + this.interactionType
         + ", origin="
         + this.origin
         + ", originOffset="
         + this.originOffset
         + ", originAfterOffset="
         + this.originAfterOffset
         + ", shapeWidth="
         + this.shapeWidth
         + ", shapeHeight="
         + this.shapeHeight
         + ", shapeThickness="
         + this.shapeThickness
         + ", capped="
         + this.capped
         + ", density="
         + this.density
         + ", shape="
         + this.shape
         + ", pattern="
         + this.pattern
         + ", historyMask="
         + this.historyMask
         + ", brushMask="
         + this.brushMask
         + ", operationMask="
         + this.operationMask
         + ", combinedMasks="
         + this.combinedMasks
         + ", enableOperationMask="
         + this.enableOperationMask
         + ", enableBrushMask="
         + this.enableBrushMask
         + ", random="
         + random
         + ", isCurrentlyExecuting="
         + this.isCurrentlyExecuting
         + "}";
   }

   @Nonnull
   public String getInfo() {
      StringBuilder builder = new StringBuilder("Brush Config Information:");
      builder.append("\nOrigin with Offset: {");
      if (this.originAfterOffset == null) {
         builder.append("Not currently executing, so no origin");
      } else {
         builder.append(this.originAfterOffset.x).append(", ").append(this.originAfterOffset.y).append(", ").append(this.originAfterOffset.z);
      }

      builder.append("}");
      builder.append("\nOffset: {").append(this.originOffset.x).append(", ").append(this.originOffset.y).append(", ").append(this.originOffset.z).append("}");
      builder.append("\nDimensions: {Width: ").append(this.shapeWidth).append(", Height: ").append(this.shapeHeight).append("}");
      builder.append("\nShape Properties: {Shape: ")
         .append(this.shape.name())
         .append(", Thickness: ")
         .append(this.shapeThickness)
         .append(", Capped: ")
         .append(this.capped)
         .append("}");
      builder.append("\nPattern: ").append(this.pattern.toString());
      builder.append("\nMasks: {HistoryMask: ")
         .append(this.historyMask.name())
         .append(", EnableOperationMask: ")
         .append(this.enableOperationMask)
         .append(", EnableBrushMask: ")
         .append(this.enableBrushMask)
         .append(", CombinedMasks: ")
         .append(this.combinedMasks.informativeToString())
         .append("}");
      builder.append("\nIs Currently Executing: ").append(this.isCurrentlyExecuting);
      return builder.toString();
   }

   public static enum BCExecutionStatus {
      Continue,
      Error,
      Complete;
   }

   public static enum DataGettingFlags {
      OffsetX(brushConfig -> brushConfig.getOriginOffset().x),
      OffsetY(brushConfig -> brushConfig.getOriginOffset().y),
      OffsetZ(brushConfig -> brushConfig.getOriginOffset().z),
      Height(brushConfig -> brushConfig.getShapeHeight()),
      Width(brushConfig -> brushConfig.getShapeWidth()),
      Density(brushConfig -> brushConfig.getDensity());

      private final Function<BrushConfig, Integer> valueGetter;

      private DataGettingFlags(Function<BrushConfig, Integer> valueGetter) {
         this.valueGetter = valueGetter;
      }

      public int getValue(BrushConfig brushConfig) {
         return this.valueGetter.apply(brushConfig);
      }
   }

   public static enum DataSettingFlags {
      Offset((copyTo, copyFrom) -> copyTo.setOriginOffset(copyFrom.getOriginOffset())),
      Shape((copyTo, copyFrom) -> copyTo.setShape(copyFrom.getShape())),
      Dimensions((copyTo, copyFrom) -> {
         copyTo.setShapeWidth(copyFrom.getShapeWidth());
         copyTo.setShapeHeight(copyFrom.getShapeHeight());
      }),
      Thickness((copyTo, copyFrom) -> copyTo.setShapeThickness(copyFrom.getShapeThickness())),
      Capped((copyTo, copyFrom) -> copyTo.setCapped(copyFrom.isCapped())),
      Pattern((copyTo, copyFrom) -> copyTo.setPattern(copyFrom.getPattern())),
      Density((copyTo, copyFrom) -> copyTo.setDensity(copyFrom.getDensity())),
      BrushMask((copyTo, copyFrom) -> {
         copyTo.setBrushMask(copyFrom.brushMask);
         copyTo.setUseBrushMask(copyFrom.enableBrushMask);
      }),
      OperationMask((copyTo, copyFrom) -> {
         copyTo.setOperationMask(copyFrom.operationMask);
         copyTo.setUseOperationMask(copyFrom.enableOperationMask);
      }),
      HistoryMask((copyTo, copyFrom) -> copyTo.setHistoryMask(copyFrom.getHistoryMask()));

      private final BiConsumer<BrushConfig, BrushConfig> stateLoader;

      private DataSettingFlags(BiConsumer<BrushConfig, BrushConfig> stateLoader) {
         this.stateLoader = stateLoader;
      }

      public void loadData(BrushConfig copyTo, BrushConfig copyFrom) {
         this.stateLoader.accept(copyTo, copyFrom);
      }
   }

   public static enum HistoryMask {
      None,
      Only,
      Not;
   }
}
