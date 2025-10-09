package tgb.cryptoexchange.devbot.handler.exception;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

@Service
public class WebClientResponseExceptionHandler extends RequestExceptionHandler{

    protected WebClientResponseExceptionHandler(ResponseSender responseSender) {
        super(responseSender);
    }

    @Override
    public boolean isInstance(Exception e) {
        return e instanceof WebClientResponseException;
    }
}
