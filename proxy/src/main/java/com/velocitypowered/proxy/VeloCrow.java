package com.velocitypowered.proxy;

import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.proxy.server.ServerMap;
import com.velocitypowered.proxy.util.AddressUtil;

public class VeloCrow {
    public VeloCrow(VelocityServer server) {
        server.registerServer(new ServerInfo("TestRegister", AddressUtil.parseAddress("0.0.0.0:33333")));
    }
}
