package dev.xing.infinitechat.authenticationservice.service.impl;

import javax.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import dev.xing.infinitechat.common.ErrorEnum;
import dev.xing.infinitechat.exception.ThrowUtils;
import dev.xing.infinitechat.authenticationservice.constants.OSSConstant;
import dev.xing.infinitechat.authenticationservice.mapper.UserBalanceMapper;
import dev.xing.infinitechat.authenticationservice.mapper.UserMapper;
import dev.xing.infinitechat.authenticationservice.model.dto.user.*;
import dev.xing.infinitechat.authenticationservice.model.entity.User;
import dev.xing.infinitechat.authenticationservice.model.entity.UserBalance;
import dev.xing.infinitechat.authenticationservice.model.enums.ConfigEnum;
import dev.xing.infinitechat.authenticationservice.model.enums.TimeOutEnum;
import dev.xing.infinitechat.authenticationservice.model.vo.user.UploadUrlResponse;
import dev.xing.infinitechat.authenticationservice.model.vo.user.UserVO;
import dev.xing.infinitechat.authenticationservice.service.UserService;
import dev.xing.infinitechat.authenticationservice.utils.*;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

/**
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2024-08-04 21:20:40
 */
@Service
@SuppressWarnings({"all"})
public class UserServiceImpl implements UserService {

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OSSUtils ossUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ServiceInstanceUtil serviceInstanceUtil;


    @Override
    public void sendClientSms(UserSMSRequest userSMSRequest) {
        String code = new RandomNumUtil().getRandomNum();
        SMSUtil sms = new SMSUtil();
        try {
            sms.sendServiceSms(userSMSRequest.getPhone(), code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        redisTemplate.opsForValue().set(userSMSRequest.getPhone(), code, TimeOutEnum.CODE_TIME_OUT.getTimeOut(), TimeUnit.MINUTES);
    }




    @Override
    public UserVO userLoginPwd(UserLoginPwdRequest userLoginPwdRequest, HttpServletResponse response) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", userLoginPwdRequest.getPhone());
        User user = userMapper.selectOne(queryWrapper);
        String encryptedPassword = DigestUtils.md5DigestAsHex((ConfigEnum.PASSWORD_SALT.getValue() + userLoginPwdRequest.getPassword()).getBytes());
        ThrowUtils.throwIf(user == null || !encryptedPassword.equals(user.getPassword()), ErrorEnum.LOGIN_ERROR);

        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        // 生成token
        String token = JwtUtil.generate(userVO.getUserId());
        userVO.setToken(token);
        redisTemplate.opsForValue().set(userVO.getUserId().toString(), token, TimeOutEnum.TOKEN_TIME_OUT.getTimeOut(), TimeUnit.DAYS);
        // 获取服务实例
        String nettyHost = serviceInstanceUtil.getServiceInstance(userVO.getUserId());
        ThrowUtils.throwIf(nettyHost == null, ErrorEnum.SYSTEM_ERROR);
        String nettyUrl = ConfigEnum.NETTY_PROTOCOL.getValue() + nettyHost + ConfigEnum.NETTY_PORT.getValue();
        userVO.setNettyUrl(nettyUrl);
        return userVO;
    }


    @Override
    public UserVO userLoginCode(UserLoginCodeRequest userLoginCodeRequest, HttpServletResponse response) {
        String redisCode = redisTemplate.opsForValue().get(userLoginCodeRequest.getPhone());
        ThrowUtils.throwIf(redisCode == null || !redisCode.equals(userLoginCodeRequest.getCode()), ErrorEnum.CODE_ERROR);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", userLoginCodeRequest.getPhone());
        User user = userMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorEnum.LOGIN_ERROR);


        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        // 生成token
        String token = JwtUtil.generate(userVO.getUserId());
        userVO.setToken(token);
        redisTemplate.opsForValue().set(userVO.getUserId(), token, TimeOutEnum.TOKEN_TIME_OUT.getTimeOut(), TimeUnit.DAYS);
        // 获取服务实例
        String nettyHost = serviceInstanceUtil.getServiceInstance(userVO.getUserId());

        ThrowUtils.throwIf(nettyHost == null, ErrorEnum.SYSTEM_ERROR);
        String nettyUrl = ConfigEnum.NETTY_PROTOCOL.getValue() + nettyHost + ConfigEnum.NETTY_PORT.getValue();
        userVO.setNettyUrl(nettyUrl);
        return userVO;
    }

    /***
     * @MethodName userRegister
     * @Description 用户注册
     * @param: phone
     * @param: password
     * @return: boolean
     * @Date 2025/1/5 14:12
     */
    @Override
    @Transactional
    public void userRegister(UserRegisterRequest userRegisterRequest) throws InterruptedException {
        String phone = userRegisterRequest.getPhone();
        String password = userRegisterRequest.getPassword();

        String redisCode = redisTemplate.opsForValue().get(userRegisterRequest.getPhone());
        ThrowUtils.throwIf(!redisCode.equals(userRegisterRequest.getCode()), ErrorEnum.CODE_ERROR);

        RegisterDistributeLockUtil registerDistributeLockUtil = new RegisterDistributeLockUtil(phone, redissonClient);
        ThrowUtils.throwIf(!registerDistributeLockUtil.lock(), ErrorEnum.GET_LOCK_ERROR);

        try{
            ThrowUtils.throwIf(isReRegister(phone), ErrorEnum.REGISTER_ERROR);

            String encryptedPassword = DigestUtils.md5DigestAsHex((ConfigEnum.PASSWORD_SALT.getValue() + password).getBytes());
            Snowflake snowflake = IdUtil.getSnowflake(Integer.parseInt(ConfigEnum.WORKED_ID.getValue()), Integer.parseInt(ConfigEnum.DATACENTER_ID.getValue()));

            User user = new User()
                    .setUserId(snowflake.nextId())
                    .setUserName(new NicknameGeneratorUtil().generateNickname())
                    .setPhone(phone)
                    .setPassword(encryptedPassword);

            int insertUser = userMapper.insert(user);
            ThrowUtils.throwIf(insertUser <= 0, ErrorEnum.MYSQL_ERROR);
            // 新建用户余额
            UserBalance userBalance = new UserBalance()
                    .setUserId(user.getUserId())
                    .setBalance(BigDecimal.valueOf(1000))
                    .setUpdatedAt(LocalDateTime.now());
            int insertBalance = userBalanceMapper.insert(userBalance);
            ThrowUtils.throwIf(insertBalance <= 0, ErrorEnum.MYSQL_ERROR);
        } finally {
            registerDistributeLockUtil.unLock();
        }
    }

    /***
     * @MethodName isReRegister
     * @Description 判断用户是否已经注册
     * @param: phone
     * @return: boolean
     * @Date 2025/1/4 23:45
     */
    private boolean isReRegister(String phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);

        long count = userMapper.selectCount(queryWrapper);

        return count > 0;
    }

    @Override
    public void updateAvatar(String id, UserUpdateAvatarRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", Long.valueOf(id));
        User user = userMapper.selectOne(queryWrapper);

        ThrowUtils.throwIf(user == null, ErrorEnum.UPDATE_AVATAR_ERROR);

        user.setAvatar(request.avatarUrl);
        int updateResult = userMapper.updateById(user);
        ThrowUtils.throwIf(updateResult <= 0, ErrorEnum.UPDATE_AVATAR_ERROR);
    }

    /***
     * @MethodName getUploadUrl
     * @Description 获取上传文件签名地址
     * @param: fileName
     * @return: dev.xing.infinitechat.authenticationservice.model.vo.user.UploadUrlResponse
     * @Date 2025/1/12 00:52
     */
    public UploadUrlResponse getUploadUrl(String fileName) {
        String uploadUrl = ossUtils.uploadUrl(OSSConstant.BUCKET_NAME, fileName, OSSConstant.PICTURE_EXPIRE_TIME);
        String downUrl = ossUtils.downUrl(fileName);
        UploadUrlResponse response = new UploadUrlResponse();
        response.setUploadUrl(uploadUrl)
                .setDownloadUrl(downUrl);

        return response;
    }

    @Override
    public void userLogout(UserLogOutRequest userLogOutRequest) {
        redisTemplate.delete(userLogOutRequest.getUserId().toString());
        redisTemplate.delete(ConfigEnum.NETTY_SERVER_HEAD.getValue() + userLogOutRequest.getUserId().toString());
        redisTemplate.convertAndSend(ConfigEnum.REDIS_CONVERT_SEND.getValue(), ConfigEnum.NETTY_SERVER_HEAD.getValue() + userLogOutRequest.getUserId().toString());
    }
}




