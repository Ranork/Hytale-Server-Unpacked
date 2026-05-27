package com.hypixel.hytale.server.core.asset.type.audiostate.config;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.protocol.AudioStateAuthority;
import com.hypixel.hytale.protocol.StateBinding;
import java.util.HashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AudioStateResolver {
   public static final int UNRESOLVED = -1;
   public static final int NAME_NOT_FOUND = -2;

   private AudioStateResolver() {
   }

   public static int resolveValueIndex(@Nullable String name, @Nullable String[] values) {
      if (name != null && !"*".equals(name)) {
         if (values == null) {
            return -2;
         } else {
            for (int i = 0; i < values.length; i++) {
               if (name.equals(values[i])) {
                  return i;
               }
            }

            return -2;
         }
      } else {
         return -1;
      }
   }

   public static void resolveBindings(@Nullable StateBindingConfig[] bindings) {
      if (bindings != null && bindings.length != 0) {
         IndexedLookupTableAssetMap<String, AudioState> assetMap = AudioState.getAssetMap();

         for (StateBindingConfig binding : bindings) {
            if (binding != null) {
               if (binding.audioStateId != null && !binding.audioStateId.isBlank()) {
                  int audioStateIndex = assetMap.getIndex(binding.audioStateId);
                  if (audioStateIndex == Integer.MIN_VALUE) {
                     binding.audioStateIndex = -1;
                     if (binding.deltas != null) {
                        for (StateDeltaConfig delta : binding.deltas) {
                           if (delta != null) {
                              delta.valueIndex = -1;
                           }
                        }
                     }
                  } else {
                     binding.audioStateIndex = audioStateIndex;
                     AudioState target = assetMap.getAsset(binding.audioStateId);
                     String[] values = target != null ? target.getValues() : null;
                     if (binding.deltas != null) {
                        for (StateDeltaConfig deltax : binding.deltas) {
                           if (deltax != null) {
                              int valueIndex = resolveValueIndex(deltax.valueName, values);
                              deltax.valueIndex = valueIndex >= 0 ? valueIndex : -1;
                           }
                        }
                     }
                  }
               } else {
                  binding.audioStateIndex = -1;
                  if (binding.deltas != null) {
                     for (StateDeltaConfig deltaxx : binding.deltas) {
                        if (deltaxx != null) {
                           deltaxx.valueIndex = -1;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void validateBindings(@Nullable StateBindingConfig[] bindings, @Nonnull String contextLabel, @Nonnull ValidationResults results) {
      if (bindings != null && bindings.length != 0) {
         HashSet<Integer> seenAudioStates = new HashSet<>();

         for (int bi = 0; bi < bindings.length; bi++) {
            StateBindingConfig binding = bindings[bi];
            if (binding == null) {
               results.fail(contextLabel + " StateBindings[" + bi + "] is null");
            } else if (binding.audioStateId != null && !binding.audioStateId.isBlank()) {
               if (binding.audioStateIndex == -1) {
                  results.fail(contextLabel + " StateBindings[" + bi + "] references unknown AudioState '" + binding.audioStateId + "'");
               } else {
                  if (!seenAudioStates.add(binding.audioStateIndex)) {
                     results.fail(
                        contextLabel
                           + " has multiple StateBindings targeting AudioState '"
                           + binding.audioStateId
                           + "'. At most one binding per AudioState per subscriber"
                     );
                  }

                  if (binding.deltas != null) {
                     for (int di = 0; di < binding.deltas.length; di++) {
                        StateDeltaConfig delta = binding.deltas[di];
                        if (delta == null) {
                           results.fail(contextLabel + " StateBindings[" + bi + "].Deltas[" + di + "] is null");
                        } else {
                           if (!Float.isFinite(delta.volumeDb)) {
                              results.fail(contextLabel + " StateBindings[" + bi + "].Deltas[" + di + "] VolumeDb is not finite");
                           }

                           if (delta.valueIndex == -1) {
                              if ("*".equals(delta.valueName)) {
                                 results.fail(
                                    contextLabel
                                       + " StateBindings["
                                       + bi
                                       + "].Deltas["
                                       + di
                                       + "] value name cannot be wildcard '*' (wildcards are for transitions only)"
                                 );
                              } else {
                                 results.fail(
                                    contextLabel
                                       + " StateBindings["
                                       + bi
                                       + "].Deltas["
                                       + di
                                       + "] references unknown state value '"
                                       + delta.valueName
                                       + "' on AudioState '"
                                       + binding.audioStateId
                                       + "'"
                                 );
                              }
                           }
                        }
                     }
                  }
               }
            } else {
               results.fail(contextLabel + " StateBindings[" + bi + "] AudioState id is missing");
            }
         }
      }
   }

   public static void resolveSetStates(@Nullable AmbienceStateWriteConfig[] writes) {
      if (writes != null && writes.length != 0) {
         IndexedLookupTableAssetMap<String, AudioState> assetMap = AudioState.getAssetMap();

         for (AmbienceStateWriteConfig write : writes) {
            if (write != null) {
               if (write.audioStateId != null && !write.audioStateId.isBlank()) {
                  int audioStateIndex = assetMap.getIndex(write.audioStateId);
                  if (audioStateIndex == Integer.MIN_VALUE) {
                     write.audioStateIndex = -1;
                     write.valueIndex = -1;
                  } else {
                     write.audioStateIndex = audioStateIndex;
                     AudioState target = assetMap.getAsset(write.audioStateId);
                     String[] values = target != null ? target.getValues() : null;
                     int valueIndex = resolveValueIndex(write.valueName, values);
                     write.valueIndex = valueIndex >= 0 ? valueIndex : -1;
                  }
               } else {
                  write.audioStateIndex = -1;
                  write.valueIndex = -1;
               }
            }
         }
      }
   }

   public static void validateSetStates(@Nullable AmbienceStateWriteConfig[] writes, @Nonnull String contextLabel, @Nonnull ValidationResults results) {
      if (writes != null && writes.length != 0) {
         IndexedLookupTableAssetMap<String, AudioState> assetMap = AudioState.getAssetMap();
         HashSet<Integer> seenAudioStates = new HashSet<>();

         for (int wi = 0; wi < writes.length; wi++) {
            AmbienceStateWriteConfig write = writes[wi];
            if (write == null) {
               results.fail(contextLabel + " SetStates[" + wi + "] is null");
            } else if (write.audioStateId == null || write.audioStateId.isBlank()) {
               results.fail(contextLabel + " SetStates[" + wi + "] AudioState id is missing");
            } else if (write.audioStateIndex == -1) {
               results.fail(contextLabel + " SetStates[" + wi + "] references unknown AudioState '" + write.audioStateId + "'");
            } else {
               if (!seenAudioStates.add(write.audioStateIndex)) {
                  results.fail(
                     contextLabel
                        + " has multiple SetStates writes targeting AudioState '"
                        + write.audioStateId
                        + "'. At most one write per AudioState per AmbienceFX"
                  );
               }

               AudioState target = assetMap.getAsset(write.audioStateId);
               if (target == null) {
                  results.fail(contextLabel + " SetStates[" + wi + "] target '" + write.audioStateId + "' could not be resolved");
               } else {
                  if (target.getAuthority() != AudioStateAuthority.Client) {
                     results.fail(
                        contextLabel
                           + " SetStates["
                           + wi
                           + "] targets AudioState '"
                           + write.audioStateId
                           + "' which has Authority: "
                           + target.getAuthority()
                           + ". SetStates may only target Authority: Client axes"
                     );
                  }

                  if (write.valueName == null || write.valueName.isBlank()) {
                     results.fail(contextLabel + " SetStates[" + wi + "] Value is missing");
                  } else if (write.valueIndex == -1) {
                     if ("*".equals(write.valueName)) {
                        results.fail(contextLabel + " SetStates[" + wi + "] value name cannot be wildcard '*'");
                     } else {
                        results.fail(
                           contextLabel
                              + " SetStates["
                              + wi
                              + "] references unknown state value '"
                              + write.valueName
                              + "' on AudioState '"
                              + write.audioStateId
                              + "'"
                        );
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   public static StateBinding[] toPacketArray(@Nullable StateBindingConfig[] bindings) {
      if (bindings != null && bindings.length != 0) {
         StateBinding[] out = new StateBinding[bindings.length];

         for (int i = 0; i < bindings.length; i++) {
            out[i] = bindings[i] != null ? bindings[i].toPacket() : null;
         }

         return out;
      } else {
         return null;
      }
   }
}
