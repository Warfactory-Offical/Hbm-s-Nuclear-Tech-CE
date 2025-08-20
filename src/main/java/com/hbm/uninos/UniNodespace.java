package com.hbm.uninos;

import com.hbm.lib.DirPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

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

    private static final Map<INetworkProvider<?>, PerTypeNodeManager<?, ?, ?, ?>> managers = new HashMap<>();

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

    public static void updateNodespace() {
        World[] currentWorlds = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
        for (World world : currentWorlds) {
            for (PerTypeNodeManager<?, ?, ?, ?> manager : managers.values()) {
                manager.updateForWorld(world);
            }
        }
        updateNetworks();
    }

    static void removeActiveNet(NodeNet<?, ?, ?, ?> net) {
        if (net.links.isEmpty()) {
            for (PerTypeNodeManager<?, ?, ?, ?> manager : managers.values()) {
                manager.removeActiveNet(net);
            }
            return;
        }
        GenNode<?> node = net.links.iterator().next();
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

        private final Map<World, UniNodeWorld<N, L>> worlds = new HashMap<>();
        private final Set<N> activeNodeNets = new HashSet<>();
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

            for (L node : new HashSet<>(nodeWorld.getAllNodes())) {
                if (node.expired) continue;
                if (!node.hasValidNet() || node.recentlyChanged) {
                    checkNodeConnection(world, node);
                    node.recentlyChanged = false;
                }
            }
        }

        void updateNetworks() {
            Set<N> netsToUpdate = new HashSet<>(activeNodeNets);
            for (N net : netsToUpdate) net.resetTrackers();
            for (N net : netsToUpdate) {
                if (net.isValid()) net.update();
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
        private final Map<BlockPos, L> nodesByPosition = new LinkedHashMap<>();

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

        Set<L> getAllNodes() {
            return new HashSet<>(nodesByPosition.values());
        }
    }
}
