package com.mrbysco.spellchecker.util;

import java.util.List;

public class SuggestionInfo {

	private final int posX;
	private final int posY;
	private final List<String> suggestions;
	private final String word;

	public SuggestionInfo(int x, int y, List<String> suggestions, String word) {
		this.posX = x;
		this.posY = y;
		this.suggestions = suggestions;
		this.word = word;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public List<String> getSuggestions() {
		return suggestions;
	}

	public String getWord() {
		return word;
	}
}
