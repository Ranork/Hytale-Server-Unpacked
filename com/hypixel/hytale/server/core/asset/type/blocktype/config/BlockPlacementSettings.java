package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class BlockPlacementSettings implements NetworkSerializable<com.hypixel.hytale.protocol.BlockPlacementSettings> {
   public static final BuilderCodec<BlockPlacementSettings> CODEC = BuilderCodec.builder(BlockPlacementSettings.class, BlockPlacementSettings::new)
      .append(
         new KeyedCodec<>("AllowRotationKey", Codec.BOOLEAN),
         (placementSettings, o) -> placementSettings.allowRotationKey = o,
         placementSettings -> placementSettings.allowRotationKey
      )
      .add()
      .<Boolean>append(
         new KeyedCodec<>("PlaceInEmptyBlocks", Codec.BOOLEAN),
         (placementSettings, o) -> placementSettings.placeInEmptyBlocks = o,
         placementSettings -> placementSettings.placeInEmptyBlocks
      )
      .documentation("If this block is allowed to be placed inside other blocks with an Empty Material (destroying them).")
      .add()
      .<BlockPlacementSettings.RotationMode>append(
         new KeyedCodec<>("RotationMode", BlockPlacementSettings.RotationMode.CODEC),
         (placementSettings, o) -> placementSettings.rotationMode = o,
         placementSettings -> placementSettings.rotationMode
      )
      .documentation("The mode determining the rotation of this block when placed.")
      .add()
      .<BlockPlacementSettings.BlockPreviewVisibility>append(
         new KeyedCodec<>("BlockPreviewVisibility", BlockPlacementSettings.BlockPreviewVisibility.CODEC),
         (placementSettings, o) -> placementSettings.previewVisibility = o,
         placementSettings -> placementSettings.previewVisibility
      )
      .documentation("An override for the block preview visibility")
      .add()
      .append(
         new KeyedCodec<>("WallPlacementOverrideBlockId", Codec.STRING),
         (placementSettings, o) -> placementSettings.wallPlacementOverrideBlockId = o,
         placementSettings -> placementSettings.wallPlacementOverrideBlockId
      )
      .add()
      .append(
         new KeyedCodec<>("FloorPlacementOverrideBlockId", Codec.STRING),
         (placementSettings, o) -> placementSettings.floorPlacementOverrideBlockId = o,
         placementSettings -> placementSettings.floorPlacementOverrideBlockId
      )
      .add()
      .append(
         new KeyedCodec<>("CeilingPlacementOverrideBlockId", Codec.STRING),
         (placementSettings, o) -> placementSettings.ceilingPlacementOverrideBlockId = o,
         placementSettings -> placementSettings.ceilingPlacementOverrideBlockId
      )
      .add()
      .append(new KeyedCodec<>("AllowBreakReplace", Codec.BOOLEAN), (o, v) -> o.allowBreakReplace = v, o -> o.allowBreakReplace)
      .add()
      .build();
   protected String wallPlacementOverrideBlockId;
   protected String floorPlacementOverrideBlockId;
   protected String ceilingPlacementOverrideBlockId;
   private boolean allowRotationKey = true;
   private boolean placeInEmptyBlocks;
   private BlockPlacementSettings.BlockPreviewVisibility previewVisibility = BlockPlacementSettings.BlockPreviewVisibility.DEFAULT;
   private BlockPlacementSettings.RotationMode rotationMode = BlockPlacementSettings.RotationMode.DEFAULT;
   protected boolean allowBreakReplace;

   protected BlockPlacementSettings() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BlockPlacementSettings toPacket() {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.IllegalStateException: Invalid switch case set: [[const(null)], [const(0)], [const(1)], [const(2)], [null]] for selector of type Lcom/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings$BlockPreviewVisibility;
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchHeadExprent.checkExprTypeBounds(SwitchHeadExprent.java:66)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExpr(VarTypeProcessor.java:140)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExprent(VarTypeProcessor.java:126)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.lambda$processVarTypes$2(VarTypeProcessor.java:114)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph.iterateExprents(DirectGraph.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.processVarTypes(VarTypeProcessor.java:114)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.calculateVarTypes(VarTypeProcessor.java:44)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsProcessor.setVarVersions(VarVersionsProcessor.java:68)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor.setVarVersions(VarProcessor.java:47)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:302)
      //
      // Bytecode:
      // 000: new com/hypixel/hytale/protocol/BlockPlacementSettings
      // 003: dup
      // 004: invokespecial com/hypixel/hytale/protocol/BlockPlacementSettings.<init> ()V
      // 007: astore 1
      // 008: aload 1
      // 009: aload 0
      // 00a: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.allowRotationKey Z
      // 00d: putfield com/hypixel/hytale/protocol/BlockPlacementSettings.allowRotationKey Z
      // 010: aload 1
      // 011: aload 0
      // 012: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.placeInEmptyBlocks Z
      // 015: putfield com/hypixel/hytale/protocol/BlockPlacementSettings.placeInEmptyBlocks Z
      // 018: aload 1
      // 019: aload 0
      // 01a: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.allowBreakReplace Z
      // 01d: putfield com/hypixel/hytale/protocol/BlockPlacementSettings.allowBreakReplace Z
      // 020: aload 1
      // 021: aload 0
      // 022: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.previewVisibility Lcom/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings$BlockPreviewVisibility;
      // 025: astore 2
      // 026: bipush 0
      // 027: istore 3
      // 028: aload 2
      // 029: iload 3
      // 02a: invokedynamic typeSwitch (Ljava/lang/Object;I)I bsm=java/lang/runtime/SwitchBootstraps.typeSwitch (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc; ]
      // 02f: tableswitch 29 -1 2 39 45 51 57
      // 04c: new java/lang/MatchException
      // 04f: dup
      // 050: aconst_null
      // 051: aconst_null
      // 052: invokespecial java/lang/MatchException.<init> (Ljava/lang/String;Ljava/lang/Throwable;)V
      // 055: athrow
      // 056: getstatic com/hypixel/hytale/protocol/BlockPreviewVisibility.Default Lcom/hypixel/hytale/protocol/BlockPreviewVisibility;
      // 059: goto 06b
      // 05c: getstatic com/hypixel/hytale/protocol/BlockPreviewVisibility.Default Lcom/hypixel/hytale/protocol/BlockPreviewVisibility;
      // 05f: goto 06b
      // 062: getstatic com/hypixel/hytale/protocol/BlockPreviewVisibility.AlwaysHidden Lcom/hypixel/hytale/protocol/BlockPreviewVisibility;
      // 065: goto 06b
      // 068: getstatic com/hypixel/hytale/protocol/BlockPreviewVisibility.AlwaysVisible Lcom/hypixel/hytale/protocol/BlockPreviewVisibility;
      // 06b: putfield com/hypixel/hytale/protocol/BlockPlacementSettings.previewVisibility Lcom/hypixel/hytale/protocol/BlockPreviewVisibility;
      // 06e: aload 1
      // 06f: aload 0
      // 070: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.rotationMode Lcom/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings$RotationMode;
      // 073: astore 2
      // 074: bipush 0
      // 075: istore 3
      // 076: aload 2
      // 077: iload 3
      // 078: invokedynamic typeSwitch (Ljava/lang/Object;I)I bsm=java/lang/runtime/SwitchBootstraps.typeSwitch (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc; ]
      // 07d: tableswitch 35 -1 3 45 51 57 63 69
      // 0a0: new java/lang/MatchException
      // 0a3: dup
      // 0a4: aconst_null
      // 0a5: aconst_null
      // 0a6: invokespecial java/lang/MatchException.<init> (Ljava/lang/String;Ljava/lang/Throwable;)V
      // 0a9: athrow
      // 0aa: getstatic com/hypixel/hytale/protocol/BlockPlacementRotationMode.Default Lcom/hypixel/hytale/protocol/BlockPlacementRotationMode;
      // 0ad: goto 0c5
      // 0b0: getstatic com/hypixel/hytale/protocol/BlockPlacementRotationMode.Default Lcom/hypixel/hytale/protocol/BlockPlacementRotationMode;
      // 0b3: goto 0c5
      // 0b6: getstatic com/hypixel/hytale/protocol/BlockPlacementRotationMode.FacingPlayer Lcom/hypixel/hytale/protocol/BlockPlacementRotationMode;
      // 0b9: goto 0c5
      // 0bc: getstatic com/hypixel/hytale/protocol/BlockPlacementRotationMode.StairFacingPlayer Lcom/hypixel/hytale/protocol/BlockPlacementRotationMode;
      // 0bf: goto 0c5
      // 0c2: getstatic com/hypixel/hytale/protocol/BlockPlacementRotationMode.BlockNormal Lcom/hypixel/hytale/protocol/BlockPlacementRotationMode;
      // 0c5: putfield com/hypixel/hytale/protocol/BlockPlacementSettings.rotationMode Lcom/hypixel/hytale/protocol/BlockPlacementRotationMode;
      // 0c8: aload 1
      // 0c9: aload 0
      // 0ca: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.wallPlacementOverrideBlockId Ljava/lang/String;
      // 0cd: ifnonnull 0d4
      // 0d0: bipush -1
      // 0d1: goto 0de
      // 0d4: invokestatic com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockType.getAssetMap ()Lcom/hypixel/hytale/assetstore/map/BlockTypeAssetMap;
      // 0d7: aload 0
      // 0d8: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.wallPlacementOverrideBlockId Ljava/lang/String;
      // 0db: invokevirtual com/hypixel/hytale/assetstore/map/BlockTypeAssetMap.getIndex (Ljava/lang/Object;)I
      // 0de: putfield com/hypixel/hytale/protocol/BlockPlacementSettings.wallPlacementOverrideBlockId I
      // 0e1: aload 1
      // 0e2: getfield com/hypixel/hytale/protocol/BlockPlacementSettings.wallPlacementOverrideBlockId I
      // 0e5: ldc -2147483648
      // 0e7: if_icmpne 0fb
      // 0ea: new java/lang/IllegalArgumentException
      // 0ed: dup
      // 0ee: aload 0
      // 0ef: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.wallPlacementOverrideBlockId Ljava/lang/String;
      // 0f2: invokedynamic makeConcatWithConstants (Ljava/lang/String;)Ljava/lang/String; bsm=java/lang/invoke/StringConcatFactory.makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ "Unknown key! \u0001" ]
      // 0f7: invokespecial java/lang/IllegalArgumentException.<init> (Ljava/lang/String;)V
      // 0fa: athrow
      // 0fb: aload 1
      // 0fc: aload 0
      // 0fd: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.floorPlacementOverrideBlockId Ljava/lang/String;
      // 100: ifnonnull 107
      // 103: bipush -1
      // 104: goto 111
      // 107: invokestatic com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockType.getAssetMap ()Lcom/hypixel/hytale/assetstore/map/BlockTypeAssetMap;
      // 10a: aload 0
      // 10b: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.floorPlacementOverrideBlockId Ljava/lang/String;
      // 10e: invokevirtual com/hypixel/hytale/assetstore/map/BlockTypeAssetMap.getIndex (Ljava/lang/Object;)I
      // 111: putfield com/hypixel/hytale/protocol/BlockPlacementSettings.floorPlacementOverrideBlockId I
      // 114: aload 1
      // 115: getfield com/hypixel/hytale/protocol/BlockPlacementSettings.floorPlacementOverrideBlockId I
      // 118: ldc -2147483648
      // 11a: if_icmpne 12e
      // 11d: new java/lang/IllegalArgumentException
      // 120: dup
      // 121: aload 0
      // 122: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.floorPlacementOverrideBlockId Ljava/lang/String;
      // 125: invokedynamic makeConcatWithConstants (Ljava/lang/String;)Ljava/lang/String; bsm=java/lang/invoke/StringConcatFactory.makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ "Unknown key! \u0001" ]
      // 12a: invokespecial java/lang/IllegalArgumentException.<init> (Ljava/lang/String;)V
      // 12d: athrow
      // 12e: aload 1
      // 12f: aload 0
      // 130: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.ceilingPlacementOverrideBlockId Ljava/lang/String;
      // 133: ifnonnull 13a
      // 136: bipush -1
      // 137: goto 144
      // 13a: invokestatic com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockType.getAssetMap ()Lcom/hypixel/hytale/assetstore/map/BlockTypeAssetMap;
      // 13d: aload 0
      // 13e: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.ceilingPlacementOverrideBlockId Ljava/lang/String;
      // 141: invokevirtual com/hypixel/hytale/assetstore/map/BlockTypeAssetMap.getIndex (Ljava/lang/Object;)I
      // 144: putfield com/hypixel/hytale/protocol/BlockPlacementSettings.ceilingPlacementOverrideBlockId I
      // 147: aload 1
      // 148: getfield com/hypixel/hytale/protocol/BlockPlacementSettings.ceilingPlacementOverrideBlockId I
      // 14b: ldc -2147483648
      // 14d: if_icmpne 161
      // 150: new java/lang/IllegalArgumentException
      // 153: dup
      // 154: aload 0
      // 155: getfield com/hypixel/hytale/server/core/asset/type/blocktype/config/BlockPlacementSettings.ceilingPlacementOverrideBlockId Ljava/lang/String;
      // 158: invokedynamic makeConcatWithConstants (Ljava/lang/String;)Ljava/lang/String; bsm=java/lang/invoke/StringConcatFactory.makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ "Unknown key! \u0001" ]
      // 15d: invokespecial java/lang/IllegalArgumentException.<init> (Ljava/lang/String;)V
      // 160: athrow
      // 161: aload 1
      // 162: areturn
   }

   public String getWallPlacementOverrideBlockId() {
      return this.wallPlacementOverrideBlockId;
   }

   public String getFloorPlacementOverrideBlockId() {
      return this.floorPlacementOverrideBlockId;
   }

   public String getCeilingPlacementOverrideBlockId() {
      return this.ceilingPlacementOverrideBlockId;
   }

   public static enum BlockPreviewVisibility {
      ALWAYS_VISIBLE,
      ALWAYS_HIDDEN,
      DEFAULT;

      public static final EnumCodec<BlockPlacementSettings.BlockPreviewVisibility> CODEC = new EnumCodec<>(BlockPlacementSettings.BlockPreviewVisibility.class);
   }

   public static enum RotationMode {
      FACING_PLAYER,
      BLOCK_NORMAL,
      STAIR_FACING_PLAYER,
      DEFAULT;

      public static final EnumCodec<BlockPlacementSettings.RotationMode> CODEC = new EnumCodec<>(BlockPlacementSettings.RotationMode.class);
   }
}
