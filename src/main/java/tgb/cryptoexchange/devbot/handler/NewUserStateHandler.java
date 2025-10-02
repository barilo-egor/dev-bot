package tgb.cryptoexchange.devbot.handler;

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
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equals(CallbackQueryId.BACK_TO_AUTH_MENU.name())) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            authHandler.sendMenu(chatId, update.getCallbackQuery().getMessage().getMessageId());
            redisUserStateService.delete(chatId);
            return;
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            responseSender.to(UpdateType.getChatId(update))
                    .message("Отправьте идентификатор пользователя, либо нажмите \"Назад\".")
                    .send();
            return;
        }
        Long chatId = update.getMessage().getChatId();
        String enteredUsername = update.getMessage().getText();
        String password;
        try {
            boolean isUsernameFree = authService.isUsernameFree(enteredUsername);
            if (!isUsernameFree) {
                responseSender.to(chatId)
                        .message("Идентификатор уже используется.")
                        .send();
                return;
            }
            password = passwordGenerator.generate(32);
            authService.register(enteredUsername, password);
        } catch (AuthException e) {
            responseSender.to(chatId)
                    .message("Ошибка при выполнении запроса.\n" + e.getMessage())
                    .send() ;
            return;
        } catch (WebClientResponseException e) {
            if (e instanceof WebClientResponseException.BadRequest badRequest) {
                responseSender.to(chatId)
                        .message(badRequest.getResponseBodyAs(ApiResponse.class).getError().getMessage())
                        .send();
            } else {
                responseSender.to(chatId)
                        .message("Ошибка при выполнении запроса: " + e.getMessage())
                        .send();
            }
            return;
        }
        responseSender.to(chatId)
                .message("Пользователь успешно зарегистрирован.\nПароль: <code>" + password + "</code>")
                .send();
    }

    @Override
    public String getUserState() {
        return DevBotUserState.NEW_USER.getState();
    }
}
