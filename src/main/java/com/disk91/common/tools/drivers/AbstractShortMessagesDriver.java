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

public abstract class AbstractShortMessagesDriver {

    /**
     * If it is necessary to connect to the SMS provider, the `connect` function will be called before sending every SMS
     * and also on startup.
     * This can be implemented specifically in the driver and, if needed, potentially executed only once.
     *
     * The connection parameters are usually provided through configuration files and are therefore not passed directly
     * to the function, since they can vary significantly depending on the driver.
     */
    public abstract ShortMessageResponse connect();

    /**
     * Send a short message, nothing else to pass and as it is async processing there is nothing to return
     * all the action must be taken inside this method
     * @param to
     * @param text
     * @param from
     */
    public abstract ShortMessageResponse sendShortMessage(
        String to,
        String text,
        String from
    );


    /**
     * Get the number of credits left for sending messages. This function is optional and may not be implemented in all drivers.
     * @return the number of credits left, or -1 if the driver does not support this feature.
     */
    public abstract int getCreditsLeft();

}
