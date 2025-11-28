package com.hbm.uninos;

import com.hbm.config.GeneralConfig;
import com.hbm.lib.DirPos;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Unified Nodespace, a Nodespace for all applications.
 * "Nodespace" is an invisible "dimension" where nodes exist, a node is basically the "soul" of a tile entity with networking capabilities.
 * Instead of tile entities having to find each other which is costly and assumes the tiles are loaded, tiles simply create nodes at their
 * respective position in nodespace, the nodespace itself handles stuff like connections which can also happen in unloaded chunks.
 * A node is so to say the "soul" of a tile entity which can act independent of its "body".
 * Edit: now every NodeNet have their own "dimension"
 * @author hbm
 */
public final class UniNodespace {

    private static final Object2ObjectOpenHashMap<INetworkProvider<?>, PerTypeNodeManager<?, ?, ?, ?>> managers = new Object2ObjectOpenHashMap<>();

    private UniNodespace() {
    }

    public static <N extends NodeNet<R, P, L, N>, L extends GenNode<N>, R, P> L getNode(World world, BlockPos pos, INetworkProvider<N> type) {
        return getManagerFor(type).getNode(world, pos);
    }

    public static <N extends NodeNet<R, P, L, N>, L extends GenNode<N>, R, P> void createNode(World world, L node) {
        getManagerFor(node.networkProvider).createNode(world, node);
    }

    public static <N extends NodeNet<R, P, L, N>, L extends GenNode<N>, R, P> void destroyNode(World world, BlockPos pos, INetworkProvider<N> type) {
        getManagerFor(type).destroyNode(world, pos);
    }

    /**
     * <code>GeneralConfig.enableThreadedNodeSpaceUpdate</code> is safe provided that:
     * <ul>
     *   <li>This method is called only from the server thread.</li>
     *   <li>While this method is running, no other thread may call createNode/destroyNode or otherwise mutate UniNodespace / PerTypeNodeManager state.</li>
     *   <li>TileEntity logic that interacts with NodeNet must not run concurrently with this method.</li>
     * </ul>
     */
    public static void updateNodespace() {
        World[] currentWorlds = DimensionManager.getWorlds();
        if (currentWorlds.length == 0) return;

        if (!GeneralConfig.enableThreadedNodeSpaceUpdate) {
            for (World world : currentWorlds) {
                ObjectIterator<Object2ObjectMap.Entry<INetworkProvider<?>, PerTypeNodeManager<?, ?, ?, ?>>> iterator = managers.object2ObjectEntrySet().fastIterator();
                while (iterator.hasNext()) {
                    PerTypeNodeManager<?, ?, ?, ?> manager = iterator.next().getValue();
                    manager.updateForWorld(world);
                }
            }
            updateNetworks();
            return;
        }
        List<Callable<Void>> tasks = new ArrayList<>(currentWorlds.length);
        for (World world : currentWorlds) {
            tasks.add(() -> {
                ObjectIterator<Object2ObjectMap.Entry<INetworkProvider<?>, PerTypeNodeManager<?, ?, ?, ?>>> iterator = managers.object2ObjectEntrySet().fastIterator();
                while (iterator.hasNext()) {
                    PerTypeNodeManager<?, ?, ?, ?> manager = iterator.next().getValue();
                    manager.updateForWorld(world);
                }
                return null;
            });
        }
        try {
            List<Future<Void>> futures = ForkJoinPool.commonPool().invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("UniNodespace update interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Exception during UniNodespace update", e.getCause());
        }
        updateNetworks();
    }

    static void removeActiveNet(NodeNet<?, ?, ?, ?> net) {
        if (net.links.isEmpty()) {
            ObjectIterator<Object2ObjectMap.Entry<INetworkProvider<?>, PerTypeNodeManager<?, ?, ?, ?>>> iterator = managers.object2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                PerTypeNodeManager<?, ?, ?, ?> manager = iterator.next().getValue();
                manager.removeActiveNet(net);
            }
            return;
        }
        GenNode<?> node = net.links.first();
        PerTypeNodeManager<?, ?, ?, ?> manager = managers.get(node.networkProvider);
        if (manager != null) manager.removeActiveNet(net);
    }

    private static void updateNetworks() {
        for (PerTypeNodeManager<?, ?, ?, ?> manager : managers.values()) {
            manager.updateNetworks();
        }
    }

    @SuppressWarnings("unchecked")
    private static <R, P, L extends GenNode<N>, N extends NodeNet<R, P, L, N>> PerTypeNodeManager<R, P, L, N> getManagerFor(INetworkProvider<N> provider) {
        PerTypeNodeManager<?, ?, ?, ?> manager = managers.get(provider);
        if (manager == null) {
            manager = new PerTypeNodeManager<>(provider);
            managers.put(provider, manager);
        }
        return (PerTypeNodeManager<R, P, L, N>) manager;
    }

    private static class PerTypeNodeManager<R, P, L extends GenNode<N>, N extends NodeNet<R, P, L, N>> {

        private final Object2ObjectOpenHashMap<World, UniNodeWorld<N, L>> worlds = new Object2ObjectOpenHashMap<>();
        private final Set<N> activeNodeNets = GeneralConfig.enableThreadedNodeSpaceUpdate ? ConcurrentHashMap.newKeySet() : new ObjectOpenHashSet<>();
        private final INetworkProvider<N> provider;

        PerTypeNodeManager(INetworkProvider<N> provider) {
            this.provider = provider;
        }

        private static boolean checkConnection(GenNode<?> connectsTo, DirPos connectFrom, boolean skipSideCheck) {
            for (DirPos revCon : connectsTo.connections) {
                if (revCon.getPos().getX() - revCon.getDir().offsetX == connectFrom.getPos().getX() && revCon.getPos().getY() - revCon.getDir().offsetY == connectFrom.getPos().getY() && revCon.getPos().getZ() - revCon.getDir().offsetZ == connectFrom.getPos().getZ() && (revCon.getDir() == connectFrom.getDir().getOpposite() || skipSideCheck)) {
                    return true;
                }
            }
            return false;
        }

        private UniNodeWorld<N, L> getWorldManager(World world) {
            return worlds.computeIfAbsent(world, k -> new UniNodeWorld<>());
        }

        L getNode(World world, BlockPos pos) {
            UniNodeWorld<N, L> nodeWorld = worlds.get(world);
            return nodeWorld != null ? nodeWorld.getNode(pos) : null;
        }

        void createNode(World world, L node) {
            getWorldManager(world).pushNode(node);
        }

        void destroyNode(World world, BlockPos pos) {
            L node = getNode(world, pos);
            if (node != null) {
                getWorldManager(world).popNode(node);
            }
        }

        void addActiveNet(N net) {
            activeNodeNets.add(net);
        }

        void removeActiveNet(Object net) {
            activeNodeNets.remove(net);
        }

        void updateForWorld(World world) {
            UniNodeWorld<N, L> nodeWorld = worlds.get(world);
            if (nodeWorld == null) return;

            for (L node : nodeWorld.getAllNodes()) {
                if (node.expired) continue;
                if (!node.hasValidNet() || node.recentlyChanged) {
                    checkNodeConnection(world, node);
                    node.recentlyChanged = false;
                }
            }
        }

        void updateNetworks() {
            if (!GeneralConfig.enableThreadedNodeSpaceUpdate) {
                for (N net : activeNodeNets) {
                    net.resetTrackers();
                }
                for (N net : activeNodeNets) {
                    if (net.isValid()) net.update();
                }
            } else {
                for (N net : activeNodeNets) {
                    net.resetTrackers();
                }
                activeNodeNets.parallelStream().forEach(net -> {
                    if (net.isValid()) {
                        net.update();
                    }
                });
            }
        }

        private void checkNodeConnection(World world, L node) {
            for (DirPos con : node.connections) {
                L conNode = getNode(world, con.getPos());
                if (conNode != null) {
                    if (conNode.hasValidNet() && conNode.net == node.net) continue;
                    if (checkConnection(conNode, con, false)) {
                        connectToNode(node, conNode);
                    }
                }
            }
            if (node.net == null || !node.net.isValid()) {
                N newNet = provider.get();
                this.addActiveNet(newNet);
                newNet.joinLink(node);
            }
        }

        private void connectToNode(L origin, L connection) {
            if (origin.hasValidNet() && connection.hasValidNet()) { // both nodes have nets, but the nets are different (previous assumption), join networks
                if (origin.net.links.size() > connection.net.links.size()) {
                    origin.net.joinNetworks(connection.net);
                } else {
                    connection.net.joinNetworks(origin.net);
                }
            } else if (!origin.hasValidNet() && connection.hasValidNet()) { // origin has no net, connection does, have origin join connection's net
                connection.net.joinLink(origin);
            } else if (origin.hasValidNet() && !connection.hasValidNet()) { // ...and vice versa
                origin.net.joinLink(connection);
            }
        }
    }

    /**
     * Holds all nodes of a single network type for a specific World.
     */
    private static class UniNodeWorld<N extends NodeNet<?, ?, L, N>, L extends GenNode<N>> {
        private final Object2ObjectLinkedOpenHashMap<BlockPos, L> nodesByPosition = new Object2ObjectLinkedOpenHashMap<>();

        L getNode(BlockPos pos) {
            return nodesByPosition.get(pos);
        }

        /** Adds a node at all its positions to the nodespace */
        void pushNode(L node) {
            for (BlockPos pos : node.positions) {
                nodesByPosition.put(pos, node);
            }
        }

        /** Removes the specified node from all positions from nodespace */
        void popNode(L node) {
            if (node.net != null) node.net.destroy();
            for (BlockPos pos : node.positions) {
                nodesByPosition.remove(pos, node);
            }
            node.expired = true;
        }

        /** @return a view of all nodes in this world, do not modify */
        Collection<L> getAllNodes() {
            return nodesByPosition.values();
        }
    }
}
