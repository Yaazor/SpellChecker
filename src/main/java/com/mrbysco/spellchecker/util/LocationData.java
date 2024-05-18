package com.mrbysco.spellchecker.util;

public class LocationData {
	private final String word;
	private final String wordsUntil;

	public LocationData(String word, String wordsUntil) {
		this.word = word;
		this.wordsUntil = wordsUntil;
	}

	public String getWord() {
		return word;
	}

	public String getWordsUntil() {
		return wordsUntil;
	}
}
