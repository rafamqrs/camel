/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.dsl.jbang.core.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.camel.util.IOHelper;
import picocli.CommandLine;

/**
 * Helper for CLI command line.
 */
public final class CommandLineHelper {

    public static final String USER_CONFIG = ".camel-jbang-user.properties";

    private CommandLineHelper() {
        super();
    }

    public static void augmentWithUserConfiguration(CommandLine commandLine, String... args) {
        File file = new File(System.getProperty("user.home"), USER_CONFIG);
        if (file.isFile() && file.exists()) {
            commandLine.setDefaultValueProvider(new CamelUserConfigDefaultValueProvider(file));
        }
    }

    public static void createPropertyFile() throws IOException {
        File file = new File(System.getProperty("user.home"), CommandLineHelper.USER_CONFIG);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public static void loadProperties(Consumer<Properties> consumer) {
        File file = new File(System.getProperty("user.home"), CommandLineHelper.USER_CONFIG);
        if (file.isFile() && file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                Properties prop = new Properties();
                prop.load(fis);
                IOHelper.close(fis);
                consumer.accept(prop);
            } catch (Exception e) {
                throw new RuntimeException("Cannot load user configuration: " + file);
            } finally {
                IOHelper.close(fis);
            }
        } else {
            System.out.println(CommandLineHelper.USER_CONFIG + " does not exists");
        }
    }

    public static void storeProperties(Properties properties) {
        File file = new File(System.getProperty("user.home"), CommandLineHelper.USER_CONFIG);
        if (file.isFile() && file.exists()) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                properties.store(fos, null);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            System.out.println(CommandLineHelper.USER_CONFIG + " does not exists");
        }
    }

    private static class CamelUserConfigDefaultValueProvider extends CommandLine.PropertiesDefaultProvider {

        public CamelUserConfigDefaultValueProvider(Properties properties) {
            super(properties);
        }

        public CamelUserConfigDefaultValueProvider(File file) {
            super(file);
        }

        @Override
        public String defaultValue(CommandLine.Model.ArgSpec arg) throws Exception {
            String value = super.defaultValue(arg);
            if (value != null) {
                if (arg instanceof CommandLine.Model.OptionSpec) {
                    // TODO: capture these default values that are in use
                    // and find a way to log them only once (and have an option to turn this off)
                    CommandLine.Model.OptionSpec os = (CommandLine.Model.OptionSpec) arg;
                    String k = os.longestName();
                    System.out.println(k + "=" + value);
                }
            }
            return value;
        }
    }

}
