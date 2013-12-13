package ZPCombatMod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.WorldServer;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid="ZPCombat", name="ZPCombat", version="0.1")
public class ZPCombat {
	
	@Instance("ZPCombat")
	public static ZPCombat instance;
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		Packet.addIdClassMapping(198, false, true, ZPCombatMoveAsyncPacketCtoS.class);
		Packet.addIdClassMapping(199, true, false, ZPCombatMoveAsyncPacketStoC.class);
		
		GameSettings mcGameSettings = Minecraft.getMinecraft().gameSettings;
		
		TickRegistry.registerTickHandler(new ZPServerTickHandler(), Side.SERVER);
		TickRegistry.registerTickHandler(new ZPClientTickHandler(), Side.CLIENT);
		
		KeyBinding[] keys = {mcGameSettings.keyBindForward, mcGameSettings.keyBindBack, 
				mcGameSettings.keyBindLeft, mcGameSettings.keyBindRight, mcGameSettings.keyBindJump};
		
		boolean[] repeats = new boolean[keys.length];
		
		
		KeyBindingRegistry.registerKeyBinding(new ZPCombatKeyHandler(keys, repeats));
	}
	
	public static Map<EntityPlayer, List<ZPCombatEvent>> combatEvents = new HashMap<EntityPlayer, List<ZPCombatEvent>>();
	
	
	public static Map threadSafePlayerListPerWorld = new HashMap<WorldServer, ArrayList>();
	
	
	
	public static boolean thisPlayerWasSprinting = false;
}
