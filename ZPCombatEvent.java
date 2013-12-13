package ZPCombatMod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ZPCombatEvent {
	
	/*
	 * Combat event IDs:
	 * 
	 */
	public static final byte combatEvtID_JumpUp = 0;
	public static final byte combatEvtID_JumpFront = 1;
	public static final byte combatEvtID_RunLeft = 2;
	public static final byte combatEvtID_RunRight = 3;
	public static final byte combatEvtID_RunForward = 4;
	public static final byte combatEvtID_RunBack = 5;
	public static final byte combatEvtID_Cruise = 6;
	public static final byte combatEvtID_Blink = 7;
	
	
	public byte combatEventID;
	
	public byte direction;
	
	public ZPCombatEvent(byte eventID)
	{
		this.combatEventID = eventID;
	}
	
	
	public static int getMetaDataSize(byte eventID)
	{
		switch (eventID)
		{
			case combatEvtID_JumpUp:
				return 0;
			case combatEvtID_JumpFront:
				return 1;
		}
		
		return 0;
	}
	
	
	
	public static byte getDirectionFromRotation(double rotationYaw)
	{
		//make sure rotation is in interval [0, 360deg]
		rotationYaw = rotationYaw % 360.0d;
		
		if (rotationYaw < 0.0d)
			rotationYaw += 360.0d;
		
		//not sure if (byte) cast also rounds down for negative numbers, so let's make sure:
		return (byte)Math.floor(rotationYaw * 256.0d / 360.0d - 128.0d);
	}
	
	public static double getRotationFromDirection(byte directionPar)
	{
		return (((double)directionPar) + 128.0d) * 360.0d / 256.0d;
	}
	
	
	public static ZPCombatEvent addCombatEventToList(EntityPlayer playerEntity, byte newEventId, byte[] packetMetaData)
	{
		ZPCombatEvent newEvent = null;
		
		synchronized(ZPCombat.combatEvents)
		{
			List<ZPCombatEvent> eventList = ZPCombat.combatEvents.get(playerEntity);
			
			if (eventList == null)
			{
				eventList = new ArrayList<ZPCombatEvent>();
				
				ZPCombat.combatEvents.put(playerEntity, eventList);
			}
			
			switch (newEventId)
			{
				case ZPCombatEvent.combatEvtID_JumpUp:
					newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_JumpUp);
					break;
				case ZPCombatEvent.combatEvtID_JumpFront:
					newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_JumpFront);
					newEvent.direction = packetMetaData[0];
					break;
			}
			
			if (newEvent != null)
				eventList.add(newEvent);
		}
		
		return newEvent;
	}
}
