package com.hypixel.hytale.builtin.instances.page;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.instances.blocks.ConfigurableInstanceBlock;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConfigureInstanceBlockPage extends InteractiveCustomUIPage<ConfigureInstanceBlockPage.PageData> {
   @Nonnull
   private final ConfigurableInstanceBlock instanceBlock;
   @Nonnull
   private final Ref<ChunkStore> ref;
   @Nullable
   private Vector3d positionOffset;
   @Nullable
   private Vector3f rotation;

   public ConfigureInstanceBlockPage(@Nonnull PlayerRef playerRef, @Nonnull Ref<ChunkStore> ref) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, ConfigureInstanceBlockPage.PageData.CODEC);
      this.instanceBlock = ref.getStore().getComponent(ref, ConfigurableInstanceBlock.getComponentType());
      this.ref = ref;
      this.positionOffset = this.instanceBlock.getPositionOffset() != null ? this.instanceBlock.getPositionOffset().clone() : null;
      this.rotation = this.instanceBlock.getRotation() != null ? this.instanceBlock.getRotation().clone() : null;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/ConfigureInstanceBlockPage.ui");
      ObjectArrayList<DropdownEntryInfo> worlds = new ObjectArrayList();
      worlds.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.configureInstanceBlockPage.noInstances"), ""));
      List<String> instances = InstancesPlugin.get().getInstanceAssets();
      instances.sort(String::compareToIgnoreCase);

      for (String instance : instances) {
         worlds.add(new DropdownEntryInfo(LocalizableString.fromString(instance), instance));
      }

      commandBuilder.set("#Instance #Input.Entries", worlds);
      commandBuilder.set("#Instance #Input.Value", this.instanceBlock.getInstanceName() == null ? "" : this.instanceBlock.getInstanceName());
      this.buildPositionOffset(commandBuilder);
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#PositionOffset #Use #CheckBox",
         new EventData()
            .append("Action", ConfigureInstanceBlockPage.Action.PositionOffset.name())
            .append("@PositionOffset", "#PositionOffset #Use #CheckBox.Value"),
         false
      );
      this.buildRotation(commandBuilder);
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#Rotation #Use #CheckBox",
         new EventData().append("Action", ConfigureInstanceBlockPage.Action.Rotation.name()).append("@Rotation", "#Rotation #Use #CheckBox.Value"),
         false
      );
      commandBuilder.set("#InstanceKey #Input.Value", this.instanceBlock.getInstanceKey() == null ? "" : this.instanceBlock.getInstanceKey());
      commandBuilder.set("#PersonalReturnPoint #CheckBox.Value", this.instanceBlock.isPersonalReturnPoint());
      commandBuilder.set("#CloseOnBlockRemove #CheckBox.Value", this.instanceBlock.isCloseOnRemove());
      commandBuilder.set("#RemoveBlockAfter #Input.Value", this.instanceBlock.getRemoveBlockAfter());
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#SaveButton",
         new EventData()
            .append("Action", ConfigureInstanceBlockPage.Action.Save.name())
            .append("@Instance", "#Instance #Input.Value")
            .append("@InstanceKey", "#InstanceKey #Input.Value")
            .append("@PositionOffset", "#PositionOffset #Use #CheckBox.Value")
            .append("@PositionOffsetX", "#PositionOffset #X #Input.Value")
            .append("@PositionOffsetY", "#PositionOffset #Y #Input.Value")
            .append("@PositionOffsetZ", "#PositionOffset #Z #Input.Value")
            .append("@Rotation", "#Rotation #Use #CheckBox.Value")
            .append("@RotationPitch", "#Rotation #Pitch #Input.Value")
            .append("@RotationYaw", "#Rotation #Yaw #Input.Value")
            .append("@RotationRoll", "#Rotation #Roll #Input.Value")
            .append("@PersonalReturnPoint", "#PersonalReturnPoint #CheckBox.Value")
            .append("@CloseOnBlockRemove", "#CloseOnBlockRemove #CheckBox.Value")
            .append("@RemoveBlockAfter", "#RemoveBlockAfter #Input.Value")
      );
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ConfigureInstanceBlockPage.PageData data) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      BlockModule.BlockStateInfo info = this.ref.getStore().getComponent(this.ref, BlockModule.BlockStateInfo.getComponentType());
      BlockComponentChunk blockComponentChunk = this.ref.getStore().getComponent(info.getChunkRef(), BlockComponentChunk.getComponentType());

      assert blockComponentChunk != null;

      switch (data.action) {
         case Save:
            this.instanceBlock.setInstanceName(this.emptyToNull(data.instance));
            this.instanceBlock.setInstanceKey(this.emptyToNull(data.instanceKey));
            this.instanceBlock.setPersonalReturnPoint(data.personalReturnPoint);
            this.instanceBlock.setCloseOnRemove(data.closeOnBlockRemove);
            this.instanceBlock.setRemoveBlockAfter(data.removeBlockAfter);
            if (data.positionOffset) {
               this.instanceBlock.setPositionOffset(new Vector3d(data.positionX, data.positionY, data.positionZ));
            } else {
               this.instanceBlock.setPositionOffset(null);
            }

            if (data.rotation) {
               this.instanceBlock
                  .setRotation(
                     new Vector3f(
                        data.rotationPitch * (float) (Math.PI / 180.0),
                        data.rotationYaw * (float) (Math.PI / 180.0),
                        data.rotationRoll * (float) (Math.PI / 180.0)
                     )
                  );
            } else {
               this.instanceBlock.setRotation(null);
            }

            blockComponentChunk.markNeedsSaving();
            playerComponent.getPageManager().setPage(ref, store, Page.None);
            break;
         case PositionOffset:
            if (data.positionOffset) {
               this.positionOffset = new Vector3d();
            } else {
               this.positionOffset = null;
            }

            blockComponentChunk.markNeedsSaving();
            UICommandBuilder commandBuilder = new UICommandBuilder();
            this.buildPositionOffset(commandBuilder);
            this.sendUpdate(commandBuilder);
            break;
         case Rotation:
            if (data.rotation) {
               this.rotation = new Vector3f();
            } else {
               this.rotation = null;
            }

            blockComponentChunk.markNeedsSaving();
            UICommandBuilder commandBuilder = new UICommandBuilder();
            this.buildRotation(commandBuilder);
            this.sendUpdate(commandBuilder);
      }
   }

   private String emptyToNull(@Nullable String s) {
      return s != null && !s.isEmpty() ? s : null;
   }

   private void buildPositionOffset(@Nonnull UICommandBuilder commandBuilder) {
      boolean hasPosition = this.positionOffset != null;
      commandBuilder.set("#PositionOffset #Use #CheckBox.Value", hasPosition);
      commandBuilder.set("#PositionOffset #X.Visible", hasPosition);
      commandBuilder.set("#PositionOffset #Y.Visible", hasPosition);
      commandBuilder.set("#PositionOffset #Z.Visible", hasPosition);
      if (hasPosition) {
         commandBuilder.set("#PositionOffset #X #Input.Value", this.positionOffset.x);
         commandBuilder.set("#PositionOffset #Y #Input.Value", this.positionOffset.y);
         commandBuilder.set("#PositionOffset #Z #Input.Value", this.positionOffset.z);
      }
   }

   private void buildRotation(@Nonnull UICommandBuilder commandBuilder) {
      boolean hasRotation = this.rotation != null;
      commandBuilder.set("#Rotation #Use #CheckBox.Value", hasRotation);
      commandBuilder.set("#Rotation #Pitch.Visible", hasRotation);
      commandBuilder.set("#Rotation #Yaw.Visible", hasRotation);
      commandBuilder.set("#Rotation #Roll.Visible", hasRotation);
      if (hasRotation) {
         commandBuilder.set("#Rotation #Pitch #Input.Value", this.rotation.x * (180.0F / (float)Math.PI));
         commandBuilder.set("#Rotation #Yaw #Input.Value", this.rotation.y * (180.0F / (float)Math.PI));
         commandBuilder.set("#Rotation #Roll #Input.Value", this.rotation.z * (180.0F / (float)Math.PI));
      }
   }

   public static enum Action {
      Save,
      PositionOffset,
      Rotation;
   }

   public static class PageData {
      @Nonnull
      public static final String INSTANCE = "@Instance";
      @Nonnull
      public static final String INSTANCE_KEY = "@InstanceKey";
      @Nonnull
      public static final String POSITION_OFFSET = "@PositionOffset";
      @Nonnull
      public static final String POSITION_OFFSET_X = "@PositionOffsetX";
      @Nonnull
      public static final String POSITION_OFFSET_Y = "@PositionOffsetY";
      @Nonnull
      public static final String POSITION_OFFSET_Z = "@PositionOffsetZ";
      @Nonnull
      public static final String ROTATION = "@Rotation";
      @Nonnull
      public static final String ROTATION_PITCH = "@RotationPitch";
      @Nonnull
      public static final String ROTATION_YAW = "@RotationYaw";
      @Nonnull
      public static final String ROTATION_ROLL = "@RotationRoll";
      @Nonnull
      public static final String PERSONAL_RETURN_POINT = "@PersonalReturnPoint";
      @Nonnull
      public static final String CLOSE_ON_BLOCK_REMOVE = "@CloseOnBlockRemove";
      @Nonnull
      public static final String REMOVE_BLOCK_AFTER = "@RemoveBlockAfter";
      public static final BuilderCodec<ConfigureInstanceBlockPage.PageData> CODEC = BuilderCodec.builder(
            ConfigureInstanceBlockPage.PageData.class, ConfigureInstanceBlockPage.PageData::new
         )
         .addField(new KeyedCodec<>("Action", new EnumCodec<>(ConfigureInstanceBlockPage.Action.class)), (o, i) -> o.action = i, o -> o.action)
         .addField(new KeyedCodec<>("@Instance", Codec.STRING), (o, i) -> o.instance = i, o -> o.instance)
         .addField(new KeyedCodec<>("@InstanceKey", Codec.STRING), (o, i) -> o.instanceKey = i, o -> o.instanceKey)
         .addField(new KeyedCodec<>("@PositionOffset", Codec.BOOLEAN), (o, i) -> o.positionOffset = i, o -> o.positionOffset)
         .addField(new KeyedCodec<>("@PositionOffsetX", Codec.DOUBLE), (o, i) -> o.positionX = i, o -> o.positionX)
         .addField(new KeyedCodec<>("@PositionOffsetY", Codec.DOUBLE), (o, i) -> o.positionY = i, o -> o.positionY)
         .addField(new KeyedCodec<>("@PositionOffsetZ", Codec.DOUBLE), (o, i) -> o.positionZ = i, o -> o.positionZ)
         .addField(new KeyedCodec<>("@Rotation", Codec.BOOLEAN), (o, i) -> o.rotation = i, o -> o.rotation)
         .addField(new KeyedCodec<>("@RotationPitch", Codec.FLOAT), (o, i) -> o.rotationPitch = i, o -> o.rotationPitch)
         .addField(new KeyedCodec<>("@RotationYaw", Codec.FLOAT), (o, i) -> o.rotationYaw = i, o -> o.rotationYaw)
         .addField(new KeyedCodec<>("@RotationRoll", Codec.FLOAT), (o, i) -> o.rotationRoll = i, o -> o.rotationRoll)
         .addField(new KeyedCodec<>("@PersonalReturnPoint", Codec.BOOLEAN), (o, i) -> o.personalReturnPoint = i, o -> o.personalReturnPoint)
         .addField(new KeyedCodec<>("@CloseOnBlockRemove", Codec.BOOLEAN), (o, i) -> o.closeOnBlockRemove = i, o -> o.closeOnBlockRemove)
         .addField(new KeyedCodec<>("@RemoveBlockAfter", Codec.DOUBLE), (o, i) -> o.removeBlockAfter = i, o -> o.removeBlockAfter)
         .build();
      public ConfigureInstanceBlockPage.Action action;
      public String instance;
      public String instanceKey;
      public boolean positionOffset;
      public double positionX;
      public double positionY;
      public double positionZ;
      public boolean rotation;
      public float rotationPitch;
      public float rotationYaw;
      public float rotationRoll;
      public boolean personalReturnPoint;
      public boolean closeOnBlockRemove;
      public double removeBlockAfter;
   }
}
