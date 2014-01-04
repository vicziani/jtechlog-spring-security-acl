package jtechlog.acltutorial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ArticleServiceImpl implements ArticleService {

    private Logger LOGGER = LoggerFactory.getLogger(ArticleServiceImpl.class);

    private Map<Long, Article> articles = new HashMap<>();

    @Resource
    private MutableAclService aclService;

    @Override
    @Transactional
    public void grantPermission(String principal, Article article, Permission[] permissions) {
        LOGGER.info("Grant {} permission to principal {} on article {}", permissions, principal, article);
        ObjectIdentity oi = new ObjectIdentityImpl(Article.class, article.getId());
        Sid sid = new PrincipalSid(principal);

        MutableAcl acl;
        try {
            acl = (MutableAcl) aclService.readAclById(oi);
        } catch (NotFoundException nfe) {
            acl = aclService.createAcl(oi);
        }

        for (Permission permission : permissions) {
            switch (permission) {
                case READ:
                    acl.insertAce(acl.getEntries().size(), BasePermission.READ, sid, true);
                    break;
                case WRITE:
                    acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, sid, true);
                    break;
            }
        }
        aclService.updateAcl(acl);
    }

    @Override
    public void createArticle(Article article) {
        LOGGER.info("Create article: {}", article);
        articles.put(article.getId(), article);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'jtechlog.acltutorial.Article', 'read') or hasRole('ADMIN')")
    public Article findArticleById(long id) {
        return articles.get(id);
    }

    @Override
    @PreAuthorize("hasPermission(#article, 'write') or hasRole('ADMIN')")
    public void updateArticle(Article article) {
        articles.put(article.getId(), article);
    }

    @Override
    @PostFilter("hasPermission(filterObject, 'read') or hasRole('ADMIN')")
    public List<Article> findAllArticles() {
        return new ArrayList<>(articles.values());
    }
}
