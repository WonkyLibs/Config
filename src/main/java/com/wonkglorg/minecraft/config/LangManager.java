package com.wonkglorg.minecraft.config;

import com.wonkglorg.minecraft.config.lang.LangRequest;
import com.wonkglorg.minecraft.config.mapping.MappingConfig;
import com.wonkglorg.minecraft.config.provider.PluginResourceProvider;
import com.wonkglorg.minecraft.config.types.Config;
import com.wonkglorg.minecraft.config.types.LangConfig;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Lang manager to handle retrieving and sending per user translatable messages.
 *
 * @author Wonkglorg
 */
@SuppressWarnings("unused")
public final class LangManager{
	/**
	 * All Lang Managers and their plugin registered namespace
	 */
	private static final Map<String, LangManager> LANG_MANAGER_MAP = new ConcurrentHashMap<>();
	
	public static final String NO_LOCALE_FOUND_FOR_FILE = "No locale found for file: ";
	/**
	 * The Logger instance
	 */
	@Getter
	private final Logger logger;
	
	/**
	 * Config reference storing the mappings and settings of the lang manager
	 */
	private final MappingConfig mappingConfig;
	/**
	 * The lang map which contains all the language configs
	 */
	private final Map<Locale, LangConfig> langMap = new ConcurrentHashMap<>();
	/**
	 * The default language
	 */
	private Locale defaultLang = Locale.ENGLISH;
	
	/**
	 * Creates a new instance of the LangManager or returns the already registered one for this plugin
	 *
	 * @param plugin the plugin to create the instance for
	 * @return the created instance
	 */
	public static LangManager getInstance(@NotNull JavaPlugin plugin) {
		if(!LANG_MANAGER_MAP.containsKey(plugin.namespace())){
			LANG_MANAGER_MAP.put(plugin.namespace(), new LangManager(plugin.getLogger(), new MappingConfig(plugin)));
		}
		return LANG_MANAGER_MAP.get(plugin.namespace());
	}
	
	/**
	 * Creates a new instance of the LangManager or returns the already registered one for this plugin
	 *
	 * @param namespace the plugin namespace to create the instance for
	 * @return the created instance
	 */
	public static LangManager getInstance(@NotNull String namespace, @NotNull MappingConfig mappingConfig) {
		LANG_MANAGER_MAP.computeIfAbsent(namespace.toLowerCase(), k -> new LangManager(mappingConfig));
		return LANG_MANAGER_MAP.get(namespace);
	}
	
	public static boolean hasInstance(@NotNull JavaPlugin plugin) {
		return LANG_MANAGER_MAP.containsKey(plugin.namespace().toLowerCase());
	}
	
	public LangManager(@NotNull MappingConfig mappingConfig) {
		logger = Logger.getLogger(LangManager.class.getName());
		this.mappingConfig = mappingConfig;
		loadMappings();
	}
	
	public LangManager(@NotNull Logger logger, @NotNull MappingConfig mappingConfig) {
		this.logger = logger;
		this.mappingConfig = mappingConfig;
		loadMappings();
	}
	
	/**
	 * Adds a language to the lang manager definition
	 *
	 * @param locale 1 or more locale this config should apply to
	 * @param extraLocale other locales to also register this under
	 * @param languageConfig the language config
	 */
	private synchronized void addLanguage(LangConfig languageConfig, Locale locale, Locale... extraLocale) {
		langMap.putIfAbsent(locale, languageConfig);
		for(Locale loc : extraLocale){
			langMap.putIfAbsent(loc, languageConfig);
		}
	}
	
	/**
	 * Adds a language to the lang manager definition
	 *
	 * @param locale 1 or more locale this config should apply to
	 * @param languageConfig the language config
	 */
	private synchronized void addLanguage(LangConfig languageConfig, List<Locale> locale) {
		for(Locale loc : locale){
			langMap.putIfAbsent(loc, languageConfig);
		}
	}
	
	/**
	 * Saves all the language files
	 */
	public synchronized void save() {
		langMap.values().forEach(Config::save);
	}
	
	public synchronized void silentSave() {
		langMap.values().forEach(Config::silentSave);
	}
	
	public synchronized void load() {
		loadMappings();
		langMap.values().forEach(Config::load);
	}
	
	public synchronized void silentLoad() {
		loadMappings();
		langMap.values().forEach(Config::silentLoad);
		
		if(defaultLang == null){
			logger.log(Level.WARNING, "No default language selected!");
		}
	}
	
	/**
	 * Loads the mappings from the config
	 */
	private synchronized void loadMappings() {
		defaultLang = mappingConfig.getDefaultLocale(Locale.ENGLISH);
		langMap.clear();
		
		for(var entry : mappingConfig.getPaths()){
			LangConfig config;
			if(entry.plugin() != null){
				config = new LangConfig(entry.outputPath(),new PluginResourceProvider(entry.plugin(),entry.resourcePath()));
			} else {
				config = new LangConfig(entry.outputPath(),
						() -> this.getClass().getClassLoader().getResourceAsStream(entry.resourcePath().toString().replace(File.separatorChar, '/')));
			}
			
			config.silentLoad();
			addLanguage(config, entry.locales());
		}
	}
	
	public synchronized LangConfig getDefaultLang() {
		try{
			return langMap.get(defaultLang);
		} catch(Exception e){
			return null;
		}
	}
	
	/**
	 * Requests a value from the lang file.
	 *
	 * @param key the key to look up
	 * @return a {@link LangRequest} object
	 */
	public LangRequest request(final String key) {
		return new LangRequest(this, null, key, key);
	}
	
	/**
	 * Requests a value from the lang file.
	 *
	 * @param key the key to look up
	 * @param defaultValue a default value to return if no value was found for the given key
	 * @return a {@link LangRequest} object
	 */
	public LangRequest request(final String key, final String defaultValue) {
		return new LangRequest(this, null, key, defaultValue);
	}
	
	/**
	 * Requests a value from the lang file.
	 *
	 * @param locale the locale to use (falls back to the default if not available)
	 * @param key the key to look up
	 * @return a {@link LangRequest} object
	 */
	public LangRequest request(final Locale locale, final String key) {
		return new LangRequest(this, locale, key, key);
	}
	
	/**
	 * Requests a value from the lang file.
	 *
	 * @param locale the locale to use (falls back to the default if not available)
	 * @param key the key to look up
	 * @param defaultValue a default value to return if no value was found for the given key
	 * @return a {@link LangRequest} object
	 */
	public LangRequest request(final Locale locale, final String key, final String defaultValue) {
		return new LangRequest(this, locale, key, defaultValue);
	}
	
	/**
	 * Gets any valid language config to use (first checks if the locale is present, then the default locale, then any locale)
	 *
	 * @param locale the locale to get the language config for
	 * @return the language config or empty if none could be found
	 */
	public Optional<LangConfig> getAnyValidLangConfig(final Locale locale) {
		if(langMap.isEmpty()){
			return Optional.empty();
		}
		
		if(locale != null && langMap.containsKey(locale)){
			return Optional.of(langMap.get(locale));
		}
		
		if(langMap.containsKey(defaultLang)){
			return Optional.of(langMap.get(defaultLang));
		}
		return Optional.of(langMap.values().iterator().next());
	}
	
	/**
	 * Gets all stored languages
	 *
	 * @return the map of all languages
	 */
	public synchronized Map<Locale, LangConfig> getAllLangs() {
		return langMap;
	}
	
}