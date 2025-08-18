package org.kubo.netty.zerocopy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * Netty ByteBuf 层面的“零拷贝”示例：
 * - 直接内存分配（DirectByteBuf）
 * - slice / duplicate / retainedSlice / retainedDuplicate（不复制底层内存）
 * - CompositeByteBuf / wrappedBuffer 组合缓冲（逻辑拼接，避免 copy）
 *
 * 运行：
 *  - IDE 运行 main 方法，观察控制台日志
 */
public class ZeroCopyByteBufDemo {

    public static void main(String[] args) {
        ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;

        // 1) 直接内存分配：写 socket 更高效
        ByteBuf header = allocator.directBuffer(8);
        header.writeBytes(new byte[]{0x01, 0x02, 0x03, 0x04});
        System.out.println("[demo] header.readableBytes=" + header.readableBytes());

        // 2) 包装已有 byte[]（不复制）
        ByteBuf body = Unpooled.wrappedBuffer("HelloNetty".getBytes(StandardCharsets.UTF_8));
        System.out.println("[demo] body.toString=" + body.toString(StandardCharsets.UTF_8));

        // 3) 组合缓冲（不复制底层内存）
        CompositeByteBuf composite = allocator.compositeBuffer();
        composite.addComponents(true, header.retainedSlice(), body.retainedDuplicate());
        System.out.println("[demo] composite.readableBytes=" + composite.readableBytes());

        // 4) 切片（不复制，基于同一块内存创建只读视图）
        ByteBuf helloSlice = composite.slice(4, 5);
        System.out.println("[demo] helloSlice=" + helloSlice.toString(StandardCharsets.UTF_8));

        // 5) duplicate（不复制，读写指针独立）
        ByteBuf dup = body.duplicate();
        System.out.println("[demo] dup=" + dup.toString(StandardCharsets.UTF_8));

        // 6) 通过 wrappedBuffer 无拷贝拼接
        ByteBuf wrapped = Unpooled.wrappedBuffer(header.retainedSlice(), body.retainedDuplicate());
        System.out.println("[demo] wrapped.readableBytes=" + wrapped.readableBytes());

        // 7) 引用计数打印（调试内存泄漏时很有用）
        System.out.println("[ref] header=" + header.refCnt() + " body=" + body.refCnt() +
                " composite=" + composite.refCnt() + " helloSlice=" + helloSlice.refCnt() + " wrapped=" + wrapped.refCnt());

        // 释放：注意 retainedXxx 带来的 +1 引用
        composite.release();
        wrapped.release();
        header.release();
        body.release();

        System.out.println("[done] ZeroCopyByteBufDemo finished.");
    }
}


