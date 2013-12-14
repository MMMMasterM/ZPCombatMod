package ZPCombatMod;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.Side;
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
	public static final byte combatEvtID_Impact = 8;//impact on ground
	public static final byte combatEvtID_Liftoff = 9;//not onGround anymore
	
	
	public byte combatEventID;
	
	public byte direction;
	
	public float impactX;
	public float impactY;
	public float impactZ;
	
	public ZPCombatEvent(byte eventID)
	{
		this.combatEventID = eventID;
	}
	
	public ZPCombatEvent(byte eventID, DataInput datainput) throws IOException
	{
		this(eventID);
		
		switch (eventID)
		{
			case combatEvtID_JumpFront:
				this.direction = datainput.readByte();
				break;
			case combatEvtID_Impact:
				this.impactX = datainput.readFloat();
				this.impactY = datainput.readFloat();
				this.impactZ = datainput.readFloat();
				break;
			case combatEvtID_Liftoff:
				break;
		}
	}
	
	public void writeDataToPacket(DataOutput dataoutput) throws IOException
	{
		switch (this.combatEventID)
		{
			case combatEvtID_JumpFront:
				dataoutput.writeByte(this.direction);
				break;
			case combatEvtID_Impact:
				dataoutput.writeFloat(this.impactX);
				dataoutput.writeFloat(this.impactY);
				dataoutput.writeFloat(this.impactZ);
				break;
			case combatEvtID_Liftoff:
				break;
		}
	}
	
	
	public static int getDataSize(byte eventID)
	{
		switch (eventID)
		{
			case combatEvtID_JumpUp:
				return 0;
			case combatEvtID_JumpFront:
				return 1;
			case combatEvtID_Impact:
				return 12;
			case combatEvtID_Liftoff:
				return 0;
		}
		
		return 0;
	}
	
	
	
	
	
	public void applyToPlayer(EntityPlayer targetPlayer, Side side)
	{
		switch (this.combatEventID)
		{
			case ZPCombatEvent.combatEvtID_JumpUp:
				targetPlayer.motionY += 1.2d;//0.27d;
				break;
			case ZPCombatEvent.combatEvtID_JumpFront:
				double rot = ZPCombatEvent.getRotationFromDirection(this.direction);
				double magX = -Math.sin(rot * Math.PI / 180.0d);
				double magZ = Math.cos(rot * Math.PI / 180.0d);
				targetPlayer.motionX += magX;
				targetPlayer.motionZ += magZ;
				break;
			case ZPCombatEvent.combatEvtID_Impact:
				//Server already tracks this from vanilla code... kindof...
				if (true)//side.isClient())
				{
					targetPlayer.setPosition(this.impactX, this.impactY, this.impactZ);
					//targetPlayer.onGround = true;
				}
				break;
			case ZPCombatEvent.combatEvtID_Liftoff:
				if (side.isClient())
					targetPlayer.onGround = false;
				break;
		}
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
	
	
	public static ZPCombatEvent addCombatEventToList(EntityPlayer playerEntity, ZPCombatEvent newEvent, Side side)
	{
		Map<EntityPlayer, List<ZPCombatEvent>> eventsMap = side.isServer() ? ZPCombat.combatEventsServer : ZPCombat.combatEventsClient;
		
		synchronized(eventsMap)
		{
			List<ZPCombatEvent> eventList = eventsMap.get(playerEntity);
			
			if (eventList == null)
			{
				eventList = new ArrayList<ZPCombatEvent>();
				
				eventsMap.put(playerEntity, eventList);
			}
			
			if (newEvent != null)
				eventList.add(newEvent);
		}
		
		return newEvent;
	}
}
