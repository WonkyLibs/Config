package com.wonkglorg.minecraft.config.lang.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Parses and resolves conditional expressions in language strings.
 *
 * <p>Supported syntax:</p>
 *
 * <pre>
 * [if:condition]content[/if]
 * [if:condition]content[else]content[/if]
 * [if:condition]content[else-if:condition]content[/if]
 * [if:condition]content[else-if:condition]content[else]content[/if]
 * </pre>
 *
 * <p>Nested conditionals are supported.</p>
 */
public final class ConditionalParser{
	
	private static final String IF_START = "[if:";
	private static final String ELSE_IF = "[else-if:";
	private static final String ELSE = "[else]";
	private static final String IF_END = "[/if]";
	
	private final Function<String, Boolean> conditionEvaluator;
	
	public ConditionalParser(Function<String, Boolean> conditionEvaluator) {
		this.conditionEvaluator = conditionEvaluator;
	}
	
	/**
	 * Resolves all conditionals found in the input.
	 *
	 * @param input input containing conditionals
	 * @return input with conditionals resolved
	 */
	public String parse(String input) {
		
		int start = input.indexOf(IF_START);
		
		if(start == -1){
			return input;
		}
		
		Conditional conditional = parseConditional(input, start);
		
		if(conditional == null){
			return input;
		}
		
		String replacement = evaluate(conditional);
		
		// The selected branch may itself contain conditionals.
		replacement = parse(replacement);
		
		String result = input.substring(0, start) + replacement + input.substring(conditional.end() + IF_END.length());
		
		// Resolve any additional conditionals after this one.
		return parse(result);
	}
	
	private Conditional parseConditional(String input, int start) {
		
		int conditionEnd = input.indexOf(']', start);
		
		if(conditionEnd == -1){
			return null;
		}
		
		List<ConditionalBranch> branches = new ArrayList<>();
		
		String condition = input.substring(start + IF_START.length(), conditionEnd);
		
		int contentStart = conditionEnd + 1;
		int position = contentStart;
		
		int depth = 1;
		
		while(position < input.length()){
			
			int nextIf = input.indexOf(IF_START, position);
			int nextElseIf = input.indexOf(ELSE_IF, position);
			int nextElse = input.indexOf(ELSE, position);
			int nextEnd = input.indexOf(IF_END, position);
			
			int next = minPositive(nextIf, nextElseIf, nextElse, nextEnd);
			
			if(next == -1){
				return null;
			}
			
			// Nested conditional.
			if(next == nextIf){
				depth++;
				position = next + IF_START.length();
				continue;
			}
			
			// Closing conditional.
			if(next == nextEnd){
				
				depth--;
				
				if(depth == 0){
					
					branches.add(new ConditionalBranch(condition, input.substring(contentStart, next)));
					
					return new Conditional(branches, "", next);
				}
				
				position = next + IF_END.length();
				continue;
			}
			
			// else / else-if belonging to a nested conditional.
			if(depth != 1){
				position = next + 1;
				continue;
			}
			
			// [else-if:condition]
			if(next == nextElseIf){
				
				branches.add(new ConditionalBranch(condition, input.substring(contentStart, next)));
				
				int newConditionEnd = input.indexOf(']', next);
				
				if(newConditionEnd == -1){
					return null;
				}
				
				condition = input.substring(next + ELSE_IF.length(), newConditionEnd);
				
				contentStart = newConditionEnd + 1;
				position = contentStart;
				
				continue;
			}
			
			// [else]
			if(next == nextElse){
				
				branches.add(new ConditionalBranch(condition, input.substring(contentStart, next)));
				
				String elseContent = parseElseContent(input, next + ELSE.length());
				
				if(elseContent == null){
					return null;
				}
				
				int end = findEndOfConditional(input, next + ELSE.length());
				
				return new Conditional(branches, elseContent, end);
			}
			
			position = next + 1;
		}
		
		return null;
	}
	
	private String parseElseContent(String input, int start) {
		
		int end = findEndOfConditional(input, start);
		
		if(end == -1){
			return null;
		}
		
		return input.substring(start, end);
	}
	
	private int findEndOfConditional(String input, int start) {
		
		int depth = 1;
		int position = start;
		
		while(position < input.length()){
			
			int nextIf = input.indexOf(IF_START, position);
			int nextEnd = input.indexOf(IF_END, position);
			
			int next = minPositive(nextIf, nextEnd);
			
			if(next == -1){
				return -1;
			}
			
			if(next == nextIf){
				depth++;
				position = next + IF_START.length();
				continue;
			}
			
			depth--;
			
			if(depth == 0){
				return next;
			}
			
			position = next + IF_END.length();
		}
		
		return -1;
	}
	
	private String evaluate(Conditional conditional) {
		
		for(ConditionalBranch branch : conditional.branches()){
			
			if(conditionEvaluator.apply(branch.condition())){
				return branch.content();
			}
		}
		
		return conditional.elseContent();
	}
	
	private int minPositive(int... values) {
		
		int result = -1;
		
		for(int value : values){
			
			if(value < 0){
				continue;
			}
			
			if(result == -1 || value < result){
				result = value;
			}
		}
		
		return result;
	}
	
	private record Conditional(List<ConditionalBranch> branches, String elseContent, int end){}
	
	private record ConditionalBranch(String condition, String content){}
}