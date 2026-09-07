/*
 * Copyright (c) - Paul Pinault (aka disk91) - 2026.
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 *    and associated documentation files (the "Software"), to deal in the Software without restriction,
 *    including without limitation the rights to use, copy, modify, merge, publish, distribute,
 *    sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in all copies or
 *    substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *    FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 *    OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 *    IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.disk91.common.tools.drivers;

import com.disk91.common.tools.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyShortMessagesDriver extends AbstractShortMessagesDriver {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public ShortMessageResponse connect() {
        // Dummy implementation, nothing to connect
        log.info(Tools.inBlue("[common] DummyShortMessagesDriver connect() called, returning SMS_CONNECT_OK"));
        return  ShortMessageResponse.SMS_CONNECT_OK;
    }

    @Override
    public ShortMessageResponse sendShortMessage(
        String to,
        String text,
        String from
    ) {
        // Dummy implementation, nothing to send
        log.info(Tools.inBlue("[common] DummyShortMessagesDriver sendShortMessage() called, from "+from+" to "+to+" text: "+text));
        return  ShortMessageResponse.SMS_SENT_OK;
    }


    @Override
    public int getCreditsLeft() {
        // Dummy implementation, always return -1
        log.info(Tools.inBlue("[common] DummyShortMessagesDriver getCreditsLeft() called, returning -1"));
        return -1;
    }

}
