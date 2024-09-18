package com.edu.pet.dao;

import com.edu.pet.exception.AlreadyExistsException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.Currency;
import com.edu.pet.util.ConnectionPool;
import org.sqlite.SQLiteException;

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

    private static final String SAVE_SQL = """
            INSERT INTO currencies (code, full_name, sign)
            VALUES (upper(?), ?, ?);
            """;

    private static final String FIND_BY_ID = """
            SELECT id, code, full_name, sign
            FROM currencies
            WHERE id = ?;
            """;

    private CurrencyDao() {

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

    public Optional<Currency> findById(int id) throws InternalErrorException {
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? Optional.of(buildCurrency(resultSet)) : Optional.empty();
        } catch (SQLException e) {
            throw new InternalErrorException("something went wrong...");
        }
    }

    public Currency save(Currency currency) throws AlreadyExistsException, InternalErrorException {
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, currency.getCode());
            statement.setString(2, currency.getFullName());
            statement.setString(3, currency.getSign());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            currency.setId(resultSet.getInt(1));
            return currency;
        } catch (SQLException e) {
            if (((SQLiteException) e).getResultCode().name().equals("SQLITE_CONSTRAINT_UNIQUE")) {
                throw new AlreadyExistsException("currency with this code already exists");
            }
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

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }
}