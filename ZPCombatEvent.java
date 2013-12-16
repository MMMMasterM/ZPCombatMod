package ZPCombatMod;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseFilter;

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
	public static final byte combatEvtID_Consume = 14;
	public static final byte combatEvtID_UpdatePower = 15;
	
	
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
			case combatEvtID_Blink:
				this.direction = datainput.readByte();
				this.pitch = datainput.readByte();
				this.impactX = datainput.readFloat();
				this.impactY = datainput.readFloat();
				this.impactZ = datainput.readFloat();
				break;
			case combatEvtID_Consume:
				break;
			case combatEvtID_UpdatePower:
				this.impactX = datainput.readFloat();
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
			case combatEvtID_Blink:
				dataoutput.writeByte(this.direction);
				dataoutput.writeByte(this.pitch);
				dataoutput.writeFloat(this.impactX);
				dataoutput.writeFloat(this.impactY);
				dataoutput.writeFloat(this.impactZ);
				break;
			case combatEvtID_Consume:
				break;
			case combatEvtID_UpdatePower:
				dataoutput.writeFloat(this.impactX);
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
			case combatEvtID_Blink:
				return 14;
			case combatEvtID_Consume:
				return 0;
			case combatEvtID_UpdatePower:
				return 4;
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
				
				if (side.isServer())
				{
					float newAmount = targetPlayer.getAbsorptionAmount() - 3.5f;
					
					if (newAmount < 0)
						targetPlayer.removePotionEffect(Potion.field_76444_x.id);
					else
						targetPlayer.setAbsorptionAmount(newAmount);
				}
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
				
				if (side.isClient() && targetPlayer == Minecraft.getMinecraft().thePlayer)
				{
					Minecraft.getMinecraft().gameSettings.smoothCamera = true;
					
					EntityRenderer entityRenderer = Minecraft.getMinecraft().entityRenderer;
					
					//Remove momentum from smooth camera:
					try {
						MouseFilter filterX = (MouseFilter)ZPCombat.mouseFilterXAxisField.get(entityRenderer);
						MouseFilter filterY = (MouseFilter)ZPCombat.mouseFilterYAxisField.get(entityRenderer);
						
						if (entityState.isCruising == false)
						{
							ZPCombat.mouseFilterField1.setFloat(filterX, 0.0f);
							ZPCombat.mouseFilterField2.setFloat(filterX, 0.0f);
							ZPCombat.mouseFilterField3.setFloat(filterX, 0.0f);
							
							ZPCombat.mouseFilterField1.setFloat(filterY, 0.0f);
							ZPCombat.mouseFilterField2.setFloat(filterY, 0.0f);
							ZPCombat.mouseFilterField3.setFloat(filterY, 0.0f);
						}
						
						float newRoll = ZPCombat.mouseFilterField3.getFloat(filterX);
						
						ZPCombat.camRollField.setFloat(entityRenderer, newRoll);//10.1f);
						
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				
				if (side.isServer())
				{
					float newAmount = targetPlayer.getAbsorptionAmount() - 0.1f;
					
					if (!entityState.isCruising)
						newAmount -= 2.5f;
					
					if (newAmount < 0)
						targetPlayer.removePotionEffect(Potion.field_76444_x.id);
					else
						targetPlayer.setAbsorptionAmount(newAmount);
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
				
				if (side.isClient() && targetPlayer == Minecraft.getMinecraft().thePlayer)
				{
					Minecraft.getMinecraft().gameSettings.smoothCamera = false;
				}
				break;
			case combatEvtID_Blink:
				if (side.isClient())
				{
					targetPlayer.setPosition(this.impactX, this.impactY, this.impactZ);
					targetPlayer.rotationYaw = (float)getRotationFromDirection(this.direction);
					targetPlayer.rotationPitch = (float)getRotationFromPitch(this.pitch);
				}
				else
				{
					((EntityPlayerMP)targetPlayer).playerNetServerHandler.setPlayerLocation(this.impactX, this.impactY, this.impactZ, (float)getRotationFromDirection(this.direction), (float)getRotationFromPitch(this.pitch));
					
					float newAmount = targetPlayer.getAbsorptionAmount() - 5.0f;
					
					if (newAmount < 0)
						targetPlayer.removePotionEffect(Potion.field_76444_x.id);
					else
						targetPlayer.setAbsorptionAmount(newAmount);
				}
				break;
			case combatEvtID_Consume:
				if (side.isServer())
				{
					ItemStack currentItem = targetPlayer.getCurrentEquippedItem();
					
					if (currentItem != null && (/*currentItem.getItem() == Item.coal || */currentItem.getItem() == Item.diamond))
					{
						int newAmount = (int)Math.ceil(targetPlayer.getAbsorptionAmount() / 4.0d) - 1;
						
						//if (currentItem.getItem() == Item.coal)
						//	newAmount += 1;
						if (currentItem.getItem() == Item.diamond)
							newAmount += 15;
						
						newAmount = Math.min(19, newAmount);
						
						currentItem.stackSize--; //.splitStack(1);
						
						if (currentItem.stackSize <= 0)
		                {
							 targetPlayer.inventory.mainInventory[targetPlayer.inventory.currentItem] = null;
		                }
						
						targetPlayer.removePotionEffect(Potion.field_76444_x.id);
						targetPlayer.addPotionEffect(new PotionEffect(Potion.field_76444_x.id, 24000, newAmount));
					}
				}
				break;
			case combatEvtID_UpdatePower:
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
		double mag = 0.9d;
		double curVelInDir = targetPlayer.motionX * entityState.dirX + targetPlayer.motionY * entityState.dirY + targetPlayer.motionZ * entityState.dirZ;
		
		double dVel = mag - curVelInDir;
		if (dVel > 0)
		{
			targetPlayer.motionX = entityState.dirX * mag;
			targetPlayer.motionY = entityState.dirY * mag + 0.1d;
			targetPlayer.motionZ = entityState.dirZ * mag;
			//TODO: doCruising
			//Accelerate in direction
		}
	}
}
