package com.mrbysco.spellchecker.language;

public enum LanguageEnum {
	EN_US("en_us"),
	FR_FR("fr_fr");

	private final String locale;

	LanguageEnum(String locale_name) {
		this.locale = locale_name;
	}

	public String getLocale() {
		return this.locale;
	}
}
