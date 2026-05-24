package com.hesung.openapi.developer.domain.service;

import com.hesung.openapi.developer.controller.request.DeviceCommandBatchRequest;
import com.hesung.openapi.developer.controller.request.PageQuery;
import com.hesung.openapi.developer.controller.response.DeviceCommandAcceptedResponse;
import com.hesung.openapi.developer.controller.response.DeviceCommandResultResponse;
import com.hesung.openapi.developer.controller.response.DeviceListResponse;
import com.hesung.openapi.developer.controller.response.DeviceResponse;
import com.hesung.openapi.developer.controller.response.PageResponse;
import com.hesung.openapi.developer.controller.response.ProductResponse;
import com.hesung.openapi.developer.domain.model.OpenPlatformResolvedAccessContext;

import java.util.List;
import java.util.Map;

public interface OpenPlatformDeviceService {

    PageResponse<DeviceListResponse> queryDevicePage(OpenPlatformResolvedAccessContext accessContext, PageQuery pageQuery);

    PageResponse<ProductResponse> queryProductPage(PageQuery pageQuery);

    ProductResponse queryProductDetail(String productId);

    DeviceResponse queryDeviceDetail(OpenPlatformResolvedAccessContext accessContext, String deviceId);

    Map<String, Object> queryDeviceState(OpenPlatformResolvedAccessContext accessContext, String deviceId);

    List<DeviceResponse> queryBatchDevices(OpenPlatformResolvedAccessContext accessContext, List<String> deviceIds);

    DeviceCommandAcceptedResponse controlDevice(OpenPlatformResolvedAccessContext accessContext,
                                                String deviceId,
                                                DeviceCommandBatchRequest request);

    DeviceCommandResultResponse getCommandResult(OpenPlatformResolvedAccessContext accessContext,
                                                 String deviceId,
                                                 String commandId);

    Map<String, Map<String, Object>> queryBatchStates(OpenPlatformResolvedAccessContext accessContext, List<String> deviceIds);

    Map<String, Object> createResyncTask(OpenPlatformResolvedAccessContext accessContext, List<String> deviceIds);

    List<String> extractRequestedDeviceIds(Map<String, Object> request);

    String extractRequestGrantId(Map<String, Object> request);
}
