package com.alrex.parcool.common.network;

import com.alrex.parcool.ParCool;
import com.alrex.parcool.common.action.impl.WallSlide;
import com.alrex.parcool.common.capability.impl.Parkourability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncWallSlideMessage {

	private boolean sliding = false;
	private UUID playerID = null;

	public boolean isSliding() {
		return sliding;
	}

	public void encode(FriendlyByteBuf packet) {
		packet.writeLong(this.playerID.getMostSignificantBits());
		packet.writeLong(this.playerID.getLeastSignificantBits());
		packet.writeBoolean(sliding);
	}

	public static SyncWallSlideMessage decode(FriendlyByteBuf packet) {
		SyncWallSlideMessage message = new SyncWallSlideMessage();
		message.playerID = new UUID(packet.readLong(), packet.readLong());
		message.sliding = packet.readBoolean();
		return message;
	}

	@OnlyIn(Dist.DEDICATED_SERVER)
	public void handleServer(Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> {
			Player player;
			player = contextSupplier.get().getSender();
			ParCool.CHANNEL_INSTANCE.send(PacketDistributor.ALL.noArg(), this);

			if (player == null) return;
			Parkourability parkourability = Parkourability.get(player);
			if (parkourability == null) return;
			parkourability.getWallSlide().synchronize(this);
		});
		contextSupplier.get().setPacketHandled(true);
	}

	@OnlyIn(Dist.CLIENT)
	public void handleClient(Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> {
			Player player;
			if (contextSupplier.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
				Level world = Minecraft.getInstance().level;
				if (world == null) return;
				player = world.getPlayerByUUID(playerID);
				if (player == null || player.isLocalPlayer()) return;
			} else {
				player = contextSupplier.get().getSender();
				ParCool.CHANNEL_INSTANCE.send(PacketDistributor.ALL.noArg(), this);
				if (player == null) return;
			}

			Parkourability parkourability = Parkourability.get(player);
			if (parkourability == null) return;
			parkourability.getWallSlide().synchronize(this);
		});
		contextSupplier.get().setPacketHandled(true);
	}

	@OnlyIn(Dist.CLIENT)
	public static void sync(Player player, WallSlide wallSlide) {
		SyncWallSlideMessage message = new SyncWallSlideMessage();
		message.playerID = player.getUUID();
		message.sliding = wallSlide.isSliding();

		ParCool.CHANNEL_INSTANCE.sendToServer(message);
	}
}