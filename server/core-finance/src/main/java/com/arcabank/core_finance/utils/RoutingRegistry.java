package com.arcabank.core_finance.utils;

public class RoutingRegistry {

    private RoutingRegistry() {
    }

    public static final String ACCOUNTS_RESOURCE = "/accounts";
    public static final String CARDS_RESOURCE = "/cards";
    public static final String TRANSFERS_RESOURCE = "/transfers";
    public static final String TRANSACTIONS_RESOURCE = "/transactions";
    public static final String EXCHANGE_RATES_RESOURCE = "/exchange-rates";
    public static final String HEALTH_RESOURCE = "/health";
    public static final String CHESTS = "/chests";

    public static final String V1 = "/api/v1";

    public static final class Api {

        public static final class Accounts {
            public static final String BASE = V1 + ACCOUNTS_RESOURCE;
            public static final String ALL = "/all";
            public static final String CREATE_WITH_CARD = "/with-card";
            public static final String BY_ID = "/{id}";
            public static final String ACCOUNT_TRANSACTIONS = "/{accountId}/transactions";
            public static final String ACCOUNT_CARDS = "/{accountId}/cards";
            public static final String BLOCK = "/{accountId}/block";
            public static final String UNBLOCK = "/{accountId}/unblock";
            public static final String STATEMENT_PDF = "/{accountId}/statement/pdf";
        }

        public static final class Cards {
            public static final String BASE = V1 + CARDS_RESOURCE;
            public static final String ALL = "/all";
            public static final String BY_ID = "/{cardId}";
            public static final String CREATE = "/{accountId}/create";
            public static final String BLOCK = "/{cardId}/block";
            public static final String UNBLOCK = "/{cardId}/unblock";
        }

        public static final class Transfers {
            public static final String BASE = V1 + TRANSFERS_RESOURCE;
            public static final String TRANSACTION = "/transaction";
            public static final String EXCHANGE = "/exchange";
            public static final String HISTORY = "/history";
        }

        public static final class Transactions {
            public static final String BASE = V1 + TRANSACTIONS_RESOURCE;
            public static final String DEPOSIT = "/deposit";
        }

        public static final class Chests {
            public static final String BASE = V1 + CHESTS;
        }

        public static final class ExchangeRates {
            public static final String BASE = V1 + EXCHANGE_RATES_RESOURCE;
        }

        public static final class System {
            public static final String HEALTH = HEALTH_RESOURCE;
        }
    }

    public enum AppRoute {
        ACCOUNT_DETAILS(V1 + ACCOUNTS_RESOURCE + "/%s"),
        CARD_DETAILS(V1 + CARDS_RESOURCE + "/%s"),
        TRANSFERS_HISTORY(V1 + TRANSFERS_RESOURCE + "/history");

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
