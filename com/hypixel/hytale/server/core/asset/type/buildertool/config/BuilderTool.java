package com.hypixel.hytale.server.core.asset.type.buildertool.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.MapProvidedMapCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolState;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.BoolArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.MaskArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.StringArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.ToolArg;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.ToolArgException;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class BuilderTool implements JsonAssetWithMap<String, DefaultAssetMap<String, BuilderTool>>, NetworkSerializable<BuilderToolState> {
   public static final String TOOL_DATA_KEY = "ToolData";
   public static final String MATERIAL_KEY = "builtin_Material";
   public static final String FAVORITE_MATERIALS_KEY = "builtin_FavoriteMaterials";
   public static final String WIDTH_KEY = "builtin_Width";
   public static final String HEIGHT_KEY = "builtin_Height";
   public static final String THICKNESS_KEY = "builtin_Thickness";
   public static final String CAPPED_KEY = "builtin_Capped";
   public static final String SHAPE_KEY = "builtin_Shape";
   public static final String ORIGIN_KEY = "builtin_Origin";
   public static final String ORIGIN_ROTATION_KEY = "builtin_OriginRotation";
   public static final String ROTATION_AXIS_KEY = "builtin_RotationAxis";
   public static final String ROTATION_ANGLE_KEY = "builtin_RotationAngle";
   public static final String MIRROR_AXIS_KEY = "builtin_MirrorAxis";
   public static final String ROTATION_FACE_KEY = "builtin_RotationFace";
   public static final String DENSITY_KEY = "builtin_Density";
   public static final String SPACING_KEY = "builtin_Spacing";
   public static final String MASK_KEY = "builtin_Mask";
   public static final String MASK_ABOVE_KEY = "builtin_MaskAbove";
   public static final String MASK_NOT_KEY = "builtin_MaskNot";
   public static final String MASK_BELOW_KEY = "builtin_MaskBelow";
   public static final String MASK_ADJACENT_KEY = "builtin_MaskAdjacent";
   public static final String MASK_NEIGHBOR_KEY = "builtin_MaskNeighbor";
   public static final String MASK_COMMANDS_KEY = "builtin_MaskCommands";
   public static final String USE_MASK_COMMANDS_KEY = "builtin_UseMaskCommands";
   public static final String INVERT_MASK_KEY = "builtin_InvertMask";
   public static HashSet<String> MASK_ARGS = setMandatoryToolArgs();
   public static final BuilderTool DEFAULT = new BuilderTool();
   public static final AssetBuilderCodec<String, BuilderTool> CODEC = AssetBuilderCodec.builder(
         BuilderTool.class, BuilderTool::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .append(new KeyedCodec<>("Id", Codec.STRING), (builderTool, o) -> builderTool.id = o, builderTool -> builderTool.id)
      .add()
      .append(new KeyedCodec<>("IsBrush", Codec.BOOLEAN), (builderTool, o) -> builderTool.isBrush = o, builderTool -> builderTool.isBrush)
      .add()
      .append(
         new KeyedCodec<>("BrushConfigurationCommand", Codec.STRING),
         (builderTool, o) -> builderTool.brushConfigurationCommand = o,
         builderTool -> builderTool.brushConfigurationCommand
      )
      .add()
      .append(new KeyedCodec<>("Args", new ArrayCodec<>(ToolArg.CODEC, ToolArg[]::new)), (builderTool, s) -> {
         builderTool.args = new LinkedHashMap<>();
         Arrays.stream(s).forEach(arg -> builderTool.args.put(arg.getId(), arg));
      }, builderTool -> builderTool.args.values().toArray(new ToolArg[builderTool.args.size()]))
      .add()
      .afterDecode(builderTool -> {
         Map<String, ToolArg> allArgs = new LinkedHashMap<>(builderTool.args);

         for (String maskKey : MASK_ARGS) {
            if (!allArgs.containsKey(maskKey)) {
               allArgs.put(maskKey, MaskArg.EMPTY);
            }
         }

         if (!allArgs.containsKey("builtin_InvertMask")) {
            allArgs.put("builtin_InvertMask", new BoolArg(false));
         }

         if (!allArgs.containsKey("builtin_UseMaskCommands")) {
            allArgs.put("builtin_UseMaskCommands", new BoolArg(false));
         }

         if (!allArgs.containsKey("builtin_MaskCommands")) {
            allArgs.put("builtin_MaskCommands", new StringArg(""));
         }

         builderTool.argsCodec = new MapProvidedMapCodec<>(allArgs, ToolArg::getCodec, HashMap::new);
      })
      .build();
   private static DefaultAssetMap<String, BuilderTool> ASSET_MAP;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected boolean isBrush;
   protected String brushConfigurationCommand;
   protected Map<String, ToolArg> args = Collections.emptyMap();
   protected Map<String, Object> defaultToolArgs;
   private MapProvidedMapCodec<Object, ToolArg> argsCodec;
   private SoftReference<BuilderToolState> cachedPacket;

   public static DefaultAssetMap<String, BuilderTool> getAssetMap() {
      if (ASSET_MAP == null) {
         ASSET_MAP = (DefaultAssetMap<String, BuilderTool>)AssetRegistry.getAssetStore(BuilderTool.class).getAssetMap();
      }

      return ASSET_MAP;
   }

   private static HashSet<String> setMandatoryToolArgs() {
      HashSet<String> argKeys = new HashSet<>();
      argKeys.add("builtin_Mask");
      argKeys.add("builtin_MaskAbove");
      argKeys.add("builtin_MaskNot");
      argKeys.add("builtin_MaskBelow");
      argKeys.add("builtin_MaskAdjacent");
      argKeys.add("builtin_MaskNeighbor");
      return argKeys;
   }

   @Nullable
   public static BuilderTool getActiveBuilderTool(@Nonnull Player player) {
      ItemStack activeItemStack = player.getInventory().getItemInHand();
      if (activeItemStack == null) {
         return null;
      } else {
         Item item = activeItemStack.getItem();
         BuilderTool builderToolData = item.getBuilderTool();
         return builderToolData == null ? null : builderToolData;
      }
   }

   public String getId() {
      return this.id;
   }

   public String getBrushConfigurationCommand() {
      return this.brushConfigurationCommand;
   }

   public boolean isBrush() {
      return this.isBrush;
   }

   public Map<String, ToolArg> getArgs() {
      return this.args;
   }

   public MapProvidedMapCodec<Object, ToolArg> getArgsCodec() {
      return this.argsCodec;
   }

   @Nonnull
   private Map<String, Object> getDefaultToolArgs(@Nonnull ItemStack itemStack) {
      BuilderTool builderToolAsset = itemStack.getItem().getBuilderTool();
      Map<String, Object> map = new Object2ObjectOpenHashMap(builderToolAsset.args.size());

      for (Entry<String, ToolArg> entry : builderToolAsset.args.entrySet()) {
         map.put(entry.getKey(), entry.getValue().getValue());
      }

      return map;
   }

   @Nonnull
   public BuilderTool.ArgData getItemArgData(@Nonnull ItemStack itemStack) {
      Map<String, Object> toolArgs = null;
      if (!this.args.isEmpty()) {
         Map<String, Object> toolData = itemStack.getFromMetadataOrNull("ToolData", this.argsCodec);
         toolArgs = toolData == null ? this.getDefaultToolArgs(itemStack) : toolData;
      }

      return new BuilderTool.ArgData(toolArgs);
   }

   @Nonnull
   public ItemStack createItemStack(@Nonnull String itemId, int quantity, @Nonnull BuilderTool.ArgData argData) {
      BsonDocument meta = new BsonDocument();
      if (argData.tool() != null) {
         meta.put("ToolData", this.argsCodec.encode(argData.tool()));
      }

      return new ItemStack(itemId, quantity, meta);
   }

   @Nonnull
   public ItemStack updateArgMetadata(@Nonnull ItemStack itemStack, @Nonnull String id, @Nullable String value) throws ToolArgException {
      BuilderTool.ArgData argData = this.getItemArgData(itemStack);
      if (!MASK_ARGS.contains(id) && !id.equals("builtin_UseMaskCommands") && !id.equals("builtin_InvertMask") && !id.equals("builtin_MaskCommands")) {
         ToolArg arg = this.args.get(id);
         if (arg == null) {
            throw new ToolArgException(Message.translation("server.builderTools.toolUnknownArg").param("arg", id));
         }

         if (value == null) {
            if (arg.isRequired()) {
               throw new ToolArgException(Message.translation("server.builderTools.toolArgMissing").param("arg", id));
            }

            argData = BuilderTool.ArgData.removeToolArg(argData, id);
         } else {
            Object newValue = arg.fromString(value);
            argData = BuilderTool.ArgData.setToolArg(argData, id, newValue);
         }
      } else if (value == null) {
         argData = BuilderTool.ArgData.removeToolArg(argData, id);
      } else if (MASK_ARGS.contains(id)) {
         BlockMask mask = BlockMask.parse(value);
         argData = BuilderTool.ArgData.setToolArg(argData, id, mask);
      } else if (id.equals("builtin_UseMaskCommands")) {
         argData = BuilderTool.ArgData.setToolArg(argData, id, Boolean.parseBoolean(value));
      } else if (id.equals("builtin_InvertMask")) {
         argData = BuilderTool.ArgData.setToolArg(argData, id, Boolean.parseBoolean(value));
      } else if (id.equals("builtin_MaskCommands")) {
         argData = BuilderTool.ArgData.setToolArg(argData, id, value);
      }

      return this.createItemStack(itemStack.getItemId(), itemStack.getQuantity(), argData);
   }

   @Nonnull
   public BuilderToolState toPacket() {
      BuilderToolState cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         BuilderToolState packet = new BuilderToolState();
         packet.id = this.id;
         packet.isBrush = this.isBrush;
         Map<String, BuilderToolArg> map = new LinkedHashMap<>(this.args.size());

         for (Entry<String, ToolArg> entry : this.args.entrySet()) {
            map.put(entry.getKey(), entry.getValue().toPacket());
         }

         packet.args = map.values().toArray(new BuilderToolArg[map.values().size()]);
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "BuilderTool{id='" + this.id + "', isBrush=" + this.isBrush + ", args=" + this.args + "}";
   }

   public record ArgData(@Nullable Map<String, Object> tool) {
      @Nonnull
      public static BuilderTool.ArgData setToolArg(@Nonnull BuilderTool.ArgData argData, String argId, Object value) {
         Map<String, Object> tool = argData.tool();
         if (tool == null) {
            return argData;
         } else {
            Object2ObjectOpenHashMap<String, Object> newToolArgs = new Object2ObjectOpenHashMap(tool);
            newToolArgs.put(argId, value);
            return new BuilderTool.ArgData(newToolArgs);
         }
      }

      @Nonnull
      public static BuilderTool.ArgData removeToolArg(@Nonnull BuilderTool.ArgData argData, String argId) {
         Map<String, Object> tool = argData.tool();
         if (tool == null) {
            return argData;
         } else {
            Object2ObjectOpenHashMap<String, Object> newToolArgs = new Object2ObjectOpenHashMap(tool);
            newToolArgs.remove(argId);
            return new BuilderTool.ArgData(newToolArgs);
         }
      }

      @Nonnull
      @Override
      public String toString() {
         return "ArgData{tool=" + this.tool + "}";
      }
   }
}
