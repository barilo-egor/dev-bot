package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.exception.AuthException;
import tgb.cryptoexchange.devbot.service.AuthMenuService;
import tgb.cryptoexchange.devbot.service.AuthService;
import tgb.cryptoexchange.devbot.service.PasswordGenerator;
import tgb.cryptoexchange.tgcommon.exception.TelegramCommonException;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;
import tgb.cryptoexchange.web.ApiResponse;

import java.util.Optional;

@Service
public class UpdatePasswordProcessHandler implements CallbackQueryHandler {

    private final AuthService authService;

    private final ResponseSender responseSender;

    private final AuthMenuService authMenuService;

    private final PasswordGenerator passwordGenerator;

    public UpdatePasswordProcessHandler(AuthService authService, ResponseSender responseSender,
                                        AuthMenuService authMenuService, PasswordGenerator passwordGenerator) {
        this.authService = authService;
        this.responseSender = responseSender;
        this.authMenuService = authMenuService;
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public void handle(PressedInlineButton button) {
        String username = button.getArgument(1)
                .orElseThrow(() -> new TelegramCommonException("Отсутствует аргумент идентификатора пользователя."));
        String password = passwordGenerator.generate(32);
        try {
            authService.patch(username, password);
        } catch (WebClientResponseException.BadRequest e) {
            responseSender.to(button.getChatId())
                    .message(
                            Optional.ofNullable(e.getResponseBodyAs(ApiResponse.class))
                                    .orElseThrow(() -> new AuthException("Отсутствует тело ответа."))
                                    .getError()
                                    .getMessage()
                    )
                    .send();
            return;
        }
        responseSender.to(button.getChatId())
                .delete(button.getMessage().getMessageId());
        responseSender.to(button.getChatId())
                .message(
                        "Пользователю <b>" + username + "</b> был обновлен пароль.\nПароль: <code>" + password + "</code>"
                )
                .send();
        authMenuService.sendUsers(button.getChatId(), null, CallbackQueryId.UPDATE_PASSWORD_CONFIRM);
    }

    @Override
    public String getId() {
        return CallbackQueryId.UPDATE_PASSWORD_PROCESS.name();
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
