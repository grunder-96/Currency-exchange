package com.edu.pet.dao;

import com.edu.pet.exception.AlreadyExistsException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.Currency;
import com.edu.pet.model.ExchangeRate;
import com.edu.pet.util.ConnectionPool;
import org.sqlite.SQLiteException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {

    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();

    private static final String FIND_ALL = """
            SELECT
               er.id er_id,
               b.id b_id, b.code b_code, b.full_name b_full_name, b.sign b_sign,
               t.id t_id, t.code t_code, t.full_name t_full_name, t.sign t_sign,
               er.rate er_rate
            FROM exchange_rates er
                JOIN currencies b ON er.base_currency_id = b.id
                JOIN currencies t ON er.target_currency_id = t.id;
            """;

    private static final String FIND_BY_CODE_PAIR = """
            SELECT
               er.id er_id,
               b.id b_id, b.code b_code, b.full_name b_full_name, b.sign b_sign,
               t.id t_id, t.code t_code, t.full_name t_full_name, t.sign t_sign,
               er.rate er_rate
            FROM exchange_rates er
                JOIN currencies b ON er.base_currency_id = b.id
                JOIN currencies t ON er.target_currency_id = t.id
            WHERE base_currency_id = (SELECT id FROM currencies WHERE code LIKE ?)
                AND target_currency_id = (SELECT id FROM currencies WHERE code LIKE ?);
            """;

    private static final String SAVE_SQL = """
            INSERT INTO exchange_rates(base_currency_id, target_currency_id, rate) VALUES (?, ?, ?);
            """;

    private static final int CONSTRAINT_UNIQUE_CODE = 2067;

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

    public ExchangeRate save(ExchangeRate exchangeRate) throws InternalErrorException, AlreadyExistsException {
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_SQL)) {
            statement.setObject(1, exchangeRate.getBaseCurrency().getId());
            statement.setObject(2, exchangeRate.getTargetCurrency().getId());
            statement.setObject(3, exchangeRate.getRate());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            exchangeRate.setId(resultSet.getInt(1));
            return exchangeRate;
        } catch (SQLException e) {
            if (((SQLiteException) e).getResultCode().code == CONSTRAINT_UNIQUE_CODE) {
                throw new AlreadyExistsException("currency pair with this code already exists");
            }
            throw new InternalErrorException("something went wrong...");
        }
    }

    private ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
        Currency baseCurrency = new Currency(
            resultSet.getObject("b_id", Integer.class),
            resultSet.getObject("b_code", String.class),
            resultSet.getObject("b_full_name", String.class),
            resultSet.getObject("b_sign", String.class)
        );

        Currency targetCurrency = new Currency(
            resultSet.getObject("t_id", Integer.class),
            resultSet.getObject("t_code", String.class),
            resultSet.getObject("t_full_name", String.class),
            resultSet.getObject("t_sign", String.class)
        );

        return new ExchangeRate(
            resultSet.getObject("er_id", Integer.class),
            baseCurrency,
            targetCurrency,
            resultSet.getObject("er_rate", BigDecimal.class)
        );
    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }
}
