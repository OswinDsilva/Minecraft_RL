package dev.minecraftrl.integration.client;

import dev.minecraftrl.integration.Minecraftrl_integration;
import dev.minecraftrl.integration.client.network.SocketProgram;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec3;
import java.util.concurrent.atomic.AtomicReference;

public class Minecraftrl_integrationClient implements ClientModInitializer {
	
	public record PlayerState(double x, double y, double z, double yaw, double pitch, Vec3 vel, boolean isOnGround) {};
	PlayerState c_state = null;
	
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,  Minecraftrl_integration.id("before_chat"), this::extract);

		AtomicReference<PlayerState> sharedState = new AtomicReference<>(c_state);

		Thread socketThread = new Thread(new SocketProgram(8000, sharedState));
		socketThread.start();


		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.player != null){
				this.c_state = new PlayerState( 
					client.player.getX(), 
					client.player.getY(), 
					client.player.getZ(), 
					client.player.getYRot(), 
					client.player.getXRot(),
					client.player.getDeltaMovement(),
					client.player.onGround()
				); 	
				sharedState.set(this.c_state);
			}
		});
	}

	private void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
		int white = 0xFFFFFFFF;
		if(c_state != null){
			Font g_font = Minecraft.getInstance().font;
			graphics.text(g_font, "x:" + c_state.x(), 0, 0, white);
			graphics.text(g_font, "y:" + c_state.y(), 0, 10, white);
			graphics.text(g_font, "z:" + c_state.z(), 0, 20, white);
			graphics.text(g_font, "yaw:" + c_state.yaw(), 0, 30, white);
			graphics.text(g_font, "pitch:" + c_state.pitch(), 0, 40, white);
			graphics.text(g_font, "X v:" + c_state.vel().x() * 20, 0, 50, white);
			graphics.text(g_font, "Y v:" + c_state.vel().y() * 20, 0, 60, white);
			graphics.text(g_font, "Z v:" + c_state.vel().z() * 20, 0, 70, white);
			graphics.text(g_font, "On Ground:" + c_state.isOnGround(), 0, 80, white);
		}
	}
}