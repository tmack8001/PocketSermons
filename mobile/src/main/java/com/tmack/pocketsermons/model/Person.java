package com.tmack.pocketsermons.model;

import com.tmack.pocketsermons.utils.StringUtils;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class Person {
    private String permalink;
    private String givenName; // aka firstName
    private String familyName; // aka lastName

    private String honorificPrefix;
    private String honorificSuffix;

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getHonorificPrefix() {
        return honorificPrefix;
    }

    public void setHonorificPrefix(String honorificPrefix) {
        this.honorificPrefix = honorificPrefix;
    }

    public String getHonorificSuffix() {
        return honorificSuffix;
    }

    public void setHonorificSuffix(String honorificSuffix) {
        this.honorificSuffix = honorificSuffix;
    }

    public String getFullName() {
        return givenName + " " + familyName;
    }

    public String getFormattedName() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(honorificPrefix)) {
            sb.append(honorificPrefix).append(" ");
        }
        if(StringUtils.isNotEmpty(givenName)) {
            sb.append(givenName).append(" ");
        }
        if(StringUtils.isNotEmpty(familyName)) {
            sb.append(familyName).append(" ");
        }
        if(StringUtils.isNotEmpty(honorificSuffix)) {
            sb.append(honorificSuffix);
        }
        return sb.toString();
    }
}
