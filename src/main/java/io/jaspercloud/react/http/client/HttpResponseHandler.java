package io.jaspercloud.react.http.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

@ChannelHandler.Sharable
public abstract class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

}
