package ZPCombatMod;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;

public class ZPCombatMoveAsyncPacketStoC extends Packet {
	
	public int playerId;
	public byte eventID;
	
	public ZPCombatEvent zpcEvent;
	
	public ZPCombatMoveAsyncPacketStoC() { }
	
	public ZPCombatMoveAsyncPacketStoC(int playerEntityId, ZPCombatEvent event)
	{
		this.playerId = playerEntityId;
		this.eventID = event.combatEventID;
		
		//Is there a reason to copy the contents instead?
		this.zpcEvent = event;
	}

	@Override
	public void readPacketData(DataInput datainput) throws IOException {
		this.playerId = datainput.readInt();
		this.eventID = datainput.readByte();
		
		this.zpcEvent = new ZPCombatEvent(this.eventID, datainput);
	}

	@Override
	public void writePacketData(DataOutput dataoutput) throws IOException {
		dataoutput.writeInt(this.playerId);
		dataoutput.writeByte(this.eventID);
		
		this.zpcEvent.writeDataToPacket(dataoutput);
	}

	@Override
	public void processPacket(NetHandler nethandler) {
		ZPCombatMoveAsyncPacketStoC.handleCombatMoveClientSide(this, nethandler);
	}

	@Override
	public int getPacketSize() {
		return 5 + ZPCombatEvent.getDataSize(this.eventID);
	}
	
	@Override
	public boolean canProcessAsync()
    {
        return true;
    }
	
	
	
	public static void handleCombatMoveClientSide(ZPCombatMoveAsyncPacketStoC packet, NetHandler netHandler)
	{
		//if (packet.playerId == netHandler.getPlayer().entityId)
		//	return;
		
		Entity targetEntity = Minecraft.getMinecraft().theWorld.getEntityByID(packet.playerId);
		if (targetEntity == null || !(targetEntity instanceof EntityPlayer))
			return;
		
		//EntityClientPlayerMP thisPlayer = Minecraft.getMinecraft().thePlayer;
		EntityPlayer targetPlayer = (EntityPlayer)targetEntity;
		
		ZPCombatEvent.addCombatEventToList(targetPlayer, packet.zpcEvent, Side.CLIENT);
	}

}
