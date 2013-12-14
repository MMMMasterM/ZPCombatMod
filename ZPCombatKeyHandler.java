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
		
		//if (kb.keyCode == mcGameSettings.keyBindJump.keyCode)
		//	mcGameSettings.keyBindJump.pressed = kb.pressed;
		
		if (kb == ZPCombat.keyBindJumpHijack)
		{
			synchronized(ZPCombat.combatEventsClient)
			{
				EntityClientPlayerMP thisPlayer = Minecraft.getMinecraft().thePlayer;
				List<ZPCombatEvent> eventList = ZPCombat.combatEventsClient.get(thisPlayer);
				
				if (eventList == null)
				{
					eventList = new ArrayList<ZPCombatEvent>();
					
					ZPCombat.combatEventsClient.put(thisPlayer, eventList);
				}
				
				if (ZPCombat.thisPlayerWasSprinting)
				{
					ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_JumpFront);
					newEvent.direction = ZPCombatEvent.getDirectionFromRotation(thisPlayer.rotationYaw);
					eventList.add(newEvent);
					thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
				}
				else
				{
					if (thisPlayer.onGround)
					{
						ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_JumpUp);
						eventList.add(newEvent);
						thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
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
		
		//if (kb.keyCode == mcGameSettings.keyBindJump.keyCode)
		//	mcGameSettings.keyBindJump.pressed = kb.pressed;
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

}
