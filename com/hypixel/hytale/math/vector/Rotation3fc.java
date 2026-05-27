package com.hypixel.hytale.math.vector;

import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public interface Rotation3fc {
   float x();

   float pitch();

   float y();

   float yaw();

   float z();

   float roll();

   Quaterniond getQuaternion(Quaterniond var1);

   Rotation3f premul(Quaterniondc var1, Rotation3f var2);

   Rotation3f mul(Quaterniondc var1, Rotation3f var2);

   Vector3d transform(Vector3dc var1, Vector3d var2);

   Vector3d transform(Vector3d var1);
}
