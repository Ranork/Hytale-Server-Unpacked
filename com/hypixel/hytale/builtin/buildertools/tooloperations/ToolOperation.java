package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.EditOperation;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.tooloperations.transform.Mirror;
import com.hypixel.hytale.builtin.buildertools.tooloperations.transform.Rotate;
import com.hypixel.hytale.builtin.buildertools.tooloperations.transform.Transform;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.math.block.BlockConeUtil;
import com.hypixel.hytale.math.block.BlockCubeUtil;
import com.hypixel.hytale.math.block.BlockCylinderUtil;
import com.hypixel.hytale.math.block.BlockDiamondUtil;
import com.hypixel.hytale.math.block.BlockDomeUtil;
import com.hypixel.hytale.math.block.BlockInvertedDomeUtil;
import com.hypixel.hytale.math.block.BlockPyramidUtil;
import com.hypixel.hytale.math.block.BlockSphereUtil;
import com.hypixel.hytale.math.block.BlockTorusUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.buildertools.BrushAxis;
import com.hypixel.hytale.protocol.packets.buildertools.BrushOrigin;
import com.hypixel.hytale.protocol.packets.buildertools.BrushShape;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BrushData;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockFilter;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ToolOperation implements TriIntObjPredicate<Void> {
   protected static final int RANDOM_MAX = 100;
   @Nonnull
   public static final Map<String, OperationFactory> OPERATIONS = new ConcurrentHashMap<>();
   @Nonnull
   public static final Map<UUID, PrototypePlayerBuilderToolSettings> PROTOTYPE_TOOL_SETTINGS = new ConcurrentHashMap<>();
   public static final double MAX_DISTANCE = 400.0;
   public static final int DEFAULT_BRUSH_SPACING = 0;
   protected final int x;
   protected final int y;
   protected final int z;
   protected final InteractionType interactionType;
   protected final int shapeRange;
   protected final int shapeHeight;
   protected final int shapeThickness;
   protected final boolean capped;
   protected final int originOffsetX;
   protected final int originOffsetY;
   protected final int originOffsetZ;
   protected final BrushShape shape;
   protected final BlockPattern pattern;
   @Nonnull
   protected final EditOperation edit;
   @Nonnull
   protected final BuilderTool.ArgData args;
   @Nonnull
   protected final Random random;
   @Nonnull
   protected final Player player;
   @Nonnull
   protected final Ref<EntityStore> playerRef;
   @Nonnull
   protected final BuilderToolsPlugin.BuilderState builderState;
   private final Transform transform;
   private final Vector3i vector = new Vector3i();
   @Nullable
   private final BlockMask mask;

   public ToolOperation(@Nonnull Ref<EntityStore> ref, @Nonnull BuilderToolOnUseInteraction packet, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.playerRef = ref;
      World world = componentAccessor.getExternalData().getWorld();
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      this.player = playerComponent;
      this.builderState = BuilderToolsPlugin.getState(playerComponent, playerRefComponent);
      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      UUID uuid = uuidComponent.getUuid();
      PrototypePlayerBuilderToolSettings playerBuilderToolSettings = PROTOTYPE_TOOL_SETTINGS.get(uuid);
      if (playerBuilderToolSettings == null) {
         playerBuilderToolSettings = new PrototypePlayerBuilderToolSettings(uuid);
         PROTOTYPE_TOOL_SETTINGS.put(uuid, playerBuilderToolSettings);
      }

      playerBuilderToolSettings.setShouldShowEditorSettings(packet.isShowEditNotifications);
      playerBuilderToolSettings.setMaxLengthOfIgnoredPaintOperations(packet.maxLengthToolIgnoreHistory);
      if (!packet.isHoldDownInteraction && (this instanceof PaintOperation || this instanceof SculptOperation)) {
         playerBuilderToolSettings.getIgnoredPaintOperations().clear();
         playerBuilderToolSettings.clearLastBrushPosition();
      }

      if (packet.isDoServerRaytraceForPosition && (this instanceof PaintOperation || this instanceof SculptOperation)) {
         Vector3i targetBlockAvoidingPaint = this.getTargetBlockAvoidingPaint(
            ref,
            400.0,
            componentAccessor,
            packet.raycastOriginX,
            packet.raycastOriginY,
            packet.raycastOriginZ,
            packet.raycastDirectionX,
            packet.raycastDirectionY,
            packet.raycastDirectionZ
         );
         if (targetBlockAvoidingPaint != null) {
            this.x = targetBlockAvoidingPaint.x + packet.offsetForPaintModeX;
            this.y = targetBlockAvoidingPaint.y + packet.offsetForPaintModeY;
            this.z = targetBlockAvoidingPaint.z + packet.offsetForPaintModeZ;
         } else {
            this.x = packet.x;
            this.y = packet.y;
            this.z = packet.z;
         }
      } else {
         this.x = packet.x;
         this.y = packet.y;
         this.z = packet.z;
      }

      this.interactionType = packet.type;
      BuilderTool builderTool = BuilderTool.getActiveBuilderTool(playerComponent);
      BuilderTool.ArgData args = this.args = builderTool.getItemArgData(playerComponent.getInventory().getItemInHand());
      BrushData.Values brush = args.brush();
      if (brush == null) {
         brush = new BrushData.Values(BrushData.DEFAULT);
      }

      this.transform = getTransform(ref, brush, this.vector, componentAccessor);
      this.shapeRange = brush.getWidth();
      this.shapeHeight = brush.getHeight();
      this.shapeThickness = brush.getThickness();
      this.capped = brush.isCapped();
      this.shape = brush.getShape();
      this.pattern = this.getPattern(packet, brush);
      this.mask = combineMasks(brush, this.builderState.getGlobalMask());
      BrushOrigin shapeOrigin = brush.getOrigin();
      boolean originRotation = brush.getOriginRotation();
      Vector3i offsets = getOffsets(this.shapeRange, this.shapeHeight, originRotation, shapeOrigin, this.transform, this.vector, true);
      this.originOffsetX = offsets.getX();
      this.originOffsetY = offsets.getY();
      this.originOffsetZ = offsets.getZ();
      this.random = this.builderState.getRandom();
      Vector3i brushMin = new Vector3i(this.x - this.shapeRange, this.y - this.shapeHeight, this.z - this.shapeRange);
      Vector3i brushMax = new Vector3i(this.x + this.shapeRange, this.y + this.shapeHeight, this.z + this.shapeRange);
      this.edit = new EditOperation(world, this.x, this.y, this.z, this.shapeRange, brushMin, brushMax, this.mask);
   }

   @Nonnull
   public static PrototypePlayerBuilderToolSettings getOrCreatePrototypeSettings(UUID playerUuid) {
      PrototypePlayerBuilderToolSettings settings = PROTOTYPE_TOOL_SETTINGS.get(playerUuid);
      if (settings == null) {
         settings = new PrototypePlayerBuilderToolSettings(playerUuid);
         PROTOTYPE_TOOL_SETTINGS.put(playerUuid, settings);
      }

      return settings;
   }

   @Nonnull
   public static List<Vector3i> calculateInterpolatedPositions(
      @Nullable Vector3i lastPosition, @Nonnull Vector3i currentPosition, int brushWidth, int brushHeight, int brushSpacing
   ) {
      ArrayList<Vector3i> positions = new ArrayList<>();
      if (lastPosition == null) {
         positions.add(currentPosition);
         return positions;
      } else {
         double dx = currentPosition.getX() - lastPosition.getX();
         double dy = currentPosition.getY() - lastPosition.getY();
         double dz = currentPosition.getZ() - lastPosition.getZ();
         double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (brushSpacing == 0) {
            float maxBrushDimension = Math.max(brushWidth, brushHeight);
            float spacingThreshold = Math.max(1.0F, maxBrushDimension * 0.5F);
            if (distance <= spacingThreshold) {
               positions.add(currentPosition);
               return positions;
            }

            int steps = (int)Math.ceil(distance / spacingThreshold);

            for (int i = 1; i <= steps; i++) {
               float t = (float)i / steps;
               int interpX = (int)Math.round(lastPosition.getX() + dx * t);
               int interpY = (int)Math.round(lastPosition.getY() + dy * t);
               int interpZ = (int)Math.round(lastPosition.getZ() + dz * t);
               positions.add(new Vector3i(interpX, interpY, interpZ));
            }
         } else if (distance >= brushSpacing) {
            positions.add(currentPosition);
         }

         return positions;
      }
   }

   @Nonnull
   public Vector3i getPosition() {
      return new Vector3i(this.x, this.y, this.z);
   }

   public int getBrushWidth() {
      return this.shapeRange;
   }

   public int getBrushHeight() {
      return this.shapeHeight;
   }

   public int getBrushSpacing() {
      Object spacingValue = this.args.tool().get("BrushSpacing");
      return spacingValue instanceof Number ? ((Number)spacingValue).intValue() : 0;
   }

   public int getBrushDensity() {
      return this.args.tool().get("BrushDensity") instanceof Number number ? number.intValue() : 100;
   }

   public void executeAsBrushConfig(
      @Nonnull PrototypePlayerBuilderToolSettings prototypePlayerBuilderToolSettings,
      @Nonnull BuilderToolOnUseInteraction packet,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      prototypePlayerBuilderToolSettings.getBrushConfigCommandExecutor()
         .execute(this.playerRef, world, new Vector3i(this.x, this.y, this.z), packet.isHoldDownInteraction, packet.type, bc -> {
            bc.setPattern(this.pattern);
            bc.setDensity(this.getBrushDensity());
            bc.setShapeHeight(this.shapeHeight);
            bc.setShapeWidth(this.shapeRange);
            bc.setShape(this.shape);
            bc.setCapped(this.capped);
            bc.modifyOriginOffset(new Vector3i(this.originOffsetX, this.originOffsetY, this.originOffsetZ));
            bc.setBrushMask(this.mask);
            bc.setShapeThickness(this.shapeThickness);
         }, componentAccessor);
   }

   private BlockPattern getPattern(@Nonnull BuilderToolOnUseInteraction packet, @Nonnull BrushData.Values brush) {
      if (packet.type == InteractionType.Primary) {
         return BlockPattern.EMPTY;
      } else {
         return (this instanceof PaintOperation || this instanceof PaintOperation) && brush.getMaterial().equals(BlockPattern.EMPTY)
            ? BlockPattern.parse("Rock_Stone")
            : brush.getMaterial();
      }
   }

   @Nullable
   public Vector3i getTargetBlockAvoidingPaint(
      @Nonnull Ref<EntityStore> ref,
      double maxDistance,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      float raycastOriginX,
      float raycastOriginY,
      float raycastOriginZ,
      float raycastDirectionX,
      float raycastDirectionY,
      float raycastDirectionZ
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      PrototypePlayerBuilderToolSettings prototypePlayerBuilderToolSettings = PROTOTYPE_TOOL_SETTINGS.get(uuidComponent.getUuid());
      return prototypePlayerBuilderToolSettings != null && !prototypePlayerBuilderToolSettings.getIgnoredPaintOperations().isEmpty()
         ? TargetUtil.getTargetBlockAvoidLocations(
            world,
            blockId -> blockId != 0,
            raycastOriginX,
            raycastOriginY,
            raycastOriginZ,
            raycastDirectionX,
            raycastDirectionY,
            raycastDirectionZ,
            maxDistance,
            prototypePlayerBuilderToolSettings.getIgnoredPaintOperations()
         )
         : TargetUtil.getTargetBlock(
            world,
            (blockId, _fluidId) -> blockId != 0,
            raycastOriginX,
            raycastOriginY,
            raycastOriginZ,
            raycastDirectionX,
            raycastDirectionY,
            raycastDirectionZ,
            maxDistance
         );
   }

   @Nonnull
   public EditOperation getEditOperation() {
      return this.edit;
   }

   public final boolean test(int x, int y, int z, Void aVoid) {
      if (this.transform == Transform.NONE) {
         return this.execute0(x, y + this.originOffsetY, z);
      } else {
         this.vector.assign(x - this.x, y - this.y, z - this.z);
         this.transform.apply(this.vector);
         x = this.x + this.originOffsetX + this.vector.x;
         y = this.y + this.originOffsetY + this.vector.y;
         z = this.z + this.originOffsetZ + this.vector.z;
         return this.execute0(x, y, z);
      }
   }

   abstract boolean execute0(int var1, int var2, int var3);

   public void execute(ComponentAccessor<EntityStore> componentAccessor) {
      executeShapeOperation(this.x, this.y, this.z, this, this.shape, this.shapeRange, this.shapeHeight, this.shapeThickness, this.capped);
   }

   public void executeAt(int posX, int posY, int posZ, ComponentAccessor<EntityStore> componentAccessor) {
      executeShapeOperation(posX, posY, posZ, this, this.shape, this.shapeRange, this.shapeHeight, this.shapeThickness, this.capped);
   }

   public static void executeShapeOperation(
      int x,
      int y,
      int z,
      @Nonnull TriIntObjPredicate<Void> operation,
      @Nonnull BrushShape shape,
      int shapeRange,
      int shapeHeight,
      int shapeThickness,
      boolean capped
   ) {
      if (shapeRange <= 1 && shapeHeight <= 1) {
         operation.test(x, y, z, null);
      } else {
         int radiusXZ = Math.max(shapeRange / 2, 1);
         int halfHeight = Math.max(shapeHeight / 2, 1);
         switch (shape) {
            case Cube:
            default:
               BlockCubeUtil.forEachBlock(x, y, z, radiusXZ, shapeHeight, radiusXZ, shapeThickness, capped, null, operation);
               break;
            case Sphere:
               BlockSphereUtil.forEachBlock(x, y, z, radiusXZ, halfHeight, radiusXZ, shapeThickness, null, operation);
               break;
            case Cylinder:
               BlockCylinderUtil.forEachBlock(x, y - halfHeight, z, radiusXZ, shapeHeight, radiusXZ, shapeThickness, capped, null, operation);
               break;
            case Cone:
               BlockConeUtil.forEachBlock(x, y - halfHeight, z, radiusXZ, shapeHeight, radiusXZ, shapeThickness, capped, null, operation);
               break;
            case InvertedCone:
               BlockConeUtil.forEachBlockInverted(x, y - halfHeight, z, radiusXZ, shapeHeight, radiusXZ, shapeThickness, capped, null, operation);
               break;
            case Pyramid:
               BlockPyramidUtil.forEachBlock(x, y - halfHeight, z, radiusXZ, shapeHeight, radiusXZ, shapeThickness, capped, null, operation);
               break;
            case InvertedPyramid:
               BlockPyramidUtil.forEachBlockInverted(x, y - halfHeight, z, radiusXZ, shapeHeight, radiusXZ, shapeThickness, capped, null, operation);
               break;
            case Dome:
               BlockDomeUtil.forEachBlock(x, y - halfHeight, z, radiusXZ, shapeHeight, radiusXZ, shapeThickness, capped, null, operation);
               break;
            case InvertedDome:
               BlockInvertedDomeUtil.forEachBlock(x, y + halfHeight, z, radiusXZ, shapeHeight, radiusXZ, shapeThickness, capped, null, operation);
               break;
            case Diamond:
               BlockDiamondUtil.forEachBlock(x, y, z, radiusXZ, shapeHeight / 2, radiusXZ, shapeThickness, capped, null, operation);
               break;
            case Torus:
               int minorRadius = Math.max(1, shapeHeight / 4);
               BlockTorusUtil.forEachBlock(x, y, z, radiusXZ, minorRadius, shapeThickness, capped, null, operation);
         }
      }
   }

   @Nonnull
   private static Vector3i getOffsets(
      int width, int height, boolean originRotation, BrushOrigin origin, @Nonnull Transform transform, @Nonnull Vector3i vector, boolean applyBottomOriginFix
   ) {
      int offsetY = height / 2;
      int offsetXZ = originRotation ? width / 2 : 0;
      vector.assign(0, offsetY, 0);
      transform.apply(vector);
      int ox = vector.getX();
      int oz = vector.getZ();
      vector.assign(offsetXZ, offsetY, -offsetXZ);
      transform.apply(vector);
      int oy = vector.getY();
      ox = origin == BrushOrigin.Center ? 0 : (origin == BrushOrigin.Bottom ? ox : -ox);
      oy = origin == BrushOrigin.Center ? 0 : (origin == BrushOrigin.Bottom ? oy + (applyBottomOriginFix ? 1 : 0) : -oy);
      oz = origin == BrushOrigin.Center ? 0 : (origin == BrushOrigin.Bottom ? oz : -oz);
      return vector.assign(ox, oy, oz);
   }

   private static Transform getTransform(
      @Nonnull Ref<EntityStore> ref, @Nonnull BrushData.Values brushData, @Nonnull Vector3i vector, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Transform rotate = getRotation(ref, brushData, vector, componentAccessor);
      Transform mirror = getMirror(ref, brushData, vector, componentAccessor);
      return rotate.then(mirror);
   }

   private static Transform getRotation(
      @Nonnull Ref<EntityStore> ref, @Nonnull BrushData.Values brushData, @Nonnull Vector3i vector, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (brushData.getRotationAxis() == BrushAxis.Auto) {
         HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         return Rotate.forDirection(headRotationComponent.getAxisDirection(vector), brushData.getRotationAngle());
      } else {
         return Rotate.forAxisAndAngle(brushData.getRotationAxis(), brushData.getRotationAngle());
      }
   }

   private static Transform getMirror(
      @Nonnull Ref<EntityStore> ref, @Nonnull BrushData.Values brushData, @Nonnull Vector3i vector, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (brushData.getMirrorAxis() == BrushAxis.Auto) {
         HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         return Mirror.forDirection(headRotationComponent.getAxisDirection(vector), false);
      } else {
         return Mirror.forAxis(brushData.getMirrorAxis());
      }
   }

   @Nonnull
   public static ToolOperation fromPacket(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) throws Exception {
      BuilderTool builderTool = BuilderTool.getActiveBuilderTool(player);
      if (builderTool == null) {
         throw new IllegalStateException("No builder tool active on player");
      } else {
         String toolType = builderTool.getId();
         OperationFactory factory = OPERATIONS.get(toolType);
         if (factory == null) {
            throw new Exception("No tool found matching id " + toolType);
         } else {
            return factory.create(ref, player, packet, componentAccessor);
         }
      }
   }

   @Nullable
   public static BlockMask combineMasks(@Nullable BrushData.Values brush, @Nullable BlockMask globalMask) {
      if (brush == null) {
         return globalMask;
      } else if (brush.shouldUseMaskCommands()) {
         BlockMask mask = BlockMask.combine(brush.getParsedMaskCommands());
         if (mask != null) {
            mask.setInverted(brush.shouldInvertMask());
         }

         return mask;
      } else {
         BlockMask brushMaskAbove = brush.getMaskAbove().withOptions(BlockFilter.FilterType.AboveBlock, false);
         BlockMask brushMaskNot = brush.getMaskNot().withOptions(BlockFilter.FilterType.TargetBlock, true);
         BlockMask brushMaskBelow = brush.getMaskBelow().withOptions(BlockFilter.FilterType.BelowBlock, false);
         BlockMask brushMaskAdjacent = brush.getMaskAdjacent().withOptions(BlockFilter.FilterType.AdjacentBlock, false);
         BlockMask brushMaskNeighbor = brush.getMaskNeighbor().withOptions(BlockFilter.FilterType.NeighborBlock, false);
         BlockMask combinedMask = BlockMask.combine(
            brush.getMask(), brushMaskAbove, brushMaskNot, brushMaskBelow, brushMaskAdjacent, brushMaskNeighbor, globalMask
         );
         if (combinedMask != null) {
            combinedMask.setInverted(brush.shouldInvertMask());
         }

         return combinedMask;
      }
   }

   static {
      OPERATIONS.put("Flood", FloodOperation::new);
      OPERATIONS.put("Noise", NoiseOperation::new);
      OPERATIONS.put("Scatter", ScatterOperation::new);
      OPERATIONS.put("Smooth", (ref, player1, packet, componentAccessor) -> new SmoothOperation(ref, packet, componentAccessor));
      OPERATIONS.put("Tint", TintOperation::new);
      OPERATIONS.put("Paint", PaintOperation::new);
      OPERATIONS.put("Sculpt", SculptOperation::new);
      OPERATIONS.put("Layers", LayersOperation::new);
      OPERATIONS.put("LaserPointer", LaserPointerOperation::new);
   }
}
