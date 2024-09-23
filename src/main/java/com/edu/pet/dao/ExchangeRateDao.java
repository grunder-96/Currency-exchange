package com.edu.pet.dao;

import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.ExchangeRate;
import com.edu.pet.util.ConnectionPool;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {

    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();

    private static final String FIND_ALL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM exchange_rates;
            """;

    private static final String FIND_BY_CODE_PAIR = """
            SELECT rates.id, rates.base_currency_id, rates.target_currency_id, rates.rate
            FROM exchange_rates rates
                JOIN currencies base ON rates.base_currency_id = base.id
                JOIN currencies target ON rates.target_currency_id = target.id
            WHERE base_currency_id = (SELECT currencies.id FROM currencies WHERE code LIKE ?)
                AND target_currency_id = (SELECT currencies.id FROM currencies WHERE code LIKE ?);
            """;

    private ExchangeRateDao() {

    }

    public List<ExchangeRate> findAll() throws InternalErrorException {
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {
            ResultSet resultSet = statement.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            while (resultSet.next()) {
                exchangeRates.add(buildExchangeRate(resultSet));
            }
            return exchangeRates;
        } catch (SQLException e) {
            throw new InternalErrorException("something went wrong...");
        }
    }

    public Optional<ExchangeRate> findByCodePair(String baseCurrencyCode, String targetCurrencyCode) throws InternalErrorException {
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CODE_PAIR)) {
            statement.setObject(1, baseCurrencyCode);
            statement.setObject(2, targetCurrencyCode);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? Optional.of(buildExchangeRate(resultSet)) : Optional.empty();
        } catch (SQLException e) {
            throw new InternalErrorException("something went wrong...");
        }
    }

    private ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(
                resultSet.getObject("id", Integer.class),
                resultSet.getObject("base_currency_id", Integer.class),
                resultSet.getObject("target_currency_id", Integer.class),
                resultSet.getObject("rate", BigDecimal.class)
        );
    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }
}
