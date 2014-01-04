package jtechlog.acltutorial;

/**
 *
 */
public class Article {

    private Long id;

    private String text;

    @Override
    public String toString() {
        return id + " " + text;
    }

    public Article(long id, String text) {
        this.id = id;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
