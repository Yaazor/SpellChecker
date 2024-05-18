package com.mrbysco.spellchecker.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrbysco.spellchecker.SpellChecker;
import com.mrbysco.spellchecker.config.SpellCheckerConfig;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.Word;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SuggestionUtil {
	public static final Map<String, List<String>> wordSuggestions = new LinkedHashMap<>();
	public static final List<LocationData> wordPosition = new LinkedList<>();
	public static final List<String> wronglySpelledWords = new LinkedList<>();
	public static final List<SuggestionInfo> keptSuggestions = new LinkedList<>();
	public static String currentLocale;

	/**
	 * Called when a key is pressed in the chat box
	 *
	 * @param keyCode   The key code of the key that was pressed
	 * @param scanCode  The scan code of the key that was pressed
	 * @param modifiers The modifiers of the key that was pressed
	 */
	public static void onKeyPressed(int keyCode, int scanCode, int modifiers, TextFieldWidget box) {
		if (isKeyDown(GLFW.GLFW_KEY_SPACE) || isKeyDown(GLFW.GLFW_KEY_BACKSPACE) || isKeyDown(GLFW.GLFW_KEY_UP) || isKeyDown(GLFW.GLFW_KEY_DOWN) || isKeyDown(GLFW.GLFW_KEY_LEFT) || isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
			refreshSuggestions(box);
		}

		if (isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) && isKeyDown(GLFW.GLFW_KEY_BACKSPACE)) {
			keptSuggestions.clear();
		}
	}


	public static void onMouseClicked(double mouseX, double mouseY, int button, ChatScreen screen) {
		final Minecraft mc = Minecraft.getInstance();
		final FontRenderer font = mc.font;
		if (wronglySpelledWords != null && !wronglySpelledWords.isEmpty() && wordSuggestions != null && !wordSuggestions.isEmpty() && wordPosition != null && !wordPosition.isEmpty()) {
			final MatrixStack matrixStack = new MatrixStack();
			for (String word : wronglySpelledWords) {
				List<String> suggestions = wordSuggestions.get(word);

				TextFieldWidget editBox = screen.input;
				int lineScrollOffset = editBox.displayPos;
				String chatText = editBox.getValue();

				if (chatText.length() > lineScrollOffset) {
					String currentlyDisplayedText = chatText.substring(lineScrollOffset);

					for (LocationData data : wordPosition) {
						String originalWord = data.getWord();
						String wordUntilTypo = data.getWordsUntil();

						if (originalWord.equals(word)) {
							if (currentlyDisplayedText.contains(wordUntilTypo)) {
								int width = font.width(wordUntilTypo);

								boolean hoveredFlag = hoverBoolean((int) mouseX, (int) mouseY, 2 + width, screen.height - 12, font.width(word), font.lineHeight);
								if (hoveredFlag) {
									SuggestionRendering.drawInfoTooltip(screen, matrixStack, font, suggestions, width - 6, screen.height - (6 + (suggestions.size() * 12)));

									addToDictionary(editBox, word);
									keepSuggestion(editBox, width - 6, screen.height - 12, word, suggestions);
								}
							} else {
								String[] Words = currentlyDisplayedText.split(" ");
								if (Words.length > 0) {
									String firstWord = Words[0];
									if (!firstWord.isEmpty() && word.contains(firstWord)) {
										int width = font.width(firstWord);

										boolean hoveredFlag = hoverBoolean((int) mouseX, (int) mouseY, 2 + width, screen.height - 12, font.width(word), font.lineHeight);
										if (hoveredFlag) {
											SuggestionRendering.drawInfoTooltip(screen, matrixStack, font, suggestions, width - 6, screen.height - (6 + (suggestions.size() * 12)));

											addToDictionary(editBox, word);
											keepSuggestion(editBox, width - 6, screen.height - 20, word, suggestions);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	protected static boolean hoverBoolean(int mouseX, int mouseY, int x, int y, int widthIn, int heigthIn) {
		return mouseX >= x && mouseY >= y && mouseX < x + widthIn && mouseY < y + heigthIn;
	}

	private static boolean isKeyDown(int keyCode) {
		return InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keyCode);
	}

	/**
	 * Adds a word to the dictionary and then refreshes the suggestions
	 *
	 * @param box  The edit box
	 * @param word The word to add
	 */
	public static void addToDictionary(TextFieldWidget box, String word) {
		if (isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			try {
				if (DictionaryUtil.personalDictionary == null) {
					DictionaryUtil.personalDictionary = new File(DictionaryUtil.personalFolder, "/dictionary.txt");
				}
				DictionaryUtil.AddToPersonal(word);
				refreshSuggestions(box);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Keeps the suggested word
	 *
	 * @param box         The edit box
	 * @param x           The x position of the word
	 * @param y           The y position of the word
	 * @param word        The word to keep
	 * @param suggestions The suggestions for the word
	 */
	public static void keepSuggestion(TextFieldWidget box, int x, int y, String word, List<String> suggestions) {
		if (isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
			if (keptSuggestions != null) {
				SuggestionInfo sInfo = new SuggestionInfo(x, y, suggestions, word);
				if (!keptSuggestions.contains(sInfo)) {
					keptSuggestions.add(sInfo);
				}
			}
		}
	}

	/**
	 * Refreshes the suggestions for the current input
	 *
	 * @param editBox The edit box
	 */
	public static void refreshSuggestions(TextFieldWidget editBox) {
		DictionaryUtil.addPersonalToLanguageMap();

		wordSuggestions.clear();
		wordPosition.clear();
		wronglySpelledWords.clear();

		String chatText = editBox.getValue();

		int lineScrollOffset = editBox.displayPos;
		String visibleString = chatText.substring(lineScrollOffset);

		String[] CurrentWords = visibleString.split(" ");
		if (CurrentWords.length > 0) {
			for (int i = 0; i < CurrentWords.length; i++) {
				String wordToCheck = CurrentWords[i];
				String strippedWord = wordToCheck;

				if (!strippedWord.isEmpty()) {
					SpellDictionary dict = SpellChecker.dict;
					if (!dict.isCorrect(strippedWord)) {
						String extraStripped = stripWord(currentLocale, strippedWord);
						strippedWord = extraStripped;
						if (!strippedWord.isEmpty() && !dict.isCorrect(strippedWord)) {
							StringBuilder tillEndOfWord = new StringBuilder();
							for (int j = 0; j <= i; j++) {
								if (j != i) {
									tillEndOfWord.append(CurrentWords[j]);
									tillEndOfWord.append(" ");
								}
							}
							List<String> suggestions = getSuggestions(strippedWord);
							LocationData locData = new LocationData(wordToCheck, tillEndOfWord.toString());

							wordSuggestions.put(wordToCheck, suggestions);
							wronglySpelledWords.add(wordToCheck);
							wordPosition.add(locData);
						}
					}
				}
			}
		}
	}

	/**
	 * Get suggestions for a word
	 *
	 * @param misspelledWord The word to get suggestions for
	 * @return ArrayList of suggestions
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getSuggestions(String misspelledWord) {
		int threshold = SpellCheckerConfig.CLIENT.checking_threshold.get();
		int maxSuggestions = SpellCheckerConfig.CLIENT.max_suggestions.get();

		List<Word> words = new ArrayList<>();
		if (!misspelledWord.isEmpty()) {
			words = SpellChecker.dict.getSuggestions(misspelledWord, threshold);
		}
		List<String> suggestions = new ArrayList<>();

		if (!words.isEmpty()) {
			for (Word suggestion : words) {
				if (suggestions.size() <= maxSuggestions)
					suggestions.add(suggestion.getWord());
			}
		}

		return suggestions;
	}

	/**
	 * Strips a word of all non-alphanumeric characters
	 *
	 * @param locale The current locale
	 * @param word   The word to strip
	 * @return The stripped word
	 */
	public static String stripWord(String locale, String word) {
		String strippedWord = word;
		if (!word.isEmpty()) {
			if (locale != null) {
				if (locale.equals("nl_nl")) {
					strippedWord = strippedWord.replace("'s", "");
					strippedWord = strippedWord.replace("'tje", "");
				}
			}

			strippedWord = strippedWord.replaceAll("[^\\p{IsAlphabetic}\\d]", "");
		}

		return strippedWord;
	}
}
