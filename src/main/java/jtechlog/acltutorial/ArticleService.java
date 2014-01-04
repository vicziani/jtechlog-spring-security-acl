package jtechlog.acltutorial;

import java.util.List;

public interface ArticleService {

    public enum Permission {
        READ, WRITE
    }

    public void createArticle(Article article);

    public Article findArticleById(long id);

    public List<Article> findAllArticles();

    public void updateArticle(Article article);

    public void grantPermission(String principal, Article article, Permission[] permissions);
}
