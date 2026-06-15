package com.wonkglorg.minecraft.config.provider;

import java.io.InputStream;

@FunctionalInterface
public interface ResourceProvider{
	
	InputStream getResource();
	
}