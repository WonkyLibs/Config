package com.wonkglorg.minecraft.config.types;

import com.wonkglorg.minecraft.config.provider.ResourceProvider;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class LangConfig extends Config{
	
	@Getter
	@Setter
	private String placeholderPath = "placeholders";
	/**
	 * surrounding character for placeholders
	 */
	@Setter
	@Getter
	private char placeholderChar = '%';
	
	/**
	 * Path to the templates definitions in the lang file, all keys defined under this path will be added to the automatic replacer map
	 * (default: "placeholders")
	 *
	 * <p>Example:
	 * <pre>
	 *     templates:
	 *  		currency-display: "<aqua>[if:isFree]Free[else]%price% %currency%![/if]"
	 * </pre>
	 * This structure gets resolved before and placeholders get replaced allowing for templates of text to be inserted before processing
	 */
	@Setter
	@Getter
	private String templatePath = "templates";
	/**
	 * surrounding character for templates
	 */
	@Setter
	@Getter
	private char templateChar = '%';
	/**
	 * Update request used when the replacer map needs to be updated
	 */
	@Setter
	@Getter
	private boolean updateRequest = false;
	/**
	 * Map of placeholders and their values to replace them by
	 */
	private final Map<String, String> replacerMap = new ConcurrentHashMap<>();
	/**
	 * Map of templates and their values to replace them by
	 */
	private final Map<String, String> templateMap = new ConcurrentHashMap<>();
	
	/**
	 * Creates a new file at the specified location or copies an existing one from the resource folder based on the sourcePath,
	 * if nothing could be found in the sourcePath it creates a new file at the destination. DestinationPath starts relative to execution path.
	 *
	 * @param path path to copy this file to
	 * @param resourceProvider how to access the provided resource to copy from
	 */
	public LangConfig(@NotNull Path path, @NotNull ResourceProvider resourceProvider) {
		super(path, resourceProvider);
	}
	
	/**
	 * Loads a file from the specified location inside the plugins data folder, or tries loading it from the plugins jar from the same position if one exists otherwise create a new file
	 *
	 * @param path path of the file to load
	 */
	public LangConfig(@NotNull Plugin plugin, @NotNull Path path) {
		super(plugin, path);
	}
	
	/**
	 * Loads a file from the specified location (creates one if non exists)
	 *
	 * @param path path of the file to load
	 */
	public LangConfig(@NotNull Path path) {
		super(path);
	}
	
	@Override
	public void load() {
		setUpdateRequest(true);
		try{
			checkFile();
			load(file);
			logger.log(Level.INFO, "Loaded data from " + path + "!");
		} catch(InvalidConfigurationException | IOException e){
			logger.log(Level.WARNING, "Error loading data from " + path + "!");
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	@Override
	public void silentLoad() {
		setUpdateRequest(true);
		try{
			checkFile();
			load(file);
		} catch(InvalidConfigurationException | IOException e){
			logger.log(Level.WARNING, "Error loading data from " + path + "!");
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public void updateEntries() {
		if(this.isSet(this.getPlaceholderPath())){
			String path = this.getPlaceholderPath();
			for(Map.Entry<String, Object> entry : getEntries(path).entrySet()){
				String placeholderValue = entry.getValue().toString();
				String searchKey = placeholderChar + entry.getKey() + placeholderChar;
				replacerMap.put(searchKey, placeholderValue);
			}
		}
		
		if(this.isSet(this.getTemplatePath())){
			String path = this.getTemplatePath();
			for(Map.Entry<String, Object> entry : getEntries(path).entrySet()){
				String placeholderValue = entry.getValue().toString();
				String searchKey = templateChar + entry.getKey() + templateChar;
				templateMap.put(searchKey, placeholderValue);
			}
		}
		
		setUpdateRequest(false);
	}
	
	/**
	 * @return the replacer map of all keys to be replaced and their values
	 */
	public Map<String, String> getPlaceholderMap() {
		if(isUpdateRequest()){
			updateEntries();
		}
		return replacerMap;
	}
	
	public Map<String, String> getTemplateMap() {
		if(isUpdateRequest()){
			updateEntries();
		}
		return templateMap;
	}
	
}