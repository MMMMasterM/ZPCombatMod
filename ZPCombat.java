package ZPCombatMod;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
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
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid="ZPCombat", name="ZPCombat", version="0.1")
@NetworkMod(clientSideRequired=true, serverSideRequired=true, channels={"zpcSync"}, packetHandler=ZPCSyncPacketHandler.class)
public class ZPCombat {
	
	public static KeyBinding keyBindForwardHijack;
	public static KeyBinding keyBindBackHijack;
	public static KeyBinding keyBindLeftHijack;
	public static KeyBinding keyBindRightHijack;
	public static KeyBinding keyBindJumpHijack;
	
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
		
		
		keyBindJumpHijack = new KeyBinding("JumpHijack", mcGameSettings.keyBindJump.keyCode);
		keyBindForwardHijack = new KeyBinding("ForwardHijack", mcGameSettings.keyBindForward.keyCode);
		keyBindBackHijack = new KeyBinding("BackHijack", mcGameSettings.keyBindBack.keyCode);
		keyBindLeftHijack = new KeyBinding("LeftHijack", mcGameSettings.keyBindLeft.keyCode);
		keyBindRightHijack = new KeyBinding("RightHijack", mcGameSettings.keyBindRight.keyCode);
		
		KeyBinding[] keys = {keyBindForwardHijack, keyBindBackHijack, 
				keyBindLeftHijack, keyBindRightHijack, keyBindJumpHijack};
		
		boolean[] repeats = new boolean[keys.length];
		
		
		KeyBindingRegistry.registerKeyBinding(new ZPCombatKeyHandler(keys, repeats));
		
		int i = 0;
		for (Field curField : EntityOtherPlayerMP.class.getDeclaredFields())
		{
			Class curClass = curField.getType();
			if (curClass == int.class)
			{
				otherPlayerMPPosRotationIncrementsField = curField;
				curField.setAccessible(true);
			}
			else if (curClass == double.class)
			{
				switch (i++)
				{
					case 0:
						otherPlayerMPXField = curField;
						curField.setAccessible(true);
						break;
					case 1:
						otherPlayerMPYField = curField;
						curField.setAccessible(true);
						break;
					case 2:
						otherPlayerMPZField = curField;
						curField.setAccessible(true);
						break;
				}
			}
		}
	}
	
	//In order to make it work with the integrated server we need to separate this -_-
	public static Map<EntityPlayer, List<ZPCombatEvent>> combatEventsClient = new HashMap<EntityPlayer, List<ZPCombatEvent>>();
	public static Map<EntityPlayer, List<ZPCombatEvent>> combatEventsServer = new HashMap<EntityPlayer, List<ZPCombatEvent>>();
	
	
	public static Map threadSafePlayerListPerWorld = new HashMap<WorldServer, ArrayList>();
	
	
	
	public static boolean thisPlayerWasSprinting = false;
	
	
	
	public static Field otherPlayerMPPosRotationIncrementsField;
	public static Field otherPlayerMPXField;
	public static Field otherPlayerMPYField;
	public static Field otherPlayerMPZField;
}
