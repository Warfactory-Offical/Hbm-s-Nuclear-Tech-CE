package com.hbm.render.amlfrom1710;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IModelCustom
{
	public String getType();
    public void renderAll();
    public void renderOnly(String... groupNames);
    public void renderPart(String partName);
    public void renderAllExcept(String... excludedGroupNames);
    public void tessellateAll(Tessellator tes);
    public void tessellatePart(Tessellator tes, String name);
    public void tessellateOnly(Tessellator tes, String... names);
    public void tessellateAllExcept(Tessellator tes, String... excluded);
}