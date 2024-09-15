package com.edu.pet.dao;

import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.Currency;
import com.edu.pet.util.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao {

    private static final CurrencyDao INSTANCE = new CurrencyDao();

    private static final String FIND_ALL = """
            SELECT id, code, full_name, sign
            FROM currencies;
            """;

    private static final String FIND_BY_CODE = """
            SELECT id, code, full_name, sign
            FROM currencies
            WHERE code LIKE ?;
            """;

    private CurrencyDao() {

    }

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }

    public List<Currency> findAll() throws InternalErrorException {
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {
            ResultSet resultSet = statement.executeQuery();
            List<Currency> currencies = new ArrayList<>();
            while (resultSet.next()) {
                currencies.add(buildCurrency(resultSet));
            }
            return currencies;
        } catch (SQLException e) {
           throw new InternalErrorException("something went wrong...");
        }
    }

    public Optional<Currency> findByCode(String code) throws InternalErrorException {
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CODE)) {
            statement.setString(1, code);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? Optional.of(buildCurrency(resultSet)) : Optional.empty();
        } catch (SQLException e) {
            throw new InternalErrorException("something went wrong...");
        }
    }

    private Currency buildCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getObject("id", Integer.class),
                resultSet.getObject("code", String.class),
                resultSet.getObject("full_name", String.class),
                resultSet.getObject("sign", String.class)
        );
    }
}
