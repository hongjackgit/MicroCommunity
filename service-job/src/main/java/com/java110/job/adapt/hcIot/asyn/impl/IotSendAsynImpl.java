/*
 * Copyright 2017-2020 吴学文 and java110 team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.java110.job.adapt.hcIot.asyn.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.client.RestTemplate;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.machine.MachineDto;
import com.java110.dto.machine.MachineTranslateDto;
import com.java110.intf.common.IMachineAttrInnerServiceSMO;
import com.java110.intf.common.IMachineInnerServiceSMO;
import com.java110.intf.common.IMachineTranslateErrorInnerServiceSMO;
import com.java110.intf.common.IMachineTranslateInnerServiceSMO;
import com.java110.job.adapt.hcIot.GetToken;
import com.java110.job.adapt.hcIot.IotConstant;
import com.java110.job.adapt.hcIot.asyn.IIotSendAsyn;
import com.java110.po.machineTranslateError.MachineTranslateErrorPo;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * IOT信息异步同步处理实现类
 * <p>
 * 接口协议地址： https://gitee.com/java110/MicroCommunityThings/blob/master/back/docs/api.md
 *
 * @desc add by 吴学文 11:55
 */
@Service
public class IotSendAsynImpl implements IIotSendAsyn {
    private static final Logger logger = LoggerFactory.getLogger(IotSendAsynImpl.class);


    @Autowired
    private RestTemplate outRestTemplate;

    @Autowired
    private IMachineInnerServiceSMO machineInnerServiceSMOImpl;

    @Autowired
    private IMachineAttrInnerServiceSMO machineAttrInnerServiceSMOImpl;

    @Autowired
    private IMachineTranslateInnerServiceSMO machineTranslateInnerServiceSMOImpl;

    @Autowired
    private IMachineTranslateErrorInnerServiceSMO machineTranslateErrorInnerServiceSMOImpl;

    /**
     * 封装头信息
     *
     * @return
     */
    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("access_token", GetToken.get(outRestTemplate));
        //httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");

        return httpHeaders;
    }

    @Override
    @Async
    public void addCommunity(JSONObject postParameters) {
        MachineTranslateDto machineTranslateDto = new MachineTranslateDto();
        machineTranslateDto.setCommunityId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setMachineCmd(MachineTranslateDto.CMD_ADD_COMMUNITY);
        machineTranslateDto.setMachineCode("-");
        machineTranslateDto.setMachineId("-");
        machineTranslateDto.setObjId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setObjName(postParameters.getString("name"));
        machineTranslateDto.setTypeCd(MachineTranslateDto.TYPE_COMMUNITY);
        machineTranslateDto.setState(MachineTranslateDto.STATE_SUCCESS);
        machineTranslateDto.setRemark("同步物联网系统成功");
        ResponseEntity<String> responseEntity = null;
        try {
            HttpEntity httpEntity = new HttpEntity(postParameters, getHeaders());
            responseEntity = outRestTemplate.exchange(IotConstant.ADD_COMMUNITY_URL, HttpMethod.POST, httpEntity, String.class);
            logger.debug("调用HC IOT信息：" + responseEntity);
            JSONObject paramOut = JSONObject.parseObject(responseEntity.getBody());

            if (paramOut.getInteger("code") != ResultVo.CODE_OK) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(paramOut.getString("msg"));
            }
        } catch (Exception e) {
            machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
            machineTranslateDto.setRemark(e.getLocalizedMessage());
            //保存 失败报文
            saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");
        } finally {
            saveTranslateLog(machineTranslateDto);
        }
    }

    @Override
    @Async
    public void editCommunity(JSONObject postParameters) {
        MachineTranslateDto machineTranslateDto = new MachineTranslateDto();
        machineTranslateDto.setCommunityId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setMachineCmd(MachineTranslateDto.CMD_UPDATE_COMMUNITY);
        machineTranslateDto.setMachineCode("-");
        machineTranslateDto.setMachineId("-");
        machineTranslateDto.setObjId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setObjName(postParameters.getString("name"));
        machineTranslateDto.setTypeCd(MachineTranslateDto.TYPE_COMMUNITY);
        machineTranslateDto.setState(MachineTranslateDto.STATE_SUCCESS);
        machineTranslateDto.setRemark("同步物联网系统成功");
        ResponseEntity<String> responseEntity = null;
        try {
            HttpEntity httpEntity = new HttpEntity(postParameters, getHeaders());
            responseEntity = outRestTemplate.exchange(IotConstant.UPDATE_COMMUNITY_URL, HttpMethod.POST, httpEntity, String.class);
            logger.debug("调用HC IOT信息：" + responseEntity);
            JSONObject paramOut = JSONObject.parseObject(responseEntity.getBody());
            if (paramOut.getInteger("code") != ResultVo.CODE_OK) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(paramOut.getString("msg"));
                //保存 失败报文
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");
            }
        } catch (Exception e) {
            machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
            machineTranslateDto.setRemark(e.getLocalizedMessage());
            //保存 失败报文
            saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

        } finally {
            saveTranslateLog(machineTranslateDto);
        }
    }

    @Override
    @Async
    public void deleteCommunity(JSONObject postParameters) {
        MachineTranslateDto machineTranslateDto = new MachineTranslateDto();
        machineTranslateDto.setCommunityId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setMachineCmd(MachineTranslateDto.CMD_DELETE_COMMUNITY);
        machineTranslateDto.setMachineCode("-");
        machineTranslateDto.setMachineId("-");
        machineTranslateDto.setObjId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setObjName(postParameters.getString("name"));
        machineTranslateDto.setTypeCd(MachineTranslateDto.TYPE_COMMUNITY);
        machineTranslateDto.setState(MachineTranslateDto.STATE_SUCCESS);
        machineTranslateDto.setRemark("同步物联网系统成功");
        ResponseEntity<String> responseEntity = null;
        try {
            HttpEntity httpEntity = new HttpEntity(postParameters, getHeaders());
            responseEntity = outRestTemplate.exchange(IotConstant.DELETE_COMMUNITY_URL, HttpMethod.POST, httpEntity, String.class);
            logger.debug("调用HC IOT信息：" + responseEntity);
            JSONObject paramOut = JSONObject.parseObject(responseEntity.getBody());
            if (paramOut.getInteger("code") != ResultVo.CODE_OK) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(paramOut.getString("msg"));
                //保存 失败报文
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");
            }
        } catch (Exception e) {
            machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
            machineTranslateDto.setRemark(e.getLocalizedMessage());
            //保存 失败报文
            saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");
        } finally {
            saveTranslateLog(machineTranslateDto);
        }
    }

    /**
     * 添加设备
     *
     * @param postParameters
     * @param ownerDtos
     */
    @Override
    @Async
    public void addMachine(JSONObject postParameters, List<JSONObject> ownerDtos) {
        MachineTranslateDto machineTranslateDto = new MachineTranslateDto();
        machineTranslateDto.setCommunityId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setMachineCmd(MachineTranslateDto.CMD_ADD_MACHINE);
        machineTranslateDto.setMachineCode(postParameters.getString("machineCode"));
        machineTranslateDto.setMachineId(postParameters.getString("extMachineId"));
        machineTranslateDto.setObjId(postParameters.getString("extMachineId"));
        machineTranslateDto.setObjName(postParameters.getString("machineName"));
        machineTranslateDto.setTypeCd(MachineTranslateDto.TYPE_MACHINE);
        machineTranslateDto.setState(MachineTranslateDto.STATE_SUCCESS);
        machineTranslateDto.setRemark("同步物联网系统成功");
        ResponseEntity<String> responseEntity = null;
        try {
            HttpEntity httpEntity = new HttpEntity(postParameters, getHeaders());
            responseEntity = outRestTemplate.exchange(IotConstant.ADD_MACHINE_URL, HttpMethod.POST, httpEntity, String.class);

            logger.debug("调用HC IOT信息：" + responseEntity);

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(responseEntity.getBody());
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");
                return;
            }
            JSONObject tokenObj = JSONObject.parseObject(responseEntity.getBody());

            if (!tokenObj.containsKey("code") || ResultVo.CODE_OK != tokenObj.getInteger("code")) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(tokenObj.getString("msg"));
                //保存 失败报文
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

                return;
            }
        } catch (Exception e) {
            machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
            machineTranslateDto.setRemark(e.getLocalizedMessage());
            //保存 失败报文
            saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

            return;
        } finally {
            saveTranslateLog(machineTranslateDto);
        }

        MachineDto machinePo = new MachineDto();
        machinePo.setMachineId(postParameters.getString("extMachineId"));
        machinePo.setCommunityId(postParameters.getString("extCommunityId"));
        machinePo.setState("1700");
        machineInnerServiceSMOImpl.updateMachineState(machinePo);

        for (JSONObject owner : ownerDtos) {
            addOwner(owner);
        }
    }

    @Override
    @Async
    public void updateMachine(JSONObject postParameters) {
        MachineTranslateDto machineTranslateDto = new MachineTranslateDto();
        machineTranslateDto.setCommunityId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setMachineCmd(MachineTranslateDto.CMD_UPDATE_MACHINE);
        machineTranslateDto.setMachineCode(postParameters.getString("machineCode"));
        machineTranslateDto.setMachineId(postParameters.getString("extMachineId"));
        machineTranslateDto.setObjId(postParameters.getString("extMachineId"));
        machineTranslateDto.setObjName(postParameters.getString("machineName"));
        machineTranslateDto.setTypeCd(MachineTranslateDto.TYPE_MACHINE);
        machineTranslateDto.setState(MachineTranslateDto.STATE_SUCCESS);
        machineTranslateDto.setRemark("同步物联网系统成功");
        ResponseEntity<String> responseEntity = null;
        try {
            HttpEntity httpEntity = new HttpEntity(postParameters, getHeaders());
            responseEntity = outRestTemplate.exchange(IotConstant.UPDATE_MACHINE_URL, HttpMethod.POST, httpEntity, String.class);

            logger.debug("调用HC IOT信息：" + responseEntity);

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(responseEntity.getBody());
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

                return;
            }
            JSONObject tokenObj = JSONObject.parseObject(responseEntity.getBody());

            if (!tokenObj.containsKey("code") || ResultVo.CODE_OK != tokenObj.getInteger("code")) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(tokenObj.getString("msg"));
                //保存 失败报文
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

            }
        } catch (Exception e) {
            machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
            machineTranslateDto.setRemark(e.getLocalizedMessage());
            //保存 失败报文
            saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");
        } finally {
            saveTranslateLog(machineTranslateDto);
        }
    }

    @Override
    public void deleteMachine(JSONObject postParameters) {
        MachineTranslateDto machineTranslateDto = new MachineTranslateDto();
        machineTranslateDto.setCommunityId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setMachineCmd(MachineTranslateDto.CMD_DELETE_MACHINE);
        machineTranslateDto.setMachineCode(postParameters.getString("machineCode"));
        machineTranslateDto.setMachineId(postParameters.getString("extMachineId"));
        machineTranslateDto.setObjId(postParameters.getString("extMachineId"));
        machineTranslateDto.setObjName(postParameters.getString("machineName"));
        machineTranslateDto.setTypeCd(MachineTranslateDto.TYPE_MACHINE);
        machineTranslateDto.setState(MachineTranslateDto.STATE_SUCCESS);
        machineTranslateDto.setRemark("同步物联网系统成功");
        ResponseEntity<String> responseEntity = null;
        try {
            HttpEntity httpEntity = new HttpEntity(postParameters, getHeaders());
            responseEntity = outRestTemplate.exchange(IotConstant.DELETE_MACHINE_URL, HttpMethod.POST, httpEntity, String.class);
            logger.debug("调用HC IOT信息：" + responseEntity);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(responseEntity.getBody());
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

                return;
            }
            JSONObject tokenObj = JSONObject.parseObject(responseEntity.getBody());

            if (!tokenObj.containsKey("code") || ResultVo.CODE_OK != tokenObj.getInteger("code")) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(tokenObj.getString("msg"));
                //保存 失败报文
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

            }
        } catch (Exception e) {
            machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
            machineTranslateDto.setRemark(e.getLocalizedMessage());
            //保存 失败报文
            saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");
        } finally {
            saveTranslateLog(machineTranslateDto);
        }
    }

    @Override
    public void addOwner(JSONObject postParameters) {

        MachineTranslateDto machineTranslateDto = new MachineTranslateDto();
        machineTranslateDto.setCommunityId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setMachineCmd(MachineTranslateDto.CMD_ADD_OWNER_FACE);
        machineTranslateDto.setMachineCode(postParameters.getString("machineCode"));
        machineTranslateDto.setMachineId(postParameters.getString("extMachineId"));
        machineTranslateDto.setObjId(postParameters.getString("userId"));
        machineTranslateDto.setObjName(postParameters.getString("name"));
        machineTranslateDto.setTypeCd(MachineTranslateDto.TYPE_OWNER);
        machineTranslateDto.setState(MachineTranslateDto.STATE_SUCCESS);
        machineTranslateDto.setRemark("同步物联网系统成功");
        ResponseEntity<String> responseEntity = null;
        try {
            HttpEntity httpEntity = new HttpEntity(postParameters, getHeaders());
            responseEntity = outRestTemplate.exchange(IotConstant.ADD_OWNER, HttpMethod.POST, httpEntity, String.class);

            logger.debug("调用HC IOT信息：" + responseEntity);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(responseEntity.getBody());
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");
                return;
            }
            JSONObject tokenObj = JSONObject.parseObject(responseEntity.getBody());

            if (!tokenObj.containsKey("code") || ResultVo.CODE_OK != tokenObj.getInteger("code")) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(tokenObj.getString("msg"));
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

                //保存 失败报文
            }
        } catch (Exception e) {
            machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
            machineTranslateDto.setRemark(e.getLocalizedMessage());
            //保存 失败报文
            saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");


        } finally {
            saveTranslateLog(machineTranslateDto);
        }
    }

    @Override
    public void sendUpdateOwner(JSONObject postParameters) {
        MachineTranslateDto machineTranslateDto = new MachineTranslateDto();
        machineTranslateDto.setCommunityId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setMachineCmd(MachineTranslateDto.CMD_UPDATE_OWNER_FACE);
        machineTranslateDto.setMachineCode(postParameters.getString("machineCode"));
        machineTranslateDto.setMachineId(postParameters.getString("extMachineId"));
        machineTranslateDto.setObjId(postParameters.getString("userId"));
        machineTranslateDto.setObjName(postParameters.getString("name"));
        machineTranslateDto.setTypeCd(MachineTranslateDto.TYPE_OWNER);
        machineTranslateDto.setState(MachineTranslateDto.STATE_SUCCESS);
        machineTranslateDto.setRemark("同步物联网系统成功");
        ResponseEntity<String> responseEntity = null;
        try {
            HttpEntity httpEntity = new HttpEntity(postParameters, getHeaders());
            responseEntity = outRestTemplate.exchange(IotConstant.EDIT_OWNER, HttpMethod.POST, httpEntity, String.class);
            logger.debug("调用HC IOT信息：" + responseEntity);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(responseEntity.getBody());
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

                return;
            }
            JSONObject tokenObj = JSONObject.parseObject(responseEntity.getBody());

            if (!tokenObj.containsKey("code") || ResultVo.CODE_OK != tokenObj.getInteger("code")) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(tokenObj.getString("msg"));
                //保存 失败报文
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

            }
        } catch (Exception e) {
            machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
            machineTranslateDto.setRemark(e.getLocalizedMessage());
            //保存 失败报文
            saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

        } finally {
            saveTranslateLog(machineTranslateDto);
        }
    }

    @Override
    public void sendDeleteOwner(JSONObject postParameters) {

        MachineTranslateDto machineTranslateDto = new MachineTranslateDto();
        machineTranslateDto.setCommunityId(postParameters.getString("extCommunityId"));
        machineTranslateDto.setMachineCmd(MachineTranslateDto.CMD_UPDATE_OWNER_FACE);
        machineTranslateDto.setMachineCode(postParameters.getString("machineCode"));
        machineTranslateDto.setMachineId(postParameters.getString("extMachineId"));
        machineTranslateDto.setObjId(postParameters.getString("userId"));
        machineTranslateDto.setObjName(postParameters.getString("name"));
        machineTranslateDto.setTypeCd(MachineTranslateDto.TYPE_OWNER);
        machineTranslateDto.setState(MachineTranslateDto.STATE_SUCCESS);
        machineTranslateDto.setRemark("同步物联网系统成功");
        ResponseEntity<String> responseEntity = null;
        try {
            HttpEntity httpEntity = new HttpEntity(postParameters, getHeaders());
            responseEntity = outRestTemplate.exchange(IotConstant.DELETE_OWNER, HttpMethod.POST, httpEntity, String.class);

            logger.debug("调用HC IOT信息：" + responseEntity);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(responseEntity.getBody());
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

                return;
            }
            JSONObject tokenObj = JSONObject.parseObject(responseEntity.getBody());

            if (!tokenObj.containsKey("code") || ResultVo.CODE_OK != tokenObj.getInteger("code")) {
                machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
                machineTranslateDto.setRemark(tokenObj.getString("msg"));
                //保存 失败报文
                saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");

            }
        } catch (Exception e) {
            machineTranslateDto.setState(MachineTranslateDto.STATE_ERROR);
            machineTranslateDto.setRemark(e.getLocalizedMessage());
            //保存 失败报文
            saveTranslateError(machineTranslateDto, postParameters.toJSONString(), responseEntity != null ? responseEntity.getBody() : "");


        } finally {
            saveTranslateLog(machineTranslateDto);
        }
    }

    /**
     * 存储交互 记录
     *
     * @param machineTranslateDto
     */
    public void saveTranslateLog(MachineTranslateDto machineTranslateDto) {
        machineTranslateDto.setbId("-1");
        if (StringUtil.isEmpty(machineTranslateDto.getMachineTranslateId())) {
            machineTranslateDto.setMachineTranslateId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_machineTranslateId));
        }
        machineTranslateDto.setObjBId("-1");
        machineTranslateDto.setUpdateTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        machineTranslateInnerServiceSMOImpl.saveMachineTranslate(machineTranslateDto);
    }

    public void saveTranslateError(MachineTranslateDto machineTranslateDto, String reqJson, String resJson) {
        machineTranslateDto.setMachineTranslateId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_machineTranslateId));
        MachineTranslateErrorPo machineTranslateErrorPo = new MachineTranslateErrorPo();
        machineTranslateErrorPo.setLogId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_logId));
        machineTranslateErrorPo.setCommunityId(machineTranslateDto.getCommunityId());
        machineTranslateErrorPo.setMachineTranslateId(machineTranslateDto.getMachineTranslateId());
        machineTranslateErrorPo.setReqBody(reqJson);
        machineTranslateErrorPo.setReqHeader("");
        machineTranslateErrorPo.setResBody(resJson);
        machineTranslateErrorInnerServiceSMOImpl.saveMachineTranslateError(machineTranslateErrorPo);
    }


}
