package ZPCombatMod;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class ZPCSyncPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		
		EntityPlayer entityPlayer = (EntityPlayer)player;
		ByteArrayDataInput in = ByteStreams.newDataInput(packet.data);
		int packetId = in.readUnsignedByte();
		
		if (packetId == PacketSyncVelUpdateCtoS.getPacketId())
		{
			PacketSyncVelUpdateCtoS updatePacket = PacketSyncVelUpdateCtoS.constructPacket();
			updatePacket.read(in);
			updatePacket.execute(entityPlayer, entityPlayer.worldObj.isRemote ? Side.CLIENT : Side.SERVER);
		}
		
	}

}
