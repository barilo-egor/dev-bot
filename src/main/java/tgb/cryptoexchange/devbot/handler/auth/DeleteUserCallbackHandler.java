package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.service.AuthService;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.InlineButton;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DeleteUserCallbackHandler implements CallbackQueryHandler {

    private final ResponseSender responseSender;

    private final KeyboardBuilder keyboardBuilder;

    private final AuthService authService;

    public DeleteUserCallbackHandler(ResponseSender responseSender, KeyboardBuilder keyboardBuilder, AuthService authService) {
        this.responseSender = responseSender;
        this.keyboardBuilder = keyboardBuilder;
        this.authService = authService;
    }

    @Override
    public void handle(PressedInlineButton button) {
        sendUsers(button.getChatId(), button.getMessage().getMessageId());
    }

    public void sendUsers(Long chatId, Integer messageId) {
        responseSender.to(chatId)
                .action()
                .typing();
        List<String> usernames = authService.getUsernames();
        List<InlineButton> buttons = new ArrayList<>();
        usernames.forEach(username -> buttons.add(new InlineButton(CallbackQueryId.DELETE_USER_CONFIRM.name(), username, username)));
        buttons.add(new InlineButton(CallbackQueryId.BACK_TO_AUTH_MENU.name(), "Назад"));
        if (Objects.nonNull(messageId)) {
            responseSender.to(chatId)
                    .editText(messageId, "Выберите пользователя для удаления.")
                    .replyKeyboard(keyboardBuilder.buildInline(2, buttons))
                    .send();
        } else {
            responseSender.to(chatId)
                    .message("Выберите пользователя для удаления.")
                    .replyKeyboard(keyboardBuilder.buildInline(2, buttons))
                    .send();
        }
    }

    @Override
    public String getId() {
        return CallbackQueryId.DELETE_USER.name();
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
