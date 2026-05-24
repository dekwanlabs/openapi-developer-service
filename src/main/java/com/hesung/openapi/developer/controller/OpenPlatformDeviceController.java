package com.hesung.openapi.developer.controller;

import com.hesung.hsmf.annotation.ResultBodyWrapper;
import com.hesung.openapi.developer.application.OpenPlatformDeviceApplicationService;
import com.hesung.openapi.developer.controller.request.DeviceCommandBatchRequest;
import com.hesung.openapi.developer.controller.request.PageQuery;
import com.hesung.openapi.developer.controller.response.CapabilityDefinitionResponse;
import com.hesung.openapi.developer.controller.response.DeviceCommandAcceptedResponse;
import com.hesung.openapi.developer.controller.response.DeviceCommandResultResponse;
import com.hesung.openapi.developer.controller.response.DeviceListResponse;
import com.hesung.openapi.developer.controller.response.DeviceResponse;
import com.hesung.openapi.developer.controller.response.PageResponse;
import com.hesung.openapi.developer.controller.response.ProductResponse;
import com.hesung.openapi.developer.model.OpenPlatformCallerContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * 开放平台设备与能力模型数据面接口。
 *
 * <p>这个 Controller 面向第三方开发者调用，负责把内部设备服务能力包装成稳定的
 * OpenAPI 资源模型，包括设备发现、设备详情、状态查询、命令控制、产品模型和标准能力模型。
 * 控制器本身只保留 HTTP 协议适配，具体的访问控制、分页和能力编排统一下沉到应用层。</p>
 */
@RestController
@RequestMapping("/open")
@ResultBodyWrapper
public class OpenPlatformDeviceController {

    private final OpenPlatformDeviceApplicationService deviceApplicationService;

    public OpenPlatformDeviceController(OpenPlatformDeviceApplicationService deviceApplicationService) {
        this.deviceApplicationService = deviceApplicationService;
    }

    /**
     * 查询当前授权用户可访问的设备列表。
     *
     * <p>分页参数统一使用 current / size，并返回 current / size / total / pages / records。</p>
     */
    @GetMapping("/devices")
    public PageResponse<DeviceListResponse> devices(
            OpenPlatformCallerContext callerContext,
            @RequestParam(value = "grantId", required = false) String grantId,
            PageQuery pageQuery) {
        return deviceApplicationService.queryDevicePage(callerContext, grantId, pageQuery);
    }

    /**
     * 查询单个设备详情。
     *
     * <p>详情中会包含设备基础信息、固件信息、该设备支持的标准能力以及标准化状态。</p>
     */
    @GetMapping("/devices/{deviceId}")
    public DeviceResponse deviceDetail(OpenPlatformCallerContext callerContext,
                                       @PathVariable("deviceId") String deviceId,
                                       @RequestParam(value = "grantId", required = false) String grantId) {
        return deviceApplicationService.queryDeviceDetail(callerContext, deviceId, grantId);
    }

    /**
     * 查询单个设备的当前状态。
     *
     * <p>返回值使用开放平台标准能力 code，例如 power、fan_speed、swing，
     * 不直接暴露内部设备字段名。</p>
     */
    @GetMapping("/devices/{deviceId}/state")
    public Map<String, Object> deviceState(OpenPlatformCallerContext callerContext,
                                           @PathVariable("deviceId") String deviceId,
                                           @RequestParam(value = "grantId", required = false) String grantId) {
        return deviceApplicationService.queryDeviceState(callerContext, deviceId, grantId);
    }

    /**
     * 下发设备控制命令。
     *
     * <p>第三方传入 commands[]，每个命令使用标准 capability code。
     * Service 层会校验能力并转换成内部 desired 控制字段。</p>
     */
    @PostMapping("/devices/{deviceId}/commands")
    public DeviceCommandAcceptedResponse commands(OpenPlatformCallerContext callerContext,
                                                  @PathVariable("deviceId") String deviceId,
                                                  @RequestBody DeviceCommandBatchRequest commandRequest) {
        return deviceApplicationService.controlDevice(callerContext, deviceId, commandRequest);
    }

    @GetMapping("/devices/{deviceId}/commands/{commandId}")
    public DeviceCommandResultResponse commandResult(OpenPlatformCallerContext callerContext,
                                                     @PathVariable("deviceId") String deviceId,
                                                     @PathVariable("commandId") String commandId,
                                                     @RequestParam(value = "grantId", required = false) String grantId) {
        return deviceApplicationService.getCommandResult(callerContext, deviceId, commandId, grantId);
    }

    @PostMapping("/devices/batch")
    public List<DeviceResponse> batchDevices(OpenPlatformCallerContext callerContext, @RequestBody Map<String, Object> body) {
        return deviceApplicationService.queryBatchDevices(callerContext, body);
    }

    @PostMapping("/devices/states/batch")
    public Map<String, Map<String, Object>> batchStates(OpenPlatformCallerContext callerContext, @RequestBody Map<String, Object> body) {
        return deviceApplicationService.queryBatchStates(callerContext, body);
    }

    @PostMapping("/devices/resync")
    public Map<String, Object> resync(OpenPlatformCallerContext callerContext, @RequestBody Map<String, Object> body) {
        return deviceApplicationService.createResyncTask(callerContext, body);
    }

    /**
     * 查询开放平台标准能力列表。
     *
     * <p>该接口用于开发者了解平台支持的统一能力定义、数据类型、读写属性和枚举范围。</p>
     */
    @GetMapping("/capabilities")
    public List<CapabilityDefinitionResponse> capabilities(OpenPlatformCallerContext callerContext) {
        return deviceApplicationService.listCapabilities(callerContext);
    }

    /**
     * 查询单个标准能力详情。
     */
    @GetMapping("/capabilities/{capabilityCode}")
    public CapabilityDefinitionResponse capability(OpenPlatformCallerContext callerContext,
                                                   @PathVariable("capabilityCode") String capabilityCode) {
        return deviceApplicationService.getCapability(callerContext, capabilityCode);
    }

    /**
     * 查询开放平台产品模型列表。
     *
     * <p>产品模型用于描述某一类产品/型号可支持的标准能力，后续可接入真实产品能力规则库。</p>
     */
    @GetMapping("/products")
    public PageResponse<ProductResponse> products(
            OpenPlatformCallerContext callerContext,
            PageQuery pageQuery) {
        return deviceApplicationService.queryProductPage(callerContext, pageQuery);
    }

    /**
     * 查询单个产品模型详情。
     */
    @GetMapping("/products/{productId}")
    public ProductResponse product(OpenPlatformCallerContext callerContext, @PathVariable("productId") String productId) {
        return deviceApplicationService.queryProductDetail(callerContext, productId);
    }
}
