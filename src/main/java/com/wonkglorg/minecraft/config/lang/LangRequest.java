package com.wonkglorg.minecraft.config.lang;

import com.wonkglorg.minecraft.config.LangManager;
import com.wonkglorg.minecraft.config.lang.parser.bool.BooleanConditionParser;
import com.wonkglorg.minecraft.config.lang.parser.MathParser;
import com.wonkglorg.minecraft.config.types.LangConfig;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Language Value request which can be further modified with additional properties and modifiers.
 */
@SuppressWarnings("unused")
public class LangRequest{
	
	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
	
	private static final Pattern MATH_PATTERN = Pattern.compile("<math>(.*?)</math>");
	
	private final Logger logger;
	/**
	 * The lang manager this request was returned by
	 */
	private final LangManager langManager;
	/**
	 * The locale used for the initial request
	 */
	@Getter
	private final Locale locale;
	/**
	 * The key used for the initial request
	 */
	private final String key;
	/**
	 * The defaultValue used for the initial request
	 */
	private final String defaultValue;
	/**
	 * Map of all replacements to apply to this request
	 */
	private final Map<String, String> replacements = new HashMap<>();
	
	private final Map<String, Component> componentReplacements = new HashMap<>();
	/**
	 * Pattern for component replacers
	 */
	private Pattern pattern;
	
	/**
	 * Last Lang Config used to resolve this request (can change on subsequent calls depending on the Locale requested)
	 */
	private LangConfig lastAccessedConfig;
	/**
	 * Weather or not the initially provided locale should be forced. If false certain methods may re request the message in the given language such as {@link #sendToAudience(Audience)}
	 */
	private boolean forceLocale = false;
	
	public LangRequest(LangManager langManager, Locale locale, String key, String defaultValue) {
		logger = langManager.getLogger();
		this.langManager = langManager;
		this.locale = locale;
		this.key = key;
		this.defaultValue = defaultValue;
	}
	
	public LangRequest forceLocale(boolean forceLocale) {
		this.forceLocale = forceLocale;
		return this;
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, String replacement) {
		if(replacement == null){
			replacement = "null";
		}
		replacements.put(value, replacement);
		return this;
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, boolean replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, char replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, short replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, int replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, long replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, double replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, float replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	public LangRequest replace(String value, Component replacement) {
		if(replacement == null){
			return this;
		}
		//invalidate pattern if one was already generated
		pattern = null;
		componentReplacements.put(value, replacement);
		return this;
	}
	
	/**
	 * @return the raw value found in the config without any modifications
	 */
	public List<String> getRawResult() {
		return getRawResult(locale);
	}
	
	/**
	 * @return the raw value found in the config without any modifications
	 */
	public List<String> getRawResult(Locale locale) {
		LangConfig config;
		var configOptional = langManager.getAnyValidLangConfig(locale);
		if(configOptional.isPresent()){
			config = configOptional.get();
			lastAccessedConfig = config;
		} else {
			logger.log(Level.INFO, "No lang file could be loaded for request: " + key + " using default value!");
			List<String> arrayList = new ArrayList<>();
			arrayList.add(defaultValue);
			return arrayList;
		}
		
		if(!config.contains(key)){
			List<String> arrayList = new ArrayList<>();
			arrayList.add(defaultValue);
			return arrayList;
		}
		
		if(config.isList(key)){
			return config.getStringList(key);
		} else {
			List<String> arrayList = new ArrayList<>();
			arrayList.add(config.getString(key, defaultValue));
			return arrayList;
		}
	}
	
	/**
	 * @return the string or first entry of the list from the config without any modifications
	 */
	public String getRawResultSingleLine() {
		return getRawResultSingleLine(locale);
	}
	
	/**
	 * @return the string or first entry of the list from the config without any modifications
	 */
	public String getRawResultSingleLine(Locale locale) {
		LangConfig config;
		var configOptional = langManager.getAnyValidLangConfig(locale);
		if(configOptional.isPresent()){
			config = configOptional.get();
			lastAccessedConfig = config;
		} else {
			logger.log(Level.INFO, "No lang file could be loaded for request: " + key + " using default value!");
			return defaultValue;
		}
		
		if(!config.contains(key)){
			return defaultValue;
		}
		
		if(config.isList(key)){
			return config.getStringList(key).getFirst();
		} else {
			return config.getString(key, defaultValue);
		}
	}
	
	/**
	 * Resolves any conditionals and placeholders in the input string (does not resolve component replacements)
	 */
	public String processRawResult(String toResolve) {
		toResolve = applyPlaceHolders(toResolve);
		toResolve = applyMath(toResolve);
		return applyConditionals(toResolve);
	}
	
	/**
	 * Resolves any conditionals, math and placeholders in the input (does not resolve component replacements)
	 */
	public List<String> processRawResult(List<String> toResolve) {
		toResolve.replaceAll(this::processRawResult);
		return toResolve;
	}
	
	/**
	 * Resolve any math operations defined in this input
	 *
	 * @param input the input to check
	 * @return any math operations resolved on this input
	 */
	private String applyMath(String input) {
		Matcher matcher = MATH_PATTERN.matcher(input);
		StringBuilder builder = new StringBuilder();
		
		while(matcher.find()){
			String expression = matcher.group(1).trim();
			
			String result;
			try{
				result = formatNumber(evaluateExpression(expression));
			} catch(Exception ex){
				logger.warning("Failed to evaluate math expression: " + expression);
				result = "!INVALID_OPERATION!";
			}
			
			matcher.appendReplacement(builder, Matcher.quoteReplacement(result));
		}
		
		matcher.appendTail(builder);
		return builder.toString();
	}
	
	private double evaluateExpression(String expression) {
		return new MathParser(expression).parse();
	}
	
	private boolean evaluateCondition(String input) {
		return new BooleanConditionParser(input).parse();
	}
	
	/**
	 * Updates the input with all replacements found in the populated replacement map
	 *
	 * @param input to modify
	 * @return modified input
	 */
	private String applyPlaceHolders(String input) {
		if(lastAccessedConfig != null){
			for(var template : lastAccessedConfig.getTemplateMap().entrySet()){
				input = input.replace(template.getKey(), template.getValue());
			}
		}
		
		//per request replacers
		if(!replacements.isEmpty()){
			for(var replacement : replacements.entrySet()){
				input = input.replace(replacement.getKey(), replacement.getValue());
			}
		}
		
		//per lang replacers
		if(lastAccessedConfig != null){
			for(var replacement : lastAccessedConfig.getPlaceholderMap().entrySet()){
				input = input.replace(replacement.getKey(), replacement.getValue());
			}
		}
		
		return input;
	}
	
	/**
	 * Resolved all component placeholders and merges them into one component output
	 *
	 * @param toComponent the function to convert components with
	 * @param value the input value
	 * @return the constructed component
	 */
	private Component applyComponentPlaceholders(Function<String, Component> toComponent, String value) {
		Matcher matcher = pattern.matcher(value);
		
		List<Component> subComponents = new ArrayList<>();
		int last = 0;
		
		while(matcher.find()){
			if(matcher.start() > last){
				subComponents.add(toComponent.apply(value.substring(last, matcher.start())));
			}
			
			String componentKey = matcher.group();
			subComponents.add(componentReplacements.getOrDefault(componentKey, toComponent.apply(componentKey)));
			
			last = matcher.end();
		}
		
		if(last < value.length()){
			subComponents.add(toComponent.apply(value.substring(last)));
		}
		return Component.join(JoinConfiguration.noSeparators(), subComponents);
	}
	
	/**
	 * Resolves any conditionals found in the result
	 *
	 * @param input the raw result to process
	 * @return resolved string
	 */
	private String applyConditionals(String input) {
		int start = input.indexOf("[if:");
		
		if(start == -1){
			return input;
		}
		
		int conditionEnd = input.indexOf("]", start);
		
		if(conditionEnd == -1){
			return input;
		}
		
		String condition = input.substring(start + 4, conditionEnd);
		
		int depth = 1;
		int pos = conditionEnd + 1;
		
		int elsePos = -1;
		int endPos = -1;
		
		while(pos < input.length()){
			
			int nextIf = input.indexOf("[if:", pos);
			int nextElse = input.indexOf("[else]", pos);
			int nextEnd = input.indexOf("[/if]", pos);
			
			int next = minPositive(nextIf, nextElse, nextEnd);
			
			if(next == -1){
				break;
			}
			
			if(next == nextIf){
				depth++;
				pos = nextIf + 4;
			} else if(next == nextEnd){
				depth--;
				
				if(depth == 0){
					endPos = nextEnd;
					break;
				}
				
				pos = nextEnd + 5;
			} else {
				if(depth == 1 && elsePos == -1){
					elsePos = nextElse;
				}
				
				pos = nextElse + 6;
			}
		}
		
		if(endPos == -1){
			return input;
		}
		
		String trueBranch;
		String falseBranch;
		
		if(elsePos == -1){
			trueBranch = input.substring(conditionEnd + 1, endPos);
			falseBranch = "";
		} else {
			trueBranch = input.substring(conditionEnd + 1, elsePos);
			falseBranch = input.substring(elsePos + 6, endPos);
		}
		
		boolean result;
		
		try{
			result = evaluateCondition(condition);
		} catch(Exception ex){
			logger.warning("Failed conditional '" + condition + "': " + ex.getMessage());
			result = false;
		}
		
		String replacement = result ? trueBranch : falseBranch;
		
		replacement = applyConditionals(replacement);
		
		String rebuilt = input.substring(0, start) + replacement + input.substring(endPos + 5);
		
		return applyConditionals(rebuilt);
	}
	
	private static int minPositive(int... values) {
		int min = -1;
		
		for(int value : values){
			if(value == -1){
				continue;
			}
			
			if(min == -1 || value < min){
				min = value;
			}
		}
		
		return min;
	}
	
	/**
	 *
	 * @param toComponent the converter to use
	 * @return the output as a component
	 */
	private List<Component> toComponent(Function<String, Component> toComponent) {
		return toComponent(toComponent, locale);
	}
	
	/**
	 *
	 * @param toComponent the converter to use
	 * @return the output as a component
	 */
	private List<Component> toComponent(Function<String, Component> toComponent, Locale locale) {
		List<Component> components = new ArrayList<>();
		List<String> results = processRawResult(getRawResult(locale));
		if(componentReplacements.isEmpty()){
			for(var resultValue : results){
				components.add(toComponent.apply(resultValue));
			}
			return components;
		}
		
		if(pattern == null){
			pattern = Pattern.compile(componentReplacements.keySet().stream().map(Pattern::quote).collect(Collectors.joining("|")));
		}
		
		for(var resultValue : results){
			components.add(applyComponentPlaceholders(toComponent, resultValue));
		}
		return components;
	}
	
	/**
	 * @param toComponent the component function
	 * @return the first line of the config defined value as a component (this cuts off any other lines also specified in the same key, should be used for lines that only have one value it can have.
	 */
	private Component toSingleComponent(Function<String, Component> toComponent) {
		return toSingleComponent(toComponent, locale);
	}
	
	/**
	 * @param toComponent the component function
	 * @return the first line of the config defined value as a component (this cuts off any other lines also specified in the same key, should be used for lines that only have one value it can have.
	 */
	private Component toSingleComponent(Function<String, Component> toComponent, Locale locale) {
		String rawResult = processRawResult(getRawResultSingleLine(locale));
		if(componentReplacements.isEmpty()){
			return toComponent.apply(rawResult);
		}
		
		if(pattern == null){
			pattern = Pattern.compile(componentReplacements.keySet().stream().map(Pattern::quote).collect(Collectors.joining("|")));
		}
		return applyComponentPlaceholders(toComponent, rawResult);
	}
	
	/**
	 * @return the output as a component
	 */
	
	public List<Component> toComponent() {
		return toComponent(MINI_MESSAGE::deserialize);
	}
	
	/**
	 * @return the first line of the config defined value as a component (this cuts off any other lines also specified in the same key, should be used for lines that only have one value it can have.
	 */
	
	public Component toSingleComponent() {
		return toSingleComponent(MINI_MESSAGE::deserialize);
	}
	
	/**
	 * Sends the request's result to the given audience using the MiniMessage formatting.
	 *
	 * @param audience if the audience is a {@link Player} requests their locale to modify the message with unless {@link #forceLocale} is set to true.
	 */
	
	public void sendToAudience(@NotNull Audience audience) {
		sendToAudience(audience, MINI_MESSAGE::deserialize);
	}
	
	/**
	 * Sends the request's result to the given audience using the MiniMessage formatting.
	 *
	 * @param audience if the audience is a {@link Player} requests their locale to modify the message with unless {@link #forceLocale} is set to true.
	 * @param toComponent the function to use turning the result into a component to send
	 */
	
	public void sendToAudience(@NotNull Audience audience, Function<String, Component> toComponent) {
		if(audience instanceof Player player && !forceLocale){
			toComponent(toComponent, player.locale()).forEach(audience::sendMessage);
		} else {
			toComponent(toComponent).forEach(audience::sendMessage);
		}
	}
	
	/**
	 * Returns the processed result as a string (does not resolve component placeholders nor parse minimessage
	 *
	 * @return the resolved result
	 */
	public List<String> toStringResult() {
		return processRawResult(getRawResult());
	}
	
	/**
	 * Returns the processed result as a single string (does not resolve component placeholders nor parse minimessage
	 *
	 * @return the resolved result
	 */
	public String toSingleStringResult() {
		return processRawResult(getRawResultSingleLine());
	}
	
	private String formatNumber(double value) {
		
		if(value == (long) value){
			return String.valueOf((long) value);
		}
		
		return String.valueOf(value);
	}
}
