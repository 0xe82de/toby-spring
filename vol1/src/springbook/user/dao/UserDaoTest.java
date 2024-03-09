package springbook.user.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * DI 컨테이너를 사용하지 않고 setUp()에서 직접 userDao 객체 생성하면 아래의 애노테이션은 사용할 필요없다.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")

/**
 * 애플리케이션 컨텍스트의 빈 객체 구성이 변경되면 동일한 설정파일을 사용하는 다른 테스트와 분리할 필요가 있다.
 * 동일한 설정파일을 사용하는 테스트 클래스들은 애플리케이션 컨텍스트를 공유하기 떄문이다.
 * DirtiesContext 애노테이션은 애플리케이션 컨텍스트가 변경되었다는 것을 다른 테스트 클래스에 알리고,
 * 다른 테스트 클래스가 애플리케이션 컨텍스트를 새로 생성하도록 한다.
 */
//@DirtiesContext
public class UserDaoTest {

    @Autowired
    UserDao dao;

    @Autowired
    DataSource dataSource;

    private User user1;

    private User user2;

    private User user3;

    @Before
    public void setUp() {
        user1 = new User("bumjin", "박범진", "springno3", Level.BASIC, 1, 0, "bumjin@gmail.com");
        user2 = new User("gyumee", "박성철", "springno1", Level.SILVER, 55, 10, "gyumee@gmail.com");
        user3 = new User("leegw700", "이길원", "springno2", Level.GOLD, 100, 40, "leegw700@gmail.com");

        /**
         * dataSource를 직접 사용하는 방법
         */
//        dao = new UserDaoJdbc();
//        DataSource dataSource = new SingleConnectionDataSource("jdbc:mysql://localhost/testdb", "spring", "book", true);
//        dao.setDataSource(dataSource);
    }

    @Test
    public void addAndGet() throws SQLException {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));


        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        User userget1 = dao.get(user1.getId());
        checkSameUser(userget1, user1);

        User userget2 = dao.get(user2.getId());
        checkSameUser(userget2, user2);
    }

    @Test
    public void count() throws SQLException {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        assertThat(dao.getCount(), is(1));

        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        dao.add(user3);
        assertThat(dao.getCount(), is(3));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getUserFailure() throws SQLException {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.get("unknown_id");
    }

    @Test
    public void getAll() {
        dao.deleteAll();

        List<User> users0 = dao.getAll();
        assertThat(users0.size(), is(0));

        dao.add(user1);
        List<User> users1 = dao.getAll();
        assertThat(users1.size(), is(1));
        checkSameUser(user1, users1.get(0));

        dao.add(user2);
        List<User> users2 = dao.getAll();
        assertThat(users2.size(), is(2));
        checkSameUser(user1, users2.get(0));
        checkSameUser(user2, users2.get(1));

        dao.add(user3);
        List<User> users3 = dao.getAll();
        assertThat(users3.size(), is(3));
        checkSameUser(user1, users3.get(0));
        checkSameUser(user2, users3.get(1));
        checkSameUser(user3, users3.get(2));
    }

    @Test(expected = DataAccessException.class)
    public void duplicateKey() {
        dao.deleteAll();

        dao.add(user1);
        dao.add(user1);
    }

    @Test
    public void sqlExceptionTranslate() {
        dao.deleteAll();

        try {
            dao.add(user1);
            dao.add(user1);
        } catch (DuplicateKeyException ex) {
            SQLException sqlEx = (SQLException) ex.getRootCause();
            SQLErrorCodeSQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            assertThat(set.translate(null, null, sqlEx), is(DuplicateKeyException.class));
        }
    }

    @Test
    public void update() {
        dao.deleteAll();

        dao.add(user1);
        dao.add(user2);

        user1.setName("오민규");
        user1.setPassword("springno6");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);

        dao.update(user1);

        User user1update = dao.get(user1.getId());
        checkSameUser(user1, user1update);
        User user2same = dao.get(user2.getId());
        checkSameUser(user2, user2same);
    }

    private void checkSameUser(User user1, User user2) {
        assertThat(user1.getId(), is(user2.getId()));
        assertThat(user1.getName(), is(user2.getName()));
        assertThat(user1.getPassword(), is(user2.getPassword()));
        assertThat(user1.getLevel(), is(user2.getLevel()));
        assertThat(user1.getLogin(), is(user2.getLogin()));
        assertThat(user1.getRecommend(), is(user2.getRecommend()));
        assertThat(user1.getEmail(), is(user2.getEmail()));
    }
}
