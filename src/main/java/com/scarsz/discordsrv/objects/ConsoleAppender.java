package com.scarsz.discordsrv.objects;

import com.scarsz.discordsrv.DiscordSRV;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.bukkit.ChatColor;

import java.util.Date;

@SuppressWarnings("unchecked")
@Plugin(name = "DiscordSRV-ConsoleChannel", category = "Core", elementType = "appender", printObject = true)
public class ConsoleAppender extends AbstractAppender {

    public ConsoleAppender() {
        super("DiscordSRV-ConsoleChannel", null, PatternLayout.createLayout("[%d{HH:mm:ss} %level]: %msg", null, null, null, null), false);
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void append(LogEvent e) {
        // return if console channel isn't available
        if (DiscordSRV.consoleChannel == null) return;

        // return if this is not an okay level to send
        boolean isAnOkayLevel = false;
        for (String consoleLevel : DiscordSRV.plugin.getConfig().getStringList("DiscordConsoleChannelLevels")) if (consoleLevel.toLowerCase().equals(e.getLevel().name().toLowerCase())) isAnOkayLevel = true;
        if (!isAnOkayLevel) return;

        String line = e.getMessage().getFormattedMessage();

        // do nothing if line is blank before parsing
        if (!lineIsOk(line)) return;

        // apply regex to line
        line = applyRegex(line);

        // do nothing if line is blank after parsing
        if (!lineIsOk(line)) return;

        // don't send if it's DiscordSRV's colors init message
        if (line.startsWith("[DiscordSRV] Colors:")) return;

        // apply formatting
        line = DiscordSRV.plugin.getConfig().getString("DiscordConsoleChannelFormat")
                .replace("%date%", new Date().toString())
                .replace("%level%", e.getLevel().name().toUpperCase())
                .replace("%line%", line)
        ;

        // if line contains a blocked phrase don't send it
        boolean doNotSendActsAsWhitelist = DiscordSRV.plugin.getConfig().getBoolean("DiscordConsoleChannelDoNotSendPhrasesActsAsWhitelist");
        for (String phrase : DiscordSRV.plugin.getConfig().getStringList("DiscordConsoleChannelDoNotSendPhrases"))
            if (line.contains(phrase) == !doNotSendActsAsWhitelist) return;

        // remove coloring shit
        line = ChatColor.stripColor(line)
                .replaceAll("[&§][0-9a-fklmnor]", "") // removing &'s with addition of non-caught §'s if they get through somehow
                .replaceAll("\\[[0-9]{1,2};[0-9]{1,2};[0-9]{1,2}m", "")
                .replaceAll("\\[[0-9]{1,3}m", "")
                .replace("[m", "");

        DiscordSRV.messageQueue.add(line);
    }

    private boolean lineIsOk(String input) {
        return input != null && !input.replace(" ", "").replace("\n", "").isEmpty();
    }
    private String applyRegex(String input) {
        return input.replaceAll(DiscordSRV.plugin.getConfig().getString("DiscordConsoleChannelRegexFilter"), DiscordSRV.plugin.getConfig().getString("DiscordConsoleChannelRegexReplacement"));
    }

}