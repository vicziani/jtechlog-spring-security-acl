package jtechlog.acltutorial;

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

public enum ArticlePermission {

    READ(BasePermission.READ), WRITE(BasePermission.WRITE);

    private Permission basePermission;

    ArticlePermission(Permission basePermission) {
        this.basePermission = basePermission;
    }

    public Permission getBasePermission() {
        return basePermission;
    }
}
