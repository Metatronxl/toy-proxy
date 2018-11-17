package com.xulei.toyproxy.proxy;

import java.net.InetAddress;

import com.xulei.toyproxy.config.Config;
import com.xulei.toyproxy.encryption.CryptFactory;
import com.xulei.toyproxy.encryption.CryptUtil;
import com.xulei.toyproxy.encryption.ICrypt;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.socks.SocksAddressType;

/**
 * 4次握手中的connect阶段，接受shadowsocks-netty发送给shadowsocks-netty-server的消息
 * 
 * 具体数据的读取可以参考类：SocksCmdRequest
 *
 *
 */
public class HostHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(HostHandler.class);
	private final static String LOG_PRE = "Enter into [HostHandler]";

	private ICrypt _crypt;

	public HostHandler(Config config) {
		this._crypt = CryptFactory.get(config.get_method(), config.get_password());
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

	    logger.info(LOG_PRE+"channel is active");
        logger.debug("监听连接的IPconnect ip:   "+ctx.channel().remoteAddress().toString()+"    connect success" );

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		logger.error("HostHandler error", cause);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
		logger.info("HostHandler channelInactive close");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buff = (ByteBuf) msg;

		if (buff.readableBytes() <= 0) {
			return;
		}
//		ByteBuf dataBuff = Unpooled.wrappedBuffer(CryptUtil.decrypt(_crypt, msg));//这样做可以减少一次拷贝操作
		ByteBuf dataBuff = Unpooled.buffer();
		dataBuff.writeBytes(CryptUtil.decrypt(_crypt, msg)); //解密模块
		if (dataBuff.readableBytes() < 2) {
			return;
		}
		String host = null;
		int port = 0;
		int addressType = dataBuff.getUnsignedByte(0);
		if (addressType == SocksAddressType.IPv4.byteValue()) {
			if (dataBuff.readableBytes() < 7) {
				return;
			}
			dataBuff.readUnsignedByte();
			byte[] ipBytes = new byte[4];
			dataBuff.readBytes(ipBytes);
			host = InetAddress.getByAddress(ipBytes).toString().substring(1);
			port = dataBuff.readShort();
		} else if (addressType == SocksAddressType.DOMAIN.byteValue()) {
			int hostLength = dataBuff.getUnsignedByte(1);
			if (dataBuff.readableBytes() < hostLength + 4) {
				return;
			}
			dataBuff.readUnsignedByte();
			dataBuff.readUnsignedByte();
			byte[] hostBytes = new byte[hostLength];
			dataBuff.readBytes(hostBytes);
			host = new String(hostBytes);
			port = dataBuff.readShort();
            logger.info("connect ip:   "+ctx.channel().remoteAddress().toString()+"    connect success" );
		}else if (addressType == SocksAddressType.IPv6.byteValue()){
			if (dataBuff.readableBytes() < 16) {
				return;
			}
            dataBuff.readUnsignedByte();
            byte[] ipBytes = new byte[16];
            dataBuff.readBytes(ipBytes);
            host = InetAddress.getByAddress(ipBytes).toString().substring(1);
            port = dataBuff.readShort();
            logger.info("IPV6 Test=== "+"addressType"+ ",host = " + host + ",port = " + port + ",dataBuff = "
                    + dataBuff.readableBytes());
		}else {
			throw new IllegalStateException("unknown address type: " + addressType);
		}

		logger.info("addressType = " + addressType + ",host = " + host + ",port = " + port + ",dataBuff = "
				+ dataBuff.readableBytes());
		ctx.channel().pipeline().addLast(new ClientProxyHandler(host, port, ctx, dataBuff, _crypt));
		ctx.channel().pipeline().remove(this);

		//释放资源
//		ReferenceCountUtil.release(dataBuff);

	}
}