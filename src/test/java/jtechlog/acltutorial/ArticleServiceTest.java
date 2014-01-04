package jtechlog.acltutorial;

import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml", "/applicationContext-test.xml"})
public class ArticleServiceTest {

    @Resource
    private ArticleService articleService;

    @Resource
    private MutableAclService aclService;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin1", "pass1", Collections.
                singletonList(new SimpleGrantedAuthority("ADMIN"))));

        aclService.deleteAcl(new ObjectIdentityImpl(Article.class, 1), true);
        aclService.deleteAcl(new ObjectIdentityImpl(Article.class, 2), true);

        Article article1 = new Article(1, "test");
        articleService.createArticle(article1);

        articleService.grantPermission("user1", article1, new ArticleService.Permission[]{ArticleService.Permission.READ, ArticleService.Permission.WRITE});
        articleService.grantPermission("user2", article1, new ArticleService.Permission[]{ArticleService.Permission.READ});

        Article article2 = new Article(2, "test");
        articleService.createArticle(article2);
        articleService.grantPermission("user1", article2, new ArticleService.Permission[]{ArticleService.Permission.READ, ArticleService.Permission.WRITE});
    }

    @Test(expected = NotFoundException.class)
    public void testGrantPermissionAuthenticationRequired() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user1", "pass1", Collections.
                singletonList(new SimpleGrantedAuthority("USER"))));
        articleService.grantPermission("user1", new Article(1, "test"), new ArticleService.Permission[]{ArticleService.Permission.READ, ArticleService.Permission.WRITE});
    }

    @Test
    public void testPersistence() {
        assertEquals(5, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ACL_ENTRY", Integer.class).intValue());
    }

    @Test
    public void testUserWithReadAndWrite() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user1", "pass1", Collections.
                singletonList(new SimpleGrantedAuthority("USER"))));
        articleService.findArticleById(1);
        articleService.updateArticle(new Article(1, "test"));
    }

    @Test
    public void testUserWithRead() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user2", "pass2", Collections.
                singletonList(new SimpleGrantedAuthority("USER"))));
        articleService.findArticleById(1);
        try {
            articleService.updateArticle(new Article(1, "test"));
            fail("Access denied");
        } catch (AccessDeniedException ade) {
        }
    }

    @Test
    public void testUserWithNothing() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user3", "pass3", Collections.
                singletonList(new SimpleGrantedAuthority("USER"))));
        try {
            articleService.findArticleById(1);
        } catch (AccessDeniedException ade) {
        }
        try {
            articleService.updateArticle(new Article(1, "test"));
            fail("Access denied");
        } catch (AccessDeniedException ade) {
        }
    }

    @Test
    public void testFilterAdmin() {
        List<Article> articles = articleService.findAllArticles();
        assertEquals(2, articles.size());
    }

    @Test
    public void testFilterUser1() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user1", "pass1", Collections.
                singletonList(new SimpleGrantedAuthority("USER"))));
        List<Article> articles = articleService.findAllArticles();
        assertEquals(2, articles.size());
    }

    @Test
    public void testFilterUser2() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user2", "pass2", Collections.
                singletonList(new SimpleGrantedAuthority("USER"))));
        List<Article> articles = articleService.findAllArticles();
        assertEquals(1, articles.size());
    }

    @Test
    public void testFilterUser3() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user3", "pass3", Collections.
                singletonList(new SimpleGrantedAuthority("USER"))));
        List<Article> articles = articleService.findAllArticles();
        assertEquals(0, articles.size());
    }

    @Resource
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
