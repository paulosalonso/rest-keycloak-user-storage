package com.github.paulosalonso.keycloak.userstorage.database;

import com.github.paulosalonso.keycloak.userstorage.model.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import static com.github.paulosalonso.keycloak.userstorage.configurations.Configurations.*;

@Slf4j
public class UserDAO {

    private final ConnectionFactory connectionFactory;
    private final Properties properties;
    private final ResultSetMapper resultSetMapper;

    public UserDAO(ConnectionFactory connectionFactory, Properties properties, ResultSetMapper resultSetMapper) {
        this.connectionFactory = connectionFactory;
        this.properties = properties;
        this.resultSetMapper = resultSetMapper;
    }

    public Optional<User> findById(String id) {
        return executeStatement(connection -> {
            try {
                var statement = connection.prepareStatement(prepareQuery(USER_ID_FIELD));
                statement.setString(1, id);
                statement.execute();

                var resultSet = statement.getResultSet();

                return resultSetMapper.map(resultSet);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Optional<User> findByUsername(String name) {
        return executeStatement(connection -> {
            try {
                var statement = connection.prepareStatement(prepareQuery(USER_USERNAME_FIELD));
                statement.setString(1, name);
                statement.execute();

                var resultSet = statement.getResultSet();

                return resultSetMapper.map(resultSet);
            } catch (SQLException e) {
                throw new RuntimeException("Error when finding user", e);
            }
        });
    }

    public Optional<User> findByEmail(String email) {
        return executeStatement(connection -> {
            try {
                var statement = connection.prepareStatement(prepareQuery(USER_EMAIL_FIELD));
                statement.setString(1, email);
                statement.execute();

                var resultSet = statement.getResultSet();

                return resultSetMapper.map(resultSet);
            } catch (SQLException e) {
                throw new RuntimeException("Error when finding user", e);
            }
        });
    }

    public Optional<String> findPasswordByUserId(String id) {
        return executeStatement(connection -> {
            try {
                var statement = connection.prepareStatement(prepareQuery(USER_ID_FIELD));
                statement.setString(1, id);
                statement.execute();

                var resultSet = statement.getResultSet();

                if (resultSet.next()) {
                    return Optional.of(resultSet.getString(properties.getProperty(USER_PASSWORD_FIELD)));
                }

                return Optional.empty();
            } catch (SQLException e) {
                throw new RuntimeException("Error when finding user password", e);
            }
        });
    }

    private <T> T executeStatement(Function<Connection, T> statement) {
        Connection connection = null;

        try {
            connection = connectionFactory.getConnection();
            return statement.apply(connection);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Error closing connection", e);
                }
            }
        }
    }

    private String prepareQuery(String wantedField) {
        var query = String.format("%s WHERE %s = ?", properties.getProperty(USER_QUERY), properties.getProperty(wantedField));
        log.debug("{}", query);
        return query;
    }

}
