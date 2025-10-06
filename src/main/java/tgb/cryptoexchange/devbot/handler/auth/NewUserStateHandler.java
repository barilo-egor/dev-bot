package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.objects.Update;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.constants.DevBotUserState;
import tgb.cryptoexchange.devbot.exception.AuthException;
import tgb.cryptoexchange.devbot.service.AuthService;
import tgb.cryptoexchange.devbot.service.PasswordGenerator;
import tgb.cryptoexchange.tgcommon.constants.UpdateType;
import tgb.cryptoexchange.tgcommon.handler.StateHandler;
import tgb.cryptoexchange.tgcommon.service.RedisUserStateService;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;
import tgb.cryptoexchange.web.ApiResponse;

import java.util.Optional;

@Service
public class NewUserStateHandler implements StateHandler {

    private final ResponseSender responseSender;

    private final AuthService authService;

    private final AuthHandler authHandler;

    private final PasswordGenerator passwordGenerator;

    private final RedisUserStateService redisUserStateService;

    public NewUserStateHandler(ResponseSender responseSender, AuthService authService, AuthHandler authHandler,
                               PasswordGenerator passwordGenerator, RedisUserStateService redisUserStateService) {
        this.responseSender = responseSender;
        this.authService = authService;
        this.authHandler = authHandler;
        this.passwordGenerator = passwordGenerator;
        this.redisUserStateService = redisUserStateService;
    }

    @Override
    public void handle(Update update) {
        if (!isValid(update)) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        responseSender.to(chatId)
                .action()
                .typing();
        String enteredUsername = update.getMessage().getText();
        String password;
        try {
            Optional<String> maybePassword = register(enteredUsername);
            if (maybePassword.isPresent()) {
                password = maybePassword.get();
            } else {
                responseSender.to(chatId)
                        .message("Идентификатор уже используется.")
                        .send();
                return;
            }
        } catch (WebClientResponseException.BadRequest e) {
            responseSender.to(chatId)
                    .message(
                            Optional.ofNullable(e.getResponseBodyAs(ApiResponse.class))
                                    .orElseThrow(() -> new AuthException("Отсутствует тело ответа."))
                                    .getError()
                                    .getMessage()
                    )
                    .send();
            return;
        }
        responseSender.to(chatId)
                .message("Пользователь успешно зарегистрирован.\nПароль: <code>" + password + "</code>")
                .send();
        redisUserStateService.delete(chatId);
        authHandler.handle(chatId);
    }

    private Optional<String> register(String enteredUsername) {
        boolean isUsernameFree = authService.isUsernameFree(enteredUsername);
        if (!isUsernameFree) {
            return Optional.empty();
        }
        String password = passwordGenerator.generate(32);
        authService.register(enteredUsername, password);
        return Optional.of(password);
    }

    private boolean isValid(Update update) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equals(CallbackQueryId.BACK_TO_AUTH_MENU.name())) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            authHandler.handle(chatId, update.getCallbackQuery().getMessage().getMessageId());
            redisUserStateService.delete(chatId);
            return false;
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            responseSender.to(UpdateType.getChatId(update))
                    .message("Отправьте идентификатор пользователя, либо нажмите \"Назад\".")
                    .send();
            return false;
        }
        return true;
    }

    @Override
    public String getUserState() {
        return DevBotUserState.NEW_USER.getState();
    }
}
