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
import com.hypixel.hytale.logger.HytaleLogger;
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
import com.hypixel.hytale.protocol.Rotation;
import com.hypixel.hytale.protocol.packets.buildertools.BrushAxis;
import com.hypixel.hytale.protocol.packets.buildertools.BrushOrigin;
import com.hypixel.hytale.protocol.packets.buildertools.BrushShape;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ToolOperation implements TriIntObjPredicate<Void> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   protected static final int RANDOM_MAX = 100;
   @Nonnull
   public static final Map<String, OperationFactory> OPERATIONS = new ConcurrentHashMap<>();
   @Nonnull
   public static final Map<UUID, PrototypePlayerBuilderToolSettings> PROTOTYPE_TOOL_SETTINGS = new ConcurrentHashMap<>();
   public static final double MAX_DISTANCE = 400.0;
   public static final int DEFAULT_BRUSH_SPACING = 0;
   private static final Pattern NEWLINES_PATTERN = Pattern.compile("\\r?\\n");
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
   protected final int density;
   protected final int spacing;
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
   private Transform transform;
   private final Vector3i vector = new Vector3i();
   protected int currentCenterX;
   protected int currentCenterY;
   protected int currentCenterZ;
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
      Object width = args.tool().get("builtin_Width");
      Object height = args.tool().get("builtin_Height");
      Object thickness = args.tool().get("builtin_Thickness");
      Object capped = args.tool().get("builtin_Capped");
      Object shape = args.tool().get("builtin_Shape");
      Object density = args.tool().get("builtin_Density");
      Object spacing = args.tool().get("builtin_Spacing");
      Object material = args.tool().get("builtin_Material");
      this.transform = getTransform(ref, args, this.vector, componentAccessor);
      this.shapeRange = width != null ? (Integer)width : 5;
      this.shapeHeight = height != null ? (Integer)height : 5;
      this.shapeThickness = thickness != null ? (Integer)thickness : 0;
      this.capped = capped != null ? (Boolean)capped : false;
      this.shape = shape != null ? BrushShape.valueOf((String)shape) : BrushShape.Sphere;
      this.density = density != null ? (Integer)density : 100;
      this.spacing = spacing != null ? (Integer)spacing : 0;
      this.pattern = this.getPattern(packet, material != null ? (BlockPattern)material : BlockPattern.EMPTY);
      this.mask = combineMasks(args, this.builderState.getGlobalMask());
      Object origin = args.tool().get("builtin_Origin");
      Object rotateOrigin = args.tool().get("builtin_OriginRotation");
      BrushOrigin shapeOrigin = origin != null ? BrushOrigin.valueOf((String)origin) : BrushOrigin.Center;
      boolean originRotation = rotateOrigin != null ? (Boolean)rotateOrigin : false;
      Vector3i offsets = getOffsets(this.shapeRange, this.shapeHeight, originRotation, shapeOrigin, this.transform, this.vector, true);
      this.originOffsetX = offsets.getX();
      this.originOffsetY = offsets.getY();
      this.originOffsetZ = offsets.getZ();
      this.random = this.builderState.getRandom();
      this.currentCenterX = this.x;
      this.currentCenterY = this.y;
      this.currentCenterZ = this.z;
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
      Object spacingValue = this.args.tool().get("builtin_BrushSpacing");
      return spacingValue instanceof Number ? ((Number)spacingValue).intValue() : 0;
   }

   public Transform getBrushRotation(ComponentAccessor<EntityStore> componentAccessor) {
      Object rotationValue = this.args.tool().get("builtin_RotationFace");
      Transform transform = Transform.NONE;
      if (rotationValue instanceof String rotationSelection) {
         if (rotationSelection.equalsIgnoreCase("down")) {
            transform = Rotate.forAxisAndAngle(BrushAxis.X, Rotation.OneEighty);
         } else if (rotationSelection.equalsIgnoreCase("north")) {
            transform = Rotate.forAxisAndAngle(BrushAxis.X, Rotation.TwoSeventy);
         } else if (rotationSelection.equalsIgnoreCase("south")) {
            transform = Rotate.forAxisAndAngle(BrushAxis.X, Rotation.Ninety);
         } else if (rotationSelection.equalsIgnoreCase("east")) {
            transform = Rotate.forAxisAndAngle(BrushAxis.Z, Rotation.TwoSeventy);
         } else if (rotationSelection.equalsIgnoreCase("west")) {
            transform = Rotate.forAxisAndAngle(BrushAxis.Z, Rotation.Ninety);
         } else if (rotationSelection.equalsIgnoreCase("camera")) {
            HeadRotation headRotationComponent = componentAccessor.getComponent(this.playerRef, HeadRotation.getComponentType());

            assert headRotationComponent != null;

            transform = Rotate.forDirection(headRotationComponent.getAxisDirection(), Rotation.None);
         }
      }

      return transform;
   }

   public void executeAsBrushConfig(
      @Nonnull PrototypePlayerBuilderToolSettings prototypePlayerBuilderToolSettings,
      @Nonnull BuilderToolOnUseInteraction packet,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      prototypePlayerBuilderToolSettings.setUndoGroupSize(packet.undoGroupSize);
      prototypePlayerBuilderToolSettings.getBrushConfigCommandExecutor()
         .execute(this.playerRef, world, new Vector3i(this.x, this.y, this.z), packet.isHoldDownInteraction, packet.type, bc -> {
            bc.setPattern(this.pattern);
            bc.setDensity(this.density);
            bc.setShapeHeight(this.shapeHeight);
            bc.setShapeWidth(this.shapeRange);
            bc.setShape(this.shape);
            bc.setCapped(this.capped);
            bc.setTransform(this.getBrushRotation(componentAccessor));
            bc.setTransformOrigin(new Vector3i(this.x, this.y, this.z));
            bc.modifyOriginOffset(new Vector3i(this.originOffsetX, this.originOffsetY, this.originOffsetZ));
            bc.setBrushMask(this.mask);
            bc.setShapeThickness(this.shapeThickness);
         }, componentAccessor);
   }

   private BlockPattern getPattern(@Nonnull BuilderToolOnUseInteraction packet, BlockPattern pattern) {
      if (packet.type == InteractionType.Primary) {
         return BlockPattern.EMPTY;
      } else {
         return (this instanceof PaintOperation || this instanceof PaintOperation) && pattern.equals(BlockPattern.EMPTY)
            ? BlockPattern.parse("Rock_Stone")
            : pattern;
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
         this.vector.assign(x - this.currentCenterX, y - this.currentCenterY, z - this.currentCenterZ);
         this.transform.apply(this.vector);
         x = this.currentCenterX + this.originOffsetX + this.vector.x;
         y = this.currentCenterY + this.originOffsetY + this.vector.y;
         z = this.currentCenterZ + this.originOffsetZ + this.vector.z;
         return this.execute0(x, y, z);
      }
   }

   public boolean showEditNotification() {
      return true;
   }

   abstract boolean execute0(int var1, int var2, int var3);

   public void execute(ComponentAccessor<EntityStore> componentAccessor) {
      executeShapeOperation(this.x, this.y, this.z, this, this.shape, this.shapeRange, this.shapeHeight, this.shapeThickness, this.capped);
   }

   public void executeAt(int posX, int posY, int posZ, ComponentAccessor<EntityStore> componentAccessor) {
      this.currentCenterX = posX;
      this.currentCenterY = posY;
      this.currentCenterZ = posZ;
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
      @Nonnull Ref<EntityStore> ref, @Nonnull BuilderTool.ArgData args, @Nonnull Vector3i vector, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Transform rotate = getRotation(ref, args, vector, componentAccessor);
      Transform mirror = getMirror(ref, args, vector, componentAccessor);
      return rotate.then(mirror);
   }

   private static Transform getRotation(
      @Nonnull Ref<EntityStore> ref, @Nonnull BuilderTool.ArgData args, @Nonnull Vector3i vector, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Object axis = args.tool().get("builtin_RotationAxis");
      Object angle = args.tool().get("builtin_RotationAngle");
      BrushAxis rotationAxis = axis != null ? BrushAxis.valueOf((String)axis) : BrushAxis.None;
      Rotation rotationAngle = angle != null ? Rotation.valueOf((String)angle) : Rotation.None;
      if (rotationAxis == BrushAxis.Auto) {
         HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         return Rotate.forDirection(headRotationComponent.getAxisDirection(vector), rotationAngle);
      } else {
         return Rotate.forAxisAndAngle(rotationAxis, rotationAngle);
      }
   }

   private static Transform getMirror(
      @Nonnull Ref<EntityStore> ref, @Nonnull BuilderTool.ArgData args, @Nonnull Vector3i vector, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Object axis = args.tool().get("builtin_MirrorAxis");
      BrushAxis mirrorAxis = axis != null ? BrushAxis.valueOf((String)axis) : BrushAxis.None;
      if (mirrorAxis == BrushAxis.Auto) {
         HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         return Mirror.forDirection(headRotationComponent.getAxisDirection(vector), false);
      } else {
         return Mirror.forAxis(mirrorAxis);
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
   public static BlockMask combineMasks(@Nullable BuilderTool.ArgData args, @Nullable BlockMask globalMask) {
      if (args == null) {
         return globalMask;
      } else {
         Object useMaskCommands = args.tool().get("builtin_UseMaskCommands");
         boolean useBrushMaskCommands = useMaskCommands != null ? (Boolean)useMaskCommands : false;
         Object invertMask = args.tool().get("builtin_InvertMask");
         boolean brushInvertMask = invertMask != null ? (Boolean)invertMask : false;
         if (useBrushMaskCommands) {
            String maskCommands = args.tool().get("builtin_MaskCommands") != null ? (String)args.tool().get("builtin_MaskCommands") : "";
            String[] commands = NEWLINES_PATTERN.split(maskCommands);
            BlockMask[] parsedMaskCommands = Arrays.stream(commands).map(m -> m.split(" ")).map(BlockMask::parse).toArray(BlockMask[]::new);
            BlockMask mask = BlockMask.combine(parsedMaskCommands);
            if (mask != null) {
               mask.setInverted(brushInvertMask);
            }

            return mask;
         } else {
            Object mask = args.tool().get("builtin_Mask");
            Object maskAbove = args.tool().get("builtin_MaskAbove");
            Object maskNot = args.tool().get("builtin_MaskNot");
            Object maskBelow = args.tool().get("builtin_MaskBelow");
            Object maskAdjacent = args.tool().get("builtin_MaskAdjacent");
            Object maskNeighbor = args.tool().get("builtin_MaskNeighbor");
            BlockMask brushMask = BlockMask.EMPTY;
            BlockMask brushMaskAbove = BlockMask.EMPTY;
            BlockMask brushMaskNot = BlockMask.EMPTY;
            BlockMask brushMaskBelow = BlockMask.EMPTY;
            BlockMask brushMaskAdjacent = BlockMask.EMPTY;
            BlockMask brushMaskNeighbor = BlockMask.EMPTY;
            if (mask != null) {
               brushMask = (BlockMask)mask;
            }

            if (maskAbove != null) {
               brushMaskAbove = (BlockMask)maskAbove;
               brushMaskAbove = brushMaskAbove.withOptions(BlockFilter.FilterType.AboveBlock, false);
            }

            if (maskNot != null) {
               brushMaskNot = (BlockMask)maskNot;
               brushMaskNot = brushMaskNot.withOptions(BlockFilter.FilterType.TargetBlock, true);
            }

            if (maskBelow != null) {
               brushMaskBelow = (BlockMask)maskBelow;
               brushMaskBelow = brushMaskBelow.withOptions(BlockFilter.FilterType.BelowBlock, false);
            }

            if (maskAdjacent != null) {
               brushMaskAdjacent = (BlockMask)maskAdjacent;
               brushMaskAdjacent = brushMaskAdjacent.withOptions(BlockFilter.FilterType.AdjacentBlock, false);
            }

            if (maskNeighbor != null) {
               brushMaskNeighbor = (BlockMask)maskNeighbor;
               brushMaskNeighbor = brushMaskNeighbor.withOptions(BlockFilter.FilterType.NeighborBlock, false);
            }

            BlockMask combinedMask = BlockMask.combine(
               brushMask, brushMaskAbove, brushMaskNot, brushMaskBelow, brushMaskAdjacent, brushMaskNeighbor, globalMask
            );
            if (combinedMask != null) {
               combinedMask.setInverted(brushInvertMask);
            }

            return combinedMask;
         }
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
      OPERATIONS.put("Revolve", RevolveOperation::new);
      OPERATIONS.put("ScriptedBrushTemplate", PaintOperation::new);
   }
}
