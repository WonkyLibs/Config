package com.wonkglorg.minecraft.config.provider;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class PluginResourceProvider implements ResourceProvider{
	
	private final Plugin plugin;
	private final Path path;
	
	public PluginResourceProvider(Plugin plugin, Path path) {
		this.plugin = plugin;
		this.path = path;
	}
	
	@Override
	public InputStream getResource() {
		return plugin.getResource(path.toString().replace(File.separatorChar, '/'));
	}
}