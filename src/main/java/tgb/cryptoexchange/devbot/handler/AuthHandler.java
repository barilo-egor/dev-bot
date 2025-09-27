package tgb.cryptoexchange.devbot.handler;

import org.springframework.stereotype.Service;
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

    private final KeyboardBuilder keyboardBuilder;

    public AuthHandler(ResponseSender responseSender, KeyboardBuilder keyboardBuilder) {
        this.responseSender = responseSender;
        this.keyboardBuilder = keyboardBuilder;
    }

    @Override
    public void handle(PressedInlineButton button) {
        responseSender.to(button.getChatId())
                .editText(button.getMessage().getMessageId(), "Меню сервиса аутентификации.")
                .replyKeyboard(keyboardBuilder.buildInline(List.of(
                        new InlineButton(CallbackQueryId.NEW_USER.name(), "Новый пользователь"),
                        new InlineButton(CallbackQueryId.DELETE_USER.name(), "Удалить пользователя"),
                        new InlineButton(CallbackQueryId.UPDATE_PASSWORD.name(), "Обновить пароль"),
                        new InlineButton(CallbackQueryId.BACK_TO_MAIN_MENU.name(), "Назад")
                )))
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
