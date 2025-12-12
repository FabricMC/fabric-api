package net.fabricmc.fabric.test.rendering.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.gizmos.CircleGizmo;
import net.minecraft.world.phys.Vec3;

@Mixin(CircleGizmo.class)
public final class CircleGizmoMixin {
	private CircleGizmoMixin() {
	}

	@WrapOperation(
			method = "emit",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private Vec3 rotateCircle(
			Vec3 instance,
			double d,
			double e,
			double f,
			Operation<Vec3> original
	) {
		return original.call(instance, f, d, e);
	}
}
