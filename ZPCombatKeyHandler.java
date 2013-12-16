package ZPCombatMod;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class ZPCombatKeyHandler extends KeyHandler {

	public ZPCombatKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings) {
		super(keyBindings, repeatings);
	}

	@Override
	public String getLabel() {
		return "ZPCombatKeyHandler";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,
			boolean tickEnd, boolean isRepeat) {
		
		GameSettings mcGameSettings = Minecraft.getMinecraft().gameSettings;
		
		if (kb.keyCode == mcGameSettings.keyBindForward.keyCode)
			mcGameSettings.keyBindForward.pressed = kb.pressed;
		else if (kb.keyCode == mcGameSettings.keyBindBack.keyCode)
			mcGameSettings.keyBindBack.pressed = kb.pressed;
		else if (kb.keyCode == mcGameSettings.keyBindLeft.keyCode)
			mcGameSettings.keyBindLeft.pressed = kb.pressed;
		else if (kb.keyCode == mcGameSettings.keyBindRight.keyCode)
			mcGameSettings.keyBindRight.pressed = kb.pressed;
		else if (kb.keyCode == mcGameSettings.keyBindSneak.keyCode)
			mcGameSettings.keyBindSneak.pressed = kb.pressed;
		
		
		if (Minecraft.getMinecraft().currentScreen == null)
		{
			EntityClientPlayerMP thisPlayer = Minecraft.getMinecraft().thePlayer;
			
			if (kb == ZPCombat.keyBindConsume && !tickEnd)
			{
				ItemStack currentItem = thisPlayer.getCurrentEquippedItem();
				if (currentItem != null && (currentItem.getItem() == Item.coal || currentItem.getItem() == Item.diamond))
				{
					synchronized(ZPCombat.combatEventsClient)
					{
						List<ZPCombatEvent> eventList = ZPCombat.combatEventsClient.get(thisPlayer);
						
						if (eventList == null)
						{
							eventList = new ArrayList<ZPCombatEvent>();
							
							ZPCombat.combatEventsClient.put(thisPlayer, eventList);
						}
						
						ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_Consume);
						eventList.add(newEvent);
						thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
					}
				}
			}
			
			if (thisPlayer.capabilities.allowFlying || thisPlayer.getAbsorptionAmount() == 0.0f)
			{
				if (kb.keyCode == mcGameSettings.keyBindJump.keyCode)
					mcGameSettings.keyBindJump.pressed = kb.pressed;
			}
			else
			{
				if (kb == ZPCombat.keyBindJumpHijack && !kb.pressed)
				{
					synchronized(ZPCombat.combatEventsClient)
					{
						List<ZPCombatEvent> eventList = ZPCombat.combatEventsClient.get(thisPlayer);
						
						if (eventList == null)
						{
							eventList = new ArrayList<ZPCombatEvent>();
							
							ZPCombat.combatEventsClient.put(thisPlayer, eventList);
						}
						
						if (thisPlayer.onGround)
						{
							if (ZPCombat.thisPlayerWasSprinting)
							{
								ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_JumpFront);
								newEvent.direction = ZPCombatEvent.getDirectionFromRotation(thisPlayer.rotationYaw);
								eventList.add(newEvent);
								thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
							}
							else
							{
								if (ZPCombat.keyBindSneakHijack.pressed)
								{
									ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_LowJump);
									eventList.add(newEvent);
									thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
								}
								else
								{
									ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_JumpUp);
									eventList.add(newEvent);
									thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
								}
							}
						}
						else
						{
							ZPClientTickHandler.doBlink = true;
						}
					}
				}
				else if (kb == ZPCombat.keyBindSneakHijack)
				{
					if (thisPlayer.getAbsorptionAmount() != 0.0f)
					{
						synchronized(ZPCombat.combatEventsClient)
						{
							List<ZPCombatEvent> eventList = ZPCombat.combatEventsClient.get(thisPlayer);
							
							if (eventList == null)
							{
								eventList = new ArrayList<ZPCombatEvent>();
								
								ZPCombat.combatEventsClient.put(thisPlayer, eventList);
							}
							
							if (!thisPlayer.onGround)
							{
								ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_Cruise);
								newEvent.direction = ZPCombatEvent.getDirectionFromRotation(thisPlayer.rotationYaw);
								newEvent.pitch = ZPCombatEvent.getPitchFromRotation(thisPlayer.rotationPitch);
								eventList.add(newEvent);
								thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
							}
						}
					}
				}
			}
		}
		
		//NetClientHandler netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
		
		//netHandler.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS());
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		
		GameSettings mcGameSettings = Minecraft.getMinecraft().gameSettings;
		
		if (kb.keyCode == mcGameSettings.keyBindForward.keyCode)
			mcGameSettings.keyBindForward.pressed = kb.pressed;
		else if (kb.keyCode == mcGameSettings.keyBindBack.keyCode)
			mcGameSettings.keyBindBack.pressed = kb.pressed;
		else if (kb.keyCode == mcGameSettings.keyBindLeft.keyCode)
			mcGameSettings.keyBindLeft.pressed = kb.pressed;
		else if (kb.keyCode == mcGameSettings.keyBindRight.keyCode)
			mcGameSettings.keyBindRight.pressed = kb.pressed;
		else if (kb.keyCode == mcGameSettings.keyBindSneak.keyCode)
			mcGameSettings.keyBindSneak.pressed = kb.pressed;
		
		EntityClientPlayerMP thisPlayer = Minecraft.getMinecraft().thePlayer;
		
		if (kb == ZPCombat.keyBindSneakHijack)
		{
			synchronized(ZPCombat.combatEventsClient)
			{
				List<ZPCombatEvent> eventList = ZPCombat.combatEventsClient.get(thisPlayer);
				
				if (eventList == null)
				{
					eventList = new ArrayList<ZPCombatEvent>();
					
					ZPCombat.combatEventsClient.put(thisPlayer, eventList);
				}
				
				ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_StopCruise);
				eventList.add(newEvent);
				thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
			}
		}
		
		if (thisPlayer.capabilities.allowFlying || thisPlayer.getAbsorptionAmount() == 0.0f)
		{
			if (kb.keyCode == mcGameSettings.keyBindJump.keyCode)
				mcGameSettings.keyBindJump.pressed = kb.pressed;
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

}
