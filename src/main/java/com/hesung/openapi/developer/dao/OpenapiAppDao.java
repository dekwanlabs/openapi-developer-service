package com.hesung.openapi.developer.dao;

import com.hesung.openapi.developer.config.datasource.ManagementDataSourceMapper;
import com.hesung.openapi.developer.dao.entity.OpenapiAppEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OpenapiAppDao extends ManagementDataSourceMapper {

    @Select({
            "SELECT",
            "id,",
            "app_id AS appId,",
            "client_id AS clientId,",
            "app_name AS appName,",
            "app_type AS appType,",
            "status",
            "FROM t_openapi_app",
            "WHERE client_id = #{clientId}",
            "LIMIT 1"
    })
    OpenapiAppEntity findByClientId(@Param("clientId") String clientId);
}
