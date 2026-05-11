package com.arcabank.core_finance.utils;

public class RoutingRegistry {

    private RoutingRegistry() {}

    public static final String ACCOUNTS_RESOURCE = "/accounts";
    public static final String CARDS_RESOURCE = "/cards";
    public static final String CHESTS_RESOURCE = "/chests";

    public static final class Api {
        public static final String V1 = "/api/v1";

        public static final class Accounts {
            public static final String BASE = V1 + ACCOUNTS_RESOURCE;
            public static final String ALL = "/all";
        }

        public static final class Cards {
            public static final String BASE = V1 + CARDS_RESOURCE;
        }
    }

    public enum AppRoute {

        ACCOUNT_DETAILS(ACCOUNTS_RESOURCE + "/%s"),
        CARD_DETAILS(CARDS_RESOURCE + "/%s"),
        CHEST_DETAILS(CHESTS_RESOURCE + "/%s");

        private final String pathTemplate;

        AppRoute(String pathTemplate) {
            this.pathTemplate = pathTemplate;
        }

        public String build(Object... args) {
            if (args == null || args.length == 0) return pathTemplate;
            return String.format(pathTemplate, args);
        }
    }

}
