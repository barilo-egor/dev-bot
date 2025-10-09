package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.InlineButton;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

import java.util.List;

@Service
public class AuthHandler implements CallbackQueryHandler {

    private final ResponseSender responseSender;

    private final InlineKeyboardMarkup keyboard;

    public AuthHandler(ResponseSender responseSender, KeyboardBuilder keyboardBuilder) {
        this.responseSender = responseSender;
        this.keyboard = keyboardBuilder.buildInline(List.of(
                new InlineButton(CallbackQueryId.NEW_USER.name(), "Новый пользователь"),
                new InlineButton(CallbackQueryId.DELETE_USER.name(), "Удалить пользователя"),
                new InlineButton(CallbackQueryId.UPDATE_PASSWORD.name(), "Обновить пароль"),
                new InlineButton(CallbackQueryId.BACK_TO_MAIN_MENU.name(), "Назад")
        ));
    }

    @Override
    public void handle(PressedInlineButton button) {
        handle(button.getChatId(), button.getMessage().getMessageId());
    }

    public void handle(Long chatId) {
        responseSender.to(chatId)
                .message("Меню сервиса аутентификации.")
                .replyKeyboard(keyboard)
                .send();
    }

    public void handle(Long chatId, Integer messageId) {
        responseSender.to(chatId)
                .editText(messageId, "Меню сервиса аутентификации.")
                .replyKeyboard(keyboard)
                .send();
    }

    @Override
    public String getId() {
        return CallbackQueryId.AUTH.name();
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
