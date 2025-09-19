package com.hbm.world;

import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.BiomeSyncPacket;
import com.hbm.util.Compat;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WorldUtil {
    private static final Method getIntBiomeArray;

    static {
        Method method = null;
        if (Compat.isIDExtensionModLoaded()) {
            try {
                method = Chunk.class.getMethod("getIntBiomeArray");
            } catch (NoSuchMethodException ignored) {
            }
        }
        getIntBiomeArray = method;
    }

    public static Chunk provideChunk(WorldServer world, int chunkX, int chunkZ) {
        try {
            ChunkProviderServer provider = world.getChunkProvider();
            Chunk chunk = provider.getLoadedChunk(chunkX, chunkZ);
            if(chunk != null) return chunk;
            return loadChunk(world, provider, chunkX, chunkZ);
        } catch(Throwable x) {
            return null;
        }
    }

    private static Chunk loadChunk(WorldServer world, ChunkProviderServer provider, int chunkX, int chunkZ) {
        long chunkCoord = ChunkPos.asLong(chunkX, chunkZ);
        provider.droppedChunks.remove(chunkCoord);
        Chunk chunk = provider.loadedChunks.get(chunkCoord);
        AnvilChunkLoader loader = null;

        if(provider.chunkLoader instanceof AnvilChunkLoader) {
            loader = (AnvilChunkLoader) provider.chunkLoader;
        }

        if(chunk == null && loader != null && loader.chunkExists(world, chunkX, chunkZ)) {
            chunk = ChunkIOExecutor.syncChunkLoad(world, loader, provider, chunkX, chunkZ);
        }

        return chunk;
    }

    public static void setBiome(World world, int blockX, int blockZ, Biome biome) {
        if (world == null || biome == null) return;
        final int chunkX = blockX >> 4;
        final int chunkZ = blockZ >> 4;
        final Chunk chunk = world.getChunk(chunkX, chunkZ);
        final int i = blockX & 15;
        final int j = blockZ & 15;
        final int idx = (j << 4) | i;
        final int biomeId = Biome.getIdForBiome(biome);
        boolean updated = false;
        if (Compat.isIDExtensionModLoaded()) {
            int[] arr = getIntBiomeArray(chunk);
            if (arr != null) {
                arr[idx] = biomeId;
                updated = true;
            }
        }
        if (!updated) {
            byte[] bArr = chunk.getBiomeArray();
            if (bArr.length == 256) {
                bArr[idx] = (byte) (biomeId & 0xFF);
            }
        }
        chunk.markDirty();
    }

    public static int @Nullable [] getIntBiomeArray(Chunk chunk) {
        int[] arr = null;
        if (getIntBiomeArray != null) {
            try {
                arr = (int[]) getIntBiomeArray.invoke(chunk);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        if (arr != null && arr.length == 256) {
            return arr;
        } else {
            MainRegistry.logger.error("JEID/REID/NEID is loaded, but getIntBiomeArray failed to get the correct data. This is a bug!");
            return null;
        }
    }

    public static void syncBiomeChange(World world, int chunkX, int chunkZ) {
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        if(Compat.isIDExtensionModLoaded()) {
            PacketDispatcher.wrapper.sendToAllAround(new BiomeSyncPacket(chunkX, chunkZ, getIntBiomeArray(chunk)), new TargetPoint(world.provider.getDimension(), chunkX << 4, 128, chunkZ << 4, 1024D));
        } else {
            PacketDispatcher.wrapper.sendToAllAround(new BiomeSyncPacket(chunkX, chunkZ, chunk.getBiomeArray()), new TargetPoint(world.provider.getDimension(), chunkX << 4, 128, chunkZ << 4, 1024D));
        }
    }

    /**Chunkloads the chunk the entity is going to spawn in and then spawns it
     * @param entity The entity to be spawned**/

	/*fun fact: this is based off of joinEntityInSurroundings in World
	  however, since mojang is staffed by field mice, that function is client side only and half-baked
	 */
    public static void loadAndSpawnEntityInWorld(Entity entity) {

        World world = entity.world;
        int chunkX = MathHelper.floor(entity.posX / 16.0D);
        int chunkZ = MathHelper.floor(entity.posZ / 16.0D);
        byte loadRadius = 2;

        for(int k = chunkX - loadRadius; k <= chunkX + loadRadius; ++k) {
            for(int l = chunkZ - loadRadius; l <= chunkZ + loadRadius; ++l) {
                world.getChunk(k, l);
            }
        }

        if(!world.loadedEntityList.contains(entity)) {
            if(!MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entity, world))) {
                world.getChunk(chunkX, chunkZ).addEntity(entity);
                world.loadedEntityList.add(entity);
                world.onEntityAdded(entity);
            }
        }
    }
}
