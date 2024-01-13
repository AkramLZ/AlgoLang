package com.akraml.algo.interpreter.token;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public interface TokenType {

    String getName();

    enum AlgorithmInformationTokenType implements TokenType {

        ALGORITHM("Algorithm"),
        BEGIN("Begin"),
        END("End");

        private final String name;

        AlgorithmInformationTokenType(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    enum DataType implements TokenType {

        INTEGER("Integer", Integer.class, str -> {
            try {
                Integer.parseInt(str);
                return true;
            } catch (final Exception exception) {
                return false;
            }
        }),
        DOUBLE("Real", Double.class, str -> {
            try {
                Double.parseDouble(str);
                return true;
            } catch (final Exception exception) {
                return false;
            }
        }),
        STRING("String", String.class, str -> {
            if (!str.startsWith("\"") || !str.endsWith("\"")) return false;
            final String str1 = str.substring(1, str.length() - 1);
            final char[] charArray = str1.toCharArray();
            for (int i = 0; i < charArray.length - 1; i++) {
                if (charArray[i] == '\\' && charArray[i + 1] == '"') {
                    i++;
                } else if (charArray[i] == '"') {
                    return false;
                }
            }
            return true;
        });

        private final String name;
        private final Class<?> classType;
        private final Function<String, Boolean> validationFunction;

        DataType(final String name,
                 final Class<?> classType,
                 final Function<String, Boolean> validationFunction) {
            this.name = name;
            this.classType = classType;
            this.validationFunction = validationFunction;
        }

        @Override
        public String getName() {
            return name;
        }

        public Class<?> getTypeClass() {
            return classType;
        }

        public boolean isValid(final String input) {
            return validationFunction.apply(input);
        }

        public static DataType fromName(final String name) {
            for (final DataType dataType : values()) {
                if (dataType.getName().equals(name)) return dataType;
            }
            return null;
        }
    }

}
