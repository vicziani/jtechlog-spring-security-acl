package jtechlog.acltutorial;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ArticleService {

    private ArticleRepository articleRepository;

    private MutableAclService aclService;

    @Transactional
    public void grantPermission(String principal, Article article, ArticlePermission... permissions) {
        log.info("Grant {} permission to principal {} on article {}", permissions, principal, article);
        ObjectIdentity oi = new ObjectIdentityImpl(Article.class, article.getId());
        Sid sid = new PrincipalSid(principal);

        MutableAcl acl;
        try {
            acl = (MutableAcl) aclService.readAclById(oi);
        } catch (NotFoundException nfe) {
            acl = aclService.createAcl(oi);
        }

        for (ArticlePermission permission : permissions) {
            acl.insertAce(acl.getEntries().size(), permission.getBasePermission(), sid, true);
        }
        aclService.updateAcl(acl);
    }

    public void createArticle(Article article) {
        log.info("Create article: {}", article);
        articleRepository.save(article);
    }

    @PreAuthorize("hasPermission(#id, 'jtechlog.acltutorial.Article', 'READ') or hasRole('ADMIN')")
    public Article findArticleById(long id) {
        return articleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Can not find article with id: %d".formatted(id)));
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'jtechlog.acltutorial.Article', 'WRITE') or hasRole('ADMIN')")
    public void updateArticle(long id, String text) {
        Article article = articleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Can not find article with id: %d".formatted(id)));
        article.setText(text);
    }

    @PostFilter("hasPermission(filterObject, 'READ') or hasRole('ADMIN')")
    public List<Article> findAllArticles() {
        return articleRepository.findAll();
    }

}
