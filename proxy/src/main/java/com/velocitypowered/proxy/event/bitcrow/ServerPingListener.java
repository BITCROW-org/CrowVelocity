package com.velocitypowered.proxy.event.bitcrow;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.command.bitcrow.WartungCMD;
import com.velocitypowered.proxy.util.UtilsManager;
import com.velocitypowered.proxy.util.gradient.GradientComponentFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ServerPingListener {
    private final VelocityServer server;
    public ServerPingListener(VelocityServer server) {
        this.server = server;
    }
    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        String raw1 = server.getConfigCfg().getString("motd", "");
        String raw2 = server.getConfigCfg().getBoolean("wartung", false)
                ? server.getConfigCfg().getString("motdWartung", "")
                : server.getConfigCfg().getString("motd2", "");

        String sp = UtilsManager.MiniFontConvert(raw1);
        String motd = UtilsManager.MiniFontConvert(raw2);

        Component line1 = GradientComponentFormatter.applyGradient(sp);
        Component line2 = GradientComponentFormatter.applyGradient(motd);

        Component c1 = UtilsManager.centerComponent(line1);
        Component c2 = UtilsManager.centerComponent(line2);

        Component test = Component.empty()
                .append(c1)
                .append(Component.newline())
                .append(c2);

        ServerPing.Builder builder = event.getPing().asBuilder();
        builder.description(test);

        event.setPing(builder.build());
    }

    @Subscribe
    public void onJoin(PostLoginEvent event) {
        if (!event.getPlayer().hasPermission("bitcrow.wartung.join")) {
            if (WartungCMD.isMaintenanceMode(server)) {
                event.getPlayer().disconnect(
                        Component.text()
                                .append(Component.text("Der ", TextColor.fromHexString("#AAAAAA")))
                                .append(Component.text("Wartungsmodus", TextColor.fromHexString("#FF0000")))
                                .append(Component.text(" ist aktuell aktiv!", TextColor.fromHexString("#AAAAAA")))
                                .appendNewline()
                                .append(Component.text("Wir bitten um euer Verständnis!", TextColor.fromHexString("#AAAAAA")))
                                .appendNewline()
                                .append(Component.text("Danke für eure Geduld!", TextColor.fromHexString("#00FF00"))
                                        .decoration(TextDecoration.ITALIC, false))
                                .build()
                );
            }
        }
    }
}