package com.hesung.openapi.developer.dao;

import com.hesung.openapi.developer.config.datasource.ManagementDataSourceMapper;
import com.hesung.openapi.developer.dao.entity.OpenapiUserGrantEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OpenapiUserGrantDao extends ManagementDataSourceMapper {

    @Select({
            "SELECT",
            "id,",
            "grant_id AS grantId,",
            "app_id AS appId,",
            "user_id AS userId,",
            "region,",
            "grant_scope_type AS grantScopeType,",
            "device_ids AS deviceIds,",
            "scopes,",
            "status,",
            "revoked_at AS revokedAt,",
            "created_at AS createdAt,",
            "updated_at AS updatedAt",
            "FROM t_openapi_user_grant",
            "WHERE grant_id = #{grantId}",
            "LIMIT 1"
    })
    OpenapiUserGrantEntity findByGrantId(@Param("grantId") String grantId);

    @Select({
            "SELECT",
            "id,",
            "grant_id AS grantId,",
            "app_id AS appId,",
            "user_id AS userId,",
            "region,",
            "grant_scope_type AS grantScopeType,",
            "device_ids AS deviceIds,",
            "scopes,",
            "status,",
            "revoked_at AS revokedAt,",
            "created_at AS createdAt,",
            "updated_at AS updatedAt",
            "FROM t_openapi_user_grant",
            "WHERE app_id = #{appId}",
            "ORDER BY created_at DESC"
    })
    List<OpenapiUserGrantEntity> listByAppId(@Param("appId") String appId);

    @Insert({
            "INSERT INTO t_openapi_user_grant (",
            "grant_id, app_id, user_id, region, grant_scope_type, device_ids, scopes, status, revoked_at, created_at, updated_at",
            ") VALUES (",
            "#{grantId}, #{appId}, #{userId}, #{region}, #{grantScopeType}, #{deviceIds}, #{scopes}, #{status}, #{revokedAt}, #{createdAt}, #{updatedAt}",
            ")"
    })
    int insert(OpenapiUserGrantEntity entity);

    @Update({
            "UPDATE t_openapi_user_grant",
            "SET status = #{status},",
            "revoked_at = #{revokedAt},",
            "updated_at = #{updatedAt}",
            "WHERE grant_id = #{grantId}",
            "AND app_id = #{appId}"
    })
    int updateStatus(@Param("grantId") String grantId,
                     @Param("appId") String appId,
                     @Param("status") String status,
                     @Param("revokedAt") LocalDateTime revokedAt,
                     @Param("updatedAt") LocalDateTime updatedAt);
}
