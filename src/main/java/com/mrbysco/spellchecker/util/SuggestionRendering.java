package com.mrbysco.spellchecker.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrbysco.spellchecker.config.SpellCheckerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.stream.Collectors;

public class SuggestionRendering {
	public static void renderSuggestions(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick, ChatScreen screen) {
		final FontRenderer font = Minecraft.getInstance().font;
		if (!SuggestionUtil.keptSuggestions.isEmpty()) {
			for (SuggestionInfo info : SuggestionUtil.keptSuggestions) {
				int posX = info.getPosX();
				int posY = info.getPosY();
				String word = info.getWord();
				List<String> suggestions = info.getSuggestions();

				drawInfoTooltip(screen, matrixStack, font, suggestions, posX, posY);
			}
		}

		if (SuggestionUtil.wronglySpelledWords != null && !SuggestionUtil.wronglySpelledWords.isEmpty() &&
				SuggestionUtil.wordSuggestions != null && !SuggestionUtil.wordSuggestions.isEmpty() &&
				SuggestionUtil.wordPosition != null && !SuggestionUtil.wordPosition.isEmpty()) {
			final boolean showSuggestionsLive = SpellCheckerConfig.CLIENT.show_suggestions_live.get();
			for (int i = 0; i < SuggestionUtil.wronglySpelledWords.size(); i++) {
				boolean isLastWord = i == SuggestionUtil.wronglySpelledWords.size() - 1;
				String word = SuggestionUtil.wronglySpelledWords.get(i);
				List<String> suggestions = SuggestionUtil.wordSuggestions.get(word);

				TextFieldWidget editBox = screen.input;
				int lineScrollOffset = editBox.displayPos;
				String chatText = editBox.getValue();

				if (chatText.length() > lineScrollOffset) {
					String currentlyDisplayedText = chatText.substring(lineScrollOffset);

					for (LocationData data : SuggestionUtil.wordPosition) {
						String originalWord = data.getWord();
						String wordUntilTypo = data.getWordsUntil();

						if (originalWord.equals(word)) {
							if (currentlyDisplayedText.contains(wordUntilTypo)) {
								int width = font.width(wordUntilTypo);

								StringBuilder wrongSquigly = new StringBuilder();
								for(int j = 0; j < word.length(); j++) {
									wrongSquigly.append("~");
								}

								if (font.width(word) <= font.width(wrongSquigly.toString())) {
									int left = font.width(wrongSquigly.toString()) - font.width(word);
									int removeCount = (int) Math.floor((double) left / (double) font.width("~"));
									wrongSquigly = new StringBuilder(wrongSquigly.substring(removeCount));
								}

								if (font.width(word) <= font.width(wrongSquigly.toString())) {
									wrongSquigly = new StringBuilder(wrongSquigly.substring(1));
								}

								if (font.width(wrongSquigly.toString()) == 0 && font.width(word) > 0) {
									wrongSquigly = new StringBuilder("~");
								}

								font.draw(matrixStack, wrongSquigly.toString(), width + 4, screen.height - 4, 16733525);
								boolean hoveredFlag = SuggestionUtil.hoverBoolean(mouseX, mouseY, 2 + width, screen.height - 12, font.width(word), font.lineHeight);
								if (hoveredFlag || (showSuggestionsLive && isLastWord)) {
									drawInfoTooltip(screen, matrixStack, font, suggestions, width - 6, screen.height - (6 + (suggestions.size() * 12)));
								}
							} else {
								String[] Words = currentlyDisplayedText.split(" ");
								if (Words.length > 0) {
									String firstWord = Words[0];
									if (!firstWord.isEmpty() && word.contains(firstWord)) {
										int width = font.width(firstWord);

										StringBuilder wrongSquigly = new StringBuilder();
										for(int j = 0; j < firstWord.length(); j++) {
											wrongSquigly.append("~");
										}

										if (font.width(word) <= font.width(wrongSquigly.toString())) {
											int left = font.width(wrongSquigly.toString()) - font.width(word);
											int removeCount = (int) Math.floor((double) left / (double) font.width("~"));
											wrongSquigly = new StringBuilder(wrongSquigly.substring(removeCount));
										}

										if (font.width(word) <= font.width(wrongSquigly.toString())) {
											wrongSquigly = new StringBuilder(wrongSquigly.substring(1));
										}

										if (font.width(wrongSquigly.toString()) == 0 && font.width(word) > 0) {
											wrongSquigly = new StringBuilder("~");
										}

										font.draw(matrixStack, wrongSquigly.toString(), width + 2, screen.height - 4, 16733525);
										boolean hoveredFlag = SuggestionUtil.hoverBoolean(mouseX, mouseY, 2 + width, screen.height - 12, font.width(word), font.lineHeight);
										if (hoveredFlag) {
											drawInfoTooltip(screen, matrixStack, font, suggestions, width - 6, screen.height - (6 + (suggestions.size() * 12)));
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

	public static void drawInfoTooltip(ChatScreen screen, MatrixStack matrixStack, FontRenderer font, List<String> textLines, int x, int y) {
		screen.renderTooltip(matrixStack, textLines.stream().map(text -> new StringTextComponent(text).getVisualOrderText()).collect(Collectors.toList()), x, y);
	}
}
