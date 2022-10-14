/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.github.joekerouac.common.tools.log;

import org.slf4j.Marker;
import org.slf4j.impl.TestILoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.reflect.bean.BeanUtils;

/**
 * 对日志进行简单的功能性测试
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class Slf4jLoggerAdaptorTest {

    @Test
    public void simpleTest() {
        String name = BeanUtils.class.getName();
        TestILoggerFactory.SystemPrintLogger logger = new TestILoggerFactory.SystemPrintLogger(name);

        {
            Assert.assertEquals(name, logger.getName());
        }

        {
            Assert.assertFalse(logger.isTraceEnabled());
            Assert.assertFalse(logger.isTraceEnabled(null));
            logger.trace("logSimpleTest");
            logger.trace("{}", "logSimpleTest");
            logger.trace("{}{}", "logSimpleTest", "");
            logger.trace("{}{}{}", "logSimpleTest", "", "");
            logger.trace("logSimpleTest", new RuntimeException());
            logger.trace((Marker)null, "logSimpleTest");
            logger.trace((Marker)null, "{}", "logSimpleTest");
            logger.trace((Marker)null, "{}{}", "logSimpleTest", "");
            logger.trace((Marker)null, "{}{}{}", "logSimpleTest", "", "");
            logger.trace((Marker)null, "logSimpleTest", new RuntimeException());
        }

        {
            Assert.assertFalse(logger.isDebugEnabled());
            Assert.assertFalse(logger.isDebugEnabled(null));
            logger.debug("logSimpleTest");
            logger.debug("{}", "logSimpleTest");
            logger.debug("{}{}", "logSimpleTest", "");
            logger.debug("{}{}{}", "logSimpleTest", "", "");
            logger.debug("logSimpleTest", new RuntimeException());
            logger.debug((Marker)null, "logSimpleTest");
            logger.debug((Marker)null, "{}", "logSimpleTest");
            logger.debug((Marker)null, "{}{}", "logSimpleTest", "");
            logger.debug((Marker)null, "{}{}{}", "logSimpleTest", "", "");
            logger.debug((Marker)null, "logSimpleTest", new RuntimeException());
        }

        {
            Assert.assertFalse(logger.isInfoEnabled());
            Assert.assertFalse(logger.isInfoEnabled(null));
            logger.info("logSimpleTest");
            logger.info("{}", "logSimpleTest");
            logger.info("{}{}", "logSimpleTest", "");
            logger.info("{}{}{}", "logSimpleTest", "", "");
            logger.info("logSimpleTest", new RuntimeException());
            logger.info((Marker)null, "logSimpleTest");
            logger.info((Marker)null, "{}", "logSimpleTest");
            logger.info((Marker)null, "{}{}", "logSimpleTest", "");
            logger.info((Marker)null, "{}{}{}", "logSimpleTest", "", "");
            logger.info((Marker)null, "logSimpleTest", new RuntimeException());
        }

        {
            Assert.assertTrue(logger.isWarnEnabled());
            Assert.assertTrue(logger.isWarnEnabled(null));
            logger.warn("logSimpleTest");
            logger.warn("{}", "logSimpleTest");
            logger.warn("{}{}", "logSimpleTest", "");
            logger.warn("{}{}{}", "logSimpleTest", "", "");
            logger.warn("logSimpleTest", new RuntimeException());
            logger.warn((Marker)null, "logSimpleTest");
            logger.warn((Marker)null, "{}", "logSimpleTest");
            logger.warn((Marker)null, "{}{}", "logSimpleTest", "");
            logger.warn((Marker)null, "{}{}{}", "logSimpleTest", "", "");
            logger.warn((Marker)null, "logSimpleTest", new RuntimeException());
        }

        {
            Assert.assertTrue(logger.isErrorEnabled());
            Assert.assertTrue(logger.isErrorEnabled(null));
            logger.error("logSimpleTest");
            logger.error("{}", "logSimpleTest");
            logger.error("{}{}", "logSimpleTest", "");
            logger.error("{}{}{}", "logSimpleTest", "", "");
            logger.error("logSimpleTest", new RuntimeException());
            logger.error((Marker)null, "logSimpleTest");
            logger.error((Marker)null, "{}", "logSimpleTest");
            logger.error((Marker)null, "{}{}", "logSimpleTest", "");
            logger.error((Marker)null, "{}{}{}", "logSimpleTest", "", "");
            logger.error((Marker)null, "logSimpleTest", new RuntimeException());
        }
    }

}
