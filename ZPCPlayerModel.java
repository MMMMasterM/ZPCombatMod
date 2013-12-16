package ZPCombatMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class ZPCPlayerModel extends ModelBiped {
	public ZPCPlayerModel(float par1)
	{
		super(par1);
	}
	
	@Override 
	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity)
	{
		boolean cruising = false;
		
		if (par7Entity instanceof EntityPlayer)
		{
			ZPCEntityState curEntityState = (ZPCEntityState)par7Entity.getExtendedProperties("zpcState");
			if (curEntityState != null && curEntityState.isCruising)
				cruising = true;
		}
		
		super.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity);
		
		if (cruising)
		{
			double animationVar = Minecraft.getSystemTime() / 500.0d * Math.PI;
			double cosAnim1 = Math.cos(animationVar * 2.0d);
			double cosAnim2 = Math.cos(animationVar * 2.0d + 1.2d);
			double cosAnim3 = Math.cos(animationVar / 1.5d);
			double cosAnim4 = Math.cos(animationVar / 1.5d + 1.2d);
			
			this.bipedBody.rotateAngleX = (float)(0.4d * Math.PI);
			this.bipedBody.rotateAngleY = 0;
			this.bipedBody.rotateAngleZ = 0;
			this.bipedLeftArm.rotateAngleX = (float)((0.35d + cosAnim1 * 0.03d) * Math.PI);
			this.bipedLeftArm.rotateAngleY = 0.1f;
			this.bipedRightArm.rotateAngleX = (float)((0.35d + cosAnim2 * 0.03d) * Math.PI);
			this.bipedRightArm.rotateAngleY = -0.1f;
			//this.bipedLeftLeg.setRotationPoint(this.bipedBody.rotationPointX, this.bipedBody.rotationPointY, this.bipedBody.rotationPointZ);
			this.bipedLeftLeg.setRotationPoint(this.bipedLeftLeg.rotationPointX, this.bipedBody.rotationPointY + 3.0f, this.bipedBody.rotationPointZ + 9.0f);
			this.bipedRightLeg.setRotationPoint(this.bipedRightLeg.rotationPointX, this.bipedBody.rotationPointY + 3.0f, this.bipedBody.rotationPointZ + 9.0f);
			//this.bipedLeftLeg.offsetY = -0.0f;
			this.bipedLeftLeg.rotateAngleX = (float)((0.47d + cosAnim3 * 0.03d) * Math.PI);
			this.bipedLeftLeg.rotateAngleY = 0.15f;
			this.bipedRightLeg.rotateAngleX = (float)((0.47d + cosAnim4 * 0.03d) * Math.PI);
			this.bipedRightLeg.rotateAngleY = -0.1f;//some asymmetry is nice
		}
	}
}
