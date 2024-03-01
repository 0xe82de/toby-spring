package springbook.user.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import springbook.user.domain.User;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDaoJdbc implements UserDao {

    private RowMapper<User> userMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            User user = new User();
            user.setId(resultSet.getString("id"));
            user.setName(resultSet.getString("name"));
            user.setPassword(resultSet.getString("password"));
            return user;
        }
    };

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override

    public void add(User user) {
        this.jdbcTemplate.update("INSERT INTO users(id, name, password) VALUES(?, ?, ?)", user.getId(), user.getName(), user.getPassword());
    }

    @Override
    public User get(String id) {
        return this.jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", new Object[]{id}, this.userMapper);
    }

    @Override
    public void deleteAll() {
        /**
         * 내장 콜백 사용
         */
        this.jdbcTemplate.update("DELETE FROM users");

        /**
         * 콜백 지정
         */
//        this.jdbcTemplate.update(new PreparedStatementCreator() {
//            @Override
//            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
//                return connection.prepareStatement("DELETE FROM users");
//            }
//        });
    }

    @Override
    public int getCount() {
        /**
         * 내장 콜백 사용
         */
        return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM users");

        /**
         * 콜백 지정
         */
//        return this.jdbcTemplate.query(new PreparedStatementCreator() {
//            @Override
//            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
//                return connection.prepareStatement("SELECT COUNT(*) FROM users");
//            }
//        }, new ResultSetExtractor<Integer>() {
//            @Override
//            public Integer extractData(ResultSet resultSet) throws SQLException, DataAccessException {
//                resultSet.next();
//                return resultSet.getInt(1);
//            }
//        });
    }

    @Override
    public List<User> getAll() {
        return this.jdbcTemplate.query("SELECT * FROM users ORDER BY id", this.userMapper);
    }
}
