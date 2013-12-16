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
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.MouseFilter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid="ZPCombat", name="ZPCombat", version="1.0")
@NetworkMod(clientSideRequired=true, serverSideRequired=true, channels={"zpcSync"}, packetHandler=ZPCSyncPacketHandler.class)
public class ZPCombat {
	
	public static KeyBinding keyBindForwardHijack;
	public static KeyBinding keyBindBackHijack;
	public static KeyBinding keyBindLeftHijack;
	public static KeyBinding keyBindRightHijack;
	public static KeyBinding keyBindJumpHijack;
	public static KeyBinding keyBindSneakHijack;
	
	public static KeyBinding keyBindConsume;
	
	@Instance("ZPCombat")
	public static ZPCombat instance;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		//Stomp renderPlayer model instance:
		if (event.getSide().isClient())
		{
			int i = 0;
			for (Field curField : RendererLivingEntity.class.getDeclaredFields())
			{
				if (curField.getType() == ModelBase.class)
				{
					if (i++ == 0)
					{
						mainModelField = curField;
						curField.setAccessible(true);
					}
				}
			}
			
			i = 0;
			for (Field curField : RenderPlayer.class.getDeclaredFields())
			{
				if (curField.getType() == ModelBiped.class)
				{
					if (i++ == 0)
					{
						modelBipedMainField = curField;
						curField.setAccessible(true);
					}
				}
			}
			
			RenderPlayer playerRenderer = (RenderPlayer)RenderManager.instance.getEntityClassRenderObject(EntityPlayer.class);
			ZPCPlayerModel newModel = new ZPCPlayerModel(0.0F);
			
			try {
				mainModelField.set((RendererLivingEntity)playerRenderer, (ModelBase)newModel);
				modelBipedMainField.set(playerRenderer, (ModelBiped)newModel);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		MinecraftForge.EVENT_BUS.register(new EventHookContainer());
	}
	
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
		keyBindSneakHijack = new KeyBinding("SneakHijack", mcGameSettings.keyBindSneak.keyCode);
		keyBindConsume = new KeyBinding("Consume", Keyboard.KEY_C);
		
		KeyBinding[] keys = {keyBindForwardHijack, keyBindBackHijack, 
				keyBindLeftHijack, keyBindRightHijack, keyBindJumpHijack, keyBindSneakHijack, keyBindConsume};
		
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
		
		i = 0;
		for (Field curField : MouseFilter.class.getDeclaredFields())
		{
			switch (i++)
			{
				case 0:
					mouseFilterField1 = curField;
					curField.setAccessible(true);
					break;
				case 1:
					mouseFilterField2 = curField;
					curField.setAccessible(true);
					break;
				case 2:
					mouseFilterField3 = curField;
					curField.setAccessible(true);
					break;
			}
		}
		
		i = 0;
		int j = 0;
		for (Field curField : EntityRenderer.class.getDeclaredFields())
		{
			if (curField.getType() == MouseFilter.class)
			{
				switch (i++)
				{
					case 0:
						mouseFilterXAxisField = curField;
						curField.setAccessible(true);
						break;
					case 1:
						mouseFilterYAxisField = curField;
						curField.setAccessible(true);
						break;
				}
			}
			else if (curField.getType() == float.class)
			{
				if (++j == 15)
				{
					camRollField = curField;
					curField.setAccessible(true);
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
	
	public static Field mouseFilterField1;
	public static Field mouseFilterField2;
	public static Field mouseFilterField3;
	
	public static Field mouseFilterXAxisField;
	public static Field mouseFilterYAxisField;
	
	public static Field camRollField; //15th float
	
	
	public static Field mainModelField;
	public static Field modelBipedMainField;
}
