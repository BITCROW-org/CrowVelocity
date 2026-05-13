package com.velocitypowered.proxy.event.bitcrow;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.plugin.virtual.VelocityVirtualPlugin;
import net.labymod.serverapi.core.LabyModProtocol;
import net.labymod.serverapi.core.model.feature.Feature;
import net.labymod.serverapi.core.packet.clientbound.game.feature.UpdateFeaturePacket;
import net.labymod.serverapi.server.velocity.LabyModProtocolService;
import net.labymod.serverapi.server.velocity.event.LabyModPlayerJoinEvent;

public class LabyListener {
    private final VelocityServer server;
    public LabyListener(VelocityServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        LabyModProtocolService.initialize(VelocityVirtualPlugin.INSTANCE, server, VelocityServer.getLabyLogger());
    }

    @Subscribe
    public void onProxyPing(LabyModPlayerJoinEvent event) {
        LabyModProtocol labyModProtocol = LabyModProtocolService.get().labyModProtocol();

        labyModProtocol.sendPacket(event.labyModPlayer().getUniqueId(), new UpdateFeaturePacket(Feature.FANCY_FONT.disable()));
    }
}
