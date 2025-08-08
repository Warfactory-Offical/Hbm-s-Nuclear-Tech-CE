package com.hbm.tileentity.machine.rbmk;

import com.hbm.api.fluidmk2.FluidNetMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.config.MachineConfig;
import com.hbm.entity.effect.EntitySpear;
import com.hbm.entity.projectile.EntityRBMKDebris;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.control_panel.ControlEventSystem;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.IControllable;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.saveddata.TomSaveData;
import com.hbm.tileentity.IOverpressurable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;
import com.hbm.util.I18nUtil;
import java.util.*;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityRBMKBase extends TileEntityLoadedBase implements ITickable, IControllable {

	@Deprecated
	public static int rbmkHeight = 4;
	
	public double heat = 20.0D;
	public double jumpheight = 0.0D;
	public float downwardSpeed = 0.0F;
	public boolean falling = false;
	public static final byte gravity = 5; //in blocks per s^2

	public int reasimWater;
	public static final int maxWater = 16000;
	public int reasimSteam;
	public static final int maxSteam = 16000;

	public boolean hasLid() {

		if(!isLidRemovable())
			return true;

		return this.getBlockMetadata() != RBMKBase.DIR_NO_LID.ordinal() + RBMKBase.offset;
	}

	public boolean isLidRemovable() {
		return true;
	}

	/**
	 * Approx melting point of steel
	 * This metric won't be used because fuel tends to melt much earlier than that
	 * @return
	 */
	public double maxHeat() {
		return 1500D;
	}

	/**
	 * Around the same for every component except boilers which do not have passive cooling
	 * @return
	 */
	public double passiveCooling() {
		return RBMKDials.getPassiveCooling(world); //default: 5.0D
	}

	//necessary checks to figure out whether players are close enough to ensure that the reactor can be safely used
	public boolean shouldUpdate() {
		return true;
	}
	
	public int trackingRange() {
		return 15;
	}
	
	@Override
	public void update() {

		if(!world.isRemote) {
			moveHeat();
			if(RBMKDials.getReasimBoilers(world))
				boilWater();
			coolPassively();
			jump();
			networkPackNT(trackingRange());
		}
	}

	private void jump(){
		if(this.heat <= MachineConfig.rbmkJumpTemp && !falling)
			return;

		if(!falling){ // linear rise
			if(this.heat > MachineConfig.rbmkJumpTemp){
				if(this.jumpheight > 0 || world.rand.nextInt((int)(25D*maxHeat()/(this.heat-MachineConfig.rbmkJumpTemp+200D))+1) == 0){
					double change = (this.heat-MachineConfig.rbmkJumpTemp)*0.0002D;
					double heightLimit = (this.heat-MachineConfig.rbmkJumpTemp)*0.002D;

					this.jumpheight = this.jumpheight + change;
					
					if(this.jumpheight > heightLimit){
						this.jumpheight = heightLimit;
						this.falling = true;
					}
				}
			} else {
				this.falling = true;
			}
		} else{ // gravity fall
			if(this.jumpheight > 0){
				this.downwardSpeed = this.downwardSpeed + gravity * 0.05F;
				this.jumpheight = this.jumpheight - this.downwardSpeed;
			} else {
				this.jumpheight = 0;
				this.downwardSpeed = 0;
				this.falling = false;
				world.playSound(null, pos.getX(),  pos.getY() + 4,  pos.getZ(), HBMSoundHandler.rbmkLid, SoundCategory.BLOCKS, 2.0F, 1.0F);
			}
		}
	}

	
	/**
	 * The ReaSim boiler dial causes all RBMK parts to behave like boilers
	 */
	private void boilWater() {

		if(heat < 100D)
			return;

		double heatConsumption = RBMKDials.getBoilerHeatConsumption(world);
		double availableHeat = (this.heat - 100) / heatConsumption;
		double availableWater = this.reasimWater;
		double availableSpace = maxSteam - this.reasimSteam;

		int processedWater = (int)Math.floor(Math.min(availableHeat, Math.min(availableWater, availableSpace)) * MathHelper.clamp(RBMKDials.getReaSimBoilerSpeed(world), 0D, 1D));

		if(processedWater <= 0) return;

		this.reasimWater -= processedWater;
		this.reasimSteam += processedWater;
		this.heat -= processedWater * heatConsumption;
	}
	
	public static final ForgeDirection[] heatDirs = new ForgeDirection[] {
			ForgeDirection.NORTH,
			ForgeDirection.EAST,
			ForgeDirection.SOUTH,
			ForgeDirection.WEST
	};
	
	protected TileEntityRBMKBase[] neighbourCache = new TileEntityRBMKBase[4];
	
	/**
	 * Moves heat to neighboring parts, if possible, in a relatively fair manner
	 */
	private void moveHeat() {

		boolean reasim = RBMKDials.getReasimBoilers(world);

		List<TileEntityRBMKBase> rec = new ArrayList<>();
		rec.add(this);
		double heatTot = this.heat;
		int waterTot = this.reasimWater;
		int steamTot = this.reasimSteam;

		int index = 0;
		for(ForgeDirection dir : heatDirs) {

			if(neighbourCache[index] != null && neighbourCache[index].isInvalid())
				neighbourCache[index] = null;

			if(neighbourCache[index] == null) {
				TileEntity te = world.getTileEntity(getPos().add(dir.offsetX, 0, dir.offsetZ));

				if(te instanceof TileEntityRBMKBase base) {
                    neighbourCache[index] = base;
				}
			}

			index++;
		}

		for(TileEntityRBMKBase base : neighbourCache) {

			if(base != null) {
				rec.add(base);
				heatTot += base.heat;
				if(reasim) {
					waterTot += base.reasimWater;
					steamTot += base.reasimSteam;
				}
			}
		}

		int members = rec.size();
		double stepSize = RBMKDials.getColumnHeatFlow(world);

		if(members > 1) {

			double targetHeat = heatTot / (double)members;

			int tWater = waterTot / members;
			int rWater = waterTot % members;
			int tSteam = steamTot / members;
			int rSteam = steamTot % members;

			for(TileEntityRBMKBase rbmk : rec) {
				double delta = targetHeat - rbmk.heat;
				rbmk.heat += delta * stepSize;

				//set to the averages, rounded down
				if(reasim) {
					rbmk.reasimWater = tWater;
					rbmk.reasimSteam = tSteam;
				}
			}

			//add the modulo to make up for the losses coming from rounding
			if(reasim) {
				this.reasimWater += rWater;
				this.reasimSteam += rSteam;
			}

			this.markDirty();
		}
	}
	
	private void coolPassively() {

		if(TomSaveData.forWorld(world).fire > 1e-5) {
			double light = this.world.getLightFor(EnumSkyBlock.SKY, getPos()) / 15D;
			if(heat < 20 + (480 * light)) {
				this.heat += this.passiveCooling() * 2;
			}
		}

		this.heat -= this.passiveCooling();

		if(heat < 20)
			heat = 20D;
	}

	public RBMKType getRBMKType() {
		return RBMKType.OTHER;
	}
	
	protected static boolean diag = false;
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		if(!diag) {
			super.readFromNBT(nbt);
		}

		this.heat = nbt.getDouble("heat");
		this.reasimWater = nbt.getInteger("realSimWater");
		this.reasimSteam = nbt.getInteger("realSimSteam");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		if(!diag) {
			super.writeToNBT(nbt);
		}

		nbt.setDouble("heat", this.heat);
		nbt.setInteger("realSimWater", this.reasimWater);
		nbt.setInteger("realSimSteam", this.reasimSteam);
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeDouble(this.heat);
		buf.writeInt(this.reasimWater);
		buf.writeInt(this.reasimSteam);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.heat = buf.readDouble();
		this.reasimWater = buf.readInt();
		this.reasimSteam = buf.readInt();
	}
	
	public void getDiagData(NBTTagCompound nbt) {
		diag = true;
		this.writeToNBT(nbt);
		diag = false;
	}

	@SideOnly(Side.CLIENT)
	public static void diagnosticPrintHook(RenderGameOverlayEvent.Pre event) {

		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.world;
		RayTraceResult mop = mc.objectMouseOver;
		ScaledResolution resolution = event.getResolution();

		if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK && world.getBlockState(mop.getBlockPos()).getBlock() instanceof RBMKBase rbmk) {
			int[] pos = rbmk.findCore(world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ());

			if (pos == null)
				return;

			TileEntityRBMKBase te = (TileEntityRBMKBase) world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
			if (te == null) return;
			NBTTagCompound flush = new NBTTagCompound();
			te.getDiagData(flush);
			Set<String> keys = flush.getKeySet();

			GlStateManager.pushMatrix();
			float scale = 0.5f;
			GlStateManager.scale(scale, scale, 1.0f);
			int pX = resolution.getScaledWidth() / 2 + 8;
			int pZ = resolution.getScaledHeight() / 2;

			List<String> exceptions = new ArrayList<>();
			exceptions.add("x");
			exceptions.add("y");
			exceptions.add("z");
			exceptions.add("items");
			exceptions.add("id");
			exceptions.add("muffled");

			String title = "Dump of Ordered Data Diagnostic (DODD)";
			mc.fontRenderer.drawString(title, (int)(pX / scale) + 1, (int)((pZ - 19) / scale), 0x006000);
			mc.fontRenderer.drawString(title, (int)(pX / scale), (int)((pZ - 20) / scale), 0x00FF00);

			mc.fontRenderer.drawString(I18nUtil.resolveKey(rbmk.getTranslationKey() + ".name"), (int)(pX / scale) + 1, (int)((pZ - 9) / scale), 0x606000);
			mc.fontRenderer.drawString(I18nUtil.resolveKey(rbmk.getTranslationKey() + ".name"), (int)(pX / scale), (int)((pZ - 10) / scale), 0xffff00);

			String[] ents = new String[keys.size()];
			keys.toArray(ents);
			Arrays.sort(ents);
			int listPz = pZ;
			for (String key : ents) {

				if (exceptions.contains(key))
					continue;
				mc.fontRenderer.drawString(key + ": " + flush.getTag(key), (int)(pX / scale), (int)(listPz / scale), 0xFFFFFF);
				listPz += (int) (10 * scale);
			}

			GlStateManager.disableBlend();

			GlStateManager.popMatrix();
			Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
		}
	}

	public void onOverheat() {

		for(int i = 0; i < 4; i++) {
			world.setBlockState(pos.up(i), Blocks.LAVA.getDefaultState());
		}
	}
	
	public void onMelt(int reduce) {

		standardMelt(reduce);

		if(this.getBlockMetadata() == RBMKBase.DIR_NORMAL_LID.ordinal() + RBMKBase.offset)
			spawnDebris(DebrisType.LID);
	}
	
	protected void standardMelt(int reduce) {

		int h = RBMKDials.getColumnHeight(world);
		reduce = MathHelper.clamp(reduce, 1, h);

		if(world.rand.nextInt(3) == 0)
			reduce++;

		for(int i = h; i >= 0; i--) {

			if(i <= h + 1 - reduce) {

				if(reduce > 1 && i == h + 1 - reduce) {
					world.setBlockState(pos.up(i), ModBlocks.pribris_burning.getDefaultState());
				} else {
					world.setBlockState(pos.up(i), ModBlocks.pribris.getDefaultState());
				}

			} else {
				world.setBlockToAir(pos.up(i));
			}
			IBlockState state = world.getBlockState(pos.up(i));
			world.notifyBlockUpdate(pos.up(i), state, state, 3);
		}
	}
	
	protected void spawnDebris(DebrisType type) {

		EntityRBMKDebris debris = new EntityRBMKDebris(world, pos.getX() + 0.5D, pos.getY() + 4D, pos.getZ() + 0.5D, type);
		debris.motionX = world.rand.nextGaussian() * 0.25D;
		debris.motionZ = world.rand.nextGaussian() * 0.25D;
		debris.motionY = 0.5D + world.rand.nextDouble() * 1.5D;

		if(type == DebrisType.LID) {
			debris.motionX *= 0.5D;
			debris.motionY += 0.5D;
			debris.motionZ *= 0.5D;
		}

		world.spawnEntity(debris);
	}
	
	public static HashSet<TileEntityRBMKBase> columns = new HashSet<>();
	public static HashSet<FluidNetMK2> pipes = new HashSet<>();
	
	//assumes that !world.isRemote
	public void meltdown() {
		RBMKBase.dropLids = false;

		columns.clear();
		pipes.clear();
		getFF(pos.getX(), pos.getY(), pos.getZ());

		int minX = pos.getX();
		int maxX = pos.getX();
		int minZ = pos.getZ();
		int maxZ = pos.getZ();

		//set meltdown bounds
		for(TileEntityRBMKBase rbmk : columns) {

			if(rbmk.pos.getX() < minX)
				minX = rbmk.pos.getX();
			if(rbmk.pos.getX() > maxX)
				maxX = rbmk.pos.getX();
			if(rbmk.pos.getZ() < minZ)
				minZ = rbmk.pos.getZ();
			if(rbmk.pos.getZ() > maxZ)
				maxZ = rbmk.pos.getZ();
		}

		//Convert every rbmk part into debris
		for(TileEntityRBMKBase rbmk : columns) {

			int distFromMinX = rbmk.pos.getX() - minX;
			int distFromMaxX = maxX - rbmk.pos.getX();
			int distFromMinZ = rbmk.pos.getZ() - minZ;
			int distFromMaxZ = maxZ - rbmk.pos.getZ();

			int minDist = Math.min(distFromMinX, Math.min(distFromMaxX, Math.min(distFromMinZ, distFromMaxZ)));

			rbmk.onMelt(minDist + 1);
		}

		//Adding extra rads near corium blocks
		for(TileEntityRBMKBase rbmk : columns) {

			if(rbmk instanceof TileEntityRBMKRod && world.getBlockState(rbmk.getPos()).getBlock() == ModBlocks.corium_block) {

				for(int x = rbmk.pos.getX() - 1; x <= rbmk.pos.getX() + 1; x ++) {
					for(int y = rbmk.pos.getY() - 1; y <= rbmk.pos.getY() + 1; y ++) {
						for(int z = rbmk.pos.getZ() - 1; z <= rbmk.pos.getZ() + 1; z ++) {

							Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();

							if(world.rand.nextInt(3) == 0 && (b == ModBlocks.pribris || b == ModBlocks.pribris_burning)) {

								if(RBMKBase.digamma)
									world.setBlockState(new BlockPos(x, y, z), ModBlocks.pribris_digamma.getDefaultState());
								else
									world.setBlockState(new BlockPos(x, y, z), ModBlocks.pribris_radiating.getDefaultState());
							}
						}
					}
				}
			}
		}

		/* Hanlde overpressure event */
		if(RBMKDials.getOverpressure(world) && !pipes.isEmpty()) {
			HashSet<Object> pipeBlocks = new HashSet<>();
			HashSet<Object> pipeReceivers = new HashSet<>();

			//unify all parts into single sets to prevent redundancy
			pipes.forEach(x -> {
				pipeBlocks.addAll(x.links);
				pipeReceivers.addAll(x.receiverEntries.entrySet());
			});

			int count = 0;
			int max = Math.min(pipeBlocks.size() / 5, 100);
			Iterator<Object> itPipes = pipeBlocks.iterator();
			Iterator<Object> itReceivers = pipeReceivers.iterator();

			while(itPipes.hasNext() && count < max) {
				Object pipe = itPipes.next();
				if(pipe instanceof TileEntity tile) {
                    world.setBlockToAir(tile.getPos());
				}
				count++;
			}

			while(itReceivers.hasNext()) {
				Object con = itReceivers.next();
				if(con instanceof TileEntity tile) {
                    if(con instanceof IOverpressurable) {
						((IOverpressurable) con).explode(world, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
					} else {
						world.setBlockToAir(tile.getPos());
						world.newExplosion(null, tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5, 5F, false, false);
					}
				}
			}
		}

		int smallDim = Math.max(maxX - minX, maxZ - minZ) * 2;
		int avgX = minX + (maxX - minX) / 2;
		int avgZ = minZ + (maxZ - minZ) / 2;

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "rbmkmush");
		data.setFloat("scale", smallDim);
		PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, avgX + 0.5, pos.getY() + 1, avgZ + 0.5), new TargetPoint(world.provider.getDimension(), avgX + 0.5, pos.getY() + 1, avgZ + 0.5, 250));
		MainRegistry.proxy.effectNT(data);

		world.playSound(null, avgX + 0.5, pos.getY() + 1, avgZ + 0.5, HBMSoundHandler.rbmk_explosion, SoundCategory.BLOCKS, 50.0F, 1.0F);

		List<EntityPlayer> list = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.getX() - 50 + 0.5, pos.getY() - 50 + 0.5, pos.getZ() - 50 + 0.5, pos.getX() + 50 + 0.5, pos.getY() + 50 + 0.5, pos.getZ() + 50 + 0.5));

		for(EntityPlayer e : list) {
			AdvancementManager.grantAchievement(e, AdvancementManager.progress_rbmk_boom);
		}

		if(RBMKBase.digamma) {
			EntitySpear spear = new EntitySpear(world);
			spear.posX = avgX + 0.5;
			spear.posZ = avgZ + 0.5;
			spear.posY = pos.getY() + 100;
			world.spawnEntity(spear);
		}

		RBMKBase.dropLids = true;
		RBMKBase.digamma = false;
	}
	
	//Family and Friends
	private void getFF(int x, int y, int z) {

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if(te instanceof TileEntityRBMKBase rbmk) {

            if(!columns.contains(rbmk)) {
				columns.add(rbmk);
				getFF(x + 1, y, z);
				getFF(x - 1, y, z);
				getFF(x, y, z + 1);
				getFF(x, y, z - 1);
			}
		}
	}
	
	public boolean isModerated() {
		return false;
	}
	
	public abstract ColumnType getConsoleType();
	
	public NBTTagCompound getNBTForConsole() {
		return null;
	}
	
	public static List<String> getFancyStats(NBTTagCompound nbt) {
		return null;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 17, pos.getZ() + 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	// control panel
	@Override
	public Map<String, DataValue> getQueryData() {
		Map<String, DataValue> data = new HashMap<>();

		data.put("heat", new DataValueFloat((float) heat));
		data.put("RSIM_feed", new DataValueFloat(reasimWater));
		data.put("RSIM_steam", new DataValueFloat(reasimSteam));

		return data;
	}

	@Override
	public void validate() {
		super.validate();
		ControlEventSystem.get(world).addControllable(this);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		ControlEventSystem.get(world).removeControllable(this);
		NeutronNodeWorld.removeNode(world, this.getPos()); // woo-fucking-hoo!!!
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		NeutronNodeWorld.removeNode(world, this.getPos()); // woo-fucking-hoo!!!
	}

	@Override
	public BlockPos getControlPos() {
		return getPos();
	}

	@Override
	public World getControlWorld() {
		return getWorld();
	}
}
