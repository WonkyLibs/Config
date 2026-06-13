package com.wonkglorg.utilitylib.config.mapping;

import com.wonkglorg.utilitylib.config.provider.ResourceProvider;
import com.wonkglorg.utilitylib.config.types.Config;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unused")
public class MappingConfig extends Config{
	/**
	 * Where the file to configure the lang manager is located
	 */
	private static final Path CONFIG_MAPPINGS_PATH = Path.of("utility-lib", "config", "mappings.yml");
	private final Plugin plugin;
	
	public MappingConfig(@NotNull Path path, @NotNull ResourceProvider resourceProvider) {
		super(path, resourceProvider);
		this.plugin = null;
	}
	
	public MappingConfig(@NotNull Plugin plugin, @NotNull Path path) {
		super(plugin, path);
		this.plugin = plugin;
	}
	
	public MappingConfig(@NotNull Path path) {
		super(path);
		this.plugin = null;
	}
	
	public MappingConfig(@NotNull Plugin plugin) {
		super(plugin, CONFIG_MAPPINGS_PATH);
		this.plugin = plugin;
	}
	
	/**
	 * @return the found locale or default
	 */
	public Locale getDefaultLocale(Locale defaultValue) {
		try{
			String string = getString("lang.default-lang");
			if(string == null){
				return defaultValue;
			}
			return Locale.of(string);
		} catch(Exception e){
			return defaultValue;
		}
	}
	
	/**
	 * @return all register config paths
	 */
	public List<LangMapping> getPaths() {
		List<Map<?, ?>> files = getMapList("lang.files");
		
		List<LangMapping> result = new ArrayList<>();
		
		for(Map<?, ?> file : files){
			
			if(!file.containsKey("path")){
				continue;
			}
			
			String pathString = String.valueOf(file.get("path"));
			
			Path outputPath = Path.of(plugin == null ? pathString : pathString.replace("%plugin-dir%", plugin.getDataPath().toString()));
			
			List<Locale> locales = new ArrayList<>();
			
			Object localesObj = file.get("locales");
			
			if(localesObj instanceof List<?> rawLocales){
				for(Object locale : rawLocales){
					try{
						locales.add(Locale.of(locale.toString()));
					} catch(Exception ignored){
					}
				}
			}
			
			String resource = pathString.replace("%plugin-dir%", "").replaceFirst("^[/\\\\]+", "");
			
			Path resourcePath = Path.of(resource);
			
			result.add(new LangMapping(plugin, resourcePath, outputPath, locales));
		}
		
		return result;
	}
	
	public record LangMapping(Plugin plugin, Path resourcePath, Path outputPath, List<Locale> locales){}
	
}