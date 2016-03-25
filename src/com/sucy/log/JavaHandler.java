/**
 * LogManager
 * com.sucy.log.JavaHandler
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.log;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Handler for overriding log messages
 */
public class JavaHandler extends Handler
{
    @Override
    public void publish(LogRecord record)
    {
        if (record.getLoggerName().equals("Minecraft"))
        {
            if (!ConsoleManager.check(record.getLevel()))
                return;
        }
        else
        {
            String plugin = record.getLoggerName().substring(record.getLoggerName().lastIndexOf('.') + 1);
            if (!ConsoleManager.check(plugin, record.getLevel()))
                return;
        }

        System.out.println(record.getMessage());
    }

    @Override
    public void flush()
    {

    }

    @Override
    public void close() throws SecurityException
    {

    }
}
