package com.mrbysco.spellchecker.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrbysco.spellchecker.config.SpellCheckerConfig;
import com.mrbysco.spellchecker.util.DictionaryUtil;
import com.mrbysco.spellchecker.util.SuggestionRendering;
import com.mrbysco.spellchecker.util.SuggestionUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

	@Shadow
	public TextFieldWidget input;

	@Inject(at = @At("TAIL"), method = "init()V")
	private void spellchecker_init(CallbackInfo info) {
		SuggestionUtil.currentLocale = SpellCheckerConfig.CLIENT.language_to_check.get().getLocale();
		DictionaryUtil.addPersonalToLanguageMap();
		SuggestionUtil.refreshSuggestions(input);
	}

	@Inject(at = @At("HEAD"), method = "onEdited(Ljava/lang/String;)V")
	private void spellchecker_onEdited(CallbackInfo info) {
		SuggestionUtil.refreshSuggestions(input);
	}

	@Inject(at = @At("HEAD"), method = "keyPressed(III)Z")
	public void spellchecker_keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		SuggestionUtil.onKeyPressed(keyCode, scanCode, modifiers, input);
	}

	@Inject(at = @At("TAIL"), method = "mouseClicked(DDI)Z")
	public void spellchecker_mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		ChatScreen screen = (ChatScreen) (Object) this;
		SuggestionUtil.onMouseClicked(mouseX, mouseY, button, screen);
	}

	@Inject(at = @At("TAIL"), method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;IIF)V")
	public void spellchecker_render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick, CallbackInfo info) {
		ChatScreen screen = (ChatScreen) (Object) this;
		SuggestionRendering.renderSuggestions(matrixStack, mouseX, mouseY, partialTick, screen);
	}
}
