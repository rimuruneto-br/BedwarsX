package dev.bedwarsx.listener;

import dev.bedwarsx.main.BedWarsX;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener implements Listener {

    private final BedWarsX plugin;

    public WeatherListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event) {
        // Cancel weather changes in arena worlds
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }
}
