package jtechlog.acltutorial;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class H2JdbcMutableAclService extends JdbcMutableAclService {

    public H2JdbcMutableAclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
        super(dataSource, lookupStrategy, aclCache);
    }

    // Private in parent class, and has only setter
    private String insertSid = "insert into acl_sid (principal, sid) values (?, ?)";

    // Private in parent class, and has only setter
    private String selectClassPrimaryKey = "select id from acl_class where class=?";

    // Private in parent class, and has only setter
    private String selectSidPrimaryKey = "select id from acl_sid where principal=? and sid=?";

    // Private in parent class
    private static final String DEFAULT_INSERT_INTO_ACL_CLASS = "insert into acl_class (class) values (?)";

    // Private in parent class
    private static final String DEFAULT_INSERT_INTO_ACL_CLASS_WITH_ID = "insert into acl_class (class, class_id_type) values (?, ?)";

    @Override
    protected Long createOrRetrieveClassPrimaryKey(String type, boolean allowCreate, Class idType) {
        // selectClassPrimaryKey private
        List<Long> classIds = this.jdbcOperations.queryForList(selectClassPrimaryKey, Long.class, type);

        if (!classIds.isEmpty()) {
            return classIds.get(0);
        }

        if (allowCreate) {
            String sql;
            String[] params;
            if (!isAclClassIdSupported()) {
                // insertClass private
                sql = DEFAULT_INSERT_INTO_ACL_CLASS;
                params = new String[]{type};
            }
            else {
                sql = DEFAULT_INSERT_INTO_ACL_CLASS_WITH_ID;
                params = new String[]{type, idType.getCanonicalName()};
            }

            KeyHolder keyHolder = new GeneratedKeyHolder();
            this.jdbcOperations.update(connection -> {
                PreparedStatement ps =
                        connection.prepareStatement(sql,
                                Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, params[0]);
                if (params.length > 1) {
                    ps.setString(2, params[1]);
                }
                return ps;
            }, keyHolder
            );
            Assert.isTrue(TransactionSynchronizationManager.isSynchronizationActive(), "Transaction must be running");
            return (Long) keyHolder.getKeys().get("id");
        }

        return null;
    }

    protected Long createOrRetrieveSidPrimaryKey(String sidName, boolean sidIsPrincipal, boolean allowCreate) {
        List<Long> sidIds = this.jdbcOperations.queryForList(this.selectSidPrimaryKey, Long.class,
                sidIsPrincipal, sidName);
        if (!sidIds.isEmpty()) {
            return sidIds.get(0);
        }
        if (allowCreate) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            this.jdbcOperations.update(connection -> {
                PreparedStatement ps =
                        connection.prepareStatement(insertSid,
                                Statement.RETURN_GENERATED_KEYS);
                ps.setBoolean(1, sidIsPrincipal);
                ps.setString(2, sidName);
                return ps;
            }, keyHolder
            );

            Assert.isTrue(TransactionSynchronizationManager.isSynchronizationActive(), "Transaction must be running");
            return (Long) keyHolder.getKeys().get("id");
        }
        return null;
    }
}
