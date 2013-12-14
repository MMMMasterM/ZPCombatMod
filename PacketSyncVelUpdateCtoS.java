package ZPCombatMod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet28EntityVelocity;
import net.minecraft.world.WorldServer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;

public class PacketSyncVelUpdateCtoS {
	
	//fuck vanilla int encoding... float isn't larger!
	public float packetMotionX;
	public float packetMotionY;
	public float packetMotionZ;
	
	public PacketSyncVelUpdateCtoS() { }
	
	public PacketSyncVelUpdateCtoS(double motionX, double motionY, double motionZ)
	{
		this.packetMotionX = (float)motionX;
		this.packetMotionY = (float)motionY;
		this.packetMotionZ = (float)motionZ;
	}
	
	public void read(ByteArrayDataInput par1DataInput)
	{
		this.packetMotionX = par1DataInput.readFloat();
		this.packetMotionY = par1DataInput.readFloat();
		this.packetMotionZ = par1DataInput.readFloat();
	}
	
	public void write(ByteArrayDataOutput par1DataOutput)
	{
		par1DataOutput.writeFloat(this.packetMotionX);
		par1DataOutput.writeFloat(this.packetMotionY);
		par1DataOutput.writeFloat(this.packetMotionZ);
	}
	
	public static final String CHANNEL = "zpcSync";

	public static final int getPacketId() 
	{
		return 180;
	}
	
	public static PacketSyncVelUpdateCtoS constructPacket()
	{
		return new PacketSyncVelUpdateCtoS();
	}
	
	public final Packet makePacket()
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeByte(getPacketId());
		write(out);
		return PacketDispatcher.getPacket(CHANNEL, out.toByteArray());
	}
	
	public void execute(EntityPlayer player, Side side)
	{
		if (!side.isServer())
			return;
		
		//Send velocity to other players
		WorldServer playerWorld = (WorldServer)player.worldObj;
		playerWorld.getEntityTracker().sendPacketToAllPlayersTrackingEntity(player, new Packet28EntityVelocity(player.entityId, this.packetMotionX, this.packetMotionY, this.packetMotionZ));
	}
}
