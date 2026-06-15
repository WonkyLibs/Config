package com.wonkglorg.minecraft.config.types;

import com.wonkglorg.minecraft.config.provider.PluginResourceProvider;
import com.wonkglorg.minecraft.config.provider.ResourceProvider;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Wonkglorg
 */
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class Config extends YamlConfiguration{
	private static final ResourceProvider DUMMY_PROVIDER = () -> null;
	protected final Path path;
	protected final File file;
	protected final @NotNull ResourceProvider resourceProvider;
	@Setter
	protected Logger logger = Logger.getLogger(Config.class.getName());
	
	/**
	 * Creates a new file at the specified location or copies an existing one from the resource folder based on the sourcePath,
	 * if nothing could be found in the sourcePath it creates a new file at the destination. DestinationPath starts relative to execution path.
	 *
	 * @param path path to copy this file to
	 * @param resourceProvider how to access the provided resource to copy from
	 */
	public Config(@NotNull Path path, @NotNull ResourceProvider resourceProvider) {
		this.path = path;
		this.resourceProvider = resourceProvider;
		file = new File(this.path.toString());
		silentLoad();
		syncWithDefaults();
	}
	
	/**
	 * Loads a file from the specified location inside the plugins data folder, or tries loading it from the plugins jar from the same position if one exists otherwise create a new file
	 *
	 * @param path path of the file to load
	 */
	public Config(@NotNull Plugin plugin, @NotNull Path path) {
		this(plugin.getDataPath().resolve(path), new PluginResourceProvider(plugin, path));
	}
	
	/**
	 * Loads a file from the specified location (creates one if non exists)
	 *
	 * @param path path of the file to load
	 */
	public Config(@NotNull Path path) {
		this(path, DUMMY_PROVIDER);
	}
	
	/**
	 * Gets a section of the config at the set path.
	 *
	 * @param path path inside yml config.
	 * @param deep deep search to get children of children
	 * @return {@link Set} of results.
	 */
	public Set<String> getKeys(String path, boolean deep) {
		if(path == null || path.isBlank()){
			return getKeys(deep);
		}
		ConfigurationSection section = getConfigurationSection(path);
		if(section != null){
			return section.getKeys(deep);
		}
		return new HashSet<>();
	}
	
	/**
	 * gets a section of the config at the set path with a value to automatically cast to
	 *
	 * @param path path inside yml config if blank, uses the root of the config
	 * @param <T> type of the map
	 * @return {@link Map} of results.
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> getEntries(String path) {
		if(path == null || path.isBlank()){
			return (Map<String, T>) getValues(false);
		}
		ConfigurationSection section = getConfigurationSection(path);
		if(section != null){
			return (Map<String, T>) section.getValues(false);
		}
		return Map.of();
	}
	
	public @Nullable String getParentPath(@NotNull String path) {
		ConfigurationSection currentSection = getConfigurationSection(path);
		if(currentSection == null){
			return null;
		}
		
		ConfigurationSection configurationSection = currentSection.getParent();
		if(configurationSection == null){
			return null;
		}
		
		return configurationSection.getCurrentPath();
	}
	
	public void setItemStack(String path, ItemStack itemStack) {
		set(path, itemStack.serialize());
	}
	
	public void setLocation(String path, Location location) {
		set(path, location.serialize());
	}
	
	/**
	 * Add all values from the default config to the existing one if not present
	 */
	public void syncWithDefaults() {
		InputStream resource = resourceProvider.getResource();
		if(resource == null){
			return;
		}
		
		YamlConfiguration defConfig = new YamlConfiguration();
		try{
			defConfig.loadFromString(new String(resource.readAllBytes()));
		} catch(IOException | InvalidConfigurationException e){
			logger.log(Level.SEVERE, "Failed to load default config for " + path, e);
			return;
		}
		
		boolean changed = false;
		
		for(Map.Entry<String, Object> entry : defConfig.getValues(true).entrySet()){
			if(!this.contains(entry.getKey())){
				this.set(entry.getKey(), entry.getValue());
				changed = true;
			}
		}
		
		if(changed){
			silentSave();
			logger.log(Level.INFO, "Updated config " + path + " with missing default values.");
		}
	}
	
	public void load() {
		try{
			checkFile();
			load(file);
			logger.log(Level.INFO, "Loaded data from " + path + "!");
		} catch(InvalidConfigurationException | IOException e){
			logger.log(Level.SEVERE, e.getMessage());
			logger.log(Level.WARNING, "Error loading data from " + path + "!");
		}
	}
	
	public void silentLoad() {
		try{
			checkFile();
			load(file);
		} catch(InvalidConfigurationException | IOException e){
			logger.log(Level.SEVERE, e.getMessage());
			logger.log(Level.WARNING, "Error loading data from " + path + "!");
		}
	}
	
	public void save() {
		try{
			checkFile();
			save(file);
			logger.log(Level.INFO, "Saved data to " + path + "!");
		} catch(IOException e){
			logger.log(Level.SEVERE, e.getMessage());
			logger.log(Level.WARNING, "Error saving data to " + path + "!");
		}
	}
	
	public void silentSave() {
		try{
			checkFile();
			save(file);
		} catch(IOException e){
			logger.log(Level.WARNING, "Error saving data to " + path + "!");
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	/**
	 * Checks if file exists in path, else create the file and all parent directories needed.
	 */
	protected void checkFile() throws NoSuchFileException {
		if(!file.exists()){
			if(path == null){
				throw new NoSuchFileException("No Destination path has been provided!!");
			}
			
			file.getParentFile().mkdirs();
			
			InputStream resource = resourceProvider.getResource();
			if(resource != null){
				try{
					Files.copy(resource, path);
				} catch(IOException e){
					logger.log(Level.SEVERE, "Error Copying data from " + resourceProvider + " to destination " + path);
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			} else {
				try{
					file.createNewFile();
				} catch(IOException e){
					throw new IllegalStateException("Cannot create file at " + path + "!", e);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("ConfigYML[path=%s]", path);
	}
	
}