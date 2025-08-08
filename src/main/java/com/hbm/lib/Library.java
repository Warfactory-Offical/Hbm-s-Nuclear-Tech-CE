package com.hbm.lib;

import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.api.energymk2.IEnergyConnectorBlock;
import com.hbm.api.energymk2.IEnergyConnectorMK2;
import com.hbm.api.fluidmk2.IFluidConnectorMK2;
import com.hbm.api.fluidmk2.IFluidConnectorBlockMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.HbmLivingCapability.EntityHbmPropsProvider;
import com.hbm.capability.HbmLivingCapability.IEntityHbmProps;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.entity.mob.EntityHunterChopper;
import com.hbm.entity.projectile.EntityChopperMine;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.interfaces.IFluidDuct;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TileEntityProxyInventory;
import com.hbm.util.BobMathUtil;
import com.hbm.util.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

import static net.minecraft.nbt.CompressedStreamTools.writeCompressed;

@Spaghetti("this whole class")
public class Library {

	static Random rand = new Random();
	
	//this is a list of UUIDs used for various things, primarily for accessories.
	//for a comprehensive list, check RenderAccessoryUtility.java
	public static String HbMinecraft = "192af5d7-ed0f-48d8-bd89-9d41af8524f8";
	public static String TacoRedneck = "5aee1e3d-3767-4987-a222-e7ce1fbdf88e";
	// Earl0fPudding
	public static String LPkukin = "937c9804-e11f-4ad2-a5b1-42e62ac73077";
	public static String Dafnik = "3af1c262-61c0-4b12-a4cb-424cc3a9c8c0";
	// anna20
	public static String a20 = "4729b498-a81c-42fd-8acd-20d6d9f759e0";
	public static String rodolphito = "c3f5e449-6d8c-4fe3-acc9-47ef50e7e7ae";
	public static String LordVertice = "a41df45e-13d8-4677-9398-090d3882b74f";
	// twillycorn
	public static String CodeRed_ = "912ec334-e920-4dd7-8338-4d9b2d42e0a1";
	public static String dxmaster769 = "62c168b2-d11d-4dbf-9168-c6cea3dcb20e";
	public static String Dr_Nostalgia = "e82684a7-30f1-44d2-ab37-41b342be1bbd";
	public static String Samino2 = "87c3960a-4332-46a0-a929-ef2a488d1cda";
	public static String Hoboy03new = "d7f29d9c-5103-4f6f-88e1-2632ff95973f";
	public static String Dragon59MC = "dc23a304-0f84-4e2d-b47d-84c8d3bfbcdb";
	public static String SteelCourage = "ac49720b-4a9a-4459-a26f-bee92160287a";
	public static String Ducxkskiziko = "122fe98f-be19-49ca-a96b-d4dee4f0b22e";
	
	public static String SweatySwiggs = "5544aa30-b305-4362-b2c1-67349bb499d5";
	public static String Drillgon = "41ebd03f-7a12-42f3-b037-0caa4d6f235b";
	public static String Alcater = "0b399a4a-8545-45a1-be3d-ece70d7d48e9";
	public static String ege444 = "42ee978c-442a-4cd8-95b6-29e469b6df10";
	public static String Doctor17 = "e4ab1199-1c22-4f82-a516-c3238bc2d0d1";
	public static String Doctor17PH = "4d0477d7-58da-41a9-a945-e93df8601c5a";
	public static String ShimmeringBlaze = "061bc566-ec74-4307-9614-ac3a70d2ef38";
	public static String FifeMiner = "37e5eb63-b9a2-4735-9007-1c77d703daa3";
	public static String lag_add = "259785a0-20e9-4c63-9286-ac2f93ff528f";
	public static String Pu_238 = "c95fdfd3-bea7-4255-a44b-d21bc3df95e3";

	public static String Golem = "058b52a6-05b7-4d11-8cfa-2db665d9a521";
	public static Set<String> contributors = Sets.newHashSet(new String[] {
			"06ab7c03-55ce-43f8-9d3c-2850e3c652de", //mustang_rudolf
			"5bf069bc-5b46-4179-aafe-35c0a07dee8b", //JMF781
			});


	public static final ForgeDirection POS_X = ForgeDirection.EAST;
	public static final ForgeDirection NEG_X = ForgeDirection.WEST;
	public static final ForgeDirection POS_Y = ForgeDirection.UP;
	public static final ForgeDirection NEG_Y = ForgeDirection.DOWN;
	public static final ForgeDirection POS_Z = ForgeDirection.SOUTH;
	public static final ForgeDirection NEG_Z = ForgeDirection.NORTH;

	public static final int[] powersOfTen = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

	public static DecimalFormat numberformat = new DecimalFormat("0.00");
		
	//the old list that allowed superuser mode for the ZOMG
	//currently unused
	public static List<String> superuser = new ArrayList<String>();

	// Drillgon200: Not like super users are used for anything, but they could
	// in the future I guess.
	public static void initSuperusers() {
		superuser.add(HbMinecraft);
		superuser.add(TacoRedneck);
		superuser.add(LPkukin);
		superuser.add(Dafnik);
		superuser.add(a20);
		superuser.add(rodolphito);
		// Drillgon200: Pretty sure he did install NEI.
		superuser.add(Ducxkskiziko);
		superuser.add(Drillgon);
		superuser.add(Alcater);
	}

	public static String getColor(long a, long b){
		float fraction = 100F * a/b;
		if(fraction > 75)
			return "§a";
		if(fraction > 25)
			return "§e";
		return "§c";
	}

	public static String getColoredMbPercent(long a, long b){
		String color = getColor(a, b);
		return color+a+" §2/ "+b+" mB "+color+"("+getPercentage(a/(double)b)+"%)";
	}

	public static String getColoredDurabilityPercent(long a, long b){
		String color = getColor(a, b);
		return "Durability: "+color+a+" §2/ "+b+" "+color+"("+getPercentage(a/(double)b)+"%)";
	}

	public static boolean checkForHeld(EntityPlayer player, Item item) {
		return player.getHeldItemMainhand().getItem() == item || player.getHeldItemOffhand().getItem() == item;
	}

	public static boolean isObstructed(World world, double x, double y, double z, double a, double b, double c) {
		RayTraceResult pos = world.rayTraceBlocks(new Vec3d(x, y, z), new Vec3d(a, b, c), false, true, true);
		return pos != null && pos.typeOfHit != Type.MISS;
	}

	public static int getColorProgress(double fraction){
		int r = (int)(255*Math.min(1, fraction*-2+2));
		int g = (int)(255*Math.min(1, fraction*2));
		return 65536 * r + 256 * g;
	}

	public static String getPercentage(double fraction){
		return numberformat.format(roundFloat(fraction*100D, 2));
	}

	public static String getShortNumber(long l) {
		return getShortNumber(new BigDecimal(l));
	}

	public static Map<Integer, String> numbersMap = null;
	

	public static void initNumbers(){
		numbersMap = new TreeMap<>();
		numbersMap.put(3, "k");
		numbersMap.put(6, "M");
		numbersMap.put(9, "G");
		numbersMap.put(12, "T");
		numbersMap.put(15, "P");
		numbersMap.put(18, "E");
		numbersMap.put(21, "Z");
		numbersMap.put(24, "Y");
		numbersMap.put(27, "R");
		numbersMap.put(30, "Q");
	}
	
	public static String getShortNumber(BigDecimal l) {
		if(numbersMap == null) initNumbers();

		boolean negative = l.signum() < 0;
		if(negative){
			l = l.negate();
		}

		String result = l.toPlainString();
		BigDecimal c = null;
		for(Map.Entry<Integer, String> num : numbersMap.entrySet()){
			c = new BigDecimal("1E"+num.getKey());
			if(l.compareTo(c) >= 0){
				double res = l.divide(c).doubleValue();
				result = numberformat.format(roundFloat(res, 2)) + num.getValue();
			} else {
				break;
			}
		}

		if (negative){
			result = "-"+result;
		}

		return result;
	}

	public static float roundFloat(float number, int decimal){
		return (float) (Math.round(number * powersOfTen[decimal]) / (float)powersOfTen[decimal]);  
	}

	public static float roundFloat(double number, int decimal){
		return (float) (Math.round(number * powersOfTen[decimal]) / (float)powersOfTen[decimal]);  
	}

	public static int getColorFromItemStack(ItemStack stack){
		ResourceLocation path = null;
		ResourceLocation actualPath = null;
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem(), stack.getMetadata());
		if(sprite != null){
			path = new ResourceLocation(sprite.getIconName()+".png");
			actualPath = new ResourceLocation(path.getNamespace(), "textures/"+path.getPath());
		} else {
			path = new ResourceLocation(stack.getItem().getRegistryName()+".png");
			actualPath = new ResourceLocation(path.getNamespace(), "textures/items/"+path.getPath());
		}
		return getColorFromResourceLocation(actualPath);
	}

	public static int getColorFromResourceLocation(ResourceLocation r){
		if(r == null) {
			return 0;
		}
		try{
			BufferedImage image = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(r).getInputStream());
			return getRGBfromARGB(image.getRGB(image.getWidth()>>1, image.getHeight()>>1));
		} catch(Exception e) {
			MainRegistry.logger.log(Level.INFO, "[NTM] Fluid Texture not found for "+e.getMessage());
			return 0xFFFFFF;
		}
	}

	public static int getRGBfromARGB(int pixel){
		return pixel & 0x00ffffff;
	}

	// Drillgon200: Just realized I copied the wrong method. God dang it.
	// It works though. Not sure why, but it works.
	// mlbv: refactored with brand new NTMBatteryCapabilityHandler helpers
	public static long chargeTEFromItems(IItemHandlerModifiable inventory, int index, long power, long maxPower) {
		ItemStack stack = inventory.getStackInSlot(index);
		if (stack.getItem() == ModItems.battery_creative || stack.getItem() == ModItems.fusion_core_infinite) {
			return maxPower;
		}
		long powerNeeded = maxPower - power;
		if (powerNeeded <= 0) return power;
		long heExtracted = disChargeBatteryIfValid(stack, powerNeeded, false);
		return power + heExtracted;
	}

	//not great either but certainly better
	// mlbv: a lot better now
	public static long chargeItemsFromTE(IItemHandlerModifiable inventory, int index, long power, long maxPower) {
		ItemStack stackToCharge = inventory.getStackInSlot(index);
		if (stackToCharge.isEmpty() || power <= 0) {
			return power;
		}
		long heCharged = chargeBatteryIfValid(stackToCharge, power, false);
		return power - heCharged;
	}

	public static boolean isArrayEmpty(Object[] array) {
		if(array == null)
			return true;
		if(array.length == 0)
			return true;

		boolean flag = true;

		for(int i = 0; i < array.length; i++) {
			if(array[i] != null)
				flag = false;
		}

		return flag;
	}

	// Drillgon200: useless method but whatever
	public static ItemStack carefulCopy(ItemStack stack) {
		if(stack == null)
			return null;
		else
			return stack.copy();
	}
	
	public static EntityPlayer getClosestPlayerForSound(World world, double x, double y, double z, double radius) {
		double d4 = -1.0D;
		EntityPlayer entity = null;
		if(world == null) return null;
		for (int i = 0; i < world.loadedEntityList.size(); ++i) {
				Entity entityplayer1 = (Entity)world.loadedEntityList.get(i);

				if (entityplayer1.isEntityAlive() && entityplayer1 instanceof EntityPlayer) {
					double d5 = entityplayer1.getDistanceSq(x, y, z);

					if ((radius < 0.0D || d5 < radius * radius) && (d4 == -1.0D || d5 < d4)) {
						d4 = d5;
						entity = (EntityPlayer)entityplayer1;
					}
			}
		}

		return entity;
	}

	public static EntityHunterChopper getClosestChopperForSound(World world, double x, double y, double z, double radius) {
		double d4 = -1.0D;
		EntityHunterChopper entity = null;

		for (int i = 0; i < world.loadedEntityList.size(); ++i) {
				Entity entityplayer1 = (Entity)world.loadedEntityList.get(i);

				if (entityplayer1.isEntityAlive() && entityplayer1 instanceof EntityHunterChopper) {
					double d5 = entityplayer1.getDistanceSq(x, y, z);
					double d6 = radius;

					if ((radius < 0.0D || d5 < d6 * d6) && (d4 == -1.0D || d5 < d4)) {
						d4 = d5;
						entity = (EntityHunterChopper)entityplayer1;
					}
			}
		}

		return entity;
	}

	public static EntityChopperMine getClosestMineForSound(World world, double x, double y, double z, double radius) {
		double d4 = -1.0D;
		EntityChopperMine entity = null;

		for (int i = 0; i < world.loadedEntityList.size(); ++i) {
				Entity entityplayer1 = (Entity)world.loadedEntityList.get(i);

				if (entityplayer1.isEntityAlive() && entityplayer1 instanceof EntityChopperMine) {
					double d5 = entityplayer1.getDistanceSq(x, y, z);
					double d6 = radius;

					if ((radius < 0.0D || d5 < d6 * d6) && (d4 == -1.0D || d5 < d4)) {
						d4 = d5;
						entity = (EntityChopperMine)entityplayer1;
					}
			}
		}

		return entity;
	}

	public static boolean checkFluidConnectables(World world, BlockPos pos, FluidType type)
	{
		TileEntity tileentity = world.getTileEntity(pos);
		if(tileentity instanceof IFluidDuct && ((IFluidDuct) tileentity).getType() == type)
			return true;

		if(world.getBlockState(pos).getBlock() == ModBlocks.machine_mining_laser && tileentity instanceof TileEntityProxyInventory)
			return true;

		return false;
	}

	public static RayTraceResult rayTrace(EntityPlayer player, double length, float interpolation) {
		Vec3d vec3 = getPosition(interpolation, player);
		vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
		Vec3d vec31 = player.getLook(interpolation);
		Vec3d vec32 = vec3.add(vec31.x * length, vec31.y * length, vec31.z * length);
		return player.world.rayTraceBlocks(vec3, vec32, false, false, true);
	}
	
	public static RayTraceResult rayTrace(EntityPlayer player, double length, float interpolation, boolean b1, boolean b2, boolean b3) {
		Vec3d vec3 = getPosition(interpolation, player);
		vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
		Vec3d vec31 = player.getLook(interpolation);
		Vec3d vec32 = vec3.add(vec31.x * length, vec31.y * length, vec31.z * length);
		return player.world.rayTraceBlocks(vec3, vec32, b1, b2, b3);
	}
	
	public static AxisAlignedBB rotateAABB(AxisAlignedBB box, EnumFacing facing){
		switch(facing){
		case NORTH:
			return new AxisAlignedBB(box.minX, box.minY, 1-box.minZ, box.maxX, box.maxY, 1-box.maxZ);
		case SOUTH:
			return box;
		case EAST:
			return new AxisAlignedBB(box.minZ, box.minY, box.minX, box.maxZ, box.maxY, box.maxX);
		case WEST:
			return new AxisAlignedBB(1-box.minZ, box.minY, box.minX, 1-box.maxZ, box.maxY, box.maxX);
		default:
			return box;
		}
	}
	
	public static RayTraceResult rayTraceIncludeEntities(EntityPlayer player, double d, float f) {
		Vec3d vec3 = getPosition(f, player);
		vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
		Vec3d vec31 = player.getLook(f);
		Vec3d vec32 = vec3.add(vec31.x * d, vec31.y * d, vec31.z * d);
		return rayTraceIncludeEntities(player.world, vec3, vec32, player);
	}
	
	public static RayTraceResult rayTraceIncludeEntitiesCustomDirection(EntityPlayer player, Vec3d look, double d, float f) {
		Vec3d vec3 = getPosition(f, player);
		vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
		Vec3d vec32 = vec3.add(look.x * d, look.y * d, look.z * d);
		return rayTraceIncludeEntities(player.world, vec3, vec32, player);
	}
	
	public static Vec3d changeByAngle(Vec3d oldDir, float yaw, float pitch){
		Vec3d dir = new Vec3d(0, 0, 1);
		dir = dir.rotatePitch((float) Math.toRadians(pitch)).rotateYaw((float) Math.toRadians(yaw));
		Vec3d angles = BobMathUtil.getEulerAngles(oldDir);
		return dir.rotatePitch((float) Math.toRadians(angles.y+90)).rotateYaw((float)Math.toRadians(angles.x));
	}
	
	public static RayTraceResult rayTraceIncludeEntities(World w, Vec3d vec3, Vec3d vec32, @Nullable Entity excluded) {
		RayTraceResult result = w.rayTraceBlocks(vec3, vec32, false, true, true);
		if(result != null)
			vec32 = result.hitVec;
		
		AxisAlignedBB box = new AxisAlignedBB(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z).grow(1D);
		List<Entity> ents = w.getEntitiesInAABBexcluding(excluded, box, Predicates.and(EntitySelectors.IS_ALIVE, entity -> entity instanceof EntityLivingBase));
		for(Entity ent : ents){
			RayTraceResult test = ent.getEntityBoundingBox().grow(0.3D).calculateIntercept(vec3, vec32);
			if(test != null){
				if(result == null || vec3.squareDistanceTo(result.hitVec) > vec3.squareDistanceTo(test.hitVec)){
					test.typeOfHit = RayTraceResult.Type.ENTITY;
					test.entityHit = ent;
					result = test;
				}
			}
		}
		
		return result;
	}
	
	public static Pair<RayTraceResult, List<Entity>> rayTraceEntitiesOnLine(EntityPlayer player, double d, float f){
		Vec3d vec3 = getPosition(f, player);
		vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
		Vec3d vec31 = player.getLook(f);
		Vec3d vec32 = vec3.add(vec31.x * d, vec31.y * d, vec31.z * d);
		RayTraceResult result = player.world.rayTraceBlocks(vec3, vec32, false, true, true);
		if(result != null)
			vec32 = result.hitVec;
		AxisAlignedBB box = new AxisAlignedBB(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z).grow(1D);
		List<Entity> ents = player.world.getEntitiesInAABBexcluding(player, box, Predicates.and(EntitySelectors.IS_ALIVE, entity -> entity instanceof EntityLiving));
		Iterator<Entity> itr = ents.iterator();
		while(itr.hasNext()){
			Entity ent = itr.next();
			AxisAlignedBB entityBox = ent.getEntityBoundingBox().grow(0.1);
			RayTraceResult entTrace = entityBox.calculateIntercept(vec3, vec32);
			if(entTrace == null || entTrace.typeOfHit == Type.MISS){
				itr.remove();
			}
		}
		return Pair.of(rayTraceIncludeEntities(player, d, f), ents);
	}
	
	public static RayTraceResult rayTraceEntitiesInCone(EntityPlayer player, double d, float f, float degrees) {
		double cosDegrees = Math.cos(Math.toRadians(degrees));
		Vec3d vec3 = getPosition(f, player);
		vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
		Vec3d vec31 = player.getLook(f);
		Vec3d vec32 = vec3.add(vec31.x * d, vec31.y * d, vec31.z * d);
		
		RayTraceResult result = player.world.rayTraceBlocks(vec3, vec32, false, true, true);
		double runningDot = Double.MIN_VALUE;
		
		AxisAlignedBB box = new AxisAlignedBB(vec3.x, vec3.y, vec3.z, vec3.x, vec3.y, vec3.z).grow(1D+d);
		List<Entity> ents = player.world.getEntitiesInAABBexcluding(player, box, Predicates.and(EntitySelectors.IS_ALIVE, entity -> entity instanceof EntityLiving));
		for(Entity ent : ents){
			Vec3d entPos = closestPointOnBB(ent.getEntityBoundingBox(), vec3, vec32);
			Vec3d relativeEntPos = entPos.subtract(vec3).normalize();
			double dot = relativeEntPos.dotProduct(vec31);
			
			if(dot > cosDegrees && dot > runningDot && !isObstructed(player.world, vec3.x, vec3.y, vec3.z, ent.posX, ent.posY + ent.getEyeHeight()*0.75, ent.posZ)){
				runningDot = dot;
				result = new RayTraceResult(ent);
				result.hitVec = new Vec3d(ent.posX, ent.posY + ent.getEyeHeight()/2, ent.posZ);
			}
			
		}
		
		return result;
	}
	
	//Drillgon200: Turns out the closest point on a bounding box to a line is a pretty good method for determine if a cone and an AABB intersect.
	//Actually that was a pretty garbage method. Changing it out for a slightly less efficient sphere culling algorithm that only gives false positives.
	//https://bartwronski.com/2017/04/13/cull-that-cone/
	//Idea is that we find the closest point on the cone to the center of the sphere and check if it's inside the sphere.
	public static boolean isBoxCollidingCone(AxisAlignedBB box, Vec3d coneStart, Vec3d coneEnd, float degrees){
		Vec3d center = box.getCenter();
		double radius = center.distanceTo(new Vec3d(box.maxX, box.maxY, box.maxZ));
		Vec3d V = center.subtract(coneStart);
		double VlenSq = V.lengthSquared();
		Vec3d direction = coneEnd.subtract(coneStart);
		double size = direction.length();
		double V1len  = V.dotProduct(direction.normalize());
		double angRad = Math.toRadians(degrees);
		double distanceClosestPoint = Math.cos(angRad) * Math.sqrt(VlenSq - V1len*V1len) - V1len * Math.sin(angRad);
		 
		boolean angleCull = distanceClosestPoint > radius;
		boolean frontCull = V1len >  radius + size;
		boolean backCull  = V1len < -radius;
		return !(angleCull || frontCull || backCull);
	}
	
	//Drillgon200: Basically the AxisAlignedBB calculateIntercept method except it clamps to edge instead of returning null
	public static Vec3d closestPointOnBB(AxisAlignedBB box, Vec3d vecA, Vec3d vecB){
		
		Vec3d vec3d = collideWithXPlane(box, box.minX, vecA, vecB);
        Vec3d vec3d1 = collideWithXPlane(box, box.maxX, vecA, vecB);

        if (vec3d1 != null && isClosest(vecA, vecB, vec3d, vec3d1))
        {
            vec3d = vec3d1;
        }

        vec3d1 = collideWithYPlane(box, box.minY, vecA, vecB);

        if (vec3d1 != null && isClosest(vecA, vecB, vec3d, vec3d1))
        {
            vec3d = vec3d1;
        }

        vec3d1 = collideWithYPlane(box, box.maxY, vecA, vecB);

        if (vec3d1 != null && isClosest(vecA, vecB, vec3d, vec3d1))
        {
            vec3d = vec3d1;
        }

        vec3d1 = collideWithZPlane(box, box.minZ, vecA, vecB);

        if (vec3d1 != null && isClosest(vecA, vecB, vec3d, vec3d1))
        {
            vec3d = vec3d1;
        }

        vec3d1 = collideWithZPlane(box, box.maxZ, vecA, vecB);

        if (vec3d1 != null && isClosest(vecA, vecB, vec3d, vec3d1))
        {
            vec3d = vec3d1;
        }
		
		return vec3d;
	}
	
	protected static Vec3d collideWithXPlane(AxisAlignedBB box, double p_186671_1_, Vec3d p_186671_3_, Vec3d p_186671_4_)
    {
        Vec3d vec3d = getIntermediateWithXValue(p_186671_3_, p_186671_4_, p_186671_1_);
        return clampToBox(box, vec3d);
        //return vec3d != null && box.intersectsWithYZ(vec3d) ? vec3d : null;
    }

	protected static Vec3d collideWithYPlane(AxisAlignedBB box, double p_186663_1_, Vec3d p_186663_3_, Vec3d p_186663_4_)
    {
        Vec3d vec3d = getIntermediateWithYValue(p_186663_3_, p_186663_4_, p_186663_1_);
        return clampToBox(box, vec3d);
        //return vec3d != null && box.intersectsWithXZ(vec3d) ? vec3d : null;
    }

	protected static Vec3d collideWithZPlane(AxisAlignedBB box, double p_186665_1_, Vec3d p_186665_3_, Vec3d p_186665_4_)
    {
        Vec3d vec3d = getIntermediateWithZValue(p_186665_3_, p_186665_4_, p_186665_1_);
        return clampToBox(box, vec3d);
        //return vec3d != null && box.intersectsWithXY(vec3d) ? vec3d : null;
    }
	
	protected static Vec3d clampToBox(AxisAlignedBB box, Vec3d vec)
    {
		return new Vec3d(MathHelper.clamp(vec.x, box.minX, box.maxX), MathHelper.clamp(vec.y, box.minY, box.maxY), MathHelper.clamp(vec.z, box.minZ, box.maxZ));
    }
	
	protected static boolean isClosest(Vec3d line1, Vec3d line2, @Nullable Vec3d p_186661_2_, Vec3d p_186661_3_)
    {
		if(p_186661_2_ == null)
			return true;
		double d1 = dist_to_segment_squared(p_186661_3_, line1, line2);
		double d2 = dist_to_segment_squared(p_186661_2_, line1, line2);
		if(Math.abs(d1-d2) < 0.01)
			return line1.squareDistanceTo(p_186661_3_) < line1.squareDistanceTo(p_186661_2_);
        return d1 < d2;
    }
	
	//Drillgon200: https://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
	//Drillgon200: I'm not figuring this out myself.
	protected static double dist_to_segment_squared(Vec3d point, Vec3d linePoint1, Vec3d linePoint2) {
		  double line_dist = linePoint1.squareDistanceTo(linePoint2);
		  if (line_dist == 0) return point.squareDistanceTo(linePoint1);
		  double t = ((point.x - linePoint1.x) * (linePoint2.x - linePoint1.x) + (point.y - linePoint1.y) * (linePoint2.y - linePoint1.y) + (point.z - linePoint1.z) * (linePoint2.z - linePoint1.z)) / line_dist;
		  t = MathHelper.clamp(t, 0, 1);
		  Vec3d pointOnLine = new Vec3d(linePoint1.x + t * (linePoint2.x - linePoint1.x), linePoint1.y + t * (linePoint2.y - linePoint1.y), linePoint1.z + t * (linePoint2.z - linePoint1.z));
		  return point.squareDistanceTo(pointOnLine);
	}
	
	/**
     * Returns a new vector with x value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    @Nullable
    public static Vec3d getIntermediateWithXValue(Vec3d vec1, Vec3d vec, double x)
    {
        double d0 = vec.x - vec1.x;
        double d1 = vec.y - vec1.y;
        double d2 = vec.z - vec1.z;

        if (d0 * d0 < 1.0000000116860974E-7D)
        {
            return vec;
        }
        else
        {
            double d3 = (x - vec1.x) / d0;
            if(d3 < 0){
            	return new Vec3d(x, vec.y, vec.z);
            } else if(d3 > 1){
            	return new Vec3d(x, vec1.y, vec1.z);
            } else {
            	return new Vec3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3);
            }
            //return d3 >= 0.0D && d3 <= 1.0D ? new Vec3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3) : null;
        }
    }

    /**
     * Returns a new vector with y value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    @Nullable
    public static Vec3d getIntermediateWithYValue(Vec3d vec1, Vec3d vec, double y)
    {
        double d0 = vec.x - vec1.x;
        double d1 = vec.y - vec1.y;
        double d2 = vec.z - vec1.z;

        if (d1 * d1 < 1.0000000116860974E-7D)
        {
            return vec;
        }
        else
        {
            double d3 = (y - vec1.y) / d1;
            if(d3 < 0){
            	return new Vec3d(vec.x, y, vec.z);
            } else if(d3 > 1){
            	return new Vec3d(vec1.x, y, vec1.z);
            } else {
            	return new Vec3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3);
            }
            //return d3 >= 0.0D && d3 <= 1.0D ? new Vec3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3) : null;
        }
    }

    /**
     * Returns a new vector with z value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    @Nullable
    public static Vec3d getIntermediateWithZValue(Vec3d vec1, Vec3d vec, double z)
    {
        double d0 = vec.x - vec1.x;
        double d1 = vec.y - vec1.y;
        double d2 = vec.z - vec1.z;

        if (d2 * d2 < 1.0000000116860974E-7D)
        {
            return vec;
        }
        else
        {
            double d3 = (z - vec1.z) / d2;
            if(d3 < 0){
            	return new Vec3d(vec.x, vec.y, z);
            } else if(d3 > 1){
            	return new Vec3d(vec1.x, vec1.y, z);
            } else {
            	return new Vec3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3);
            }
            //return d3 >= 0.0D && d3 <= 1.0D ? new Vec3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3) : null;
        }
    }
    
    public static Vec3d getEuler(Vec3d vec){
    	double yaw = Math.toDegrees(Math.atan2(vec.x, vec.z));
		double sqrt = MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z);
		double pitch = Math.toDegrees(Math.atan2(vec.y, sqrt));
		return new Vec3d(yaw, pitch, 0);
    }
    
    //Drillgon200: https://thebookofshaders.com/glossary/?search=smoothstep
    public static double smoothstep(double t, double edge0, double edge1){
    	t = MathHelper.clamp((t - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);	
    }
    public static float smoothstep(float t, float edge0, float edge1){
    	t = MathHelper.clamp((t - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);	
    }
	
	public static Vec3d getPosition(float interpolation, EntityPlayer player) {
		if(interpolation == 1.0F) {
			return new Vec3d(player.posX, player.posY + (player.getEyeHeight() - player.getDefaultEyeHeight()), player.posZ);
		} else {
			double d0 = player.prevPosX + (player.posX - player.prevPosX) * interpolation;
			double d1 = player.prevPosY + (player.posY - player.prevPosY) * interpolation + (player.getEyeHeight() - player.getDefaultEyeHeight());
			double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * interpolation;
			return new Vec3d(d0, d1, d2);
		}
	}

public static boolean canConnect(IBlockAccess world, BlockPos pos, ForgeDirection dir /* cable's connecting side */) {
		
		if(world instanceof World){
			if(((World)world).isOutsideBuildHeight(pos))
				return false;
		} else {
			if(pos.getY() < 0 || pos.getY() > 255)
				return false;
		}
		
		Block b = world.getBlockState(pos).getBlock();
		
		if(b instanceof IEnergyConnectorBlock) {
			IEnergyConnectorBlock con = (IEnergyConnectorBlock) b;
			
			if(con.canConnect(world, pos, dir.getOpposite() /* machine's connecting side */))
				return true;
		}
		
		TileEntity te = world.getTileEntity(pos);
		
		if(te instanceof IEnergyConnectorMK2) {
			IEnergyConnectorMK2 con = (IEnergyConnectorMK2) te;
			
			if(con.canConnect(dir.getOpposite() /* machine's connecting side */))
				return true;
		}
		
		return false;
	}

	//Alcater: Finally this shit is no more

	//TODO: jesus christ
	// Flut-Füll gesteuerter Energieübertragungsalgorithmus
	// Flood fill controlled energy transmission algorithm
	// public static void ffgeua(MutableBlockPos pos, boolean newTact, ISource that, World worldObj) {
		
	// 	/*
	// 	 * This here smoldering crater is all that remains from the old energy system.
	// 	 * In loving memory, 2019-2023.
	// 	 * You won't be missed.
	// 	 */
	// }

	//Th3_Sl1ze: Sincerely I hate deprecated interfaces but couldn't figure out how to make mechs work without them. Will let them live for now

	/** dir is the direction along the fluid duct entering the block */
	public static boolean canConnectFluid(IBlockAccess world, BlockPos pos, ForgeDirection dir /* duct's connecting side */, FluidType type) {
		return canConnectFluid(world, pos.getX(),pos.getY(),pos.getZ(),dir,type);
	}
	public static boolean canConnectFluid(IBlockAccess world, int x, int y, int z, ForgeDirection dir /* duct's connecting side */, FluidType type) {

		if(y > 255 || y < 0)
			return false;

		Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();

		if(b instanceof IFluidConnectorBlockMK2) {
			IFluidConnectorBlockMK2 con = (IFluidConnectorBlockMK2) b;

			if(con.canConnect(type, world, x, y, z, dir.getOpposite() /* machine's connecting side */))
				return true;
		}

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if(te instanceof IFluidConnectorMK2) {
			IFluidConnectorMK2 con = (IFluidConnectorMK2) te;

			if(con.canConnect(type, dir.getOpposite() /* machine's connecting side */))
				return true;
		}

		return false;
	}

	/**
	 * Itemstack equality method except it accounts for possible null stacks and
	 * doesn't check if empty
	 */
	public static boolean areItemsEqual(ItemStack stackA, ItemStack stackB) {
		if(stackA == null & stackB == null)
			return true;
		else if((stackA == null && stackB != null) || (stackA != null && stackB == null))
			return false;
		else
			return stackA.getMetadata() == stackB.getMetadata() && stackA.getItem() == stackB.getItem();
	}

	public static boolean hasInventoryItem(InventoryPlayer inventory, Item ammo) {
		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack.getItem() == ammo) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasInventoryOreDict(InventoryPlayer inventory, String name) {
		int oreId = OreDictionary.getOreID(name);
		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack.isEmpty())
				continue;
			int[] ids = OreDictionary.getOreIDs(stack);
			for(int id : ids){
				if(id == oreId)
					return true;
			}
		}
		return false;
	}
	
	public static int countInventoryItem(InventoryPlayer inventory, Item ammo) {
		int count = 0;
		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack.getItem() == ammo) {
				count += stack.getCount();
			}
		}
		return count;
	}

	public static void consumeInventoryItem(InventoryPlayer inventory, Item ammo) {
		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack.getItem() == ammo && !stack.isEmpty()) {
				stack.shrink(1);
				inventory.setInventorySlotContents(i, stack.copy());
				return;
			}
		}
	}

	//////  //////  //////  //////  //////  ////        //////  //////  //////
	//      //  //  //        //    //      //  //      //      //      //    
	////    //////  /////     //    ////    ////        ////    //  //  //  //
	//      //  //     //     //    //      //  //      //      //  //  //  //
	//////  //  //  /////     //    //////  //  //      //////  //////  //////
	//Alcater: Huh thats interesing... You can hide from the chopper as long as you are outside 80% of its radius??
	public static EntityLivingBase getClosestEntityForChopper(World world, double x, double y, double z, double radius) {
		double d4 = -1.0D;
		EntityLivingBase entityplayer = null;

		for (int i = 0; i < world.loadedEntityList.size(); ++i) {
			if (world.loadedEntityList.get(i) instanceof EntityLivingBase && !(world.loadedEntityList.get(i) instanceof EntityHunterChopper)) {
				EntityLivingBase entityplayer1 = (EntityLivingBase) world.loadedEntityList.get(i);

				if (entityplayer1.isEntityAlive() && !(entityplayer1 instanceof EntityPlayer && ((EntityPlayer)entityplayer1).capabilities.disableDamage)) {
					double d5 = entityplayer1.getDistanceSq(x, y, z);
					double d6 = radius;

					if (entityplayer1.isSneaking()) {
						d6 = radius * 0.800000011920929D;
					}

					if ((radius < 0.0D || d5 < d6 * d6) && (d4 == -1.0D || d4 > d5)) {
						d4 = d5;
						entityplayer = entityplayer1;
					}
				}
			}
		}

		return entityplayer;
	}
	
	//Drillgon200: Loot tables? I don't have time for that!
	public static void generateChestContents(Random p_76293_0_, WeightedRandomChestContentFrom1710[] p_76293_1_, ICapabilityProvider p_76293_2_, int p_76293_3_)
    {
		if(p_76293_2_.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)){
			IItemHandler test = p_76293_2_.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if(test instanceof IItemHandlerModifiable){
				IItemHandlerModifiable inventory = (IItemHandlerModifiable)test;
				
				for (int j = 0; j < p_76293_3_; ++j)
		        {
					WeightedRandomChestContentFrom1710 weightedrandomchestcontent = (WeightedRandomChestContentFrom1710)WeightedRandom.getRandomItem(p_76293_0_, Arrays.asList(p_76293_1_));
		            ItemStack[] stacks = weightedrandomchestcontent.generateChestContent(p_76293_0_, inventory);

		            for (ItemStack item : stacks)
		            {
		            	inventory.setStackInSlot(p_76293_0_.nextInt(inventory.getSlots()), item);
		            }
		        }
			}
		}
        
    }
	
	public static Block getRandomConcrete() {
		int i = rand.nextInt(100);

		if(i < 5)
			return ModBlocks.brick_concrete_broken;
		if(i < 20)
			return ModBlocks.brick_concrete_cracked;
		if(i < 50)
			return ModBlocks.brick_concrete_mossy;
		
		return ModBlocks.brick_concrete;
	}
	
	public static void placeDoorWithoutCheck(World worldIn, BlockPos pos, EnumFacing facing, Block door, boolean isRightHinge)
    {
        BlockPos blockpos2 = pos.up();
        boolean flag2 = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos2);
        IBlockState iblockstate = door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HINGE, isRightHinge ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT).withProperty(BlockDoor.POWERED, Boolean.valueOf(flag2)).withProperty(BlockDoor.OPEN, Boolean.valueOf(flag2));
        worldIn.setBlockState(pos, iblockstate.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER), 2);
        worldIn.setBlockState(blockpos2, iblockstate.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), 2);
        worldIn.notifyNeighborsOfStateChange(pos, door, false);
        worldIn.notifyNeighborsOfStateChange(blockpos2, door, false);
    }
	
	public static boolean areItemStacksEqualIgnoreCount(ItemStack a, ItemStack b){
		if (a.isEmpty() && b.isEmpty())
        {
            return true;
        }
        else
        {
            if(!a.isEmpty() && !b.isEmpty()){

                if (a.getItem() != b.getItem())
                {
                    return false;
                }
                else if (a.getMetadata() != b.getMetadata())
                {
                    return false;
                }
                else
                {
                    return (a.getTagCompound() == null || a.getTagCompound().equals(b.getTagCompound())) && a.areCapsCompatible(b);
                }
            }
        }
		return false;
	}
	
	/**
	 * Same as ItemStack.areItemStacksEqual, except the second one's tag only has to contain all the first one's tag, rather than being exactly equal.
	 */
	public static boolean areItemStacksCompatible(ItemStack base, ItemStack toTest, boolean shouldCompareSize){
		if (base.isEmpty() && toTest.isEmpty())
        {
            return true;
        }
        else
        {
            if(!base.isEmpty() && !toTest.isEmpty()){

            	if(shouldCompareSize && base.getCount() != toTest.getCount()){
            		return false;
            	} 
            	else if (base.getItem() != toTest.getItem())
                {
                    return false;
                }
                else if (base.getMetadata() != toTest.getMetadata() && !(base.getMetadata() == OreDictionary.WILDCARD_VALUE))
                {
                    return false;
                }
                else if (base.getTagCompound() == null && toTest.getTagCompound() != null)
                {
                    return false;
                }
                else
                {
                    return (base.getTagCompound() == null || tagContainsOther(base.getTagCompound(), toTest.getTagCompound())) && base.areCapsCompatible(toTest);
                }
            }
        }
		return false;
	}
	
	public static boolean areItemStacksCompatible(ItemStack base, ItemStack toTest){
		return areItemStacksCompatible(base, toTest, true);
	}
	
	/**
	 * Returns true if the second compound contains all the tags and values of the first one, but it can have more. This helps with intermod compatibility
	 */
	public static boolean tagContainsOther(NBTTagCompound tester, NBTTagCompound container){
		if(tester == null && container == null){
			return true;
		} else if (tester == null && container != null) {
			return true;
		} else if (tester != null && container == null) {
		} else {
			for(String s : tester.getKeySet()){
				if(!container.hasKey(s)){
					return false;
				} else {
					NBTBase nbt1 = tester.getTag(s);
					NBTBase nbt2 = container.getTag(s);
					if(nbt1 instanceof NBTTagCompound && nbt2 instanceof NBTTagCompound){
						if(!tagContainsOther((NBTTagCompound)nbt1, (NBTTagCompound) nbt2))
							return false;
					} else {
						if(!nbt1.equals(nbt2))
							return false;
					}
				}
			}
		}
		return true;
	}
	
	public static List<int[]> getBlockPosInPath(BlockPos pos, int length, Vec3d vec0) {
		List<int[]> list = new ArrayList<int[]>();
		
		for(int i = 0; i <= length; i++) {
			list.add(new int[] { (int)(pos.getX() + (vec0.x * i)), pos.getY(), (int)(pos.getZ() + (vec0.z * i)), i });
		}
		
		return list;
	}

	public static List<ItemStack> copyItemStackList(List<ItemStack> inputs) {
		List<ItemStack> list = new ArrayList<ItemStack>();
		inputs.forEach(stack -> {list.add(stack.copy());});
		return list;
	}
	
	public static List<List<ItemStack>> copyItemStackListList(List<List<ItemStack>> inputs) {
		List<List<ItemStack>> list = new ArrayList<List<ItemStack>>(inputs.size());
		inputs.forEach(list2 -> {
			List<ItemStack> newList = new ArrayList<>(list2.size());
			list2.forEach(stack -> {newList.add(stack.copy());});
			list.add(newList);
			});
		return list;
	}
	
	public static IEntityHbmProps getEntRadCap(Entity e){
		if(e.hasCapability(EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null))
			return e.getCapability(EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null);
		return EntityHbmPropsProvider.DUMMY;
	}

	public static void addToInventoryOrDrop(EntityPlayer player, ItemStack stack) {
		if(!player.inventory.addItemStackToInventory(stack)){
			player.dropItem(stack, false);
		}
	}

	public static Vec3d normalFromRayTrace(RayTraceResult r) {
		Vec3i n = r.sideHit.getDirectionVec();
		return new Vec3d(n.getX(), n.getY(), n.getZ());
	}
	
	
	public static Explosion explosionDummy(World w, double x, double y, double z){
		return new Explosion(w, null, x, y, z, 1000, false, false);
	}

	/** @return true if is instance of IBatteryItem or has FE capability */
	public static boolean isItemBattery(@NotNull ItemStack stack){
		if(stack.isEmpty()) return false;
		return stack.getItem() instanceof IBatteryItem || stack.hasCapability(CapabilityEnergy.ENERGY, null);
	}

	public static boolean isItemDischargeableBattery(@NotNull ItemStack stack){
		if(stack.isEmpty()) return false;
		if (stack.getItem() instanceof IBatteryItem battery) {
			return battery.getCharge(stack) > 0 && battery.getDischargeRate() > 0;
		} else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
			IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
			if (cap != null) return cap.getEnergyStored() > 0 && cap.canExtract();
		}
		return false;
	}

	public static boolean isItemChargeableBattery(@NotNull ItemStack stack) {
		if (stack.isEmpty()) return false;
		if (stack.getItem() instanceof IBatteryItem battery) {
			return battery.getMaxCharge(stack) > battery.getCharge(stack) && battery.getChargeRate() > 0;
		} else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
			IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
			if (cap != null) return cap.getMaxEnergyStored() > cap.getEnergyStored() && cap.canReceive();
		}
		return false;
	}

	public static boolean isItemEmptyBattery(@NotNull ItemStack stack){
		if(stack.isEmpty()) return true;
		if (stack.getItem() instanceof IBatteryItem battery) {
			return battery.getCharge(stack) == 0;
		} else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
			IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
			if (cap!= null) return cap.getEnergyStored() == 0;
		}
		return false;
	}

	public static boolean isItemFullBattery(@NotNull ItemStack stack){
		if(stack.isEmpty()) return false;
		if (stack.getItem() instanceof IBatteryItem battery) {
			return battery.getCharge(stack) == battery.getMaxCharge(stack);
		} else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
			IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
			if (cap!= null) return cap.getEnergyStored() == cap.getMaxEnergyStored();
		}
		return false;
	}

	/**
	 * @return amount of energy charged in HE
	 */
	public static long chargeBatteryIfValid(@NotNull ItemStack stack, long heCharge, boolean instant) {
		if (stack.isEmpty()) return 0;
		long added = 0;
		if (stack.getItem() instanceof IBatteryItem battery) {
			long maxcharge = battery.getMaxCharge(stack);
			long charge = battery.getCharge(stack);
			added = instant ? heCharge : Math.min(heCharge, battery.getChargeRate());
			added = charge + added > maxcharge ? maxcharge - charge : added;
			battery.setCharge(stack, charge + added);
		} else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
			if (GeneralConfig.conversionRateHeToRF <= 0) return 0;
			IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
			if (cap != null) {
				int feReceived = cap.receiveEnergy((int) (heCharge * GeneralConfig.conversionRateHeToRF), false);
				added = (long) (feReceived / GeneralConfig.conversionRateHeToRF);
			}
		}
		return added;
	}

	/**
	 * @return The amount of energy that was actually extracted, in HE.
	 */
	public static long disChargeBatteryIfValid(@NotNull ItemStack stack, long ntmCharge, boolean instant) {
		if (stack.isEmpty()) return 0;
		if (stack.getItem() instanceof IBatteryItem battery) {
			long currentCharge = battery.getCharge(stack);
			long extractLimit = instant ? ntmCharge : Math.min(ntmCharge, battery.getDischargeRate());
			long actualExtracted = Math.min(extractLimit, currentCharge);
			if (actualExtracted > 0) {
				battery.setCharge(stack, currentCharge - actualExtracted);
			}
			return actualExtracted;
		} else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
			if (GeneralConfig.conversionRateHeToRF <= 0) {
				return 0;
			}
			IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
			if (cap != null) {
				int feToExtract = (int) (ntmCharge * GeneralConfig.conversionRateHeToRF);
				int feExtracted = cap.extractEnergy(feToExtract, false);
				return (long) (feExtracted / GeneralConfig.conversionRateHeToRF);
			}
		}
		return 0;
	}

    public static long getCompressedNbtSize(NBTTagCompound compound) {
        try {
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            writeCompressed(compound, bytearrayoutputstream);
            return bytearrayoutputstream.size();
        } catch (IOException ignored) {
            return -1;
        }
    }

    public static <T extends TileEntity> float getTENbtPercentage(T te, float limitByteSize) {
        NBTTagCompound compound = new NBTTagCompound();
        compound = te.writeToNBT(compound);
        float percent = 0.0f;
        if (limitByteSize > 0) {
            percent = (float) Library.getCompressedNbtSize(compound) / limitByteSize;
        }
        return percent;
    }

	/**
	 * Performs a raycast against all blocks and optionally entities in the world.
	 * The ray is defined by a starting position, a direction vector, and a maximum length.
	 *
	 * @param world                         The world instance to perform the raycast in.
	 * @param startPos                      The starting position of the ray.
	 * @param directionVec                  The direction vector of the ray. This vector will be normalized.
	 * @param maxLength                     The exact distance the ray will travel from the start vector.
	 * @param stopOnLiquid                  If true, the ray will stop on liquid blocks.
	 * @param ignoreBlockWithoutBoundingBox If true, blocks without a collision bounding box are ignored.
	 * @param returnLastUncollidableBlock   If true, returns the last passable block before a miss.
	 * @param stopOnEntity                  If true, the ray will also check for entity collisions.
	 * @return A RayTraceResult containing the hit information, or null if nothing was hit.
	 */
	@Nullable
	public static RayTraceResult rayTrace(@NotNull WorldServer world, @NotNull Vec3d startPos, @NotNull Vec3d directionVec, double maxLength, boolean stopOnLiquid,
										  boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, boolean stopOnEntity) {
		if (Double.isNaN(startPos.x) || Double.isNaN(startPos.y) || Double.isNaN(startPos.z) || Double.isNaN(directionVec.x) || Double.isNaN(directionVec.y) || Double.isNaN(directionVec.z)) {
			return null;
		}

		if (directionVec.lengthSquared() < 1.0E-7D) {
			return null;
		}

		Vec3d endPos = startPos.add(directionVec.normalize().scale(maxLength));

		RayTraceResult blockHitResult = rayTraceBlocksInternal(world, startPos, endPos, stopOnLiquid, ignoreBlockWithoutBoundingBox,
				returnLastUncollidableBlock);

		if (stopOnEntity) {
			Vec3d rayTraceEndForEntities = endPos;
			if (blockHitResult != null) {
				rayTraceEndForEntities = blockHitResult.hitVec;
			}

			Entity closestEntity = null;
			Vec3d entityHitVec = null;
			double closestHitDistSq = rayTraceEndForEntities.squareDistanceTo(startPos);
			AxisAlignedBB searchBB = new AxisAlignedBB(startPos.x, startPos.y, startPos.z, rayTraceEndForEntities.x, rayTraceEndForEntities.y,
					rayTraceEndForEntities.z).expand(1.0, 1.0, 1.0);
			List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(null, searchBB);

			for (Entity entity : entities) {
				if (entity.canBeCollidedWith() && !entity.noClip) {
					AxisAlignedBB entityBB = entity.getEntityBoundingBox().grow(0.3D);
					RayTraceResult entityIntercept = entityBB.calculateIntercept(startPos, rayTraceEndForEntities);

					if (entityIntercept != null) {
						double distSq = startPos.squareDistanceTo(entityIntercept.hitVec);
						if (distSq < closestHitDistSq) {
							closestEntity = entity;
							entityHitVec = entityIntercept.hitVec;
							closestHitDistSq = distSq;
						}
					}
				}
			}
			if (closestEntity != null) {
				return new RayTraceResult(closestEntity, entityHitVec);
			}
		}
		return blockHitResult;
	}

	@Nullable
	private static RayTraceResult rayTraceBlocksInternal(@NotNull WorldServer world, @NotNull Vec3d startVec, @NotNull Vec3d endVec, boolean stopOnLiquid,
														 boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
		Vec3d dir = endVec.subtract(startVec);
		if (dir.lengthSquared() < 1.0E-7D) return null;
		int x = MathHelper.floor(startVec.x);
		int y = MathHelper.floor(startVec.y);
		int z = MathHelper.floor(startVec.z);
		if (!world.isChunkGeneratedAt(x >> 4, z >> 4)) {
			return null;
		}

		int stepX = (int) Math.signum(dir.x);
		int stepY = (int) Math.signum(dir.y);
		int stepZ = (int) Math.signum(dir.z);

		double tDeltaX = (stepX == 0) ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.x);
		double tDeltaY = (stepY == 0) ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.y);
		double tDeltaZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.z);

		double tMaxX = (stepX > 0) ? (x + 1 - startVec.x) * tDeltaX : (startVec.x - x) * tDeltaX;
		double tMaxY = (stepY > 0) ? (y + 1 - startVec.y) * tDeltaY : (startVec.y - y) * tDeltaY;
		double tMaxZ = (stepZ > 0) ? (z + 1 - startVec.z) * tDeltaZ : (startVec.z - z) * tDeltaZ;

		BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos(x, y, z);
		IBlockState startState = world.getBlockState(currentPos);
		if (isCollidable(world, startState, stopOnLiquid, ignoreBlockWithoutBoundingBox, currentPos)) {
			return startState.collisionRayTrace(world, currentPos, startVec, endVec);
		}

		RayTraceResult lastMissResult = null;

		while (true) {
			EnumFacing sideHit;
			if (tMaxX < tMaxY) {
				if (tMaxX < tMaxZ) {
					if (tMaxX > 1.0) break;
					x += stepX;
					tMaxX += tDeltaX;
					sideHit = (stepX > 0) ? EnumFacing.WEST : EnumFacing.EAST;
				} else {
					if (tMaxZ > 1.0) break;
					z += stepZ;
					tMaxZ += tDeltaZ;
					sideHit = (stepZ > 0) ? EnumFacing.NORTH : EnumFacing.SOUTH;
				}
			} else {
				if (tMaxY < tMaxZ) {
					if (tMaxY > 1.0) break;
					y += stepY;
					tMaxY += tDeltaY;
					sideHit = (stepY > 0) ? EnumFacing.DOWN : EnumFacing.UP;
				} else {
					if (tMaxZ > 1.0) break;
					z += stepZ;
					tMaxZ += tDeltaZ;
					sideHit = (stepZ > 0) ? EnumFacing.NORTH : EnumFacing.SOUTH;
				}
			}
			if (!world.getChunkProvider().chunkExists(x >> 4, z >> 4)) break;
			currentPos.setPos(x, y, z);
			IBlockState currentState = world.getBlockState(currentPos);
			if (isCollidable(world, currentState, stopOnLiquid, ignoreBlockWithoutBoundingBox, currentPos)) {
				return currentState.collisionRayTrace(world, currentPos, startVec, endVec);
			}
			if (returnLastUncollidableBlock) {
				double t = Math.min(tMaxX - tDeltaX, Math.min(tMaxY - tDeltaY, tMaxZ - tDeltaZ));
				Vec3d missHitVec = startVec.add(dir.scale(t));
				lastMissResult = new RayTraceResult(RayTraceResult.Type.MISS, missHitVec, sideHit, currentPos.toImmutable());
			}
		}

		return returnLastUncollidableBlock ? lastMissResult : null;
	}

	private static boolean isCollidable(World world, IBlockState state, boolean stopOnLiquid, boolean ignoreNoBoundingBox, BlockPos pos) {
		Block block = state.getBlock();
		boolean hasBoundingBox = !ignoreNoBoundingBox || state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB;

		if (hasBoundingBox && block.canCollideCheck(state, stopOnLiquid)) {
			return true;
		}
		return hasBoundingBox && state.getMaterial() == Material.PORTAL;
	}

	/**
	 * Attempts to export a list of items to an external inventory or conveyor belt at a given position.
	 * It first tries to insert into an IItemHandler (chest, etc.), then tries to place on an IConveyorBelt.
	 *
	 * @param world         The world object.
	 * @param exportToPos   The block position of the target inventory/conveyor.
	 * @param accessSide    The direction from which the target block is being accessed.
	 * @param itemsToExport A list of ItemStacks to be exported. This list will not be modified.
	 * @return A new list containing any leftover ItemStacks that could not be fully exported. Returns an empty list on full success.
	 */
	public static @NotNull List<ItemStack> popProducts(@NotNull World world, @NotNull BlockPos exportToPos, @NotNull ForgeDirection accessSide,
													   @NotNull List<ItemStack> itemsToExport) {
		if (itemsToExport.isEmpty()) return Collections.emptyList();

		List<ItemStack> remainingItems = new ArrayList<>();
		for (ItemStack item : itemsToExport) {
			if (!item.isEmpty()) {
				remainingItems.add(item.copy());
			}
		}

		if (remainingItems.isEmpty()) {
			return Collections.emptyList();
		}

		TileEntity tile = world.getTileEntity(exportToPos);
		if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide.toEnumFacing())) {
			IItemHandler inv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide.toEnumFacing());
			if (inv != null) {
				ListIterator<ItemStack> iterator = remainingItems.listIterator();
				while (iterator.hasNext()) {
					ItemStack originalStack = iterator.next();
					ItemStack leftover = InventoryUtil.tryAddItemToInventory(inv, 0, inv.getSlots() - 1, originalStack);
					iterator.set(leftover);
				}
				remainingItems.removeIf(ItemStack::isEmpty);
			}
		}

		if (remainingItems.isEmpty()) {
			return Collections.emptyList();
		}

		Block block = world.getBlockState(exportToPos).getBlock();
		if (block instanceof IConveyorBelt belt) {
			ListIterator<ItemStack> iterator = remainingItems.listIterator();
			while (iterator.hasNext()) {
				ItemStack item = iterator.next();
				Vec3d base = new Vec3d(exportToPos.getX() + 0.5, exportToPos.getY() + 0.5, exportToPos.getZ() + 0.5);
				Vec3d vec = belt.getClosestSnappingPosition(world, exportToPos, base);
				EntityMovingItem moving = new EntityMovingItem(world);
				moving.setPosition(base.x, vec.y, base.z);
				moving.setItemStack(item.copy());
				if (world.spawnEntity(moving)) {
					iterator.set(ItemStack.EMPTY);
				}
			}
			remainingItems.removeIf(ItemStack::isEmpty);
		}

		return remainingItems;
	}

	/**
	 * Attempts to pull items for a given recipe from a source inventory into a destination inventory.
	 *
	 * @param sourceContainer      The IItemHandler of the inventory to pull from.
	 * @param sourceSlots          The specific slots in the source inventory that can be accessed.
	 * @param destinationInventory The IItemHandlerModifiable of the machine's inventory to pull into.
	 * @param recipeIngredients    A list of AStacks representing the required ingredients.
	 * @param sourceTE             The TileEntity of the source inventory, used for canExtractItem checks. Can be null.
	 * @param destStartSlot        The starting slot index (inclusive) of the ingredient area in the destination inventory.
	 * @param destEndSlot          The ending slot index (inclusive) of the ingredient area in the destination inventory.
	 * @param finalizeBy           A Runnable that is executed if at least one item is successfully pulled. Can be null.
	 * @return true if any items were successfully pulled, false otherwise.
	 */
	@CanIgnoreReturnValue
	public static boolean pullItemsForRecipe(@NotNull IItemHandler sourceContainer, int @NotNull [] sourceSlots,
											 @NotNull IItemHandlerModifiable destinationInventory, @NotNull List<AStack> recipeIngredients,
											 @Nullable TileEntityMachineBase sourceTE, int destStartSlot, int destEndSlot,
											 @Nullable Runnable finalizeBy) {
		if (recipeIngredients.isEmpty() || sourceSlots.length == 0) return false;
		boolean itemsPulled = false;

		Map<Integer, ItemStack> availableItems = new HashMap<>();
		for (int slot : sourceSlots) {
			ItemStack stack = sourceContainer.getStackInSlot(slot);
			if (!stack.isEmpty()) {
				availableItems.put(slot, stack.copy());
			}
		}

		if (availableItems.isEmpty()) {
			return false;
		}

		for (AStack recipeIngredient : recipeIngredients) {
			if (recipeIngredient == null || recipeIngredient.count() <= 0) {
				continue;
			}

			AStack singleIngredient = recipeIngredient.copy().singulize();
			int maxStackSize = singleIngredient.getStack().getMaxStackSize();
			if (maxStackSize <= 0) maxStackSize = 1;
			int slotsNeeded = (int) Math.ceil((double) recipeIngredient.count() / maxStackSize);

			List<Integer> partialSlots = new ArrayList<>();
			List<Integer> fullSlots = new ArrayList<>();
			List<Integer> emptySlots = new ArrayList<>();

			for (int i = destStartSlot; i <= destEndSlot; i++) {
				ItemStack destStack = destinationInventory.getStackInSlot(i);
				if (destStack.isEmpty()) {
					emptySlots.add(i);
					continue;
				}
				ItemStack compareStack = destStack.copy();
				compareStack.setCount(1);
				if (singleIngredient.isApplicable(compareStack)) {
					if (destStack.getCount() < destStack.getMaxStackSize()) {
						partialSlots.add(i);
					} else {
						fullSlots.add(i);
					}
				}
			}
			int slotsOccupied = partialSlots.size() + fullSlots.size();

			for (int destSlot : partialSlots) {
				ItemStack destStack = destinationInventory.getStackInSlot(destSlot);
				int amountToPull = destStack.getMaxStackSize() - destStack.getCount();
				if (amountToPull <= 0) continue;
				itemsPulled = isItemsPulled(sourceContainer, destinationInventory, sourceTE, itemsPulled, availableItems, singleIngredient, destSlot, amountToPull);
			}

			int newSlotsToFill = slotsNeeded - slotsOccupied;
			if (newSlotsToFill > 0) {
				for (int i = 0; i < newSlotsToFill && i < emptySlots.size(); i++) {
					int destSlot = emptySlots.get(i);
                    itemsPulled = isItemsPulled(sourceContainer, destinationInventory, sourceTE, itemsPulled, availableItems, singleIngredient, destSlot, maxStackSize);
				}
			}
		}

		if (itemsPulled && finalizeBy != null) {
			finalizeBy.run();
		}

		return itemsPulled;
	}

	private static boolean isItemsPulled(@NotNull IItemHandler sourceContainer, @NotNull IItemHandlerModifiable destinationInventory, @Nullable TileEntityMachineBase sourceTE, boolean itemsPulled, Map<Integer, ItemStack> availableItems, AStack singleIngredient, int destSlot, int amountToPull) {
		for (Map.Entry<Integer, ItemStack> entry : availableItems.entrySet()) {
			if (amountToPull <= 0) break;
			int sourceSlot = entry.getKey();
			ItemStack sourceStack = entry.getValue();
			if (sourceStack.isEmpty()) continue;

			ItemStack compareSourceStack = sourceStack.copy();
			compareSourceStack.setCount(1);
			if (!singleIngredient.isApplicable(compareSourceStack)) continue;

			int pullThisTime = Math.min(amountToPull, sourceStack.getCount());
			if (sourceTE != null && !sourceTE.canExtractItem(sourceSlot, sourceStack, pullThisTime)) continue;

			ItemStack extracted = sourceContainer.extractItem(sourceSlot, pullThisTime, false);
			if (!extracted.isEmpty()) {
				sourceStack.shrink(extracted.getCount());

				ItemStack destStack = destinationInventory.getStackInSlot(destSlot);
				if (destStack.isEmpty()) {
					destinationInventory.setStackInSlot(destSlot, extracted);
				} else {
					destStack.grow(extracted.getCount());
					destinationInventory.setStackInSlot(destSlot, destStack);
				}
				amountToPull -= extracted.getCount();
				itemsPulled = true;
			}
		}
		return itemsPulled;
	}
}
