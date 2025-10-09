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
public class DeleteUserConfirmHandler implements CallbackQueryHandler {

    private final ResponseSender responseSender;

    private final KeyboardBuilder keyboardBuilder;

    public DeleteUserConfirmHandler(ResponseSender responseSender, KeyboardBuilder keyboardBuilder) {
        this.responseSender = responseSender;
        this.keyboardBuilder = keyboardBuilder;
    }

    @Override
    public void handle(PressedInlineButton button) {
        String username = button.getArgument(1)
                .orElseThrow(() -> new TelegramCommonException("Отсутствует аргумент идентификатора пользователя."));
        responseSender.to(button.getChatId())
                .editText(button.getMessage().getMessageId(), "Вы действительно хотите удалить пользователя <b>" + username + "</b>?")
                .replyKeyboard(keyboardBuilder.buildInline(2, List.of(
                        new InlineButton(CallbackQueryId.DELETE_USER_PROCESS.name(), "Да", username),
                        new InlineButton(CallbackQueryId.BACK_TO_DELETE_USER_MENU.name(), "Нет")
                )))
                .send();
    }

    @Override
    public String getId() {
        return CallbackQueryId.DELETE_USER_CONFIRM.name();
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
