package net.cookedseafood.candywrapper.mixin;

import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.cookedseafood.candywrapper.api.EntityApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityApi{
	@Override
    public boolean hasCommandTag(String commandTag) {
        return this.getCommandTags().contains(commandTag);
    }

    @Redirect(
        method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityType;isSaveable()Z"
        )
    )
    private boolean isSaveableOrPlayer(EntityType<?> entityType) {
        if (entityType.isSaveable()) {
            return true;
        }

        return this.isPlayer();
    }

    @Inject(
        method = "addPassenger(Lnet/minecraft/entity/Entity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/Entity;)V",
            shift = At.Shift.AFTER
        )
    )
    private void sendPacketAddPassenger(Entity passenger, CallbackInfo ci) {
        if (this.isPlayer()) {
            ((ServerPlayerEntity)(Object)this).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(((Entity)(Object)this)));
        }
    }

    @Inject(
        method = "removePassenger(Lnet/minecraft/entity/Entity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/Entity;)V",
            shift = At.Shift.AFTER
        )
    )
    private void sendPacketRemovePassenger(Entity passenger, CallbackInfo ci) {
        if (this.isPlayer()) {
            ((ServerPlayerEntity)(Object)this).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(((Entity)(Object)this)));
        }
    }

    @Shadow
    public abstract Set<String> getCommandTags();

    @Shadow
    public abstract boolean isPlayer();
}
