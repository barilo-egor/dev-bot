package tgb.cryptoexchange.devbot.service;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.tgcommon.keyboard.InlineButton;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AuthMenuService {

    private final ResponseSender responseSender;

    private final AuthService authService;

    private final KeyboardBuilder keyboardBuilder;

    public AuthMenuService(ResponseSender responseSender, AuthService authService, KeyboardBuilder keyboardBuilder) {
        this.responseSender = responseSender;
        this.authService = authService;
        this.keyboardBuilder = keyboardBuilder;
    }

    public void sendUsers(Long chatId, Integer messageId, CallbackQueryId callbackQueryId) {
        String message = switch (callbackQueryId) {
            case DELETE_USER_CONFIRM -> "Выберите пользователя для удаления.";
            case UPDATE_PASSWORD_CONFIRM -> "Выберите пользователя для обновления пароля.";
            default -> throw new IllegalStateException("Unexpected value: " + callbackQueryId);
        };
        responseSender.to(chatId)
                .action()
                .typing();
        List<String> usernames = authService.getUsernames();
        List<InlineButton> buttons = new ArrayList<>();
        usernames.forEach(username -> buttons.add(new InlineButton(callbackQueryId.name(), username, username)));
        buttons.add(new InlineButton(CallbackQueryId.BACK_TO_AUTH_MENU.name(), "Назад"));
        if (Objects.nonNull(messageId)) {
            responseSender.to(chatId)
                    .editText(messageId, message)
                    .replyKeyboard(keyboardBuilder.buildInline(2, buttons))
                    .send();
        } else {
            responseSender.to(chatId)
                    .message(message)
                    .replyKeyboard(keyboardBuilder.buildInline(2, buttons))
                    .send();
        }
    }
}
