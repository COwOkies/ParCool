package com.alrex.parcool.utilities;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class PlayerUtils {
	public static Vec3d getVelocity(Entity entity) {
		return new Vec3d(entity.field_70159_w, entity.field_70181_x, entity.field_70179_y);
	}

	public static void setVelocity(Entity entity, Vec3d vec3d) {
		entity.field_70159_w = vec3d.x;
		entity.field_70181_x = vec3d.y;
		entity.field_70179_y = vec3d.z;
		entity.velocityChanged = true;
	}

	public static float getHeight(Entity entity) {
		return entity.field_70131_O;
	}

	public static float getWidth(Entity entity) {
		return entity.field_70130_N;
	}

	public static boolean isSneaking(Entity entity) {
		return entity.func_174832_aS();
	}
}