package tgb.cryptoexchange.devbot.handler.exception;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.exception.AuthException;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

@Service
public class AuthExceptionHandler extends RequestExceptionHandler {

    protected AuthExceptionHandler(ResponseSender responseSender) {
        super(responseSender);
    }

    @Override
    public boolean isInstance(Exception e) {
        return e instanceof AuthException;
    }
}
