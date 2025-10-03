package tgb.cryptoexchange.devbot.handler.exception;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

@Service
public class WebClientExceptionHandler extends RequestExceptionHandler{

    protected WebClientExceptionHandler(ResponseSender responseSender) {
        super(responseSender);
    }

    @Override
    public boolean isInstance(Exception e) {
        return e instanceof WebClientException;
    }
}
