package com.disk91.users.mdb.entities.sub;

import com.disk91.common.tools.CloneableObject;
import com.disk91.common.tools.HexCodingTools;
import com.disk91.common.tools.Now;
import com.disk91.users.services.UsersRolesCache;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

public class UserApiKeys implements CloneableObject<UserApiKeys> {

    @Schema(
            description = "API key id, used to identify the right key, 6 hex char, random, unique for a user",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String id;

    @Schema(
            description = "API key name, given by user, used to identify the key",
            example = "myApiKey",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
            description = "API key secret, shall not be exported in the API",
            example = "myApiKeySecret",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String secret;

    @Schema(
            description = "API key expiration date in MS since epoch, 0 means the key has been disabled",
            example = "172545052000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long expiration;

    @Schema(
            description = "The roles associated to this key to easily identify the key to remove when the user right change.",
            example = "[\"ROLE_USER_ADMIN\",\"ROLE_GROUP_ADMIN\"]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<String> roles;

    @Schema(
            description = "The ACLs associated to this key to easily identify the key to remove when the user right change.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<UserAcl> acls;

    // === FUNCTIONALITY ===

    /**
     * Init the structure
     */
    public void init() {
        this.roles = new ArrayList<String>();
        this.acls = new ArrayList<UserAcl>();
        this.id = null;
    }

    /**
     * This will delete the ability to reuse the key and kill the current JWTs using his key
     */
    public void disable() {
        this.expiration = 0; // Set expiration at 0 means the key has been expired
        this.secret = HexCodingTools.getRandomHexString(64); // new random secret
    }

    /**
     * Check if a given role has been attributed to the user
     * @param role
     */
    public boolean isInRole(String role) {
        if ( this.roles == null ) return false;
        for ( String r : this.roles ) {
            if ( r.compareTo(role) == 0 ) return true;
        }
        return false;
    }

    public boolean isInRole(UsersRolesCache.StandardRoles role) {
        return isInRole(role.getRoleName());
    }


    // === GETTER / SETTER ===

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public List<UserAcl> getAcls() {
        return acls;
    }

    public void setAcls(List<UserAcl> acls) {
        this.acls = acls;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    // === CLONE ===

    public UserApiKeys clone() {
        UserApiKeys u = new UserApiKeys();
        u.setId(this.id);
        u.setName(this.name);
        u.setSecret(this.secret);
        u.setExpiration(this.expiration);
        if (this.acls != null) {
            ArrayList<UserAcl> cf = new ArrayList<>();
            for (UserAcl c : this.acls) {
                cf.add(c.clone());
            }
            u.setAcls(cf);
        }
        if (this.roles != null) {
            ArrayList<String> rf = new ArrayList<>(this.roles);
            u.setRoles(rf);
        }
        return u;
    }

}
