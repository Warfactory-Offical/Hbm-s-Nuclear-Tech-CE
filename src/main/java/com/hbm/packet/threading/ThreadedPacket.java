package com.hbm.packet.threading;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * This is the base class for any packets passing through the PacketThreading system. Instances <em>must not</em> be reused.
 */
public abstract class ThreadedPacket implements IMessage {
    private ByteBuf compiledBuffer;

    private void compile0() {
        if (compiledBuffer != null) {
            if (compiledBuffer.refCnt() > 0) compiledBuffer.release();
            compiledBuffer = null;
        }
        ByteBuf newBuf = PooledByteBufAllocator.DEFAULT.directBuffer();
        try {
            this.toBytes(newBuf);
            this.compiledBuffer = newBuf;
        } catch (Throwable t) {
            if (newBuf.refCnt() > 0) newBuf.release();
            this.compiledBuffer = null;
            throw t;
        }
    }

    public synchronized final void releaseBuffer() {
        if (compiledBuffer != null) {
            if (compiledBuffer.refCnt() > 0) {
                compiledBuffer.release();
            }
            compiledBuffer = null;
        }
    }

    /**
     * Returns the compiled buffer.
     */
    public synchronized final ByteBuf getCompiledBuffer() {
        if(compiledBuffer == null)
            this.compile0();
        return compiledBuffer;
    }
}
