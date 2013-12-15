package ZPCombatMod;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

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
	public static final byte combatEvtID_LowJump = 10;//about normal jump height
	public static final byte combatEvtID_PunchUp = 11;//Punch entity up in the air (from ground)
	public static final byte combatEvtID_PunchDown = 12;//Punch entity down to the ground (from air)
	public static final byte combatEvtID_StopCruise = 13;
	
	
	public byte combatEventID;
	
	public byte direction;
	
	public byte pitch;
	
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
			case combatEvtID_LowJump:
				break;
			case combatEvtID_PunchUp:
				break;
			case combatEvtID_PunchDown:
				this.impactX = datainput.readFloat();
				this.impactY = datainput.readFloat();
				this.impactZ = datainput.readFloat();
				break;
			case combatEvtID_Cruise:
				this.direction = datainput.readByte();
				this.pitch = datainput.readByte();
				break;
			case combatEvtID_StopCruise:
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
			case combatEvtID_LowJump:
				break;
			case combatEvtID_PunchUp:
				break;
			case combatEvtID_PunchDown:
				dataoutput.writeFloat(this.impactX);
				dataoutput.writeFloat(this.impactY);
				dataoutput.writeFloat(this.impactZ);
				break;
			case combatEvtID_Cruise:
				dataoutput.writeByte(this.direction);
				dataoutput.writeByte(this.pitch);
				break;
			case combatEvtID_StopCruise:
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
			case combatEvtID_LowJump:
				return 0;
			case combatEvtID_PunchUp:
				return 0;
			case combatEvtID_PunchDown:
				return 12;
			case combatEvtID_Cruise:
				return 2;
			case combatEvtID_StopCruise:
				return 0;
		}
		
		return 0;
	}
	
	
	
	
	
	public void applyToPlayer(EntityPlayer targetPlayer, Side side)
	{
		switch (this.combatEventID)
		{
			case combatEvtID_JumpUp:
				targetPlayer.motionY += 1.2d;//0.27d;
				break;
			case combatEvtID_JumpFront:
				double rot = ZPCombatEvent.getRotationFromDirection(this.direction);
				
				double magX = -Math.sin(rot * Math.PI / 180.0d) * 1.9d;
				double magZ = Math.cos(rot * Math.PI / 180.0d) * 1.9d;
				targetPlayer.motionX += magX;
				targetPlayer.motionY += 0.8d;
				targetPlayer.motionZ += magZ;
				break;
			case combatEvtID_Impact:
				//Server already tracks this from vanilla code... kindof...
				if (true)//side.isClient())
				{
					targetPlayer.setPosition(this.impactX, this.impactY, this.impactZ);
					//targetPlayer.onGround = true;
					if (side.isServer())
					{
						if (targetPlayer.motionY < 0)
						{
							EventHookContainer.isZPCFall = true;
							float newAmount = (float) Math.max(0, targetPlayer.motionY * targetPlayer.motionY * 6.0f);
							
							
							PotionEffect potioneffect = targetPlayer.getActivePotionEffect(Potion.jump);
					        float f1 = potioneffect != null ? (float)(potioneffect.getAmplifier() + 1) : 0.0F;
					        int i = MathHelper.ceiling_float_int(targetPlayer.fallDistance - 3.0F - f1);
							if (i > 0)
								targetPlayer.attackEntityFrom(DamageSource.fall, newAmount);
							EventHookContainer.isZPCFall = false;
						}
					}
				}
				break;
			case combatEvtID_Liftoff:
				if (side.isClient())
					targetPlayer.onGround = false;
				break;
			case combatEvtID_LowJump:
				targetPlayer.motionY += 0.55d;
				break;
			case combatEvtID_PunchUp:
				targetPlayer.motionY += 1.2d;
				break;
			case combatEvtID_PunchDown:
				targetPlayer.motionY -= 1.2d;
				targetPlayer.setPosition(this.impactX, this.impactY, this.impactZ);
				break;
			case combatEvtID_Cruise:
				double rotYaw = getRotationFromDirection(this.direction);
				double rotPitch = getRotationFromPitch(this.pitch);
				
				ZPCEntityState entityState = (ZPCEntityState)targetPlayer.getExtendedProperties("zpcState");
				if (entityState == null)
				{
					entityState = new ZPCEntityState();
					targetPlayer.registerExtendedProperties("zpcState", entityState);
				}
				
				double cosPitch = Math.cos(rotPitch * Math.PI / 180.0d);
				entityState.isCruising = true;
				entityState.dirX = cosPitch * -Math.sin(rotYaw * Math.PI / 180.0d);
				entityState.dirY = -Math.sin(rotPitch * Math.PI / 180.0d);
				entityState.dirZ = cosPitch * Math.cos(rotYaw * Math.PI / 180.0d);
				break;
			case combatEvtID_StopCruise:
				ZPCEntityState curEntityState = (ZPCEntityState)targetPlayer.getExtendedProperties("zpcState");
				if (curEntityState != null)
					curEntityState.isCruising = false;
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
	
	public static byte getPitchFromRotation(double rotationPitch)
	{
		rotationPitch = rotationPitch % 180.0d;

		if (rotationPitch < 0)
			rotationPitch += 180.0d;

		return (byte)Math.floor(rotationPitch * 256.0d / 180.0d - 128.0d);
	}
	
	public static double getRotationFromPitch(byte pitchPar)
	{
		double result = (((double)pitchPar) + 128.0d) * 180.0d / 256.0d;
		
		if (result > 90.0d)
			result -= 180.0d;
		
		return result;
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
	
	public static void updateCruising(EntityPlayer targetPlayer, ZPCEntityState entityState)
	{
		double mag = 1.2d;
		double curVelInDir = targetPlayer.motionX * entityState.dirX + targetPlayer.motionY * entityState.dirY + targetPlayer.motionZ * entityState.dirZ;
		
		double dVel = mag - curVelInDir;
		if (dVel > 0)
		{
			targetPlayer.motionX = entityState.dirX * mag;
			targetPlayer.motionY = entityState.dirY * mag + 0.5d;
			targetPlayer.motionZ = entityState.dirZ * mag;
			//TODO: doCruising
			//Accelerate in direction
		}
	}
}
