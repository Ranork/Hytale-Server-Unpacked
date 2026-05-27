package com.hypixel.hytale.builtin.audio.components;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.audio.AudioPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.AudioStateAuthority;
import com.hypixel.hytale.protocol.StateTransition;
import com.hypixel.hytale.protocol.packets.world.SetAudioState;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.AudioState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AudioStateComponent implements Component<EntityStore> {
   private transient int[] currentValueIndices;
   private transient int[] valueVersions;
   private transient StateTransition[] activeTransitionOverrides;
   private transient int[] lastSentValueVersions;
   @Nonnull
   private final transient SetAudioState pooledPacket = new SetAudioState();

   public static ComponentType<EntityStore, AudioStateComponent> getComponentType() {
      return AudioPlugin.get().getAudioStateComponentType();
   }

   private void ensureArrays() {
      int count = AudioState.getAssetMap().getNextIndex();
      if (this.currentValueIndices == null) {
         this.currentValueIndices = new int[count];
         this.valueVersions = new int[count];
         this.activeTransitionOverrides = new StateTransition[count];
         this.lastSentValueVersions = new int[count];
         this.seedDefaults(0, count);
      } else if (this.currentValueIndices.length < count) {
         int oldLen = this.currentValueIndices.length;
         this.currentValueIndices = Arrays.copyOf(this.currentValueIndices, count);
         this.valueVersions = Arrays.copyOf(this.valueVersions, count);
         this.activeTransitionOverrides = Arrays.copyOf(this.activeTransitionOverrides, count);
         this.lastSentValueVersions = Arrays.copyOf(this.lastSentValueVersions, count);
         this.seedDefaults(oldLen, count);
      }
   }

   private void seedDefaults(int fromInclusive, int toExclusive) {
      IndexedLookupTableAssetMap<String, AudioState> assetMap = AudioState.getAssetMap();

      for (int i = fromInclusive; i < toExclusive; i++) {
         AudioState asset = assetMap.getAsset(i);
         this.currentValueIndices[i] = asset != null ? asset.getDefaultValueIndex() : 0;
      }
   }

   public void setValue(int axisIndex, int valueIndex, @Nullable StateTransition transitionOverride) {
      this.ensureArrays();
      if (axisIndex >= 0 && axisIndex < this.currentValueIndices.length) {
         AudioState asset = AudioState.getAssetMap().getAsset(axisIndex);
         if (asset != null && asset.getAuthority() == AudioStateAuthority.Server) {
            String[] values = asset.getValues();
            if (values != null && valueIndex >= 0 && valueIndex < values.length) {
               if (this.currentValueIndices[axisIndex] != valueIndex || !Objects.equals(this.activeTransitionOverrides[axisIndex], transitionOverride)) {
                  this.currentValueIndices[axisIndex] = valueIndex;
                  this.activeTransitionOverrides[axisIndex] = transitionOverride;
                  this.valueVersions[axisIndex]++;
               }
            }
         }
      }
   }

   public int[] getCurrentValueIndices() {
      this.ensureArrays();
      return this.currentValueIndices;
   }

   public int[] getValueVersions() {
      this.ensureArrays();
      return this.valueVersions;
   }

   public StateTransition[] getActiveTransitionOverrides() {
      this.ensureArrays();
      return this.activeTransitionOverrides;
   }

   public int[] getLastSentValueVersions() {
      this.ensureArrays();
      return this.lastSentValueVersions;
   }

   public void setLastSentValueVersion(int axisIndex, int version) {
      this.ensureArrays();
      this.lastSentValueVersions[axisIndex] = version;
   }

   @Nonnull
   public SetAudioState getPooledPacket() {
      return this.pooledPacket;
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      AudioStateComponent clone = new AudioStateComponent();
      if (this.currentValueIndices != null) {
         clone.currentValueIndices = (int[])this.currentValueIndices.clone();
         clone.valueVersions = (int[])this.valueVersions.clone();
         clone.activeTransitionOverrides = (StateTransition[])this.activeTransitionOverrides.clone();
         clone.lastSentValueVersions = (int[])this.lastSentValueVersions.clone();
      }

      return clone;
   }
}
