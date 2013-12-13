package ZPCombatMod;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.NetClientHandler;
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
		
		if (kb == Minecraft.getMinecraft().gameSettings.keyBindJump)
		{
			synchronized(ZPCombat.combatEvents)
			{
				EntityClientPlayerMP thisPlayer = Minecraft.getMinecraft().thePlayer;
				List<ZPCombatEvent> eventList = ZPCombat.combatEvents.get(thisPlayer);
				
				if (eventList == null)
				{
					eventList = new ArrayList<ZPCombatEvent>();
					
					ZPCombat.combatEvents.put(thisPlayer, eventList);
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
					ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_JumpUp);
					eventList.add(newEvent);
					thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
				}
			}
		}
		
		//NetClientHandler netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
		
		//netHandler.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS());
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

}
