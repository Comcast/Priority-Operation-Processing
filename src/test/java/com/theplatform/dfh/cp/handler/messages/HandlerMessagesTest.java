package com.theplatform.dfh.cp.handler.messages;

import com.theplatform.dfh.cp.handler.base.messages.HandlerMessages;
import org.testng.annotations.Test;

import java.util.Arrays;

public class HandlerMessagesTest
{
    @Test
    public void verifyMessages()
    {
        Arrays.stream(HandlerMessages.values()).forEach(HandlerMessages::getMessage);
    }
}
