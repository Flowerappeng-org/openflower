package org.lowcoder.domain.organization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.lowcoder.domain.mongodb.AfterMongodbRead;
import org.lowcoder.domain.mongodb.BeforeMongodbWrite;
import org.lowcoder.domain.mongodb.MongodbInterceptorContext;
import org.lowcoder.sdk.auth.AbstractAuthConfig;
import org.lowcoder.sdk.models.HasIdAndAuditing;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.lowcoder.infra.util.AssetUtils.toAssetPath;


@Getter
@Setter
@NoArgsConstructor
@Document
@Jacksonized
@SuperBuilder
public class Organization extends HasIdAndAuditing implements BeforeMongodbWrite, AfterMongodbRead {

    private static final OrganizationCommonSettings EMPTY_SETTINGS = new OrganizationCommonSettings();
    @Getter
    private String gid;

    private String slug;

    private String name;

    private Boolean isAutoGeneratedOrganization;

    private String contactName;

    private String contactEmail;

    private String contactPhoneNumber;

    private Boolean isEmailDisabled;

    public Boolean getIsEmailDisabled() {
        if(isEmailDisabled == null) return false;
        else return isEmailDisabled;
    }

    @JsonIgnore
    private String logoAssetId;

    public String getLogoUrl() {
        return toAssetPath(logoAssetId);
    }

    private String source; // if it created from third party login

    private String thirdPartyCompanyId;

    private OrganizationState state;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private OrganizationDomain organizationDomain;

    private OrganizationCommonSettings commonSettings;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public void afterMongodbRead(MongodbInterceptorContext context) {
        ofNullable(getOrganizationDomain())
                .ifPresent(domain -> domain.afterMongodbRead(context));
    }

    @Override
    public void beforeMongodbWrite(MongodbInterceptorContext context) {
        ofNullable(getOrganizationDomain())
                .ifPresent(domain -> domain.beforeMongodbWrite(context));
    }

    public OrganizationCommonSettings getCommonSettings() {
        return firstNonNull(commonSettings, EMPTY_SETTINGS);
    }

    public static class OrganizationCommonSettings extends HashMap<String, Object> {
        public static final String PASSWORD_RESET_EMAIL_TEMPLATE = "PASSWORD_RESET_EMAIL_TEMPLATE";

        /**
         * Settings excluded from sanitized export
         */
        private final Set<String> excludedKeys = Set.of(
            PASSWORD_RESET_EMAIL_TEMPLATE
        );
        public OrganizationCommonSettings sanitized() {
            OrganizationCommonSettings sanitized = new OrganizationCommonSettings();
            if (isEmpty()) {
                return sanitized;
            }
            this.entrySet().stream()
                    .filter((entry) -> !excludedKeys.contains(entry.getKey()))
                    .forEach((entry) -> sanitized.put(entry.getKey(), entry.getValue()));
            return sanitized;
        }
    }

    public long getCreateTime() {
        return createdAt != null ? createdAt.toEpochMilli() : 0;
    }

    public List<AbstractAuthConfig> getAuthConfigs() {
        return Optional.ofNullable(organizationDomain)
                .map(OrganizationDomain::getConfigs)
                .orElse(Collections.emptyList());
    }
}
