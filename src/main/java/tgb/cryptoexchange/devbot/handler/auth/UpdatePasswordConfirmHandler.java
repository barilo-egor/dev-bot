package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.tgcommon.exception.TelegramCommonException;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.InlineButton;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

import java.util.List;

@Service
public class UpdatePasswordConfirmHandler implements CallbackQueryHandler {

    private final ResponseSender responseSender;

    private final KeyboardBuilder keyboardBuilder;

    public UpdatePasswordConfirmHandler(ResponseSender responseSender, KeyboardBuilder keyboardBuilder) {
        this.responseSender = responseSender;
        this.keyboardBuilder = keyboardBuilder;
    }

    @Override
    public void handle(PressedInlineButton button) {
        String username = button.getArgument(1)
                .orElseThrow(() -> new TelegramCommonException("Отсутствует аргумент идентификатора пользователя."));
        responseSender.to(button.getChatId())
                .editText(
                        button.getMessage().getMessageId(),
                        "Вы действительно хотите обновить пароль пользователю <b>" + username + "</b>?"
                )
                .replyKeyboard(keyboardBuilder.buildInline(2, List.of(
                        new InlineButton(CallbackQueryId.UPDATE_PASSWORD_PROCESS.name(), "Да", username),
                        new InlineButton(CallbackQueryId.BACK_TO_DELETE_USER_MENU.name(), "Нет")
                )))
                .send();
    }

    @Override
    public String getId() {
        return CallbackQueryId.UPDATE_PASSWORD_CONFIRM.name();
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
